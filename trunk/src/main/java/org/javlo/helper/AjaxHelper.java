package org.javlo.helper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;

import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.module.core.Module;
import org.javlo.module.core.Module.Box;

public class AjaxHelper {

	public static final void updateBox(ContentContext ctx, Box box) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
		Map<String, Object> attibutes = new HashMap<String, Object>();
		attibutes.put("box", box);
		ctx.scheduleAjaxInsideZone(box.getId(), editCtx.getBoxTemplate(), attibutes);
	}

	public static final void updateMainRenderer(ContentContext ctx, Module module) {
		ctx.scheduleAjaxInsideZone("main-renderer", module.getRenderer(), null);
	}

	private static String render(ContentContext ctx, ScheduledRender scheduledRender) throws ServletException, IOException {
		Map<String, Object> attributes = scheduledRender.getAttributes();
		if (attributes != null) {
			for (Entry<String, Object> attribute : attributes.entrySet()) {
				ctx.getRequest().setAttribute(attribute.getKey(), attribute.getValue());
			}
		}
		return ServletHelper.executeJSP(ctx, scheduledRender.getUri());
	}

	public static Map<String, String> render(ContentContext ctx, Map<String, String> out, Map<String, ScheduledRender> scheduledRenders) throws ServletException, IOException {
		for (Entry<String, ScheduledRender> entry : scheduledRenders.entrySet()) {
			String xhtml = AjaxHelper.render(ctx, entry.getValue());
			out.put(entry.getKey(), xhtml);
		}
		return out;
	}

	public static class ScheduledRender {

		private final String uri;
		private final Map<String, Object> attributes;

		public ScheduledRender(String uri, Map<String, Object> attributes) {
			this.uri = uri;
			this.attributes = attributes;
		}

		public String getUri() {
			return uri;
		}

		public Map<String, Object> getAttributes() {
			return attributes;
		}

	}

}
