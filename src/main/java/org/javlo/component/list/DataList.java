/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.list;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.IListItem;
import org.javlo.service.ListService.ListItem;
import org.javlo.service.RequestService;
import org.javlo.service.ReverseLinkService;
import org.javlo.utils.SuffixPrefix;

/**
 * @author pvandermaesen
 */
public class DataList extends AbstractVisualComponent {
	
	public static final String TYPE = "data-list";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		if (getRenderer(ctx) != null) {
			ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(ctx.getGlobalContext());
			String value = XHTMLHelper.autoLink(getValue());
			value = reverserLinkService.replaceLink(ctx, this, value);
			BufferedReader read = new BufferedReader(new StringReader(value));
			String line = read.readLine();
			List<String> lines = new LinkedList<String>();
			while (line != null) {				
				lines.add(line);
				line = read.readLine();
			}
			ctx.getRequest().setAttribute("lines", lines);
		}
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}

	@Override
	public boolean isInline() {
		return false;
	}

	public String getNameInputName() {
		return "name-" + getId();
	}

	public String getNameValue() {
		String value = getValue();
		if (value != null) {
			if (value.length() >= 3) {
				if (value.startsWith("{")) {
					return "" + value.substring(1, value.indexOf('}'));
				}
			}
		}
		return "";
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		finalCode.append(getSpecialInputTag());
		finalCode.append("<div class=\"form-group\"><div class=\"row\"><div class=\"col-sm-2\">");
		finalCode.append("<label for=\"" + getNameInputName() + "\">" + i18nAccess.getText("content.data-list.name", "name") + " :</label></div><div class=\"col-sm-3\">");
		finalCode.append("<input class=\"form-control\" id=\"" + getNameInputName() + "\" name=\"" + getNameInputName() + "\" value=\"" + getNameValue() + "\" />");
		finalCode.append("</div></div></div>");
		finalCode.append("<textarea class=\"form-control\" id=\"" + getContentName() + "\" name=\"" + getContentName() + "\"");
		finalCode.append(" rows=\"" + (Math.max(countLine(),4) + 1) + "\">");
		String value = getValue();
		if (getNameValue().length() > 0) {
			value = value.substring(value.indexOf('}')+1);
		}
		finalCode.append(value);
		finalCode.append("</textarea>");

		return finalCode.toString();
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String newContent = requestService.getParameter(getContentName(), null);
		String sepValue = requestService.getParameter(getNameInputName(), "");

		if (newContent != null) {
			if (sepValue.length() > 0) {
				newContent = '{' + sepValue + '}' + newContent;
			}
			if (!getComponentBean().getValue().equals(newContent)) {
				getComponentBean().setValue(newContent);
				setModify();
			}
		}
		return null;
	}

	@Override
	public List<SuffixPrefix> getMarkerList(ContentContext ctx) {
		return getItalicAndStrongLanguageMarkerList(ctx);
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return getRenderer(ctx) != null;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}
	
	@Override
	public String getFontAwesome() {	
		return "list-ul";
	}
	
	public List<IListItem> getList(ContentContext ctx) {
		String value = getValue();
		if (value.contains("}")) {
			value = value.substring(value.indexOf('}')+1);
		}
		BufferedReader read = new BufferedReader(new StringReader(value));
		String line;
		List<IListItem> outList = new LinkedList<>();
		try {
			line = read.readLine();
			while (line != null) {
				outList.add(new ListItem(line, line));
				line = read.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return outList;
	}

}
