/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.ISubTitle;
import org.javlo.component.image.IImageTitle;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.content.Edit;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

/**
 * display the list of component create in content area of a other page in place
 * of the mirrotComponent. use "copy page" for create the link.
 * 
 * @author pvandermaesen
 */
public class PageMirrorComponent extends AbstractVisualComponent implements IImageTitle, ISubTitle {

	public static final String TYPE = "mirror-page";

	private static final String DELETE_IF_NO_SOURCE = "delete-if-source-deleted";

	private static final String NO_DELETE = "never-delete-page";

	private static final String[] STYLES = new String[] { NO_DELETE, DELETE_IF_NO_SOURCE };

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(PageMirrorComponent.class.getName());

	public String getCurrentInputName() {
		return "linked-comp-" + getId();
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		if (super.getStyleList(ctx) == null || super.getStyleList(ctx).length == 0) {
			return STYLES;
		} else {
			return super.getStyleList(ctx);
		}
	}

	protected String getUnlinkAndCopyInputName() {
		return "unlink-copy-" + getId();
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
		out.println("<div class=\"row\"><div class=\"col-xs-6\"><div class=\"form-group\"><input name=\"" + getCurrentInputName() + "\" id=\"" + getCurrentInputName() + "\" type=\"hidden\" readonly=\"readonly\" value=\"" + StringHelper.neverNull(getValue()) + "\" /></div>");

		MenuElement currentPage = getMirrorPage(ctx);
		if (currentPage == null) {
			if (copiedPage != null && !copiedPage.getPath().equals(ctx.getPath())) {
				String[][] params = new String[][] { { "path", copiedPage.getPath() } };
				String label = i18nAccess.getText("content.mirror-page.link", params);
				out.println("<div class=\"form-group\"><input class=\"btn btn-default\" type=\"submit\" value=\"" + label + "\" onclick=\"jQuery('#" + getCurrentInputName() + "').val('" + copiedPage.getId() + "');\" /></div>");
			} else {
				out.println("<div class=\"alert alert-info\" role=\"alert\">"+i18nAccess.getText("mirror-page.copy-page", "copy a page.")+"</div>");
			}
		}
		if (currentPage != null) {
			String pageURL;
			String target = "";
			if (ctx.isEditPreview()) {
				pageURL = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), currentPage);
				target = " target=\"_parent\"";
			} else {
				pageURL = URLHelper.createURL(ctx, currentPage);
			}
			out.println("<div class=\"form-group\"><a href=\"" + pageURL + "\"" + target + ">" + i18nAccess.getText("global.path") + " : " + currentPage.getPath() + "</a></div>");
			out.println("</div><div class=\"col-xs-6\"><div class=\"form-group\"><input class=\"btn btn-default\" name=\"" + getUnlinkAndCopyInputName() + "\" type=\"submit\" value=\"" + i18nAccess.getText("action.unlink-copy") + "\" /></div>");
		}
		out.println("</div></div>");
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

	public MenuElement getMirrorPage(ContentContext ctx) throws Exception {
		String pageId = getValue();
		ContentService content = ContentService.getInstance(ctx.getRequest());
		return content.getNavigation(ctx).searchChildFromId(pageId);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public void prepareView(ContentContext ctx) throws Exception {		
		MenuElement page = getMirrorPage(ctx);
		if (page != null) {
			if (ctx.getSpecialContentRenderer() == null) {
				if (page.getId().equals(getPage().getId()) || page.getId().equals(ctx.getCurrentPage().getId())) {
					ctx.getRequest().setAttribute("xhtml", "[!!! RECURSIVE MIRROR CALL !!!]");
				} else {
					ctx.setVirtualCurrentPage(getPage()); 
					String area = ctx.getArea();
					String path = ctx.getPath();
					MenuElement currentPage = ctx.getCurrentPage();
					Template pageTemplate = TemplateFactory.getTemplate(ctx, page);
					if (!pageTemplate.getAreas().contains(area)) {
						ctx.setVirtualArea(area);
						ctx.setArea(ComponentBean.DEFAULT_AREA);
						ctx.getRequest().setAttribute(ContentContext.CHANGE_AREA_ATTRIBUTE_NAME, ComponentBean.DEFAULT_AREA);
					}					
					ctx.setPath(page.getPath());
					RequestService rs = RequestService.getInstance(ctx.getRequest());					
					rs.setParameter(NOT_EDIT_PREVIEW_PARAM_NAME, "true");
					rs.setParameter(CACHE_KEY_SUFFIX_PARAM_NAME, getPage().getId());
					String param = Edit.CONTENT_RENDERER + '?' + NOT_EDIT_PREVIEW_PARAM_NAME + "=true";
					String xhtml = executeJSP(ctx, param);
					rs.setParameter(NOT_EDIT_PREVIEW_PARAM_NAME, "false");
					ctx.setVirtualCurrentPage(null);
					ctx.setArea(area);
					ctx.setVirtualArea(null);
					ctx.setPath(path);
					ctx.setCurrentPageCached(currentPage);
					ctx.getRequest().setAttribute("xhtml", xhtml);
				}
			}
		} else {
			deleteMySelf(ctx);
			ctx.getRequest().setAttribute("xhtml", "");
		}
		super.prepareView(ctx);
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		prepareView(ctx);
		return (String) ctx.getRequest().getAttribute("xhtml");
	}

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {
		super.init(bean, newContext);
	}

	@Override
	public boolean isList(ContentContext ctx) {
		return false;
	}

	private List<ComponentBean> getCopiedPageContent(ContentContext ctx) throws Exception {
		List<ComponentBean> outBeans = new LinkedList<ComponentBean>();
		MenuElement copiedPage = getMirrorPage(ctx);		
		ContentElementList content = copiedPage.getContent(ctx);
		while (content.hasNext(ctx)) {
			outBeans.add(new ComponentBean(content.next(ctx).getComponentBean()));
		}
		return outBeans;
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String newPage = requestService.getParameter(getCurrentInputName(), getValue());
		if (newPage != null) {
			if (!newPage.equals(getValue())) {
				setValue(newPage);
				setModify();
				setNeedRefresh(true);
			}
		}
		if (requestService.getParameter(getUnlinkAndCopyInputName(), null) != null) {
			String previousId = "0";
			if (getPreviousComponent() != null) {
				previousId = getPreviousComponent().getId();
			}
			List<ComponentBean> data = getCopiedPageContent(ctx.getContextWithArea(getArea()));
			ContentService content = ContentService.getInstance(ctx.getRequest());
			ComponentHelper.changeAllArea(data, getArea()); 
			String id = content.createContent(ctx, getPage(), data, previousId, true);
			deleteMySelf(ctx);
			setNeedRefresh(true);
			if (ctx.isEditPreview()) {
				ctx.setClosePopup(true);
			}
		}
		return null;
	}
	
	public boolean isDeleteIfNoSource() {
		return getStyle().equals(DELETE_IF_NO_SOURCE);
	}
	
	@Override
	public int getLabelLevel(ContentContext ctx) {
		if (!StringHelper.isEmpty(getValue())) {
			return LOW_LABEL_LEVEL;
		} else {
			return 0;
		}
	}
	
	@Override
	public String getTextTitle(ContentContext ctx) {
		MenuElement page;
		try {
			page = getMirrorPage(ctx);
			if (page != null) {
				return page.getTitle(ctx);
			} else {
				return "[error bad page mirror]";
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		MenuElement page;
		try {
			page = getMirrorPage(ctx);
			if (page != null) {
				return page.isRealContent(ctx);
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String getImageDescription(ContentContext ctx) {
		MenuElement page;
		try {
			page = getMirrorPage(ctx);
			if (page != null) {
				return page.getImage(ctx).getImageDescription(ctx);
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getResourceURL(ContentContext ctx) {
		MenuElement page;
		try {
			page = getMirrorPage(ctx);
			if (page != null) {
				return page.getImage(ctx).getResourceURL(ctx);
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getImageLinkURL(ContentContext ctx) {
		MenuElement page;
		try {
			page = getMirrorPage(ctx);
			if (page != null) {
				return page.getImage(ctx).getImageLinkURL(ctx);
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean isImageValid(ContentContext ctx) {
		MenuElement page;
		try {
			page = getMirrorPage(ctx);
			if (page != null && page.getImage(ctx) != null ) {
				return page.getImage(ctx).isImageValid(ctx);
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public int getPriority(ContentContext ctx) {
		MenuElement page;
		try {
			page = getMirrorPage(ctx);
			if (page != null) {
				return page.getImage(ctx).getPriority(ctx);
			} else {
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public String getSubTitle(ContentContext ctx) {
		MenuElement page;
		try {
			page = getMirrorPage(ctx);
			if (page != null) {
				return page.getSubTitle(ctx);
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int getSubTitleLevel(ContentContext ctx) {
		MenuElement page;
		try {
			page = getMirrorPage(ctx);
			if (page != null) { 
				return page.getSubTitleLevel(ctx);
			} else {
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	@Override
	public String getXHTMLId(ContentContext ctx) {
		return getType()+'-'+getId();
	}

}
