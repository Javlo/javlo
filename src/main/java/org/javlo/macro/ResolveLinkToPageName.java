package org.javlo.macro;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.text.WysiwygParagraph;
import org.javlo.component.text.XHTML;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * merge component meta data defined in the template and meta data define in
 * content (only meta data, don't touch to data)
 * 
 * @author pvandermaesen
 * 
 */
public class ResolveLinkToPageName extends AbstractMacro {

	private boolean onlyPage = true;

	private static Logger logger = Logger.getLogger(ResolveLinkToPageName.class.getName());

	public ResolveLinkToPageName(boolean onlyPage) {
		this.onlyPage = onlyPage;
	}

	@Override
	public String getName() {
		return "url-to-page-name-"+(onlyPage?"page":"all-site");
	}

	private List<MenuElement> getPages(ContentContext ctx, String lg) throws Exception {
		List<MenuElement> pages = new ArrayList<MenuElement>();
		ContentContext ctxLg = new ContentContext(ctx);
		ctxLg.setLanguage(lg);
		ctxLg.setContentLanguage(lg);
		ctxLg.setRequestContentLanguage(lg);
		ctxLg.setArea(null);
		if (onlyPage) {
			pages.add(ctxLg.getCurrentPage(true));
		} else {
			ContentService content = ContentService.getInstance(ctx.getRequest());
			return content.getNavigation(ctxLg).getAllChildrenList();
		}
		return pages;
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

			for (MenuElement page : getPages(ctx, lg)) {
				ContentElementList comps = page.getContent(ctxLg);
				while (comps.hasNext(ctxLg)) {
					IContentVisualComponent comp = comps.next(ctxLg);
					if (comp instanceof WysiwygParagraph || comp instanceof XHTML) {
						String value = ((AbstractVisualComponent) comp).getValue(ctx);
						List<String> urls = NetHelper.extractHrefs(value);
						for (String url : urls) {
							String pageName = NetHelper.getPageName(ctx, url);
							logger.info("##SEARCH_NAME## - page:"+pageName+" // url:"+url);
							if (pageName != null) {
								value = value.replace(url, "page:"+pageName);
							}
						}
						comp.setValue(value);
					}
				}
			}
		}

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.setAskStore(true);

		return null;
	}

	@Override
	public String getIcon() {
		return "bi bi-link-45deg";
	}

	@Override
	public boolean isPreview() {
		return true;
	}
};
