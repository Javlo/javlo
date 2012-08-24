package org.javlo.service;

import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.fields.IFieldContainer;
import org.javlo.navigation.MenuElement;
import org.javlo.service.exception.ServiceException;

public class DynamicComponentService {

	GlobalContext globalContext;

	private static final String KEY = DynamicComponentService.class.getName();

	private DynamicComponentService() {
	};

	private DynamicComponentService(GlobalContext globalContext) {
		this.globalContext = globalContext;
	}

	public static DynamicComponentService getInstance(GlobalContext globalContext) {
		DynamicComponentService outService = (DynamicComponentService) globalContext.getAttribute(KEY);
		if (outService == null) {
			outService = new DynamicComponentService(globalContext);
		}
		return outService;
	}

	public List<IFieldContainer> getFieldContainers(ContentContext ctx, MenuElement page, String fieldType) throws Exception {
		MenuElement[] children = page.getAllChilds();
		List<IFieldContainer> outContainer = new LinkedList<IFieldContainer>();
		for (MenuElement child : children) {
			ContentContext ctxWithContent = ctx.getContextWithContent(page);
			if (ctxWithContent != null) {
				List<IContentVisualComponent> content = child.getContentByType(ctxWithContent, fieldType);
				for (IContentVisualComponent item : content) {
					if (item instanceof IFieldContainer) {
						outContainer.add((IFieldContainer) item);
					} else {
						throw new ServiceException("component : " + fieldType + " is not a IFieldContainer.");
					}
				}
			}
		}
		return outContainer;
	}

	public List<String> getAllType(ContentContext ctx, MenuElement page) throws Exception {
		MenuElement[] children = page.getAllChilds();
		List<String> outContainer = new LinkedList<String>();

		ContentContext noAreaCtx = ctx.getContextWithoutArea();
		ContentElementList content = page.getContent(noAreaCtx);
		while (content.hasNext(noAreaCtx)) {
			IContentVisualComponent comp = content.next(noAreaCtx);
			if (comp instanceof DynamicComponent) {
				if (!outContainer.contains(comp.getType())) {
					outContainer.add(comp.getType());
				}
			}
		}

		for (MenuElement child : children) {
			content = child.getContent(noAreaCtx);
			while (content.hasNext(noAreaCtx)) {
				IContentVisualComponent comp = content.next(noAreaCtx);
				if (comp instanceof DynamicComponent) {
					if (!outContainer.contains(comp.getType())) {
						outContainer.add(comp.getType());
					}
				}
			}
		}
		return outContainer;
	}

}
