package org.javlo.macro;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.StringHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public abstract class AbstractMacro implements IMacro {

	@Override
	public String toString() {
		return getName();
	}

	public void createPageStructure(ContentContext ctx, MenuElement page, Map componentsType) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Collection<String> lgs = globalContext.getContentLanguages();
		if (!StringHelper.isTrue(""+componentsType.get("all-languages"))) {
			lgs = Arrays.asList(new String[] {ctx.getRequestContentLanguage()});
		}

		for (String lg : lgs) {
			String parentId = "0";
			Set<String> keysSet = componentsType.keySet();
			List<String> keys = new LinkedList<String>();
			keys.addAll(keysSet);
			Collections.sort(keys);
			for (String compName : keys) {
				if (compName.contains(".") && !compName.endsWith(".style") && !compName.endsWith(".list") && !compName.endsWith(".area")) {
					String style = (String)componentsType.get(compName+".style");					
					boolean asList = StringHelper.isTrue((String)componentsType.get(compName+".list"));
					String area = (String)componentsType.get(compName+".area");
					parentId = MacroHelper.addContent(lg, page, parentId, StringHelper.split(compName, ".")[1], style, area, (String)componentsType.get(compName), asList);
				}
			}
		}
	}
	
	public List<IContentVisualComponent> getAllComponent(ContentContext ctx) throws Exception {
		List<IContentVisualComponent> outList = new LinkedList<IContentVisualComponent>();
		MenuElement root = ContentService.createContent(ctx.getRequest()).getNavigation(ctx);
		MenuElement[] children = root.getAllChilds();
		ContentContext noAreaCtx = new ContentContext(ctx);
		noAreaCtx.setArea(null);
		for (MenuElement child : children) {
			Collection<ContentContext> lgCtxs = noAreaCtx.getContextForAllLanguage();
			for (ContentContext lgCtx : lgCtxs) {
				ContentElementList content = child.getContent(lgCtx);
				while (content.hasNext(lgCtx)) {
					outList.add(content.next(lgCtx));
				}
			}
		}
		return outList;
	}
	
	@Override
	public boolean isAdmin() {
		return true;
	}

}
