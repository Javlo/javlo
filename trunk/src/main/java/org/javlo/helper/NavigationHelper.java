package org.javlo.helper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.javlo.comparator.MenuElementPriorityComparator;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.IInternalLink;
import org.javlo.component.links.RSSRegistration;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.DebugHelper.StructureException;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.xml.NodeXML;

public class NavigationHelper {

	public static final boolean canMoveDown(MenuElement elem) {
		if (elem.getParent() == null) {
			return false;
		} else {
			if (elem.getParent().getChildMenuElements()[elem.getParent().getChildMenuElements().length - 1].equals(elem)) {
				return false;
			} else {
				return true;
			}
		}
	}

	public static final boolean canMoveToChild(MenuElement elem) {
		return canMoveDown(elem);
	}

	public static final boolean canMoveToParent(MenuElement elem) {
		if ((elem.getParent() != null) && (elem.getParent().getParent() != null)) {
			return true;
		} else {
			return false;
		}
	}

	public static final boolean canMoveUp(MenuElement elem) {
		if (elem.getParent() == null) {
			return false;
		} else {
			if (elem.getParent().getChildMenuElements()[0].equals(elem)) {
				return false;
			} else {
				return true;
			}
		}
	}

	public static final void changeStepPriority(List<MenuElement> elems, int newStep) {
		if ((elems == null) || (elems.size() == 0)) {
			return;
		} else {
			Collections.sort(elems, new MenuElementPriorityComparator());
			int pos = newStep;
			for (MenuElement menuElement : elems) {
				menuElement.setPriorityNoSort(pos);
				pos = pos + newStep;
			}
		}
	}

	public static final void changeStepPriority(MenuElement[] elems, int newStep) {
		if ((elems == null) || (elems.length == 0)) {
			return;
		} else {
			elems[0].setPriority(newStep);
			for (int i = 1; i < elems.length; i++) {
				elems[i].setPriority(newStep * (i + 1));
			}
		}
	}

	/**
	 * copy all element attribute (without children)
	 * 
	 * @param src
	 *            source MenuElement
	 * @param target
	 *            target MenuElement
	 * @throws Exception
	 */
	public static void copyElement(ContentContext ctx, MenuElement src, MenuElement target) throws Exception {
		target.setId(src.getId());
		target.setName(src.getName());
		target.setVisible(src.isVisible(ctx));
		try {
			target.setContent(extractContent(src));
		} catch (Exception e) {
			e.printStackTrace();
		}
		target.setCreationDate(src.getCreationDate());
		target.setCreator(src.getCreator());
		target.setModificationDate(src.getModificationDate());
		target.setLatestEditor(src.getLatestEditor());
		target.setPriority(src.getPriority());
		target.setTemplateName(src.getTemplateId());
		List<MenuElement> vParent = new LinkedList<MenuElement>();
		vParent.addAll(src.getVirtualParent());
		target.setVirtualParent(vParent);
	}

	private static int countUnvalidChildren(Collection<MenuElement> allreadyFound, MenuElement elem) {
		if (allreadyFound.contains(elem)) {
			return 0;
		} else {
			allreadyFound.add(elem);
		}
		int countValidChild = 0;
		MenuElement[] children = elem.getChildMenuElements();
		for (int i = 0; i < children.length; i++) {
			if (!children[i].isValid() && !children[i].isBlocked()) {
				countValidChild++;
			}
			countValidChild = countValidChild + countUnvalidChildren(children[i]);
		}
		return countValidChild;
	}

	public static int countUnvalidChildren(MenuElement elem) {
		return countUnvalidChildren(new HashSet<MenuElement>(), elem);
	}

