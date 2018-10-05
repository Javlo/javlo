package org.javlo.component.dynamic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.user.User;

public class DynamicComponentCreator extends AbstractVisualComponent implements IAction {

	private static final String TYPE = "dynamic-component-creator";
	private static final String REQUEST_KEY = "dynamicComponentCreator";
	private static final String EDIT_KEY = "dynamicComponentEdition";
	private static final String NAME_PREFIX = "ud-";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getActionGroupName() {
		return getType();
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (ctx.getCurrentUser() == null) {
			return "";
		}
		ctx.getRequest().setAttribute(REQUEST_KEY, true);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		out.println("<form action=\"" + URLHelper.createURL(ctx) + "\" method=\"post\" enctype=\"multipart/form-data\">");
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		if (rs.getParameter("new", null) == null && rs.getParameter("id", null) == null && rs.getParameter("edit", null) == null || rs.getParameter("valid", null) != null || rs.getParameter("cancel", null) != null || rs.getParameter("delete", null) != null) {
			out.println("<div class=\"form-group\">");
			out.println("<button class=\"btn btn-default\" type=\"submit\" name=\"new\">" + i18nAccess.getViewText("global.create") + ' ' + getValue() + "</button>");
			out.println("</div>");
		} else if (rs.getParameter("new", null) != null || rs.getParameter("edit", null) != null) {
			ContentService contentService = ContentService.getInstance(ctx.getRequest());
			DynamicComponent compDef = (DynamicComponent) contentService.getComponent(ctx, rs.getParameter("id", null));
			String id = StringHelper.getRandomId();
			if (compDef == null) {
				compDef = (DynamicComponent) ComponentFactory.getComponentWithType(ctx, getValue());
				if (compDef == null) {
					return "<div class=\"alert alert-danger\" role=\"alert\">technical error : '"+getValue()+"' not found.</div>";
				}
			} else {
				id = compDef.getId();
			}
			compDef.getComponentBean().setId(id);
			out.println("<div class=\"card\"><div class=\"card-body\"><h4 class=\"card-title\">" + i18nAccess.getViewText("global.new") + "</h4>");
			ContentContext editCtx = new ContentContext(ctx);			
			i18nAccess.forceReloadEdit(ctx.getGlobalContext(), ctx.getRequest().getSession(), ctx.getRequestContentLanguage());			
			editCtx.setContextRequestLanguage(ctx.getRequestContentLanguage());
			editCtx.setRenderMode(ContentContext.EDIT_MODE);
			out.println(compDef.getEditXHTMLCode(editCtx));			
			i18nAccess.resetForceEditLg();			
			out.println("<div><input type=\"hidden\" name=\"type\" value=\"" + getValue() + "\" />");
			out.println("<input type=\"hidden\" name=\"id\" value=\"" + id + "\" />");
			out.println("<input type=\"hidden\" name=\"webaction\" value=\"" + getActionGroupName() + ".createcomponent\" />");
			out.println("</div>");
			out.println("<div class=\"form-group pull-right\">");
			out.println("<button class=\"btn btn-primary\" type=\"submit\" name=\"create\">" + i18nAccess.getViewText("global.ok") + "</button>");
			out.println("<button class=\"btn btn-warning\" type=\"submit\" name=\"delete\" value=\"1\">" + i18nAccess.getViewText("global.delete") + "</button>");
			out.println("<button class=\"btn btn-default\" type=\"submit\" name=\"cancel\">" + i18nAccess.getViewText("global.cancel") + "</button>");
			out.println("</div></div>");
		} else if (rs.getParameter("id", null) != null) {
			ContentService contentService = ContentService.getInstance(ctx.getRequest());
			IContentVisualComponent comp = contentService.getComponent(ctx, rs.getParameter("id", null));
			ctx.getRequest().setAttribute(EDIT_KEY, true);
			out.println(comp.getXHTMLCode(ctx));
			ctx.getRequest().removeAttribute(EDIT_KEY);
			out.println("<div class=\"form-group\">");
			out.println("<input type=\"hidden\" name=\"id\" value=\"" + rs.getParameter("id", null) + "\" />");
			out.println("<button class=\"btn btn-primary\" type=\"submit\" name=\"valid\">" + i18nAccess.getViewText("global.ok") + "</button>");
			out.println("<button class=\"btn btn-default\" type=\"submit\" name=\"edit\"  >"+ i18nAccess.getViewText("global.edit") + "</button>");
			out.println("</div>");
		}
		out.println("</form>");
		out.close();
		return new String(outStream.toByteArray());
	}

	public static String performCreatecomponent(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		User user = ctx.getCurrentUser();
		String type = rs.getParameter("type", null);
		String id = rs.getParameter("id", null);
		boolean delete = StringHelper.isTrue(rs.getParameter("delete", null));
		if (user == null || type == null) {
			return "security error ! ("+user+" - "+type+")";
		}
		String mainPageName = StringHelper.createFileName(NAME_PREFIX + user.getLogin()).replace('.', '-');
		MenuElement localRoot = ctx.getCurrentPage().searchChildFromName(mainPageName);
		if (localRoot == null) {
			localRoot = MenuElement.getInstance(ctx);
			localRoot.setName(mainPageName);
			localRoot.setCreator(user.getLogin());
			ctx.getCurrentPage().addChildMenuElementOnTop(localRoot);
			ctx.getCurrentPage().releaseCache();
		}
		String compPageName = StringHelper.createFileName(localRoot.getName() + '-' + StringHelper.createFileName(type));
		MenuElement compRoot = localRoot.searchChildFromName(compPageName);
		if (compRoot == null) {
			compRoot = MenuElement.getInstance(ctx);
			compRoot.setName(compPageName);
			compRoot.setCreator(user.getLogin());
			localRoot.addChildMenuElementOnTop(compRoot);
			localRoot.releaseCache();
		}
		ContentService contentService = ContentService.getInstance(ctx.getRequest());
		IContentVisualComponent comp = contentService.getComponent(ctx, rs.getParameter("id", null));
		if (comp == null) {
			ComponentBean bean = new ComponentBean(type, "", ctx.getRequestContentLanguage());
			bean.setId(id);
			id = contentService.createContentWidthId(ctx, compRoot, ComponentBean.DEFAULT_AREA, "0", bean, true);
			comp = contentService.getComponent(ctx, id);
		}
		if (comp.getAuthors().equals(ctx.getCurrentUserId()) || ctx.isUserWebSiteManager()) {
			if (delete) {				
				comp.getPage().removeContent(ctx, id, true);
			} else {
				comp.performEdit(ctx);
			}
			PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
		} else {
			return "security error! ("+ctx.getCurrentUserId()+" - "+comp.getAuthors()+")";
		}		
		return null;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_ADMIN;
	}
	
	@Override
	public String getFontAwesome() {
		return "plus-circle";
	}
}
