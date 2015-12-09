package org.javlo.data;

import javax.servlet.http.HttpServletRequest;

import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;

public class EditInfoBean {

	public static final String REQUEST_KEY = "editInfo";

	public static final String NEW_SESSION_PARAM = "__new_session";

	public static EditInfoBean getCurrentInfoBean(HttpServletRequest request) {
		return (EditInfoBean) request.getAttribute(REQUEST_KEY);
	}

	public static EditInfoBean getCurrentInfoBean(ContentContext ctx) throws Exception {
		EditInfoBean ib = getCurrentInfoBean(ctx.getRequest());
		if (ib == null) {
			ib = updateInfoBean(ctx);
		}
		return ib;
	}

	/**
	 * create info bean in request (key=info) for jstp call in template.
	 * 
	 * @param ctx
	 * @throws Exception
	 */
	public static EditInfoBean updateInfoBean(ContentContext ctx) throws Exception {
		EditInfoBean info = new EditInfoBean();		
		info.ctx = ctx;
		ctx.getRequest().setAttribute(REQUEST_KEY, info);

		return info;
	}

	private ContentContext ctx;
	
	public String getCopiedPage() throws Exception {
		ContentContext copiedCtx = EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession()).getContextForCopy(ctx);
		if (copiedCtx == null) {
			return null;
		} else {
			return copiedCtx.getCurrentPage().getName();
		}
	}
	
		
}
