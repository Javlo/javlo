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
import org.javlo.data.taxonomy.TaxonomyService;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.SecurityHelper;
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
		if (StringHelper.isEmpty(filter.getTitle()) && StringHelper.isEmpty(filter.getGlobal())) {
			return false;
		}
		boolean outFilter = true;
		if (!StringHelper.isEmpty(filter.getTitle())) {
			if ((comp.getType().equals(Title.TYPE) || comp.getType().equals(Heading.TYPE)) && comp.getValue().toLowerCase().contains(filter.getTitle().toLowerCase())) {
				outFilter = true;
			} else {
				outFilter = false;
			}
		}
		if (outFilter && !StringHelper.isEmpty(filter.getGlobal())) {
			if (comp.getValue().toLowerCase().contains(filter.getGlobal().toLowerCase())) {
				outFilter = true;
			} else {
				outFilter = false;
			}
		}
		return outFilter;
	}

	private static boolean isMatching(ContentContext ctx, StaticInfo info, SearchFilter filter) {
		if (!StringHelper.isEmpty(filter.getTitle())) {
			if ((StringHelper.neverNull(info.getTitle(ctx)).toLowerCase().contains(filter.getTitle().toLowerCase()))) {
				return true;
			}
		}
		if (!StringHelper.isEmpty(filter.getGlobal()) && StringHelper.isEmpty(filter.getTitle())) {
			String text = (info.getTitle(ctx) + info.getDescription(ctx) + info.getFile().getName()).toLowerCase();
			if (text.contains(filter.getGlobal().toLowerCase())) {
				return true;
			}
		}
		return false;
	}
	
	protected static void addPage(ContentContext ctx, Map<MenuElement, SearchResultBean> outResult, MenuElement page, String lg, String authors) throws Exception {
		String url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), page);
		if (ctx.isEditPreview()) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("webaction", "edit.closepopup");
			params.put("url", url);
			url = URLHelper.createURL(ctx, params);
		}
		String previewURL = null;
		if (page.getImage(ctx) != null) {
			previewURL = URLHelper.createTransformURL(ctx, page.getImage(ctx).getResourceURL(ctx), "list");
		}
		outResult.put(page, new SearchResultBean("page", page.getTitle(ctx), lg , url, authors, StringHelper.renderSortableDate(page.getModificationDate()), previewURL, 1));
	}
	
	public static Collection<SearchResultBean> searchInPage(ContentContext ctx, SearchFilter filter) throws Exception {
		Map<MenuElement, SearchResultBean> outResult = new HashMap<MenuElement, SearchResultBean>();
		for (MenuElement page : ctx.getCurrentPage().getRoot().getAllChildrenList()) {
			if (SecurityHelper.userAccessPage(ctx, ctx.getCurrentEditUser(), page)) {				
				if (filter.getTaxonomy().size() == 0 || TaxonomyService.getInstance(ctx).isAllMatch(page, filter)) {
					if (filter.isOnlyTaxonomy()) {						
						addPage(ctx,outResult,page,"p",page.getCreator());
					}
					for (ComponentBean comp : page.getContent()) {
						if (isMatching(comp, filter)) {
							if (outResult.get(page) == null) {								
								addPage(ctx,outResult,page,comp.getLanguage(),comp.getAuthors());
							} else {
								outResult.get(page).setMatching(outResult.get(page).getMatching() + 1);
							}
						}
					}
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

						// String url = staticInfo.getURL(ctx);
						String url;
						if (!ctx.isEditPreview()) {
							url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE));
							url = URLHelper.addParam(url, "webaction", "search-module.search");
							url = URLHelper.addParam(url, "module", "file");
							url = URLHelper.addParam(url, "page", "meta");
							url = URLHelper.addParam(url, "file", URLHelper.encodePathForAttribute(file.getPath()));
							String folderFile = file.getParentFile().getAbsolutePath();
							folderFile = folderFile.replace(ctx.getGlobalContext().getDataFolder(), "");
							url = URLHelper.addParam(url, "path", folderFile);
						} else {

							String formAction = URLHelper.createURL(ctx);
							formAction = URLHelper.addParam(formAction, "webaction", "search-module.search");
							formAction = URLHelper.addParam(formAction, "title", filter.getTitle());
							formAction = URLHelper.addParam(formAction, "type", filter.getType());
							formAction = URLHelper.addParam(formAction, "global", filter.getGlobal());
							formAction = URLHelper.addParam(formAction, "module", "search");
							formAction = URLHelper.addParam(formAction, "previewEdit", "true");
							formAction = URLHelper.addParam(formAction, "webaction", "edit.editPreview");

							url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE));
							url = URLHelper.addParam(url, "webaction", "file.previewEdit");
							url = URLHelper.addParam(url, "module", "file");
							url = URLHelper.addParam(url, "formAction", formAction);
							url = URLHelper.addParam(url, "nobreadcrumbs", "true");
							url = URLHelper.addParam(url, "file", URLHelper.encodePathForAttribute(file.getPath()));
							url = URLHelper.addParam(url, "previewEdit", "true");
						}
						String previewURL = URLHelper.createTransformURL(ctx, staticInfo, "list");
						outResult.put(staticInfo, new SearchResultBean("file", title, ctx.getContentLanguage(), url, staticInfo.getAuthors(ctx), StringHelper.renderSortableDate(staticInfo.getCreationDate(ctx)), previewURL, 1));
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
		SearchFilter searchFilter = SearchFilter.getInstance(ctx.getRequest());
		if (StringHelper.isEmpty(searchFilter.getType()) || searchFilter.getType().equals("page")) {
			items.addAll(searchInPage(ctx, searchFilter));
		}
		if (StringHelper.isEmpty(searchFilter.getType()) || searchFilter.getType().equals("file")) {
			items.addAll(searchInResource(ctx, searchFilter));
		}
		ctx.getRequest().setAttribute("items", items);
		ctx.getRequest().setAttribute("taxoSelect", ctx.getGlobalContext().getAllTaxonomy(ctx).getSelectHtml("taxonomy", "form-control chosen-select", searchFilter.getTaxonomy()));
		return null;
	}

	public static String performSearch(RequestService rs, HttpServletRequest request, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		SearchFilter.getInstance(ctx.getRequest()).update(request);
		return null;
	}

}
