package org.javlo.component.multimedia;

import org.apache.commons.io.FilenameUtils;
import org.javlo.actions.IAction;
import org.javlo.bean.Link;
import org.javlo.component.core.*;
import org.javlo.component.image.IImageTitle;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.InfoBean;
import org.javlo.data.taxonomy.ITaxonomyContainer;
import org.javlo.data.taxonomy.TaxonomyService;
import org.javlo.helper.*;
import org.javlo.helper.filefilter.HTMLFileFilter;
import org.javlo.helper.filefilter.ImageFileFilter;
import org.javlo.helper.filefilter.SoundFileFilter;
import org.javlo.helper.filefilter.VideoOrURLFileFilter;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.file.FileAction;
import org.javlo.module.file.FileBean;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.service.resource.Resource;
import org.javlo.user.AdminUserSecurity;
import org.javlo.ztatic.IStaticContainer;
import org.javlo.ztatic.StaticInfo;
import org.javlo.ztatic.StaticInfoBean;

import java.io.*;
import java.text.ParseException;
import java.util.*;

/**
 * standard image component.
 * <h4>exposed variable :</h4>
 * <ul>
 * <li>inherited from {@link AbstractVisualComponent}</li>
 * <li>{@link String} title : the title.</li>
 * <li>{@link PaginationContext} pagination : pagination context.</li>
 * <li>{@link MultimediaResource} resources : list of resources to be displayed.
 * </li>
 * </ul>
 * 
 * @author pvandermaesen
 */
public class Multimedia extends AbstractPropertiesComponent implements IImageTitle, IStaticContainer, IAction, ITaxonomyContainer {
	
	public static final String ROOT_FOLDER = "root-folder";

	public static final String MAX_LIST_SIZE = "max-list-size";

	public static final String PAGE_SIZE = "page-size";

	private static final String VALUE_SEPARATOR = "%";

	public static final String TYPE = "multimedia";

	protected static final String STATIC_VIDEO_FOLDER = "videos";
	protected static final String STATIC_SOUND_FOLDER = "sounds";
	protected static final String STATIC_IMAGE_FOLDER = "images";

	protected static final String STATIC_EMBED_FOLDER = "embed";
	protected static final String IMAGE_FIRST = "image first";
	protected static final String IMAGE_AFTER_EXEPT_FIRST = "only first item with image first";

	protected static final String ORDER_BY_ACCESS = "order by access";
	protected static final String REVERSE_ORDER = "reverse order";
	protected static final String NAME_ORDER = "name order";
	protected static final String RANDOM_ORDER = "random order";

	public static final String ALL = "all";
	public static final String IMAGE = "image";
	public static final String SOUND = "sound";
	public static final String VIDEO = "video";
	public static final String EMBED = "embed";
	
	private static final String TAXONOMY = "taxonomy";

	private List<File> multimediaFiles = null;

	protected boolean acceptStaticInfo(ContentContext ctx, StaticInfo info) throws IOException {

		Collection<String> tags = getTags();
		if (tags.size() > 0) {
			tags = new HashSet<String>(tags);
			tags.retainAll(info.getTags(ctx));
			if (tags.size() == 0) {
				return false;
			}
		}

		Calendar currentDate = GregorianCalendar.getInstance();
		if (info.getDate(ctx) != null) {
			currentDate.setTime(info.getDate(ctx));
		}
		Calendar startDate = GregorianCalendar.getInstance();
		if (getStartDate() == null) {
			startDate = null;
		} else {
			startDate.setTime(getStartDate());
		}
		Calendar endDate = GregorianCalendar.getInstance();
		if (getEndDate() == null) {
			endDate = null;
		} else {
			endDate.setTime(getEndDate());
		}

		boolean afterAccept = true;
		if (startDate != null && !currentDate.after(startDate)) {
			afterAccept = false;
		}

		boolean beforeAccept = true;
		if (endDate != null && !currentDate.before(endDate)) {
			beforeAccept = false;
		}

		if (info.getDate(ctx) == null && (startDate != null || endDate != null)) {
			afterAccept = false;
			beforeAccept = false; // not necessary, just more "clean" :-)
		}

		if (info.getReadRoles(ctx).size() > 0) {
			if (ctx.getCurrentUser() == null) {
				return false;
			} else if (Collections.disjoint(info.getReadRoles(ctx), ctx.getCurrentUser().getRoles())) {
				return false;
			}
		}
		
		if (ctx.getGlobalContext().getAllTaxonomy(ctx).isActive()) {
			TaxonomyService taxonomyService = TaxonomyService.getInstance(ctx);
			if (getTaxonomy().size() > 0) {
				if (!taxonomyService.isMatch(this, new FileBean(ctx, info.getFile()))) {
					return false;
				}
			}
		}

		return (info.isShared(ctx) || !isDisplayOnlyShared()) && afterAccept && beforeAccept;
	}
	
