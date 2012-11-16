/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.core;

import org.javlo.context.ContentContext;

/**
 * @author pvandermaesen
 */
public class Unknown extends AbstractVisualComponent {
	
	String type = "unknow";
	
	public Unknown ( ContentContext newCtx, ComponentBean bean  ) throws Exception {
		init(bean, newCtx );		
	}	

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getSpecialInputTag());
		finalCode.append("<br />");
		finalCode.append("<b>&nbsp;not  found : "+getComponentBean().getType()+"</b>");
		finalCode.append("<br /><br />");
		finalCode.append("<textarea id=\"" + getContentName() + "\" name=\"" + getContentName() + "\"");
		finalCode.append(" rows=\"" + (countLine() + 1) + "\" onkeyup=\"javascript:resizeTextArea($('" + getContentName() + "'));\">");
		finalCode.append(getValue());
		finalCode.append("</textarea>");

		return finalCode.toString();
	}
	
	@Override
	public String getXHTMLConfig(ContentContext ctx) throws Exception {
		return "?";
	}

	/*
	 * @see org.javlo.itf.IContentVisualComponent#getType()
	 */
	public String getType() {		
		return type;
	}
}
