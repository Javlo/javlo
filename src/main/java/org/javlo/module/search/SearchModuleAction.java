package org.javlo.module.search;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.title.Heading;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextFactory;
import org.javlo.data.taxonomy.TaxonomyService;
import org.javlo.data.taxonomy.TaxonomyServiceAgregation;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.SecurityHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.RequestService;
import org.javlo.utils.Cell;
import org.javlo.utils.XLSTools;
import org.javlo.ztatic.StaticInfo;

public class SearchModuleAction extends AbstractModuleAction {

	private static Logger logger = Logger.getLogger(SearchModuleAction.class.getName());

	@Override
	public String getActionGroupName() {
		return "search-module";
	}

	private static boolean isMatching(ComponentBean comp, SearchFilter filter) {

		if (StringHelper.isEmpty(filter.getTitle()) && StringHelper.isEmpty(filter.getGlobal()) && StringHelper.isEmpty(filter.getSmartquery()) && StringHelper.isEmpty(filter.getSmartqueryre()) && filter.getComponents().size() == 0) {
			return false;
		}
		if (filter.getComponents().size() > 0) {
			if (!filter.getComponents().contains(comp.getType())) {
				return false;
			}
		}
		if (!StringHelper.isEmpty(filter.getSmartquery())) {
			if (!StringHelper.matchStarPattern(comp.getValue().toLowerCase(), filter.getSmartquery().toLowerCase())) {
				return false;
			}
		}
		if (!StringHelper.isEmpty(filter.getSmartqueryre())) {
			try {
				if (!Pattern.matches(filter.getSmartqueryre(), StringHelper.removeCR(comp.getValue()))) {
					return false;
				}
			} catch (Exception e) {
				logger.warning(e.getMessage());
			}
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
			if (comp.getValue().toLowerCase().contains(filter.getGlobal().toLowerCase()) || filter.getGlobal().contains(comp.getId())) {
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
		ctx = ctx.getContextForAbsoluteURL();
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
		outResult.put(page, new SearchResultBean(ctx.getGlobalContext().getContextKey(), "page", page.getTitle(ctx), lg, url, authors, StringHelper.renderSortableDate(page.getModificationDate(ctx)), previewURL, 1));
	}

	public static Collection<SearchResultBean> searchInPage(ContentContext ctx, SearchFilter filter) throws Exception {
		Map<MenuElement, SearchResultBean> outResult = new HashMap<MenuElement, SearchResultBean>();
		for (MenuElement page : ctx.getCurrentPage().getRoot().getAllChildrenList()) {
			if (SecurityHelper.userAccessPage(ctx, ctx.getCurrentEditUser(), page)) {
				if (filter.getTaxonomy().size() == 0 || TaxonomyService.getInstance(ctx).isAllMatch(page, filter)) {
					if (filter.isOnlyTaxonomy()) {
						addPage(ctx, outResult, page, "p", page.getCreator());
					}
					for (ComponentBean comp : page.getContent()) {
						if (isMatching(comp, filter)) {
							if (outResult.get(page) == null) {
								addPage(ctx, outResult, page, comp.getLanguage(), comp.getAuthors());
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
		ctx = ctx.getContextForAbsoluteURL();
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
						outResult.put(staticInfo, new SearchResultBean(ctx.getGlobalContext().getContextKey(), "file", title, ctx.getContentLanguage(), url, staticInfo.getAuthors(ctx), StringHelper.renderSortableDate(staticInfo.getCreationDate(ctx)), previewURL, 1));
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
		Collection<GlobalContext> contexts;
		if (ctx.getGlobalContext().isMaster()) {
			contexts = GlobalContextFactory.getAllGlobalContext(ctx.getRequest().getSession().getServletContext());
		} else {
			contexts = new LinkedList<GlobalContext>();
			contexts.add(ctx.getGlobalContext());
		}
		Collection<SearchResultBean> items = new LinkedList<SearchResultBean>();
		SearchFilter searchFilter = SearchFilter.getInstance(ctx.getRequest());
		ContentContext localCtx = new ContentContext(ctx);
		for (GlobalContext globalContext : contexts) {
			localCtx.setForceGlobalContext(globalContext);
			if (StringHelper.isEmpty(searchFilter.getType()) || searchFilter.getType().equals("page")) {
				items.addAll(searchInPage(localCtx, searchFilter));
			}
			if (StringHelper.isEmpty(searchFilter.getType()) || searchFilter.getType().equals("file") && searchFilter.getComponents().size() == 0) {
				items.addAll(searchInResource(localCtx, searchFilter));
			}
		}
		if (items.size() > 0) {

			Map<String, String> params = new HashMap<String, String>();
			params.put("webaction", "downloadxlsx");
			ctx.getRequest().setAttribute("downloadUrl", URLHelper.createURL(ctx, params));
		}
		ctx.getRequest().setAttribute("items", items);
		TaxonomyServiceAgregation taxoService = ctx.getGlobalContext().getAllTaxonomy(ctx);
		if (taxoService.isActive()) {
			ctx.getRequest().setAttribute("taxoSelect", taxoService.getSelectHtml("taxonomy", "form-control chosen-select", searchFilter.getTaxonomy(), true));
		}
		ctx.getRequest().setAttribute("components", ComponentHelper.getCurrentContextComponentsList(ctx));
		return null;
	}

	public static String performSearch(RequestService rs, HttpServletRequest request, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		SearchFilter.getInstance(ctx.getRequest()).update(request);
		return null;
	}

	public static String performDownloadxlsx(RequestService rs, HttpServletRequest request, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		Collection<GlobalContext> contexts;
		if (ctx.getGlobalContext().isMaster()) {
			contexts = GlobalContextFactory.getAllGlobalContext(ctx.getRequest().getSession().getServletContext());
		} else {
			contexts = new LinkedList<GlobalContext>();
			contexts.add(ctx.getGlobalContext());
		}
		Collection<SearchResultBean> items = new LinkedList<SearchResultBean>();
		SearchFilter searchFilter = SearchFilter.getInstance(ctx.getRequest());
		ContentContext localCtx = new ContentContext(ctx);
		for (GlobalContext globalContext : contexts) {
			localCtx.setForceGlobalContext(globalContext);
			if (StringHelper.isEmpty(searchFilter.getType()) || searchFilter.getType().equals("page")) {
				items.addAll(searchInPage(localCtx, searchFilter));
			}
			if (StringHelper.isEmpty(searchFilter.getType()) || searchFilter.getType().equals("file") && searchFilter.getComponents().size() == 0) {
				items.addAll(searchInResource(localCtx, searchFilter));
			}
		}
		String[][] stringArray = new String[items.size() + 1][10];
		int p = 0;
		stringArray[0][p++] = "title";
		stringArray[0][p++] = "type";
		stringArray[0][p++] = "context";
		stringArray[0][p++] = "authors";
		stringArray[0][p++] = "date";
		stringArray[0][p++] = "url";
		stringArray[0][p++] = "preview url";
		stringArray[0][p++] = "language";
		stringArray[0][p++] = "matching";
		
		int i = 1;
		for (SearchResultBean item : items) {
			p = 0;
			stringArray[i][p++] = item.getTitle();
			stringArray[i][p++] = item.getType();
			stringArray[i][p++] = item.getContext();
			stringArray[i][p++] = item.getAuthors();
			stringArray[i][p++] = item.getDate();
			stringArray[i][p++] = item.getUrl();
			stringArray[i][p++] = item.getPreviewURL();
			stringArray[i][p++] = item.getLanguage();
			stringArray[i][p++] = ""+item.getMatching();
			i++;
		}

		Cell[][] cells = XLSTools.getCellArray(stringArray);
		ctx.getResponse().setContentType(ResourceHelper.getFileExtensionToMineType("xlsx"));
		String filename = "search_" + StringHelper.stringToFileName(searchFilter.getSmartquery() + searchFilter.getSmartqueryre()) + ".xlsx";
		ctx.getResponse().setHeader("Content-Disposition", "attachment; filename=\"" + filename); // fileName)
		ctx.setStopRendering(true);

		XLSTools.writeXLSX(cells, ctx.getResponse().getOutputStream());
		return null;
	}

	public static void main(String[] args) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("#heading");
		out.println("Mes&nbsp;articles 2");
		out.close();
		System.out.println(">>>>>>>>> SearchModuleAction.main : match = " + Pattern.matches(".*(articles).*", StringHelper.removeCR(new String(outStream.toByteArray())))); // TODO: remove debug trace
	}

}
