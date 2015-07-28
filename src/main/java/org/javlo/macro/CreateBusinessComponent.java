package org.javlo.macro;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang3.tuple.Pair;
import org.javlo.actions.IAction;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.MessageRepository;
import org.javlo.module.content.Edit.ComponentWrapper;
import org.javlo.module.macro.MacroModuleContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;

public class CreateBusinessComponent implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(CreateBusinessComponent.class.getName());

	@Override
	public String getName() {
		return "create-business-component";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		return null;
	}

	@Override
	public boolean isAdmin() {
		return false;
	}

	@Override
	public String getActionGroupName() {
		return "macro-create-business-component";
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/create-business-component.jsp";
	}

	@Override
	public String prepare(ContentContext ctx) {
		try {
			List<Pair<String, String>> dynamicComponents = new LinkedList<Pair<String, String>>();
			List<ComponentWrapper> components = ComponentFactory.getComponentForDisplay(ctx);
			for (ComponentWrapper wrapper : components) {
				if (wrapper.isDynamicComponent()) {
					DynamicComponent dynComp = ((DynamicComponent) wrapper.getComponent());
					dynComp.init(dynComp.getComponentBean(), ctx);
					if (dynComp.getFieldsNames(ctx).contains("name") && dynComp.getDataPath() != null) {
						dynamicComponents.add(Pair.of(wrapper.getType(), wrapper.getLabel()));
					}
				}
			}
			Collections.sort(dynamicComponents, new Comparator<Pair<String, String>>() {
				@Override
				public int compare(Pair<String, String> o1, Pair<String, String> o2) {
					return o1.getValue().compareToIgnoreCase(o2.getValue());
				}
			});
			ctx.getRequest().setAttribute("components", dynamicComponents);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}

		return null;
	}

	public static String performCreate(RequestService rs, EditContext editCtx, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		String componentType = rs.getParameter("component", null);
		String name = rs.getParameter("name", null);
		try {
			ContentService contentService = ContentService.getInstance(ctx.getGlobalContext());
			DynamicComponent compDef = (DynamicComponent) ComponentFactory.getComponentWithType(ctx, componentType);
			MenuElement parentPage = contentService.getNavigation(ctx).searchChild(ctx, compDef.getDataPath());
			MenuElement childPage = getChildPage(ctx, parentPage, name);
			String compId = contentService.createContent(ctx, childPage, ComponentBean.DEFAULT_AREA, "0", componentType, "", true);
			DynamicComponent dynComp = (DynamicComponent) contentService.getComponent(ctx, compId);
			dynComp.getField(ctx, "name").setValue(name);
			dynComp.storeProperties();
			PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
			MacroModuleContext.getInstance(ctx.getRequest()).setActiveMacro(null);

			String newEditURL = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE), childPage);

			if (ctx.isEditPreview()) {
				newEditURL = URLHelper.addParam(newEditURL, "comp_id", compId);
				newEditURL = URLHelper.addParam(newEditURL, "module", "content");
				newEditURL = URLHelper.addParam(newEditURL, "webaction", "editPreview");
				newEditURL = URLHelper.addParam(newEditURL, ContentContext.PREVIEW_EDIT_PARAM, "true");
				NetHelper.sendRedirectTemporarily(ctx.getResponse(), newEditURL);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return null;
	}

	private static MenuElement getChildPage(ContentContext ctx, MenuElement parentPage, String name) throws Exception {
		Character letter = Character.toLowerCase(name.charAt(0));
		boolean isOther = MacroHelper.ALPHABET.indexOf(letter) < 0;
		if (isOther) {
			letter = null;
		}
		String childPageName = MacroHelper.getAlphabeticChildrenName(parentPage, letter);
		MenuElement childPage = parentPage.searchChildFromName(childPageName);
		if (childPage == null) {
			if (isOther) {
				if (null == parentPage.searchChildFromName(MacroHelper.getAlphabeticChildrenName(parentPage, 'a'))) {
					MacroHelper.createAlphabeticChildren(ctx, parentPage);
				}
				childPage = MacroHelper.addPageIfNotExistWithoutMessage(ctx, parentPage, childPageName, false);
			} else {
				MacroHelper.createAlphabeticChildren(ctx, parentPage);
				childPage = parentPage.searchChildFromName(childPageName);
			}
		}
		return childPage;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
}
