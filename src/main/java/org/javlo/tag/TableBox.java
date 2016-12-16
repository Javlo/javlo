package org.javlo.tag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.javlo.helper.StringHelper;

public class TableBox extends TagSupport {

	private static final long serialVersionUID = 1L;

	private Integer padding = null;

	private Integer margin = null;
	
	private String style = null;

	@Override
	public int doStartTag() throws JspException {
		try {
			if (margin==null) {
				margin=0;
			}
			int marginTop = margin;
			int marginRight = margin;
			int marginBottom = margin;
			int marginLeft = margin;
			if (padding==null) {
				padding=0;
			}
			int paddingTop = padding;
			int paddingRight = padding;
			int paddingBottom = padding;
			int paddingLeft = padding;
			
			String styleAttr = "";
			if (!StringHelper.isEmpty(style)) {
				styleAttr = " style=\""+style+"\"";
			}
			
			String html = "<table style=\"border-collapse: collapse;\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"3\" height=\"" + marginTop + "\"></td></tr><tr><td width=\""+marginLeft+"\"></td><td"+styleAttr+">";
			html = html + "<table style=\"border-collapse: collapse;\"  border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr><td colspan=\"3\" height=\"" + paddingTop + "\"></td></tr><tr><td width=\""+paddingLeft+"\"></td><td>";
			JspWriter out = pageContext.getOut();
			out.print(html);

		} catch (Exception ioe) {
			throw new JspException("Error: " + ioe.getMessage());
		}
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() throws JspException {
		if (margin==null) {
			margin=0;
		}
		int marginTop = margin;
		int marginRight = margin;
		int marginBottom = margin;
		int marginLeft = margin;		
		if (padding==null) {
			padding=0;
		}
		int paddingTop = padding;
		int paddingRight = padding;
		int paddingBottom = padding;
		int paddingLeft = padding;
		
		String html = "</td><td width=\""+paddingBottom+"\"></td></tr><tr><td colspan=\"3\" height=\"" + paddingBottom + "\"></td></tr></td></table>";
		html = html + "</td><td width=\""+marginBottom+"\"></tr><tr><td colspan=\"3\" height=\"" + marginBottom + "\"></td></tr></td></table>";
		JspWriter out = pageContext.getOut();
		try {
			out.print(html);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return EVAL_BODY_INCLUDE;
	}

	public Integer getPadding() {
		return padding;
	}

	public void setPadding(Integer padding) {
		this.padding = padding;
	}

	public Integer getMargin() {
		return margin;
	}

	public void setMargin(Integer margin) {
		this.margin = margin;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

}
