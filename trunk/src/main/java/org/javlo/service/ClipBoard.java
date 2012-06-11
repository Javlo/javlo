/*
 * Created on 25-janv.-2004
 */
package org.javlo.service;

import javax.servlet.http.HttpServletRequest;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;


/**
 * @author pvandermaesen
 * used for copy&paste a component in the site.
 */
public class ClipBoard {
	
	private static final String CLIPBOARD_SESSION_KEY = "__clipboard__";
	
	Object copied = null;
	
	private ClipBoard(){};
	
	public static ClipBoard getInstance ( HttpServletRequest request ) {
		ClipBoard res = (ClipBoard)request.getSession().getAttribute(CLIPBOARD_SESSION_KEY);
		if ( res == null ) {
			res = new ClipBoard();
			request.getSession().setAttribute(CLIPBOARD_SESSION_KEY, res );
		}
		return res;
	}
	
	public void copy ( Object inCopied ) {
		copied=inCopied;
	}
	
	public Object getCopied() {
		return copied;
	}
	
	public boolean isEmpty(ContentContext ctx) throws Exception {
		return getCopiedComponent(ctx)==null;
	}
	
	public void clear() {
		copied=null;
	}
	
	public ComponentBean getCopiedComponent(ContentContext ctx) throws Exception {
		if (copied == null) {
			return null;
		}
		
		if (copied instanceof ComponentBean) {
			ContentService content = ContentService.createContent(ctx.getRequest());
			return (ComponentBean)copied;
		} else {
			return null;
		}
	}

}