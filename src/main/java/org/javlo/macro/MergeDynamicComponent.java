package org.javlo.macro;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

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
 * merge component meta data defined in the template and meta data define in
 * content (only meta data, don't touch to data)
 * 
 * @author pvandermaesen
 * 
 */
public class MergeDynamicComponent extends AbstractMacro {
	
	private static Logger logger = Logger.getLogger(MergeDynamicComponent.class.getName());

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
			for (MenuElement page : content.getNavigation(ctxLg).getAllChildrenList()) {
				ContentElementList comps = page.getContent(ctxLg);
				while (comps.hasNext(ctxLg)) {
					IContentVisualComponent comp = comps.next(ctxLg);
					if (comp instanceof DynamicComponent) {
						DynamicComponent dynComp = (DynamicComponent) comp;
						DynamicComponent newComp = (DynamicComponent) ComponentFactory.getComponentWithType(ctxLg, comp.getPage(), dynComp.getType());
						// newComp.init(new
						// ComponentBean(dynComp.getComponentBean()), ctxLg);
						if (newComp != null) {
							Properties compProp = dynComp.getProperties();
							Properties newProp = newComp.getProperties();							
							if (compProp != null && newProp != null) {
								Enumeration<Object> keys = newProp.keys();
								while (keys.hasMoreElements()) {
									String key = (String) keys.nextElement();
									if (compProp.get(key) != null) {
										compProp.remove(key);
										compProp.put(key, newProp.get(key));
										dynComp.setModify();
									} else if (!key.endsWith(".value")) {
										compProp.put(key, newProp.get(key));
									}
								}
							}
							dynComp.storeProperties();
							dynComp.reload(ctx);
						} else {
							logger.warning("bad dynamic component : "+dynComp.getType()+ " ("+dynComp.getPage().getPath()+')');
						}
					}
				}
			}
		}

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.setAskStore(true);

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
};
