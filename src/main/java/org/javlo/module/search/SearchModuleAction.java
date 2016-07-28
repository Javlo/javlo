package org.javlo.module.search;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.title.Heading;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.RequestService;
import org.javlo.ztatic.StaticInfo;

public class SearchModuleAction extends AbstractModuleAction {

	@Override
	public String getActionGroupName() {
		return "search-module";
	}

	private static boolean isMatching(ComponentBean comp, SearchFilter filter) {
		if (!StringHelper.isEmpty(filter.getTitle())) {
			if ((comp.getType().equals(Title.TYPE) || comp.getType().equals(Heading.TYPE)) && comp.getValue().toLowerCase().contains(filter.getTitle().toLowerCase())) {
				return true;
			}
		}
		if (!StringHelper.isEmpty(filter.getGlobal()) && StringHelper.isEmpty(filter.getTitle())) {
			if (comp.getValue().toLowerCase().contains(filter.getGlobal().toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean isMatching(ContentContext ctx, StaticInfo info, SearchFilter filter) {
		if (!StringHelper.isEmpty(filter.getTitle())) {
			if ((StringHelper.neverNull(info.getTitle(ctx)).toLowerCase().contains(filter.getTitle().toLowerCase()))) {
				return true;
			}
		}
		if (!StringHelper.isEmpty(filter.getGlobal()) && StringHelper.isEmpty(filter.getTitle())) {
			String text = (info.getTitle(ctx)+info.getDescription(ctx)+info.getFile().getName()).toLowerCase();
			if (text.contains(filter.getGlobal().toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static Collection<SearchResultBean> searchInPage(ContentContext ctx, SearchFilter filter) throws Exception {
		Map<MenuElement, SearchResultBean> outResult = new HashMap<MenuElement, SearchResultBean>();
		for (MenuElement page : ctx.getCurrentPage().getRoot().getAllChildren()) {
			for (ComponentBean comp : page.getContent()) {
				if (isMatching(comp, filter)) {
					if (outResult.get(page) == null) {
						outResult.put(page, new SearchResultBean("page", page.getTitle(ctx), comp.getLanguage(), URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), page), comp.getAuthors(), StringHelper.renderSortableDate(page.getModificationDate()), 1));
					} else {
						outResult.get(page).setMatching(outResult.get(page).getMatching() + 1);
					}
					break;
				}
			}
		}
		return outResult.values();
	}
	
	public static Collection<SearchResultBean> searchInResource(ContentContext ctx, SearchFilter filter) throws Exception {
		Map<Object, SearchResultBean> outResult = new HashMap<Object, SearchResultBean>();		
		File staticFolder = new File(ctx.getGlobalContext().getStaticFolder());
		if (staticFolder.exists()) {
			for (File file : ResourceHelper.getAllFilesList(staticFolder)) {
				StaticInfo staticInfo = StaticInfo.getInstance(ctx, file);
				if (isMatching(ctx, staticInfo, filter)) {
					if (outResult.get(staticInfo) == null) {
						String title = staticInfo.getTitle(ctx);
						if (StringHelper.isEmpty(title)) {
							title = staticInfo.getFile().getName();
						}
						outResult.put(staticInfo, new SearchResultBean("file", title, ctx.getContentLanguage(), staticInfo.getURL(ctx), staticInfo.getAuthors(ctx), StringHelper.renderSortableDate(staticInfo.getCreationDate(ctx)), 1));
					} else {
						outResult.get(staticInfo).setMatching(outResult.get(staticInfo).getMatching() + 1);
					}
				}
			}
		}
		return outResult.values();
	}
	
	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		Collection<SearchResultBean> items = new LinkedList<SearchResultBean>();
		items.addAll(searchInPage(ctx, SearchFilter.getInstance(ctx.getRequest())));
		items.addAll(searchInResource(ctx, SearchFilter.getInstance(ctx.getRequest())));
		ctx.getRequest().setAttribute("items", items);
		return null;
	}

	public static String performSearch(RequestService rs, HttpServletRequest request, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		SearchFilter.getInstance(ctx.getRequest()).update(request);
		return null;
	}

}
