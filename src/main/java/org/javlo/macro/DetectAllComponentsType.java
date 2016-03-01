package org.javlo.macro;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

/**
 * merge component meta data defined in the template and meta data define in
 * content (only meta data, don't touch to data)
 * 
 * @author pvandermaesen
 * 
 */
public class DetectAllComponentsType extends AbstractMacro {
	
	private static Logger logger = Logger.getLogger(DetectAllComponentsType.class.getName());

	@Override
	public String getName() {
		return "detect-components-types";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		ContentService content = ContentService.getInstance(ctx.getRequest());

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Collection<String> languages = globalContext.getContentLanguages();
		
		Set<String> components = new HashSet<String>();

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
					components.add(comp.getClassName());
				}
			}
		}
		
		List<String> compList = globalContext.getComponents();
		compList.addAll(components);
		globalContext.setComponents(compList);
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
};
