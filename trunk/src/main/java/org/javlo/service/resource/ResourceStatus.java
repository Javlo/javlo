package org.javlo.service.resource;

import java.util.Stack;

import javax.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.ztatic.FileCache;

public class ResourceStatus {

	private static final String KEY = "resourceStatus";

	private Stack<LocalResource> sources = new Stack<LocalResource>();
	private Stack<LocalResource> targets = new Stack<LocalResource>();

	public static boolean isResource(HttpSession session) {
		if (session.getAttribute(KEY) != null && session.getAttribute(KEY) instanceof ResourceStatus) {
			ResourceStatus resourceStatus = (ResourceStatus) session.getAttribute(KEY);
			if (!resourceStatus.sources.empty()) {
				return true;
			}
		}
		return false;
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
		fileCache.deleteAllFile(globalContext.getInstance(ctx.getRequest()).getContextKey(), getSource().getFile().getName());		
		release();
	}

	private void release() {
		if (!sources.empty()) {
			sources.pop().getFile().delete();
			targets.pop();
		}
	}
	
	public int getSize() {
		return sources.size();
	}

	@Override
	protected void finalize() throws Throwable {
		release();
		super.finalize();
	}

	public LocalResource getSource() {
		if (!sources.empty()) {
			return sources.peek();
		}
		return null;
	}

	public void addSource(LocalResource source) {
		source.getFile().deleteOnExit();
		sources.push(source);
	}

	public LocalResource getTarget() {
		if (!targets.empty()) {
			return targets.peek();
		}
		return null;
	}

	public void addTarget(LocalResource target) {
		targets.push(target);
	}

}
