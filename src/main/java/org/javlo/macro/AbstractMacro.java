package org.javlo.macro;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IMacro;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public abstract class AbstractMacro implements IMacro {
	
	@Override
	public String toString() {
		return getName();
	}

	protected Properties getMacroProperties(ContentContext ctx) throws IOException, Exception {
		return ctx.getCurrentTemplate().getMacroProperties(ctx.getGlobalContext(), getName());
	}

	public List<IContentVisualComponent> getAllComponent(ContentContext ctx) throws Exception {
		List<IContentVisualComponent> outList = new LinkedList<IContentVisualComponent>();
		MenuElement root = ContentService.getInstance(ctx.getRequest()).getNavigation(ctx);
		ContentContext noAreaCtx = new ContentContext(ctx);
		noAreaCtx.setArea(null);
		for (MenuElement child : root.getAllChildrenList()) {
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
	public String getInfo(ContentContext ctx) {
		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
			return i18nAccess.getText("macro.info."+getName(), (String)null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean isAdmin() {
		return true;
	}

	@Override
	public boolean isAdd() {
		return false;
	}

	@Override
	public boolean isInterative() {	
		return false;
	}
	
	@Override
	public boolean isActive() {
		return true;
	}
	
	@Override
	public void init(ContentContext ctx) {
	}
	
	@Override
	public String getIcon() {
		return "bi bi-nut";
	}
	
	@Override
	public String getUrl() {
		return null;
	}
	
	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}

	@Override
	public int getType() {
		return TYPE_TOOLS;
	}

	public String getLabel() {
		String name = getName();
		if (name == null || name.length() < 3) {
			return name;
		}
		String label = name.replace("macro.", "").replace("-", " ");
		label = label.substring(0, 1).toUpperCase() + label.substring(1);
		return label;
	}
	
}
