package org.javlo.service.resource;

import javax.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.ztatic.FileCache;

public class ResourceStatus {

	private static final String KEY = "resourceStatus";

	private LocalResource source = null;
	private LocalResource target = null;

	public static boolean isInstance(HttpSession session) {
		if (session.getAttribute(KEY) == null) {
			return false;
		} else if (session.getAttribute(KEY) instanceof ResourceStatus) {
			return true;
		} else {
			return false;
		}
	}

	public static ResourceStatus getInstance(HttpSession session) {
		ResourceStatus instance = (ResourceStatus) session.getAttribute(KEY);
		if (instance == null) {
			instance = new ResourceStatus();
			session.setAttribute(KEY, instance);
		}
		return instance;
	}

	public void release(ContentContext ctx) {

		FileCache fileCache = FileCache.getInstance(ctx.getRequest().getSession().getServletContext());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		fileCache.deleteAllFile(globalContext.getInstance(ctx.getRequest()).getContextKey(), source.getFile().getName());
		ctx.getRequest().getSession().removeAttribute(KEY);
		release();
	}

	private void release() {
		if (source.getFile().exists()) {
			source.getFile().delete();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		release();
		super.finalize();
	}

	public LocalResource getSource() {
		return source;
	}

	public void setSource(LocalResource source) {
		source.getFile().deleteOnExit();
		this.source = source;
	}

	public LocalResource getTarget() {
		return target;
	}

	public void setTarget(LocalResource target) {
		this.target = target;
	}

}
