package org.javlo.macro;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;

/**
 * merge component meta data defined in the template and meta data define in content (only meta data, don't touch to data)
 * 
 * @author pvandermaesen
 * 
 */
public class MergeDynamicComponent extends AbstractMacro {

	@Override
	public String getName() {
		return "merge-dynamic-component";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		ContentService content = ContentService.getInstance(ctx.getRequest());

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Collection<String> languages = globalContext.getContentLanguages();

		for (String lg : languages) {
			ContentContext ctxLg = new ContentContext(ctx);
			ctxLg.setLanguage(lg);
			ctxLg.setContentLanguage(lg);
			ctxLg.setRequestContentLanguage(lg);
			ctxLg.setArea(null);
			MenuElement[] pages = content.getNavigation(ctxLg).getAllChildren();
			for (MenuElement page : pages) {
				ContentElementList comps = page.getContent(ctxLg);
				while (comps.hasNext(ctxLg)) {
					IContentVisualComponent comp = comps.next(ctxLg);
					if (comp instanceof DynamicComponent) {
						DynamicComponent dynComp = (DynamicComponent) comp;
						DynamicComponent newComp = (DynamicComponent) ComponentFactory.getComponentWithType(ctxLg, dynComp.getType());
						newComp.init(new ComponentBean(dynComp.getComponentBean()), ctxLg);
						Properties compProp = dynComp.getProperties();
						Properties newProp = newComp.getProperties();
						Enumeration<Object> keys = newProp.keys();
						while (keys.hasMoreElements()) {
							String key = (String) keys.nextElement();
							if (compProp != null && compProp.get(key) != null) {
								compProp.remove(key);
								compProp.put(key, newProp.get(key));
								dynComp.setModify();
							}
						}
						dynComp.storeProperties();
					}
				}
			}
		}

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
};
