package org.javlo.component.navigation;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;

public class PageURL extends AbstractVisualComponent {

	public static final String TYPE = "URL";

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getDebugHeader(ctx));
		finalCode.append(getSpecialInputTag());
		finalCode.append("<input  id=\"" + getContentName() + "\" name=\"" + getContentName() + "\" value=\"");
		finalCode.append(getValue());
		finalCode.append("\" />");
		return finalCode.toString();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean isUnique() {
		return true;
	}
}
