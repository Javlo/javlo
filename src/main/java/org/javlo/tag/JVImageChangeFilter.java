package org.javlo.tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.tagext.TagSupport;
import org.apache.commons.lang3.StringUtils;
import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;

import java.util.logging.Logger;

public class JVImageChangeFilter extends TagSupport {

	private static Logger logger = Logger.getLogger(JVImageChangeFilter.class.getName());

	private static final long serialVersionUID = 1L;

	private String var = null;

	private String filter = null;

	private String newFilter = null;

	private String url = null;

	@Override
	public int doStartTag() {
		try {
			ContentContext ctx = ContentContext.getContentContext((HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse());
			String newURL;
			boolean abs = url.startsWith("http");
			if (url.contains('/' + filter + '/') && !url.contains("/img/"+filter+'/')) {
				newURL = StringUtils.replaceFirst(url, "/transform/" + filter + '/', "/transform/" + newFilter + '/');
				//newURL = url.replaceF('/' + filter + '/', '/' + newFilter + '/');				
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
					if (newURL.startsWith(filter)) {
						newURL = newFilter+newURL.substring(filter.length());
					} else {
						logger.warning("newURL don't start with filter : "+filter);
					}					
//					newURL = newURL.replaceFirst(filter + '/', newFilter + '/');	
//					newURL = StringUtils.replaceFirst(newURL, filter + '/',  newFilter + '/');
					newURL = ctx.getGlobalContext().setTransformShortURL(newURL, newFilter, null);
					newURL = URLHelper.createStaticURL(localCtx, URLHelper.mergePath("img", newURL));
				} else {
					newURL = url;
				}
			}
			ctx.getRequest().setAttribute(getVar(), newURL);
		} catch (Exception ioe) {
			ioe.printStackTrace();
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

