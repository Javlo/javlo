package org.javlo.service;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.component.links.MirrorComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.fields.IFieldContainer;
import org.javlo.navigation.MenuElement;

public class DynamicComponentService {
	
	private static Logger logger = Logger.getLogger(DynamicComponentService.class.getName());

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

	private ContentContext getContentContextWithDynamicComponent(ContentContext ctx, MenuElement page) throws Exception {
		if (page.getContentByImplementation(ctx, DynamicComponent.class).size() > 0) {
			return ctx;
		}
		for (IContentVisualComponent comp : page.getContentByImplementation(ctx, MirrorComponent.class)) {
			if (((MirrorComponent) comp).getMirrorComponent(ctx) instanceof DynamicComponent) {
				return ctx;
			}
		}
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ContentContext lgCtx = new ContentContext(ctx);
		Collection<String> languages = globalContext.getContentLanguages();
		for (String lg : languages) {
			lgCtx.setContentLanguage(lg);
			lgCtx.setRequestContentLanguage(lg);
			if (page.getContentByImplementation(lgCtx, DynamicComponent.class).size() > 0) {
				return lgCtx;
			}
			for (IContentVisualComponent comp : page.getContentByImplementation(lgCtx, MirrorComponent.class)) {
				if (((MirrorComponent) comp).getMirrorComponent(lgCtx) instanceof DynamicComponent) {
					return lgCtx;
				}
			}
		}
		return null;
	}

	public List<IFieldContainer> getFieldContainers(ContentContext ctx, MenuElement page, String fieldType) throws Exception {
		if (page == null) {
			return Collections.emptyList();
		}			
		String REQUEST_KEY = page.getId() + "__TYPE__" + fieldType;
		List<IFieldContainer> outContainer = (List<IFieldContainer>) ctx.getRequest().getAttribute(REQUEST_KEY);
		if (outContainer == null) {
			outContainer = new LinkedList<IFieldContainer>();
			for (MenuElement child : page.getAllChildrenList()) {
				ContentContext ctxWithContent = ctx;
				if (ctx.getGlobalContext().isAutoSwitchToDefaultLanguage()) {
					ctxWithContent = getContentContextWithDynamicComponent(ctx, child);
				}
				if (ctxWithContent != null) {
					List<IContentVisualComponent> content = child.getContentByImplementation(ctxWithContent, IFieldContainer.class);
					for (IContentVisualComponent item : content) {
						if (((IFieldContainer) item).isFieldContainer(ctxWithContent) && ((IFieldContainer) item).getContainerType(ctxWithContent).equals(fieldType)) {
							outContainer.add((IFieldContainer) item);
						}
					}
					/*content = child.getContentByImplementation(ctxWithContent, PageMirrorComponent.class);
					for (IContentVisualComponent item : content) {
						PageMirrorComponent pageMirror = (PageMirrorComponent) item;
						MenuElement targetPage = pageMirror.getMirrorPage(ctx);
						List<IContentVisualComponent> targetContent = child.getContentByImplementation(ctxWithContent, IFieldContainer.class);
						for (IContentVisualComponent targetItem : targetContent) {
							if (((IFieldContainer) targetItem).isFieldContainer(ctxWithContent) && ((IFieldContainer) targetItem).getContainerType(ctxWithContent).equals(fieldType)) {
								outContainer.add((IFieldContainer) targetItem);
							}
						}
					}*/
				}
			}
			ctx.getRequest().setAttribute(REQUEST_KEY, outContainer);
		}
		return outContainer;
	}

	public List<String> getAllType(ContentContext ctx, MenuElement page) throws Exception {		
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

		for (MenuElement child : page.getAllChildrenList()) {
			ContentContext contextWithContent = noAreaCtx.getContextWithContent(child);
			if (contextWithContent != null) { // if content exist in any
												// language
				content = child.getContent(contextWithContent);
				while (content.hasNext(contextWithContent)) {
					IContentVisualComponent comp = content.next(contextWithContent);
					if (comp instanceof DynamicComponent) {
						if (!outContainer.contains(comp.getType())) {
							outContainer.add(comp.getType());
						}
					}
				}
			}
		}
		return outContainer;
	}

}
