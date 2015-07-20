/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.ISubTitle;
import org.javlo.component.image.IImageTitle;
import org.javlo.context.ContentContext;
import org.javlo.fields.Field;
import org.javlo.fields.IFieldContainer;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.ClipBoard;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 */
public class MirrorComponent extends AbstractVisualComponent implements IFieldContainer, IImageTitle, ISubTitle {

	public static final String TYPE = "mirror";

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(MirrorComponent.class.getName());

	public String getCurrentInputName() {
		return "linked-comp-" + getId();
	}

	public String getUnlinkInputName() {
		return "unlink-comp-" + getId();
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		ClipBoard clibBoard = ClipBoard.getInstance(ctx.getRequest());
		ComponentBean comp = null;
		synchronized (clibBoard) {
			if (clibBoard.getCopied() instanceof ComponentBean) {
				comp = (ComponentBean) clibBoard.getCopied();
			}
		}

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println("<div class=\"row\"><div class=\"col-sm-4\"><div class=\"form-group\"><input name=\"" + getCurrentInputName() + "\" id=\"" + getCurrentInputName() + "\" type=\"hidden\" readonly=\"readonly\" value=\"" + StringHelper.neverNull(getMirrorComponentId()) + "\" />");

		IContentVisualComponent currentComp = getMirrorComponent(ctx);
		if (comp != null && !comp.getId().equals(getId())) {
			String[][] params = new String[][] { { "type", i18nAccess.getText("comp." + comp.getType(), comp.getType()) }, { "language", comp.getLanguage() } };
			String label = i18nAccess.getText("content.mirror.link", params);
			out.println("<input class=\"btn btn-default\" type=\"submit\" value=\"" + label + "\" onclick=\"jQuery('#" + getCurrentInputName() + "').val('" + comp.getId() + "');\" />");
		} else {
			out.println("<input class=\"btn btn-primary\" disabled type=\"submit\" value=\"" + i18nAccess.getText("content.mirror.no-copy", "copy a other component for link.") + "\" />");
		}
		out.println("</div></div>");
		if (currentComp != null) {
			out.println("<div class=\"col-sm-4\">" + i18nAccess.getText("content.mirror.type") + currentComp.getType() + " (<a href=\"" + URLHelper.createURL(ctx, currentComp.getPage()) + "\">" + currentComp.getPage().getPath() + "</a>)</div>");
		}
		if (getValue().trim().length() > 0) {
			out.println("<div class=\"col-sm-4\">");
			String label = i18nAccess.getText("content.mirror.unlink");
			out.println("<input class=\"btn btn-default\" onclick=\"jQuery(this.form).data('ajaxSubmit', false); return true;\" type=\"submit\" value=\"" + label + "\" name=\"" + getUnlinkInputName() + "\" />");
			out.println("</div>");
		}

		out.println("</div>");
		out.close();
		return writer.toString();
	}

