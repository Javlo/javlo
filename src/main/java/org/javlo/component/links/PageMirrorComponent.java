/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.content.Edit;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 */
public class PageMirrorComponent extends AbstractVisualComponent {

	public static final String TYPE = "mirror-page";

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(PageMirrorComponent.class.getName());

	private void deleteMySelf(ContentContext ctx) throws Exception {
		MenuElement elem = ctx.getCurrentPage();
		elem.removeContent(ctx, getId());
		logger.warning("delete mirror page component url : " + getId());
	}

	public String getCurrentInputName() {
		return "linked-comp-" + getId();
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		ContentService content = ContentService.getInstance(ctx.getRequest());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		EditContext editContext = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		MenuElement copiedPage = null;
		if (editContext.getContextForCopy(ctx) != null) {
			copiedPage = content.getNavigation(ctx).searchChild(ctx, editContext.getContextForCopy(ctx).getPath());
		}

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println("<div class=\"line\"><input name=\"" + getCurrentInputName() + "\" id=\"" + getCurrentInputName() + "\" type=\"hidden\" readonly=\"readonly\" value=\"" + StringHelper.neverNull(getValue()) + "\" />");

		MenuElement currentPage = getMirrorPage(ctx);
		if (currentPage == null) {
			if (copiedPage != null && !copiedPage.getPath().equals(ctx.getPath())) {
				String[][] params = new String[][] { { "path", copiedPage.getPath() } };
				String label = i18nAccess.getText("content.mirror-page.link", params);
				out.println("<input type=\"submit\" value=\"" + label + "\" onclick=\"jQuery('#" + getCurrentInputName() + "').val('" + copiedPage.getId() + "');\" />");
			}
		}
		if (currentPage != null) {
			out.println("<div class=\"line\">" + i18nAccess.getText("global.path") + " : " + currentPage.getPath() + "</div>");
		}
		out.println("</div>");
		out.close();
		return writer.toString();
	}

	@Override
	public Collection<String> getExternalResources(ContentContext ctx) {
		try {
			if (getMirrorPage(ctx) != null) {
				return getMirrorPage(ctx).getExternalResources(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.EMPTY_LIST;
	}

	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

	private MenuElement getMirrorPage(ContentContext ctx) throws Exception {
		String pageId = getValue();
		ContentService content = ContentService.getInstance(ctx.getRequest());
		return content.getNavigation(ctx).searchChildFromId(pageId);
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	@Override
	public String getType() {
		return TYPE;
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		MenuElement page = getMirrorPage(ctx);
		if (page != null) {
			if (ctx.getSpecialContentRenderer() == null && ctx.getRequest().getParameter("_wcms_content_path") == null) {
				return executeJSP(ctx, Edit.CONTENT_RENDERER + "?_wcms_content_path=" + page.getPath());
			}
		} else {
			deleteMySelf(ctx);
		}
		return "";
	}

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {
		super.init(bean, newContext);
	}

	@Override
	public boolean isList(ContentContext ctx) {
		return false;
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String newPage = requestService.getParameter(getCurrentInputName(), getValue());
		if (newPage != null) {
			if (!newPage.equals(getValue())) {
				setValue(newPage);
				setModify();
				setNeedRefresh(true);
			}
		}
	}

}