	public Date getStartDate() {
		String date = getFieldValue("start-date");
		if (!StringHelper.isEmpty(date)) {
			try {
				return StringHelper.parseTime(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public Date getEndDate() {
		String date = getFieldValue("end-date");
		if (!StringHelper.isEmpty(date)) {
			try {
				return StringHelper.parseTime(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	protected boolean acceptResource(ContentContext ctx, MultimediaResource info) {

		Collection<String> tags = getTags();
		if (tags.size() > 0) {
			tags = new HashSet<String>(tags);
			tags.retainAll(info.getTags());
			if (tags.size() == 0) {
				return false;
			}
		}

		Calendar currentDate = GregorianCalendar.getInstance();
		if (info.getDate() != null) {
			currentDate.setTime(info.getDate());
		} else {
			return true;
		}

		Calendar startDate = null;
		if (getStartDate() == null) {
			startDate = null;
		} else {
			startDate = GregorianCalendar.getInstance();
			startDate.setTime(getStartDate());
		}
		Calendar endDate = null;
		if (getEndDate() == null) {
			endDate = null;
		} else {
			endDate = GregorianCalendar.getInstance();
			endDate.setTime(getEndDate());
		}

		boolean afterAccept = true;
		if (startDate != null && !currentDate.after(startDate)) {
			afterAccept = false;
		}

		boolean beforeAccept = true;
		if (endDate != null && !currentDate.before(endDate)) {
			beforeAccept = false;
		}

		if (info.getDate() == null && (startDate != null || endDate != null)) {
			afterAccept = false;
			beforeAccept = false; // not necessary, just more "clean" :-)
		}

		return afterAccept && beforeAccept;
	}

	protected boolean displayEmbed(ContentContext ctx) {
		return getStyle().equals(ALL) || getStyle().equals(EMBED);
	}

	protected boolean displayImage(ContentContext ctx) {
		return getStyle().equals(ALL) || getStyle().equals(IMAGE);
	}

	protected boolean displaySound(ContentContext ctx) {
		return getStyle().equals(ALL) || getStyle().equals(SOUND);
	}

	protected boolean displayVideo(ContentContext ctx) {
		return getStyle().equals(ALL) || getStyle().equals(VIDEO);
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

	protected String getImageFilter(ContentContext ctx) {
		return getConfig(ctx).getProperty("image-filter", "preview");
	}

	protected ContentContext getValidVideoCtx(ContentContext ctx, IVideo video) {
		if (video.isRealContent(ctx)) {
			return ctx;
		}
		ContentContext lgCtx = new ContentContext(ctx);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Collection<String> languages = globalContext.getContentLanguages();
		for (String lg : languages) {
			lgCtx.setAllLanguage(lg);
			if (video.isRealContent(lgCtx)) {
				return lgCtx;
			}
		}
		return ctx;
	}

	private boolean contentVideoOnlyShared(ContentContext ctx) {
		return StringHelper.isTrue(getConfig(ctx).getProperty("only-shared", null));
	}

	protected MultimediaResource createResource(ContentContext ctx, IVideo video) {
		MultimediaResource resource = new MultimediaResource();
		ContentContext lgCtx = getValidVideoCtx(ctx, video);
		resource.setURL(video.getURL(lgCtx));
		resource.setAbsoluteURL(video.getURL(lgCtx.getContextForAbsoluteURL()));
		resource.setDescription(video.getImageDescription(lgCtx));
		resource.setAbsolutePreviewURL(video.getPreviewURL(ctx.getContextForAbsoluteURL(), getImageFilter(lgCtx)));
		resource.setPreviewURL(video.getPreviewURL(ctx, getImageFilter(lgCtx)));
		resource.setDate(video.getDate(lgCtx));
		resource.renderDate(lgCtx);
		resource.setLocation(video.getLocation(lgCtx));
		resource.setCssClass(video.getCssClass(lgCtx));
		resource.setTitle(video.getTitle(lgCtx));
		resource.setTags(video.getTags(lgCtx));
		resource.setIndex(video.getPopularity(lgCtx));
		resource.setLanguage(lgCtx.getRequestContentLanguage());
		resource.setId(video.getId());
		return resource;
	}

	protected List<MultimediaResource> getContentVideo(ContentContext ctx) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		List<MultimediaResource> outResources = new LinkedList<MultimediaResource>();
		ContentContext freeCtx = ctx.getContextWithArea(null);
		freeCtx.setFree(true);
		MenuElement page = content.getNavigation(freeCtx);
		ContentContext lgCtx = freeCtx.getContextWithContent(page);
		if (lgCtx == null) {
			lgCtx = freeCtx;
		}
		ContentElementList comps = page.getAllContent(lgCtx);
		while (comps.hasNext(lgCtx)) {
			IContentVisualComponent comp = comps.next(lgCtx);
			if (comp instanceof IVideo) {
				IVideo video = (IVideo) comp;
				if (video.isShared(lgCtx) || !contentVideoOnlyShared(lgCtx)) {
					MultimediaResource resource = createResource(ctx, video);
					outResources.add(resource);
				}
			}
		}
		for (MenuElement child : page.getAllChildrenList()) {
			lgCtx = freeCtx.getContextWithContent(child);
			if (lgCtx == null) {
				lgCtx = freeCtx;
			}
			comps = child.getAllContent(lgCtx);
			while (comps.hasNext(lgCtx)) {
				IContentVisualComponent comp = comps.next(lgCtx);
				if (comp instanceof IVideo) {
					IVideo video = (IVideo) comp;
					if (video.isShared(lgCtx) || !contentVideoOnlyShared(lgCtx)) {
						MultimediaResource resource = createResource(ctx, video);
						outResources.add(resource);
					}
				}
			}
		}

		return outResources;
	}

	public Collection<File> getAllMultimediaFiles(ContentContext ctx) {

		if (ctx.isAsViewMode() && multimediaFiles != null) {
			return multimediaFiles;
		}

		List<File> files = new LinkedList<File>();
		/* Collection<String> filesName = new HashSet<String>(); */

		/** Images **/
		File imageDir = null;
		if (displayImage(ctx)) {
			File newImageDir = new File(getFilesDirectory(ctx));
			if (imageDir == null || !newImageDir.equals(imageDir)) {
				imageDir = newImageDir;
				if (imageDir.exists()) {
					Collection<File> filesLg = ResourceHelper.getAllFiles(imageDir, new ImageFileFilter());
					for (File file : filesLg) {
						/*
						 * if (!filesName.contains(file.getName())) { filesName.add(file.getName());
						 */
						files.add(file);
						/* } */
					}
				}
			}
		}

		/** Videos **/
		if (displayVideo(ctx)) {
			File videoDir = new File(getFilesDirectory(ctx));
			if (videoDir.exists()) {
				Collection<File> filesLg = ResourceHelper.getAllFiles(videoDir, new VideoOrURLFileFilter());
				for (File file : filesLg) {
					/*
					 * if (!filesName.contains(file.getName())) { filesName.add(file.getName());
					 */
					files.add(file);
					/* } */
				}
			}
		}

		/** Sounds **/
		if (displaySound(ctx)) {
			File soundDir = new File(getFilesDirectory(ctx));
			if (soundDir.exists()) {
				Collection<File> filesLg = ResourceHelper.getAllFiles(soundDir, new SoundFileFilter());
				for (File file : filesLg) {
					/*
					 * if (!filesName.contains(file.getName())) { filesName.add(file.getName());
					 */
					files.add(file);
					/* } */
				}
			}
		}

		/** Embed **/
		if (displayEmbed(ctx)) {
			File embedDir = new File(getFilesDirectory(ctx));
			if (embedDir.exists()) {
				Collection<File> filesLg = ResourceHelper.getAllFiles(embedDir, new HTMLFileFilter());
				for (File file : filesLg) {
					/*
					 * if (!filesName.contains(file.getName())) { filesName.add(file.getName());
					 */
					files.add(file);
					/* } */
				}
			}
		}

		/*
		 * if (isOrderByAccess(ctx)) { Collections.sort(files, new
		 * StaticInfo.StaticFileSortByPopularity(ctx, false)); } else {
		 * Collections.sort(files, new StaticInfo.StaticFileSort(ctx, false)); }
		 */

		if (ctx.isAsViewMode()) {
			multimediaFiles = files;
		}

		return files;
	}

	public String getBaseStaticDir(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String folder = URLHelper.mergePath(globalContext.getDataFolder(), getRelativeFileDirectory(ctx));
		folder = folder.replace('\\', '/');
		return folder;
	}

	public String getCurrentRootFolder() {
		return getFieldValue(ROOT_FOLDER);
	}
	
	public void setCurrentRootFolder(ContentContext ctx, String folder) {
		setFieldValue(ROOT_FOLDER, folder);
		File targetFolder = new File(getFilesDirectory(ctx));
		if (!targetFolder.exists()) {
			targetFolder.mkdirs();
			logger.info("create folder  : " + targetFolder);
		}
	}
	
	protected boolean isSessionTaxonomy(ContentContext ctx) {
		return StringHelper.isTrue(getFieldValue("session-taxonomy"), false);
	}

	@Override
	protected String getDisplayAsInputName() {
		return "display-as-" + getId();
	}

	protected List<String> getSelection(ContentContext ctx) {
		String baseDir = getBaseStaticDir(ctx);
		File rootDir = new File(baseDir);
		Collection<File> files = ResourceHelper.getAllFiles(rootDir, null);
		List<String> folderSelection = new LinkedList<String>();
		folderSelection.add("/");
		for (File file : files) {
			if (file.isDirectory()/* && file.list().length > 0 */) {
				folderSelection.add(file.getAbsolutePath().replace('\\', '/').replaceFirst(baseDir, ""));
			}
		}
		return folderSelection;
	}

	protected boolean isDateRange() {
		return true;
	}

	protected boolean isOrder() {
		return true;
	}

	protected boolean isManualOrder() {
		return true;
	}

	protected boolean isTag() {
		return true;
	}

	protected String getCurrentRootFolderForBrowse() {
		return getCurrentRootFolder();
	}

	protected boolean isSelectBrowse() {
		return false;
	}

	protected String getEditPreview(ContentContext ctx) throws Exception {
		//if (!isManualOrder()) {
			List<MultimediaResource> medias = getMultimediaResources(ctx);
			if (medias.size() == 0) {
				return "";
			} else {
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				PrintStream out = new PrintStream(outStream);
				out.println("<div class=\"preview\">");
				out.println("<img src=\"" + medias.get(0).getPreviewURL() + "\" />");
				out.println("</div>");
				out.close();
				return new String(outStream.toByteArray());
			}
//		} else {
//			return null;
//		}
	}
	
	private String getTaxonomiesInputName() {
		return "taxonomie-" + getId();
	}
	
	private String getInputStartDateName() {
		return getInputName("start-date");
	}
	
	private String getInputEndDateName() {
		return getInputName("end-date");
	}
	
	public String getInputTag(String tag) {
		return "__" + getId() + ID_SEPARATOR + tag;
	}
	
	private void setTaxonomy(Collection<String> taxonomy) {
		setFieldValue(TAXONOMY, StringHelper.collectionToString(taxonomy));
	}
	
	public Set<String> getTaxonomy() {
		return StringHelper.stringToSet(getFieldValue(TAXONOMY));
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println(getSpecialInputTag());

		String preview = getEditPreview(ctx);
		boolean displayPreview = !StringHelper.isEmpty(preview);

		if (displayPreview) {
			out.println("<div class=\"row\"><div class=\"col-md-2\">" + preview + "</div><div class=\"col-md-10\">");
		}

		List<String> folderSelection = getSelection(ctx);
		if (StringHelper.isEmpty(getSpecialTagTitle(ctx))) {
			out.println("<div class=\"form-group form-inline\">");
			out.println("<label>" + i18nAccess.getText("global.title"));
			out.println(" : <input class=\"form-control title\" type=\"text\" id=\"" + getInputTitle() + "\" name=\"" + getInputTitle() + "\" value=\"" + getTitle() + "\"/></label>");
			out.println("</div>");
		}
		
		Map<String, String> params = new HashMap<String, String>();
		params.put(IContentVisualComponent.COMP_ID_REQUEST_PARAM, getId());
		params.put("webaction", getType() + ".orderhtml");
		String loadOrder = "jQuery('.order input').prop('checked', false); jQuery(this).parent().css('background-image','url("+InfoBean.getCurrentInfoBean(ctx).getViewAjaxLoaderURL()+")'); jQuery(this).remove(); ajaxRequest('" + URLHelper.createURL(ctx, params) + "');";
		String changeOrderButton = "<button onclick=\"" + loadOrder + "; return false;\" class=\"btn btn-standard btn-manual-order\">" + i18nAccess.getText("global.manual-order") + "</button>";

		out.println("<div class=\"form-group form-inline\">");
		if (isFolder()) {
			RequestService requestService = RequestService.getInstance(ctx.getRequest());
			String folder = getCurrentRootFolder();
			String newFolder = URLHelper.removeStaticFolderPrefix(ctx, requestService.getParameter("path", ""));
			if (newFolder.trim().length() > 1) {
				folder = newFolder;
			}
			params = new HashMap<String, String>();
			params.put(IContentVisualComponent.COMP_ID_REQUEST_PARAM, getId());
			params.put("webaction", getType() + ".changegallery");
			String chgGalUrl = URLHelper.createAjaxURL(ctx, params)+"&folder=";
			String js = "ajaxRequest('"+chgGalUrl+"'+jQuery(this).find('option:selected').val())";
			out.println(XHTMLHelper.getInputOneSelect(getInputBaseFolderName(), folderSelection, folder, "form-control select-galleries", js, true));
		}

		String backURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "content");
		backURL = URLHelper.addParam(backURL, "comp_id", "cp_" + getId());
		backURL = URLHelper.addParam(backURL, "webaction", "editPreview");
		backURL = URLHelper.addParam(backURL, ContentContext.PREVIEW_EDIT_PARAM, "true");

		Map<String, String> filesParams = new HashMap<String, String>();
		filesParams.put("path", URLHelper.mergePath(FileAction.getPathPrefix(ctx), getCurrentRootFolderForBrowse()));
		filesParams.put("webaction", "changeRenderer");
		filesParams.put("page", "meta");
		filesParams.put(ElementaryURLHelper.BACK_PARAM_NAME, backURL);
		if (isSelectBrowse()) {
			backURL = filesParams.put("select", "back");
		}

		String staticURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams);
		out.println("<a class=\"" + EDIT_ACTION_CSS_CLASS + " btn btn-default btn-xs\" href=\"" + staticURL + "\">");
		out.println(i18nAccess.getText("content.goto-static"));
		out.println("</a>");
		out.println("</div>");
		
		if (globalContext.getAllTaxonomy(ctx).isActive()) {
			String taxoName = getTaxonomiesInputName();
			out.println("<fieldset class=\"taxonomy\"><legend><label for=\"" + taxoName + "\">" + i18nAccess.getText("taxonomy") + "</label></legend>");
			out.println("<div class=\"line reverse\">");
			out.println(XHTMLHelper.getCheckbox(getInputName("taxosession"), isSessionTaxonomy(ctx)));
			out.println("<label for=\"" + getInputName("taxosession") + "\">" + i18nAccess.getText("content.page-teaser.session-taxonomy", "session taxonomy") + "</label></div>");
			out.println(globalContext.getAllTaxonomy(ctx).getSelectHtml(taxoName, "form-control chosen-select", getTaxonomy(), true, ctx.getGlobalContext().getSpecialConfig().isTaxonomyUnderlineActive()));
			out.println("</fieldset>");
		}

		out.println("<div class=\"row\">");
		if (isDateRange()) {
			out.println("<div class=\"col-lg-4 form-inline\">");
			out.println("<div class=\"form-group\">");
			out.println("<label for=\"" + getInputStartDateName() + "\">" + i18nAccess.getText("content.multimedia-gallery.date-range") + "</label>");
			out.println(" : <input class=\"date form-control\" id=\"contentdate-"+getId()+"\" type=\"text\" id=\"" + getInputStartDateName() + "\" name=\"" + getInputStartDateName() + "\" value=\"" + StringHelper.renderDateWithDefaultValue(getStartDate(), "") + "\"/> - ");
			out.println("<input class=\"form-control date\" type=\"text\" id=\"" + getInputEndDateName() + "\" name=\"" + getInputEndDateName() + "\" value=\"" + StringHelper.renderDateWithDefaultValue(getEndDate(), "") + "\"/>");
			out.println("</div></div>");
		}
		out.println("<div class=\"col-xs-3 form-inline\">");

		out.println("<div class=\"form-group\">");
		out.println("<label>" + i18nAccess.getText("content.multimedia-gallery.list-size"));
		out.println(" : <input class=\"form-control number\" type=\"number\" id=\"" + getInputMaxListSizeName() + "\" min=\"0\" name=\"" + getInputMaxListSizeName() + "\" value=\"" + getMaxListSize() + "\"/></label>");
		out.println("</div></div><div class=\"col-xs-3 form-inline\">");

		out.println("<div class=\"form-group\">");
		out.println("<label>" + i18nAccess.getText("content.multimedia-gallery.page-size"));
		out.println(" : <input class=\"form-control number\" type=\"number\" id=\"" + getInputPageSizeName() + "\"  min=\"0\" name=\"" + getInputPageSizeName() + "\" value=\"" + getPageSize() + "\"/></label>");
		out.println("</div></div></div>");

		if (isOrder()) {
			out.println("<fieldset class=\"order\">");
			out.println("<legend>" + i18nAccess.getText("global.order") + "</legend>");

			out.println("<div class=\"checkbox-inline\">");
			out.print("<label><input type=\"checkbox\" name=\"" + getInputNameReverseOrder() + "\" id=\"" + getInputNameReverseOrder() + "\" ");
			if (isReverseOrder(ctx)) {
				out.print("checked=\"checked\" ");
			}
			out.print("/>");
			out.println("reverse order.</label>");
			out.println("</div>");

			out.println("<div class=\"checkbox-inline\">");
			out.print("<label><input type=\"checkbox\" name=\"" + getInputNameNameOrder() + "\" id=\"" + getInputNameNameOrder() + "\" ");
			if (isNameOrder(ctx)) {
				out.print("checked=\"checked\" ");
			}
			out.print("/>");
			out.println(" order by name.</label>");
			out.println("</div>");

			out.println("<div class=\"checkbox-inline\">");
			out.print("<label><input type=\"checkbox\" name=\"" + getInputNameOrderByAccess() + "\" id=\"" + getInputNameOrderByAccess() + "\" ");
			if (isOrderByAccess(ctx)) {
				out.print("checked=\"checked\" ");
			}
			out.print("/>");
			out.println(" order by access.</label>");
			out.println("</div>");

			out.println("<div class=\"checkbox-inline\">");
			out.print("<label><input type=\"checkbox\" name=\"" + getInputNameRandomOrder() + "\" id=\"" + getInputNameRandomOrder() + "\" ");
			if (isOrderRandom(ctx)) {
				out.print("checked=\"checked\" ");
			}
			out.print("/>");
			out.println(" random.</label>");
			out.println("</div></fieldset>");
		}

		if (isManualOrder() && getAllResources(ctx).size()<255) {
			out.println("<div class=\"manual-order-area\" id=\"manual-order-" + getId() + "\">");
			out.println(changeOrderButton);
			out.println("</div>");
			// out.println(getManualOrderXhtml(ctx));
		}

		if (isTag()) {
			/* tags */
			Collection<String> tags = globalContext.getTags();
			Collection<String> selectedTags = getTags();
			if (tags.size() > 0) {
				out.println("<fieldset class=\"tags\">");
				out.println("<legend>" + i18nAccess.getText("global.tags") + "</legend>");
				for (String tag : tags) {
					String checked = "";
					if (selectedTags.contains(tag)) {
						checked = " checked=\"checked\"";
					}
					out.println("<span class=\"checkbox-inline\"><label class=\"checkbox-inline\"><input type=\"checkbox\" id=\"" + getInputTag(tag) + "\" name=\"" + getInputTag(tag) + "\"" + checked + " /> " + tag + "</label></span>");
				}
				out.println("</fieldset>");
			}
		}
		if (displayPreview) {
			out.println("</div></div>");
		}
		out.close();

		return writer.toString();
	}

	private String getManualOrderXhtml(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
		PrintStream out = new PrintStream(outStream);
		out.println("<fieldset class=\"manual-order\">");
		out.println("<legend>" + i18nAccess.getText("global.manual-order", "manual order") + "</legend>");

		List<MultimediaResource> resources = getMultimediaResources(ctx);
		int pos = 1;
		Map<String, String> params = new HashMap<String, String>();
		params.put(IContentVisualComponent.COMP_ID_REQUEST_PARAM, getId());
		params.put("webaction", getType() + ".orderhtml");
		String ajaxURL = URLHelper.createAjaxURL(ctx, params);
		for (MultimediaResource media : resources) {
			out.println("<div class=\"order-item-wrapper\">");
			out.println("<div class=\"order-item\">");
			out.println("   <div class=\"name\">" + media.getName() + "</div>");
			out.println("   <div class=\"ordre-row\">");
			out.println("      <div class=\"order-preview\">");
			if (media.getPath() != null) {
				String imageURL = URLHelper.createTransformURL(ctx, new File(media.getPath()), "_order");
				out.println("         <img style=\"background-image: url('"+InfoBean.getCurrentInfoBean(ctx).getViewAjaxLoaderURL()+"');\" width=\"160\" height=\"160\" data-name=\""+media.getName()+"\" src=\"" + imageURL + "\" />");
			}
			out.println("      </div>");
			out.println("      <div class=\"commands\">");
			out.println("         <button data-url=\""+ajaxURL+"\" class=\"btn btn-standard btn-first\" data-name=\""+media.getName()+"\">" + i18nAccess.getText("global.first") + "</button>");
			out.println("         <button class=\"btn btn-standard btn-move\" data-name=\""+media.getName()+"\">" + i18nAccess.getText("global.move") + "</button>");
			out.println("      </div>");
			out.println("   </div>");
			out.println("</div>");			
			out.println("<a href=\""+ajaxURL+"\" class=\"order-drop\" data-pos=\"" + pos + "\"><i class=\"fa fa-angle-double-left\" aria-hidden=\"true\"></i>&nbsp;<i class=\"fa fa-angle-double-right\" aria-hidden=\"true\"></i></a>");
			out.println("</div>");
			pos++;
		}
		out.println("</fieldset>");
		out.println("<script>updateOrder();</script>");
		out.close();
		return new String(outStream.toByteArray());
	}

	public String getFilesDirectory(ContentContext ctx) {
		String fileDir = URLHelper.mergePath(getBaseStaticDir(ctx), getCurrentRootFolder());
		return fileDir;
	}

	protected String getGlobalCssClass() {
		return "multimedia";
	}

	@Override
	public String getHexColor() {
		return GRAPHIC_COLOR;
	}

	protected String getHTMLRelation(ContentContext ctx) {
		return "shadowbox" + getId();
	}

	protected String getImageFilePath(ContentContext ctx, String fileLink) {
		if (StringHelper.isImage(fileLink) || StringHelper.isVideo(fileLink)) {
			return fileLink;
		} else {
			return FilenameUtils.getBaseName(fileLink) + ".jpg";
		}
	}

	public String getInputBaseFolderName() {
		return "__" + getId() + ID_SEPARATOR + "base-folder";
	}

	public String getInputMaxListSizeName() {
		return "__" + getId() + ID_SEPARATOR + "list-size";
	}

	public String getInputPageSizeName() {
		return "__" + getId() + ID_SEPARATOR + PAGE_SIZE;
	}

	public String getInputTitle() {
		return "__" + getId() + ID_SEPARATOR + "title";
	}

	protected String getInputNameOrderByAccess() {
		return "order_by_access_" + getId();
	}

	protected String getInputNameRandomOrder() {
		return "order_random_" + getId();
	}

	protected String getInputNameReverseOrder() {
		return "reverse_order_" + getId();
	}

	protected String getInputNameNameOrder() {
		return getInputName("name_order");
	}

	protected String getItemCssClass() {
		return "page-link";
	}

	public int getMaxListSize() {
		String maxListSize = getFieldValue(MAX_LIST_SIZE);
		if (StringHelper.isEmpty(maxListSize)) {
			return 0;
		} else {
			return Integer.parseInt(maxListSize);
		}
	}

	public int getPageSize() {
		String pageSize = getFieldValue(PAGE_SIZE);
		if (StringHelper.isEmpty(pageSize)) {
			return 0;
		} else {
			return Integer.parseInt(pageSize);
		}
	}

	public List<String> getFileOrder() {
		return StringHelper.stringToCollection(getFieldValue("files-order"), ",");
	}

	public void setFileOrder(List<String> order) {
		setFieldValue("files-order", StringHelper.collectionToString(order, ","));
	}
	
	public List<String> getTags() {
		return StringHelper.stringToCollection(getFieldValue("tags"), ",");
	}

	public String getTitle() {
		return getFieldValue("title");
	}

	protected String getMultimediaFilePath(ContentContext ctx, String lg, File file) {
		return URLHelper.mergePath(getFilesDirectory(ctx), file.getName());
	}

	protected String getMultimediaFileURL(ContentContext ctx, File file) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String relativeFileName = file.getAbsolutePath().replace(globalContext.getDataFolder(), "");
		return relativeFileName;
	}

	protected String getMultimediaFileURL(ContentContext ctx, String lg, File file) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String relativeFileName = file.getAbsolutePath().replace(globalContext.getDataFolder(), "");
		return relativeFileName;
	}

	protected String getPreviewFilter(File file) {
		return "preview";
	}

	protected String getRelativeFileDirectory(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return staticConfig.getStaticFolder();
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		return getStyleList(ctx);
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { ALL, IMAGE, VIDEO, SOUND };
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		try {
			I18nAccess i18n = I18nAccess.getInstance(ctx.getRequest());
			return i18n.getText("content.date.style-title");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	protected String getTitle(ContentContext ctx) {
		return null;
	}

	protected String getTransformFilter(File file) {
		return "video";
	}

	@Override
	public String getType() {
		return TYPE;
	}

	protected MultimediaResource getFirstResource(ContentContext ctx) throws Exception {
		List<MultimediaResource> resources = getMultimediaResources(ctx);
		if (resources.size() == 0) {
			return null;
		} else {
			MultimediaResource resource = null;
			int i = 0;
			while (i < resources.size() && (resource == null || resource.getPath() == null)) {
				resource = resources.get(i);
				i++;
			}
			if (resource == null || resource.getPath() == null) {
				return null;
			}
			String fileName = ResourceHelper.removeDataFolderDir(ctx.getGlobalContext().getMainContextOrContext(), resource.getPath());
			resource.setURL(fileName);
			resource.setPreviewURL(fileName);
			return resource;
		}
	}

	protected List<String> getAllFileName(ContentContext ctx) throws Exception {
		List<String> outNames = new LinkedList<String>();
		for (MultimediaResource rsc : getMultimediaResources(ctx)) {
			outNames.add(rsc.getName());
		}
		return outNames;
	}

	protected List<MultimediaResource> getMultimediaResources(ContentContext ctx) throws Exception {
		//super.prepareView(ctx);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		Collection<File> mulFiles = getAllMultimediaFiles(ctx);

		List<MultimediaResource> allResource = new LinkedList<MultimediaResource>();
		Map<String, MultimediaResource> allURL = new HashMap<String, MultimediaResource>();

		boolean countAccess = isCountAccess(ctx);

		Date firstDate = new Date();
		for (File file : mulFiles) {
			String cssClass = "embed";
			if (StringHelper.isVideo(file.getName())) {
				cssClass = "video";
			} else if (StringHelper.isSound(file.getName())) {
				cssClass = "sound";
			} else if (StringHelper.isImage(file.getName())) {
				cssClass = "image";
			}

			MultimediaResource resource = new MultimediaResource();
			resource.setCssClass(cssClass);

			String currentLg = ctx.getRequestContentLanguage();
			if (ctx.getGlobalContext().getContentLanguages().size() > 1) {
				File multimediaFile = new File(getMultimediaFilePath(ctx, currentLg, file));
				if (!(multimediaFile.exists())) {
					currentLg = globalContext.getDefaultLanguages().iterator().next();
				} else {
					file = multimediaFile;
				}
			}

			ContentContext lgCtx = new ContentContext(ctx);
			Iterator<String> defaultLg = globalContext.getDefaultLanguages().iterator();
			lgCtx.setRequestContentLanguage(lgCtx.getRequestContentLanguage());
			StaticInfo info = StaticInfo.getInstance(lgCtx, file);

			while (!info.isPertinent(lgCtx) && defaultLg.hasNext()) {
				String lg = defaultLg.next();
				lgCtx.setAllLanguage(lg);
			}
			if (!info.isPertinent(lgCtx)) {
				lgCtx = ctx;
			}

			if (acceptStaticInfo(ctx, info)) {

				String multimediaURL = URLHelper.createResourceURL(lgCtx, getPage(), getMultimediaFileURL(ctx, currentLg, file));
				String absoluteMultimediaURL = URLHelper.createResourceURL(lgCtx.getContextForAbsoluteURL(), getPage(), getMultimediaFileURL(ctx, currentLg, file));

				String previewURL = multimediaURL;
				String fileName = ResourceHelper.removeDataFolderDir(globalContext.getMainContextOrContext(), file.getAbsolutePath());
				if (StringHelper.isImage(file.getName()) || StringHelper.isVideo(file.getName())) {
					if (countAccess) {
						previewURL = URLHelper.createTransformURL(lgCtx, getPage(), getImageFilePath(ctx, fileName), getPreviewFilter(file));
					} else {
						previewURL = URLHelper.createTransformURLWithoutCountAccess(lgCtx, getImageFilePath(ctx, fileName), getPreviewFilter(file));
					}
				} else {
					previewURL = URLHelper.createTransformURL(lgCtx, getPage(), getImageFilePath(ctx, fileName), getTransformFilter(file));
				}

				String absolutePreviewURL = multimediaURL;
				ContentContext absCtx = lgCtx.getContextForAbsoluteURL();
				if (StringHelper.isImage(file.getName()) || StringHelper.isVideo(file.getName())) {
					if (countAccess) {
						absolutePreviewURL = URLHelper.createTransformURL(absCtx, getPage(), getImageFilePath(ctx, fileName), getPreviewFilter(file));
					} else {
						absolutePreviewURL = URLHelper.createTransformURLWithoutCountAccess(absCtx, getImageFilePath(ctx, fileName), getPreviewFilter(file));
					}
				} else {
					absolutePreviewURL = URLHelper.createTransformURL(absCtx, getPage(), getImageFilePath(ctx, fileName), getTransformFilter(file));
				}

				resource.setId(info.getId(lgCtx));
				resource.setName(file.getName());
				if (info.getReadRoles(absCtx).size() > 0) {
					resource.setAccessToken(info.getAccessToken(ctx));
					resource.setFreeAccess(false);
				} else {
					resource.setFreeAccess(true);
				}
				resource.setTitle(info.getTitle(lgCtx));
				resource.setParentTitle(info.getParent(lgCtx).getTitle(lgCtx));
				resource.setRelation(getHTMLRelation(lgCtx));
				resource.setLocation(info.getLocation(lgCtx));
				resource.setCopyright(info.getCopyright(lgCtx));
				resource.setDescription(info.getDescription(lgCtx));
				resource.setFullDescription(StringHelper.removeTag(info.getFullDescription(lgCtx)));
				resource.setDate(info.getDate(lgCtx));
				resource.renderDate(ctx);
				if (firstDate.getTime() > info.getDate(lgCtx).getTime()) {
					firstDate = info.getDate(lgCtx);
				}
				// resource.setShortDate(StringHelper.renderDate(resource.getDate(),
				// globalContext.getShortDateFormat()));
				// resource.setMediumDate(StringHelper.renderDate(resource.getDate(),
				// globalContext.getMediumDateFormat()));
				// resource.setFullDate(StringHelper.renderDate(resource.getDate(),
				// globalContext.getFullDateFormat()));
				resource.setURL(multimediaURL);
				resource.setAbsoluteURL(absoluteMultimediaURL);
				resource.setTags(info.getTags(lgCtx));
				resource.setLanguage(lgCtx.getRequestContentLanguage());
				resource.setIndex(info.getAccessFromSomeDays(lgCtx));
				resource.setPath(info.getFile().getAbsolutePath());
				resource.setStaticInfo(new StaticInfoBean(info.getContextWithContent(absCtx), info));
				if (info.getPosition(lgCtx) != null) {
					resource.setGpsPosition(info.getPosition(lgCtx).toString());
				}

				allURL.put(resource.getURL(), resource);

				resource.setPreviewURL(previewURL);
				resource.setAbsolutePreviewURL(absolutePreviewURL);

				if (isRenderInfo(ctx)) {
					if (!StringHelper.isEmpty(resource.getPreviewURL())) {
						allResource.add(resource);
					}
				}
			}
		}

		Calendar firstCal = Calendar.getInstance();
		firstCal.setTime(firstDate);
		ctx.getRequest().setAttribute("firstYear", firstCal.get(Calendar.YEAR));
		ctx.getRequest().setAttribute("lastYear", Calendar.getInstance().get(Calendar.YEAR));

		int countContentResource = 0;

		if (displayVideo(ctx)) {
			List<MultimediaResource> contentVideos = getContentVideo(ctx);
			for (MultimediaResource resource : contentVideos) {
				if (acceptResource(ctx, resource)) {
					if (allURL.get(resource.getURL()) != null) {
						allResource.remove(allURL.get(resource.getURL()));
					}
					allResource.add(resource);
					countContentResource++;
				}
			}
		}
		logger.fine("load content resource : " + countContentResource);

		MultimediaResourceFilter filter = MultimediaResourceFilter.getInstance(ctx);
		if (filter.isActive()) {
			for (Iterator<MultimediaResource> iterRsc = allResource.listIterator(); iterRsc.hasNext();) {
				MultimediaResource rsc = iterRsc.next();
				if (!filter.accept(rsc)) {
					iterRsc.remove();
				}
			}
		}
		final List<String> orderList = getFileOrder();
		boolean sort = false;
		if (isOrderByAccess(ctx)) {
			Collections.sort(allResource, new MultimediaResource.SortByIndex(true));
			sort = true;
		} else {
			if (isNameOrder(ctx)) {
				Collections.sort(allResource, new MultimediaResource.SortByName(isReverseOrder(ctx)));
				sort = true;
			} else if (isOrderRandom(ctx)) {
				Collections.shuffle(allResource);
				sort = true;
			} else if (orderList != null && orderList.size()==0){
				Collections.sort(allResource, new MultimediaResource.SortByDate(isReverseOrder(ctx)));
				sort = true;
			}
		}
		if (!sort) {
			if (orderList != null && orderList.size() > 0) {
				Collections.sort(allResource, new Comparator<MultimediaResource>() {
					@Override
					public int compare(MultimediaResource o1, MultimediaResource o2) {
						return orderList.indexOf(o1.getName()) - orderList.indexOf(o2.getName());
					}
				});
			}
		}
		return allResource;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		// LocalLogger.cronoStart();
		List<MultimediaResource> allResource = getMultimediaResources(ctx);
		// LocalLogger.cronoStep(getType());
		int max = Math.min(getMaxListSize(), allResource.size());
		// LocalLogger.cronoStep(getType());
		PaginationContext pagination = PaginationContext.getInstance(ctx.getRequest(), getId(), max, getPageSize());
		// LocalLogger.cronoStep(getType());
		ctx.getRequest().setAttribute("title", getTitle());
		ctx.getRequest().setAttribute("pagination", pagination);
		ctx.getRequest().setAttribute("resources", allResource.subList(0, max));
		// LocalLogger.cronoStep(getType());
	}

	@Override
	public int getWordCount(ContentContext ctx) {
		return 0;
	}

	protected boolean isImported(ContentContext ctx) {
		return getDirSelected(ctx).contains(ctx.getGlobalContext().getStaticConfig().getImportFolder() + '/');
	}

	@Override
	public void init(ComponentBean bean, ContentContext ctx) throws Exception {
		super.init(bean, ctx);
		/**
		 * convert old version
		 */
		if (getValue().contains(VALUE_SEPARATOR)) {
			String[] values = getValue().split(VALUE_SEPARATOR);
			if (values.length>3) {
				setCurrentRootFolder(ctx, values[3]);
				String maxListSizeStr = getValue().split(VALUE_SEPARATOR)[2];
				if (maxListSizeStr.contains(",")) {
					setFieldValue(MAX_LIST_SIZE, maxListSizeStr.split(",")[1]);
				} else {
					setFieldValue(MAX_LIST_SIZE, maxListSizeStr);
				}
				String pageSizeStr = getValue().split(VALUE_SEPARATOR)[2];
				if (pageSizeStr.contains(",")) {
					setFieldValue(PAGE_SIZE, pageSizeStr.split(",")[0]);
				}
				if (values.length > 6) {
					String title = values[6];
					int orderPos = title.indexOf("MANORD[[");
					if (orderPos>=0) {
						title = title.substring(0, orderPos);
					}
					setFieldValue("title", title);
				}
				Properties prop = new Properties();
				for(Object key : properties.keySet()) {
					if (!key.toString().contains(VALUE_SEPARATOR)) {
						prop.setProperty((String)key, (String)properties.get(key));
					}
				}
				properties = prop;
				if (getTitle().length()>512) {
					properties.remove("title");
				}
				storeProperties();
				
			}
		}
		
		if (isImported(ctx) && getPage() != null && ctx.isAsModifyMode()) {
			String importFolder = getImportFolderPath(ctx).replaceFirst("/" + ctx.getGlobalContext().getStaticConfig().getStaticFolder(), "");

			if (!getDirSelected(ctx).equals(importFolder)) {
				File sourceDir = new File(getFilesDirectory(ctx));
				if (sourceDir.exists() && sourceDir.isDirectory()) {
					try {
						setCurrentRootFolder(ctx, importFolder);
						File targetDir = new File(getFilesDirectory(ctx));
						logger.info("transfert imported file : " + sourceDir + " >>> " + targetDir);
						for (File file : sourceDir.listFiles())
							if (file.exists()) {
								File targetFile = new File(URLHelper.mergePath(targetDir.getAbsolutePath(), file.getName()));
								if (!targetFile.exists()) {
									ResourceHelper.writeFileToFile(file, targetFile);
									ResourceHelper.copyResourceData(ctx, file, targetFile);
									ResourceHelper.cleanImportResource(ctx, targetFile);
								}
							}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		if (StringHelper.isEmpty(getValue())) {
			String importFolder = getImportFolderPath(ctx).replaceFirst("/" + ctx.getGlobalContext().getStaticConfig().getStaticFolder(), "");
			setCurrentRootFolder(ctx, importFolder);
			properties.setProperty(PAGE_SIZE, "99");
			properties.setProperty(MAX_LIST_SIZE, "99");
		}
	}

	public String getImportFolderPath(ContentContext ctx) throws Exception {
		return URLHelper.mergePath("/", ctx.getGlobalContext().getStaticConfig().getGalleryFolder(), getImportFolderPath(ctx, getPage()));
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
//		if (isForceCachable()) {
//			return true;
//		}
		String contentCache = getConfig(ctx).getProperty("cache.content", null);
		if (contentCache != null) {
			return StringHelper.isTrue(contentCache);
		}
		if (isOrderRandom(ctx)) {
			return false;
		}
		return !isOrderByAccess(ctx);
	}

	@Override
	public boolean isContentTimeCachable(ContentContext ctx) {
		return isOrderByAccess(ctx);
	}

	@Override
	public boolean isContentCachableByQuery(ContentContext ctx) {
		return !isRepeat();
	}

	protected boolean isCountAccess(ContentContext ctx) {
		if (isOrderByAccess(ctx)) {
			return false;
		}
		return true;
	}

	protected boolean isDisplayOnlyShared() {
		return true;
	}
	
	boolean isFolder() {
		return true;
	}

	public boolean isOrderByAccess(ContentContext ctx) {
		return StringHelper.isTrue(getFieldValue(ORDER_BY_ACCESS));
	}

	public boolean isOrderRandom(ContentContext ctx) {
		return StringHelper.isTrue(getFieldValue(RANDOM_ORDER));
	}

	public boolean isReverseOrder(ContentContext ctx) {
		return StringHelper.isTrue(getFieldValue(REVERSE_ORDER));
	}

	public boolean isNameOrder(ContentContext ctx) {
		return StringHelper.isTrue(getFieldValue(NAME_ORDER));
	}

	protected boolean isRenderInfo(ContentContext ctx) {
		return true;
	}

	protected boolean isRenderLanguage() {
		return true;
	}

	@Override
	public boolean isUnique() {
		return false;
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		performColumnable(ctx);
		String folder = requestService.getParameter(getInputBaseFolderName(), null);
		String newStartDate = requestService.getParameter(getInputStartDateName(), null);
		String newEndDate = requestService.getParameter(getInputEndDateName(), null);
		String newPageSize = requestService.getParameter(getInputPageSizeName(), null);
		String newListSize = requestService.getParameter(getInputMaxListSizeName(), null);
		String newDisplayType = requestService.getParameter(getDisplayAsInputName(), null);
		String title = requestService.getParameter(getInputTitle(), null);

		if (title != null) {
			boolean isOrderByAcess = requestService.getParameter(getInputNameOrderByAccess(), null) != null;
			boolean isReverseOrder = requestService.getParameter(getInputNameReverseOrder(), null) != null;
			boolean isNameOrder = requestService.getParameter(getInputNameNameOrder(), null) != null;
			boolean isRandom = requestService.getParameter(getInputNameRandomOrder(), null) != null;
			Date startDate = StringHelper.parseDateOrTime(newStartDate);
			Date endDate = StringHelper.parseDateOrTime(newEndDate);
			if (newDisplayType == null) {
				newDisplayType = "";
			}
			/* tags */
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			List<String> selectedTags = new LinkedList<String>();
			for (String tag : globalContext.getTags()) {
				if (requestService.getParameter(getInputTag(tag), null) != null) {
					selectedTags.add(tag);
				}
			}
			
			setFieldValue("start-date", StringHelper.renderTime(startDate));
			setFieldValue("end-date", StringHelper.renderTime(endDate));
			setFieldValue(PAGE_SIZE, newPageSize);
			setFieldValue(MAX_LIST_SIZE, newListSize);
			
			// newListSizeDate ?
			setFieldValue(ROOT_FOLDER, folder);
			setFieldValue("tags", StringHelper.collectionToString(selectedTags, ","));
			setFieldValue("title", title);
			setFieldValue(NAME_ORDER, ""+isNameOrder);
			setFieldValue(ORDER_BY_ACCESS, ""+isOrderByAcess);
			setFieldValue(REVERSE_ORDER, ""+isReverseOrder);
			setFieldValue(RANDOM_ORDER, ""+isRandom);
			
			if (ctx.getGlobalContext().getAllTaxonomy(ctx).isActive()) {
				String[] taxonomy = requestService.getParameterValues(getTaxonomiesInputName(), null);
				if (taxonomy != null) {
					setTaxonomy(Arrays.asList(taxonomy));
				} else {
					setTaxonomy(Collections.EMPTY_SET);
				}
			}
			
//			String multimediaInfo = StringHelper.neverNull(StringHelper.renderTime(startDate_OK)) + VALUE_SEPARATOR + StringHelper.neverNull(StringHelper.renderTime(endDate_OK)) + VALUE_SEPARATOR + newPageSize_OK + ',' + newListSizeDate + VALUE_SEPARATOR + folder_OK + VALUE_SEPARATOR + newDisplayType + VALUE_SEPARATOR + StringHelper.collectionToString(selectedTags) + VALUE_SEPARATOR + title;
//			if (isNameOrder) {
//				multimediaInfo = multimediaInfo + VALUE_SEPARATOR + NAME_ORDER;
//			}
//			if (isOrderByAcess && !isNameOrder) {
//				multimediaInfo = multimediaInfo + VALUE_SEPARATOR + ORDER_BY_ACCESS;
//			}
//			if (isReverseOrder) {
//				multimediaInfo = multimediaInfo + VALUE_SEPARATOR + REVERSE_ORDER;
//			}
//			if (isRandom)  
//				multimediaInfo = multimediaInfo + VALUE_SEPARATOR + RANDOM_ORDER;
//			}
//			multimediaInfo = setFileOrder(getFileOrder(), multimediaInfo);
			
			storeProperties();
		}
		
		return null;
	}
	
	public static String performChangegallery(ContentContext ctx, RequestService rs) throws Exception {
		Multimedia comp = (Multimedia) ComponentHelper.getComponentFromRequest(ctx);
		if (comp != null) {
			String folder = rs.getParameter("folder");
			if (!StringHelper.isEmpty(folder)) {
				comp.setCurrentRootFolder(ctx, folder);
				ctx.getAjaxInsideZone().put("tab1-"+comp.getId(), comp.getEditXHTMLCode(ctx));
			}
		}
		return null;
	}

	public static String performOrderhtml(ContentContext ctx, RequestService rs) throws Exception {
		Multimedia comp = (Multimedia) ComponentHelper.getComponentFromRequest(ctx);
		if (comp != null) {
			String orderFile = rs.getParameter("file");
			String orderPosition = rs.getParameter("position");
			List<String> orderFiles = comp.getFileOrder();
			List<String> allFiles = comp.getAllFileName(ctx);
			if (orderFile != null) {
				allFiles.remove(orderFile);
				String value = comp.getValue(ctx);
				value = value.replace(ORDER_BY_ACCESS,"");
				value = value.replace(RANDOM_ORDER,"");
				value = value.replace(REVERSE_ORDER,"");
				value = value.replace(NAME_ORDER,"");
				if (!value.equals(comp.getValue())) {
					comp.setValue(value);
				}
			}
			// synchronise sort list and list
			for (Iterator iterator = orderFiles.iterator(); iterator.hasNext();) {
				String item = (String) iterator.next();
				if (!allFiles.contains(item) || item.equals(orderFile)) {
					iterator.remove();
				} else {
					allFiles.remove(item);
				}
			}
			orderFiles.addAll(allFiles);
			if (orderFile != null && StringHelper.isDigit(orderPosition)) {
				int pos = Integer.parseInt(orderPosition);
				if (pos<=orderFiles.size()) {
					orderFiles.add(pos, orderFile);
				} else {
					orderFiles.add(orderFile);
				}
			}
			comp.setFileOrder(orderFiles);
			ctx.getAjaxInsideZone().put("manual-order-" + comp.getId(), comp.getManualOrderXhtml(ctx));			
		}
		return null;
	}

	@Override
	public String getImageDescription(ContentContext ctx) {
		try {
			return getFirstResource(ctx).getDescription();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public String getResourceURL(ContentContext ctx) {
		try {
			return getFirstResource(ctx).getPreviewURL();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public String getImageLinkURL(ContentContext ctx) {
		try {
			return getFirstResource(ctx).getURL();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public boolean isImageValid(ContentContext ctx) {
		try {
			boolean out  = getFirstResource(ctx) != null;
			return out;			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	protected Object getLock(ContentContext ctx) {
		return ctx.getGlobalContext().getLockLoadContent();
	}

	@Override
	public int getPriority(ContentContext ctx) {
		if (getConfig(ctx).getProperty("image.priority", null) == null) {
			return 4;
		} else {
			return Integer.parseInt(getConfig(ctx).getProperty("image.priority", null));
		}
	}

	@Override
	public String getEmptyXHTMLCode(ContentContext ctx) throws Exception {
		if (isHiddenInMode(ctx, ctx.getRenderMode(), ctx.isMobile()) || !AdminUserSecurity.getInstance().canModifyConponent(ctx, getId())) {
			return "";
		} else {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String prefix = "";
			String suffix = "";
			if (!isWrapped(ctx)) {
				prefix = getForcedPrefixViewXHTMLCode(ctx);
				suffix = getForcedSuffixViewXHTMLCode(ctx);
			}
			if (getCurrentRootFolder() != null && getCurrentRootFolder().length() > 2) {
				return prefix + "<div class=\"empty\">[" + i18nAccess.getText("preview.upload-here", "upload here") + "]</div>" + suffix;
			} else {
				return prefix + "<div class=\"empty\" ondrop=\"return false;\">[" + i18nAccess.getText("preview.choose-folder", "choose a folder to upload") + "]</div>" + suffix;
			}
		}
	}

	@Override
	public boolean isMirroredByDefault(ContentContext ctx) {
		return true;
	}

	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		boolean out = super.initContent(ctx);
		setStyle(ctx, IMAGE);
		return out;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}

	@Override
	public boolean contains(ContentContext ctx, String uri) {
		return uri.startsWith(getCurrentRootFolder());
	}

	@Override
	public Collection<Resource> getAllResources(ContentContext ctx) {
		Collection<Resource> outResources = new LinkedList<Resource>();
		try {
			for (MultimediaResource mulRes : getMultimediaResources(ctx)) {
				Resource res = new Resource();
				res.setName(mulRes.getName());
				if (mulRes.getPath() != null) {
					String url = mulRes.getPath().replace(ctx.getGlobalContext().getDataFolder(), "");
					res.setUri(url);
				}
				res.setDescription(mulRes.getTitle());
				res.setId(mulRes.getPath());
				outResources.add(res);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outResources;
	}

	@Override
	public boolean renameResource(ContentContext ctx, File oldName, File newName) {
		return false;
	}

	@Override
	public Collection<Link> getAllResourcesLinks(ContentContext ctx) {
		return null;
	}

	@Override
	public int getPopularity(ContentContext ctx) {
		return 0;
	}

	@Override
	public void setDirSelected(String dir) {
	}

	@Override
	public String getDirSelected(ContentContext ctx) {
		return getCurrentRootFolder();
	}

	@Override
	public List<File> getFiles(ContentContext ctx) {
		return null;
	}

	/*@Override
	public String getFontAwesome() {
		return "th-large";
	}*/
	
	@Override
	public String getIcon() {
		return "bi bi-images";
	}

	@Override
	public String getSpecialTagTitle(ContentContext ctx) throws Exception {
		if (ctx.getGlobalContext().isMailingPlatform() || (getCurrentRenderer(ctx) != null && getCurrentRenderer(ctx).contains("text"))) {
			return "text";
		} else {
			return super.getSpecialTagTitle(ctx);
		}
	}

	protected String getLabelTextInputName() {
		return getId() + ID_SEPARATOR + "label-text";
	}

	@Override
	public String getSpecialTagXHTML(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		Map<String, String> filesParams = new HashMap<String, String>();
		String path = FileAction.getPathPrefix(ctx);
		filesParams.put("path", path);
		filesParams.put("webaction", "changeRenderer");
		filesParams.put("page", "meta");
		filesParams.put("select", "_TYPE_");
		filesParams.put(ContentContext.PREVIEW_EDIT_PARAM, "true");
		String chooseImageURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams);

		out.println("<div class=\"text\">");

		out.println("<div class=\"line label-text\"><label for=\"" + getInputTitle() + "\">label text : </label>");
		String id = "special-label-" + getId();
		String rows = "3";

		String[][] paramsLabelText = new String[][] { { "rows", rows }, { "cols", "100" }, { "class", "tinymce-light" }, { "id", id } };
		out.println(XHTMLHelper.getTextArea(getInputTitle(), getTitle(), paramsLabelText));
		out.println("<script type=\"text/javascript\">jQuery(document).ready(loadWysiwyg('#" + id + "','" + getEditorComplexity(ctx) + "','" + chooseImageURL + "'));</script>");
		out.println("</div>");

		out.println("</div>");

		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	protected boolean getColumnableDefaultValue() {
		return true;
	}

	@Override
	public String getActionGroupName() {
		return getType();
	}
	
	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return Collections.EMPTY_LIST;
	}
	
	@Override
	public boolean isMobileOnly(ContentContext ctx) {
		return false;
	}

}
