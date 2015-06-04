package org.javlo.helper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.javlo.comparator.MenuElementPriorityComparator;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.IInternalLink;
import org.javlo.component.links.RSSRegistration;
import org.javlo.component.title.SubTitle;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.ConvertToCurrentVersion;
import org.javlo.service.PersistenceService;
import org.javlo.xml.NodeXML;

public class NavigationHelper {
	
	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(NavigationHelper.class.getName());

	public static final boolean canMoveDown(MenuElement elem) {
		if (elem.getParent() == null) {
			return false;
		} else {
			if (elem.getParent().getChildMenuElements().get(elem.getParent().getChildMenuElements().size() - 1).equals(elem)) {
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
			if (elem.getParent().getChildMenuElements().iterator().next().equals(elem)) {
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
		Collection<MenuElement> children = elem.getChildMenuElements();
		for (MenuElement menuElement : children) {
			if (!menuElement.isValid() && !menuElement.isBlocked()) {
				countValidChild++;
			}
			countValidChild = countValidChild + countUnvalidChildren(menuElement);
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

	public static final void importPage(ContentContext ctx, PersistenceService persistenceService, NodeXML pageNode, MenuElement currentPage, String lang, boolean readOnly) throws Exception {

		NodeXML parent = pageNode;
		if (parent != null && parent.getParent() != null) {
			while (parent.getParent().getParent() != null) {
				parent = parent.getParent();
			}
		}
		String version = parent.getAttributeValue("version", "1.0");

		persistenceService.insertContent(pageNode, currentPage, lang);
		for (ComponentBean data : currentPage.getAllLocalContentBean()) {
			ConvertToCurrentVersion.convert(ctx, data, version);
		}
		NodeXML child = pageNode.getChild("page");

		logger.info("import page version : " + version);

		while (child != null) {
			String pageName = child.getAttributeValue("name");
			if (pageName != null) {
				GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
				MenuElement newPage = persistenceService.insertPage(globalContext, child, currentPage, new HashMap<MenuElement, String[]>(), lang);
				try {
					for (ComponentBean data : newPage.getAllLocalContentBean()) {
						ConvertToCurrentVersion.convert(ctx, data, version);
					}
					newPage.releaseCache();
				} catch (Exception e) {
					e.printStackTrace();
				}

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
			Collection<MenuElement> children = srcRoot.getChildMenuElements();
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
			Collection<MenuElement> targetChildren = targetRoot.getChildMenuElements();
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
		Collection<MenuElement> children = currentElem.getChildMenuElements();
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

	/**
	 * check if a page is a parent of a other page.
	 * 
	 * @param page
	 *            a page with parent
	 * @param parent
	 *            the suposed parent page
	 * @return
	 */
	public static boolean isParent(MenuElement page, MenuElement parent) {
		MenuElement directParent = page.getParent();
		while (directParent != null) {
			if (directParent.getId().equals(parent.getId())) {
				return true;
			}
			directParent = directParent.getParent();
		}
		return false;
	}

	/**
	 * check if pages are parent of a other page.
	 * 
	 * @param page
	 *            a page with parent
	 * @param parent
	 *            the suposed parent page
	 * @return
	 */
	public static boolean isParent(MenuElement page, List<MenuElement> parents) {
		if (parents.size() == 0) {
			return true;
		}
		for (MenuElement parent : parents) {
			if (isParent(page, parent)) {
				return true;
			}
		}
		return false;
	}
	
	private static void getPageLocalBookmark(PrintStream out, ContentContext ctx, MenuElement page, boolean childPage) throws Exception {
		String href="#";
		if (childPage) {
			href = "#page_"+page.getId();
		}
		List<IContentVisualComponent> subtitles = page.getContentByType(ctx, SubTitle.TYPE);
		
		out.print("<bookmark name=\""+page.getTitle(ctx).trim()+"\" href=\""+href+"\">");
		for (IContentVisualComponent iContentVisualComponent : subtitles) {
			SubTitle subTitle = (SubTitle)iContentVisualComponent;
			if (subTitle.getTitleLevel(ctx) == 2) {
				out.println("<bookmark name=\""+subTitle.getValue().trim()+"\" href=\"#"+subTitle.getXHTMLId(ctx)+"\"></bookmark>");
			}
		}
		out.println("</bookmark>");		
	}
	
	/**
	 * get the page bookmark for the html header.
	 * used for pdf generation.
	 * @return
	 * @throws Exception 
	 */
	public static String getPageBookmark(ContentContext ctx, MenuElement page) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<bookmarks>");
		if (!page.isChildrenAssociation()) {
			getPageLocalBookmark(out, ctx, page, false);
		} else {
			for (MenuElement child : page.getAllChildren()) {
				getPageLocalBookmark(out, ctx, child, true);
			}
		}
		out.println("</bookmarks>");
		out.close();
		return new String(outStream.toByteArray());
	}
	
	public static void movePage(ContentContext ctx, MenuElement parent, MenuElement previousBrother, MenuElement page) {
		page.moveToParent(parent);
		if (previousBrother != null) {
			page.setPriority(previousBrother.getPriority()+1);
		} else {
			page.setPriority(0);
		}
		NavigationHelper.changeStepPriority(page.getParent().getChildMenuElements(), 10);
	}
	
	public static MenuElement createChildPageAutoName(MenuElement page, ContentContext ctx) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		
		String newPageName = page.getName()+"-1";
		int index = 2;
		while (content.getNavigation(ctx).searchChildFromName(newPageName) != null) {
			newPageName = page.getName()+'-'+index;
			index++;
		}		
		MenuElement newPage = MacroHelper.addPageIfNotExist(ctx, page,newPageName, true, true);
		boolean changeNotification = true;
		
		newPage.setChangeNotification(changeNotification);
		return newPage;
	}
	
	public static MenuElement getPageById(ContentContext ctx, String id) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement root = content.getNavigation(ctx);
		if (root.getId().equals(id)) {
			return root;
		} else {
			return root.searchChildFromId(id);
		}
	}
}
