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
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ClipBoard;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

/**
 * @author pvandermaesen
 */
public class MirrorComponent extends AbstractVisualComponent {

	public static final String TYPE = "mirror";

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(MirrorComponent.class.getName());

	private void deleteMySelf(ContentContext ctx) throws Exception {
		ContentService.getInstance(ctx.getRequest());
		MenuElement elem = ctx.getCurrentPage();
		elem.removeContent(ctx, getId());
		logger.warning("delete miror component url : " + getId());
	}

	public String getCurrentInputName() {
		return "linked-comp-" + getId();
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
		out.println("<div class=\"line\"><input name=\"" + getCurrentInputName() + "\" id=\"" + getCurrentInputName() + "\" type=\"hidden\" readonly=\"readonly\" value=\"" + StringHelper.neverNull(getMirrorComponentId()) + "\" />");

		IContentVisualComponent currentComp = getMirrorComponent(ctx);

		if (comp != null && !comp.getId().equals(getId())) {
			String[][] params = new String[][] { { "type", comp.getType() }, { "language", comp.getLanguage() } };
			String label = i18nAccess.getText("content.mirror.link", params);
			out.println("<input type=\"submit\" value=\"" + label + "\" onclick=\"jQuery('#" + getCurrentInputName() + "').val('" + comp.getId() + "');\" />");
		}

		if (currentComp != null) {
			out.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + i18nAccess.getText("content.mirror.type") + currentComp.getType()+" (<a href=\""+URLHelper.createURL(ctx, currentComp.getPage())+"\">"+currentComp.getPage().getPath()+"</a>)");
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

	protected IContentVisualComponent getMirrorComponent(ContentContext ctx) throws Exception {
		String compId = getMirrorComponentId();
		ContentService content = ContentService.getInstance(ctx.getRequest());
		return content.getComponentAllLanguage(ctx, compId);
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		IContentVisualComponent comp;
		try {
			comp = getMirrorComponent(ctx);
			if (comp != null) {
				return comp.getPrefixViewXHTMLCode(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return super.getPrefixViewXHTMLCode(ctx);
	}

	@Override
	public String getSufixViewXHTMLCode(ContentContext ctx) {
		IContentVisualComponent comp;
		try {
			comp = getMirrorComponent(ctx);
			if (comp != null) {
				return comp.getSufixViewXHTMLCode(ctx);
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
			comp.prepareView(ctx);
			return comp.getViewXHTMLCode(ctx);
		} else {
			deleteMySelf(ctx);
		}
		return "";
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
		if (newLink != null) {
			if (!newLink.equals(getMirrorComponentId())) {
				setMirrorComponentId(newLink);
				setModify();
				setNeedRefresh(true);
			}
		}
	}

}