	@Override
	public Collection<String> getExternalResources(ContentContext ctx) {
		IContentVisualComponent comp;
		try {
			comp = getMirrorComponent(ctx);
			if (comp != null) {
				return comp.getExternalResources(ctx);
			} else {
				return Collections.EMPTY_LIST;
			}
		} catch (Exception e) {
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

	protected String getMirrorComponentId() {
		return getValue();
	}

	protected void setMirrorComponentId(String compId) {
		setValue(compId);
	}

	public IContentVisualComponent getMirrorComponent(ContentContext ctx) throws Exception {
		String compId = getMirrorComponentId();
		ContentService content = ContentService.getInstance(ctx.getRequest());
		return content.getComponentNoRealContentType(ctx, compId);
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		IContentVisualComponent comp;
		try {
			comp = getMirrorComponent(ctx);
			if (comp != null) {
				AbstractVisualComponent.setForcedId(ctx, getId());
				return comp.getPrefixViewXHTMLCode(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			AbstractVisualComponent.setForcedId(ctx, null);
		}
		return super.getPrefixViewXHTMLCode(ctx);
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		IContentVisualComponent comp;
		try {
			comp = getMirrorComponent(ctx);
			if (comp != null) {
				return comp.getSuffixViewXHTMLCode(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return super.getPrefixViewXHTMLCode(ctx);
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
		AbstractVisualComponent comp = (AbstractVisualComponent) getMirrorComponent(ctx);
		if (comp != null) {
			AbstractVisualComponent.setForcedId(ctx, getId());
			comp.prepareView(ctx);
			String xhtml = comp.getXHTMLCode(ctx);
			AbstractVisualComponent.setForcedId(ctx, null);
			return xhtml;
		} else {
			deleteMySelf(ctx);
		}
		return "";
	}

	@Override
	public String getEmptyXHTMLCode(ContentContext ctx) throws Exception {
		AbstractVisualComponent comp = (AbstractVisualComponent) getMirrorComponent(ctx);
		if (comp != null) {
			return comp.getEmptyXHTMLCode(ctx);
		} else {
			return super.getEmptyXHTMLCode(ctx);
		}
	}

	@Override
	public int getWordCount(ContentContext ctx) {
		String value = getValue();
		if (value != null) {
			return value.split(" ").length;
		}
		return 0;
	}

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {
		super.init(bean, newContext);
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		try {
			return getMirrorComponent(ctx).isContentCachable(ctx);
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean isList(ContentContext ctx) {
		IContentVisualComponent comp;
		try {
			comp = getMirrorComponent(ctx);
			if (comp != null) {
				return comp.isList(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return super.isList(ctx);
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String newLink = requestService.getParameter(getCurrentInputName(), getMirrorComponentId());
		if (requestService.getParameter(getUnlinkInputName(), null) != null) {
			IContentVisualComponent comp = getMirrorComponent(ctx);
			if (comp != null) {
				ContentService content = ContentService.getInstance(ctx.getGlobalContext());
				ComponentBean bean = new ComponentBean(comp.getComponentBean());
				bean.setArea(getArea());
				content.createContent(ctx, bean, getId(), isBackgroundColored());
				deleteMySelf(ctx);
				ctx.setClosePopup(true);
				setModify();
				setNeedRefresh(true);
			}
		} else if (newLink != null) {
			if (!newLink.equals(getMirrorComponentId())) {
				setMirrorComponentId(newLink);
				setModify();
				setNeedRefresh(true);
			}
		}
	}

	@Override
	public String getLabel(ContentContext ctx) {
		IContentVisualComponent comp = null;
		try {
			comp = getMirrorComponent(ctx);
			if (comp instanceof IFieldContainer) {
				return ((IFieldContainer) comp).getLabel(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<String> getFieldsNames(ContentContext ctx) throws Exception {
		IContentVisualComponent comp = null;
		try {
			comp = getMirrorComponent(ctx);
			if (comp instanceof IFieldContainer) {
				return ((IFieldContainer) comp).getFieldsNames(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<Field> getFields(ContentContext ctx) throws Exception {
		IContentVisualComponent comp = null;
		try {
			comp = getMirrorComponent(ctx);
			if (comp instanceof IFieldContainer) {
				return ((IFieldContainer) comp).getFields(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Field getField(ContentContext ctx, String name) throws Exception {
		IContentVisualComponent comp = null;
		try {
			comp = getMirrorComponent(ctx);
			if (comp instanceof IFieldContainer) {
				return ((IFieldContainer) comp).getField(ctx, name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getFieldValue(ContentContext ctx, String name) throws Exception {
		IContentVisualComponent comp = null;
		try {
			comp = getMirrorComponent(ctx);
			if (comp instanceof IFieldContainer) {
				return ((IFieldContainer) comp).getFieldValue(ctx, name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Map<String, String> getList(ContentContext ctx, String listName, Locale locale) throws Exception {
		IContentVisualComponent comp = null;
		try {
			comp = getMirrorComponent(ctx);
			if (comp instanceof IFieldContainer) {
				return ((IFieldContainer) comp).getList(ctx, listName, locale);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Map<String, String> getList(ContentContext ctx, String listName) throws Exception {
		IContentVisualComponent comp = null;
		try {
			comp = getMirrorComponent(ctx);
			if (comp instanceof IFieldContainer) {
				return ((IFieldContainer) comp).getList(ctx, listName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getViewListXHTMLCode(ContentContext ctx) throws Exception {
		IContentVisualComponent comp = null;
		try {
			comp = getMirrorComponent(ctx);
			if (comp instanceof IFieldContainer) {
				return ((IFieldContainer) comp).getViewListXHTMLCode(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean isFieldContainer(ContentContext ctx) {
		IContentVisualComponent comp = null;
		try {
			comp = getMirrorComponent(ctx);
			if (comp instanceof IFieldContainer) {
				return ((IFieldContainer) comp).isFieldContainer(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public String getContainerType(ContentContext ctx) {
		IContentVisualComponent comp = null;
		try {
			comp = getMirrorComponent(ctx);
			if (comp instanceof IFieldContainer) {
				return ((IFieldContainer) comp).getContainerType(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getImageDescription(ContentContext ctx) {
		IContentVisualComponent comp = null;
		try {
			comp = getMirrorComponent(ctx);
			if (comp instanceof IImageTitle) {
				return ((IImageTitle) comp).getImageDescription(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getResourceURL(ContentContext ctx) {
		IContentVisualComponent comp = null;
		try {
			comp = getMirrorComponent(ctx);
			if (comp instanceof IImageTitle) {
				return ((IImageTitle) comp).getResourceURL(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getImageLinkURL(ContentContext ctx) {
		IContentVisualComponent comp = null;
		try {
			comp = getMirrorComponent(ctx);
			if (comp instanceof IImageTitle) {
				return ((IImageTitle) comp).getImageLinkURL(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean isImageValid(ContentContext ctx) {
		IContentVisualComponent comp = null;
		try {
			comp = getMirrorComponent(ctx);
			if (comp instanceof IImageTitle) {
				return ((IImageTitle) comp).isImageValid(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public int getPriority(ContentContext ctx) {
		IContentVisualComponent comp = null;
		try {
			comp = getMirrorComponent(ctx);
			if (comp instanceof IImageTitle) {
				return ((IImageTitle) comp).getPriority(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public String getSubTitle(ContentContext ctx) {
		IContentVisualComponent comp = null;
		try {
			comp = getMirrorComponent(ctx);
			if (comp instanceof IImageTitle) {
				return ((ISubTitle) comp).getSubTitle(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getSubTitleLevel(ContentContext ctx) {
		IContentVisualComponent comp = null;
		try {
			comp = getMirrorComponent(ctx);
			if (comp instanceof ISubTitle) {
				return ((ISubTitle) comp).getSubTitleLevel(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {		
		IContentVisualComponent comp;
		try {
			comp = getMirrorComponent(ctx);
			if (comp != null) {
				boolean realContent = comp.isRealContent(ctx);				
				return realContent;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public int getLabelLevel(ContentContext ctx) {
		IContentVisualComponent comp;
		try {
			comp = getMirrorComponent(ctx);
			if (comp != null) {
				/* If we find the component on the page, we take this one --> -1 */
				return comp.getLabelLevel(ctx)-1;  
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	@Override
	public String getXHTMLId(ContentContext ctx) {
		return getType()+'-'+getId();
	}

}
