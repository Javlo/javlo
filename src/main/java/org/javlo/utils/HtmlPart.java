package org.javlo.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.javlo.helper.StringHelper;
import org.owasp.encoder.Encode;

public class HtmlPart {
	
	private String text;
	private String cssClass = null;
	private String tag = "div";
	private String id = null;
	private String style;
	
	public HtmlPart(String text) {
		super();
		this.text = text;
	}
	
	public HtmlPart(String text, String cssClass) {
		super();
		this.text = text;
		this.cssClass = cssClass;
	}
	
	public HtmlPart(String text, String tag, String cssClass) {
		super();
		this.text = text;
		this.tag = tag;
		this.cssClass = cssClass;
	}
	
	@Override
	public String toString() {
		return text;
	}
	
	public String getHtml() {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.print("<"+tag);
		if (!StringHelper.isEmpty(cssClass)) {
			out.print(" class=\""+Encode.forHtmlAttribute(cssClass)+"\"");
		}
		if (!StringHelper.isEmpty(id)) {
			out.print(" id=\""+Encode.forHtmlAttribute(id)+"\"");
		}
		if (!StringHelper.isEmpty(style)) {
			out.print(" style=\""+Encode.forHtmlAttribute(style)+"\"");
		}
		out.print(">"+text+"</"+tag+">");
		out.close();
		return new String(outStream.toByteArray());
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}
	
	
	
	

}
