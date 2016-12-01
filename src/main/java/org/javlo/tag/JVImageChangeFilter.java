package org.javlo.tag;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;

public class JVImageChangeFilter extends TagSupport {

	private static Logger logger = Logger.getLogger(JVImageChangeFilter.class.getName());

	private static final long serialVersionUID = 1L;

	private String var = null;

	private String filter = null;

	private String newFilter = null;

	private String url = null;

	@Override
	public int doStartTag() throws JspException {
		try {
			ContentContext ctx = ContentContext.getContentContext((HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse());
			String newURL;
			boolean abs = url.startsWith("http");
			if (url.contains('/' + filter + '/') && !url.contains("/img/"+filter+'/')) {				
				newURL = url.replace('/' + filter + '/', '/' + newFilter + '/');				
			} else {
				String img = "/img/";
				int imgPos = url.indexOf(img);
				if (imgPos >= 0) {
					url = url.substring(url.indexOf(img) + img.length());
					newURL = ctx.getGlobalContext().getTransformShortURL(url);
					if (newURL == null) {
						logger.severe("url not found");
						ctx.getRequest().setAttribute(getVar(), url);
						return SKIP_BODY;
					}
					ContentContext localCtx = ctx;
					if (abs) {
						localCtx = ctx.getContextForAbsoluteURL();
					}
					newURL = newURL.replaceFirst(filter + '/', newFilter + '/');					
					newURL = ctx.getGlobalContext().setTransformShortURL(newURL, filter, null);
					newURL = URLHelper.createStaticURL(localCtx, URLHelper.mergePath("img", newURL));
				} else {
					newURL = url;
				}
			}
			ctx.getRequest().setAttribute(getVar(), newURL);
		} catch (Exception ioe) {
			ioe.printStackTrace();
			throw new JspException("Error: " + ioe.getMessage());
		}
		return SKIP_BODY;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getNewFilter() {
		return newFilter;
	}

	public void setNewFilter(String newFilter) {
		this.newFilter = newFilter;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}

}

