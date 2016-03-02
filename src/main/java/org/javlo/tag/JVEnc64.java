package org.javlo.tag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.codec.binary.Base64;

public class JVEnc64 extends TagSupport {

	private static final long serialVersionUID = 1L;

	private String value = null;

	public void setValue(String value) {
		this.value = value;
	}
	
	private String var = null;

	public void setVar(String var) {
		this.var = var;
	}
	
	public String getVar() {
		return var;
	}


	@Override
	public int doStartTag() throws JspException {
		try {						
			pageContext.getRequest().setAttribute(getVar(), Base64.encodeBase64String(value.getBytes()));
		} catch (Exception ioe) {
			throw new JspException("Error: " + ioe.getMessage());
		}
		return SKIP_BODY;
	}

}
