/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.meta;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComplexPropertiesLink;
import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.RequestService;

/**
 * list of tags of the current page. <h4>JSTL variable :</h4>
 * <ul>
 * <li>inherited from {@link AbstractVisualComponent}</li>
 * <li>{@link String} tags : list of tags. See {@link MenuElement#getTags}</li> *
 * </ul>
 * 
 * @author pvandermaesen
 */
public class Tags extends ComplexPropertiesLink {

	public static final String TYPE = "tags";

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		List<String> tags = globalContext.getTags();
		Collections.sort(tags);
		int i = 0;
		List<String> currentTags = getTags();
		out.print("<input type=\"hidden\" name=\"tag-" + getId() + "\" value=\"tag\"/>");
		out.println("<ul>");
		for (String tag : tags) {
			String checked = "";
			if (currentTags.contains(tag)) {
				checked = " checked=\"checked\"";
			}
			out.print("<li class=\"line\"><input type=\"checkbox\"" + checked + " id=\"" + getInputName(i) + "\" name=\"" + getInputName(i) + "\" value=\"" + tag + "\"/>");
			out.print("<label for=\"" + getInputName(i) + "\" >" + tag + "</label></li>");
			i++;
		}
		out.println("</ul>");
		return writer.toString();
	}

	@Override
	public String getHexColor() {
		return WEB2_COLOR;
	}

	private String getInputName(int i) {
		return "__tag__" + getId() + "__" + i;
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {		
		return super.getPrefixViewXHTMLCode(ctx) + "<div" + getSpecialPreviewCssClass(ctx, getStyle(ctx)+" list count"+getTags().size()) + getSpecialPreviewCssId(ctx) + ">";
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "</div>" + super.getSuffixViewXHTMLCode(ctx);
	}

	public List<String> getTags() {
		String[] tags = StringHelper.stringToArray(getValue(), ";");
		List<String> tagsList = new LinkedList<String>();
		for (String tag : tags) {
			if (tag.trim().length() > 0) {
				tagsList.add(tag);
			}
		}
		return tagsList;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		if (ctx.getCurrentPage() != null) {
			ctx.getRequest().setAttribute("tags", ctx.getCurrentPage().getTags(ctx));
		}
	}

	

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		Collection<String> tags = getTags();
		if (tags.size() == 0) {
			MenuElement currentPage = ctx.getCurrentPage();
			tags = currentPage.getTags(ctx);
		}
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		ContentContext lgCtx = new ContentContext(ctx);
		lgCtx.setLanguage(ctx.getRequestContentLanguage());
		i18nAccess.changeViewLanguage(lgCtx);

		boolean isFirst = true;
		String sep="";
		for (String tag : tags) {			
			String trad = i18nAccess.getViewText("tag." + tag, (String) null);
			if (trad == null) {
				i18nAccess.changeViewLanguage(ctx);
				trad = i18nAccess.getViewText("tag." + tag, tag);
				i18nAccess.changeViewLanguage(lgCtx);
			}
			out.println(sep+"<span class=\""+tag+" tag label label-default" + (isFirst ? " first" : "") + "\">");
			out.println(trad);
			out.println("</span>");
			sep = "<span class=\"sep\">-</span>";
			isFirst = false;
		}
		out.close();

		i18nAccess.changeViewLanguage(ctx);

		return new String(outStream.toByteArray());

	}

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {
		super.init(bean, newContext);
		properties.load(stringToStream(getValue()));
	}

	@Override
	public boolean isUnique() {
		return true;
	}

	@Override
	public void performEdit(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		List<String> tags = globalContext.getTags();
		if (requestService.getParameter("tag-" + getId(), null) == null) { // not a real refresh
			return;
		}
		String finalTags = "";
		String sep = "";
		for (int i = 0; i < tags.size(); i++) {
			String tag = requestService.getParameter(getInputName(i), null);
			if (tag != null) {
				finalTags = finalTags + sep + tag;
				sep = ";";
			}
		}
		if (!getValue().equals(finalTags)) {
			setValue(finalTags);
			setModify();
		}
	}

}
