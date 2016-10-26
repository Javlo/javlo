package org.javlo.macro;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.context.ContentContext;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;

public class ClearContext extends AbstractMacro {

	protected static Logger logger = Logger.getLogger(ClearContext.class.getName());

	@Override
	public String getName() {
		return "clear-context";
	}

	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		ContentService contentService = ContentService.getInstance(ctx.getGlobalContext());
		Collection<String> keys = new LinkedList<String>(); 
		keys.addAll(contentService.getGlobalMap(ctx).keySet());
		for (Object key : keys) {
			if (key.toString().contains("&amp;")) {
				String newKey = key.toString();
				while (newKey.toString().contains("amp;amp;")) {
					newKey = newKey.replaceAll("amp;amp;", "amp;");
				}
				newKey = contentService.cleanKey(newKey);
				String val = contentService.getAttribute(ctx, key.toString());
				if (val == null) {
					contentService.setAttribute(ctx, newKey, val);
				}
				contentService.removeAttributeRAWKey(ctx, key.toString());
			}			
		}
		PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
		return null;
	}

	@Override
	public boolean isPreview() {
		return false;
	}

	@Override
	public boolean isAdmin() {
		return true;
	}
};