	/**
	 * extract the content of a element.
	 * 
	 * @param elem
	 *            a menu element
	 * @return a copy of the element content.
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 */
	private static ComponentBean[] extractContent(MenuElement elem) throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
		ComponentBean[] currentContent = elem.getContent();
		ComponentBean[] contentCopy = new ComponentBean[currentContent.length];
		for (int i = 0; i < contentCopy.length; i++) {
			contentCopy[i] = (ComponentBean) BeanUtils.cloneBean(currentContent[i]);
		}
		return contentCopy;
	}

	public static MenuElement firstSelectionElement(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		MenuElement root = ContentService.getInstance(globalContext).getNavigation(ctx);
		MenuElement currentPage = root.searchChild(ctx);
		if ((currentPage == null) || (currentPage.getParent() == null)) {
			return null;
		}
		while (currentPage.getParent().getParent() != null) {
			currentPage = currentPage.getParent();
		}
		return currentPage;
	}

	public static List<String> getAllRSSChannels(ContentContext ctx, MenuElement page) throws Exception {
		List<String> pageChannel = getRSSChannels(ctx, page);
		MenuElement[] children = page.getAllChildren();
		for (MenuElement menuElement : children) {
			List<String> allChildrenChannels = getRSSChannels(ctx, menuElement);
			for (String channel : allChildrenChannels) {
				if (!pageChannel.contains(channel)) {
					pageChannel.add(channel);
				}
			}
		}
		return pageChannel;
	}

	/**
	 * get the linked url of the current page, maybe defined in a parent node.
	 * 
	 * @param elem
	 * @return
	 */
	public static String getLinkedURL(MenuElement elem) {
		if (elem.getLinkedURL().trim().length() > 0) {
			return elem.getLinkedURL();
		} else {
			MenuElement parent = elem.getParent();
			while (parent != null && parent.getLinkedURL().trim().length() == 0) {
				parent = parent.getParent();
			}
			if (parent != null) {
				return parent.getLinkedURL();
			} else {
				return "";
			}
		}
	}

	public static List<String> getRSSChannels(ContentContext ctx, MenuElement page) throws Exception {
		ContentElementList contentList = page.getAllContent(ctx);
		List<String> pageChannel = new LinkedList<String>();
		while (contentList.hasNext(ctx)) {
			IContentVisualComponent comp = contentList.next(ctx);
			if (comp.getType().equals(RSSRegistration.TYPE)) {
				pageChannel.add(((RSSRegistration) comp).getChannel().trim());
			}
		}
		return pageChannel;
	}

	public static final void importPage(ContentContext ctx, PersistenceService persistenceService, NodeXML pageNode, MenuElement currentPage, String lang, boolean readOnly) throws StructureException, ConfigurationException, SQLException, IOException {
		persistenceService.insertContent(pageNode, currentPage, lang);
		NodeXML child = pageNode.getChild("page");
		while (child != null) {
			String pageName = child.getAttributeValue("name");
			if (pageName != null) {
				GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
				MenuElement newPage = persistenceService.insertPage(globalContext, child, currentPage, new HashMap<MenuElement, String[]>(), lang);
				newPage.setRemote(readOnly);
			}
			child = child.getNext("page");
		}
		NodeXML properties = pageNode.getParent().getChild("properties");
		if (properties != null && properties.getAttributeValue("name", "").equals("global")) {
			NodeXML property = properties.getChild("property");
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			ContentService content = ContentService.getInstance(globalContext);
			while (property != null) {
				if (content.getAttribute(ctx, property.getAttributeValue("key")) == null) { // if this key doesn't exist in local -> set in global map
					content.setAttribute(ctx, property.getAttributeValue("key"), property.getContent());
				}
				property = property.getNext("property");
			}
		}
	}

	public static void publishNavigation(ContentContext ctx, MenuElement srcRoot, MenuElement targetRoot) throws Exception {

		if (srcRoot.isValid()) {
			copyElement(ctx, srcRoot, targetRoot);
			MenuElement[] children = srcRoot.getChildMenuElements();
			for (MenuElement element : children) {
				MenuElement oldVersion = targetRoot.searchChildFromId(element.getId());
				if (oldVersion == null) {
					if (element.isValid()) {
						oldVersion = MenuElement.getInstance(GlobalContext.getInstance(ctx.getRequest()));
						targetRoot.addChildMenuElement(oldVersion);
					}
				}
				if (oldVersion != null) {
					publishNavigation(ctx, element, oldVersion);
				}
				// TODO: remove page in view mode when she is removed is edit
				// mode (preview).

			}
			MenuElement[] targetChildren = targetRoot.getChildMenuElements();
			for (MenuElement element : targetChildren) {
				MenuElement srcChild = srcRoot.searchChildFromId(element.getId());
				if ((srcChild == null) || (!srcChild.getParent().getId().equals(targetRoot.getId()))) {
					targetRoot.removeChild(element);
				}
			}
		}
	}

	public static void publishOneComponent(ContentContext ctx, String componentId) throws Exception {

		ContentContext viewCtx = new ContentContext(ctx);
		viewCtx.setRenderMode(ContentContext.VIEW_MODE);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ContentService content = ContentService.getInstance(globalContext);
		IContentVisualComponent comp = content.getCachedComponent(ctx, componentId);
		IContentVisualComponent viewComp = content.getCachedComponent(viewCtx, componentId);
		if (comp != null & viewComp != null) {
			viewComp.setValue(comp.getValue(ctx));
			viewComp.setStyle(viewCtx, comp.getStyle(ctx));
			viewComp.setRepeat(comp.isRepeat());
		}
	}

	private static void searchLinkTo(ContentContext ctx, List<MenuElement> pageList, MenuElement currentElem, MenuElement target) throws Exception {
		if (target == null) {
			return;
		}
		ComponentBean[] beans = currentElem.getContent();
		for (ComponentBean bean : beans) {
			IContentVisualComponent comp = ComponentFactory.createComponent(ctx, bean, null, null, null);
			if (comp != null) {
				if ((comp instanceof IInternalLink)) {
					IInternalLink iLink = (IInternalLink) comp;
					if (iLink.getLinkId() != null) {
						if (iLink.getLinkId().equals(target.getId())) {
							pageList.add(currentElem);
						}
					}
				}
			}
		}
		MenuElement[] children = currentElem.getChildMenuElements();
		for (MenuElement element : children) {
			searchLinkTo(ctx, pageList, element, target);
		}
	}

	/**
	 * search page with a link to a specific page
	 * 
	 * @param elem
	 *            a specific page
	 * @return a list of page
	 */
	public static List<MenuElement> searchLinkTo(ContentContext ctx, MenuElement elem) throws Exception {
		List<MenuElement> pageList = new LinkedList<MenuElement>();
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ContentService content = ContentService.getInstance(globalContext);
		MenuElement rootElement = content.getNavigation(ctx);
		searchLinkTo(ctx, pageList, rootElement, elem);
		return pageList;
	}

}
