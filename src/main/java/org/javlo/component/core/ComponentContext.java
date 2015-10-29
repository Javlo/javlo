package org.javlo.component.core;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class ComponentContext {

	private static Logger logger = Logger.getLogger(ComponentContext.class.getName());

	private static final String INSERTION_COMPONENT_KEY = "_insertion_component";

	private static final String KEY = "componentContext";

	private final List<IContentVisualComponent> newComponentId = new LinkedList<IContentVisualComponent>();

	private boolean renderLink = true;

	public static ComponentContext getInstance(HttpServletRequest request) {
		ComponentContext outInstance = (ComponentContext) request.getAttribute(KEY);
		if (outInstance == null) {
			outInstance = new ComponentContext();
			request.setAttribute(KEY, outInstance);
		}
		return outInstance;
	}

	public void addNewComponent(IContentVisualComponent comp) {
		if (comp != null) {
			newComponentId.add(comp);
		}
	}

	public void clearComponents() {
		newComponentId.clear();
	}

	public List<IContentVisualComponent> getNewComponents() {
		return newComponentId;
	}

	public boolean isRenderLink() {
		return renderLink;
	}

	public void setRenderLink(boolean renderLink) {
		this.renderLink = renderLink;
	}

	public static void prepareComponentInsertion(HttpSession session, IContentVisualComponent comp) {
		session.setAttribute(INSERTION_COMPONENT_KEY, comp);
	}

	public static IContentVisualComponent getPreparedComponent(HttpSession session) {
		return (IContentVisualComponent) session.getAttribute(INSERTION_COMPONENT_KEY);
	}

	public static void clearPreparedComponent(HttpSession session) {
		session.removeAttribute(INSERTION_COMPONENT_KEY);
	}
		
}
