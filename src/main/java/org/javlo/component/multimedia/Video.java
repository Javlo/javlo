/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.multimedia;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.actions.IAction;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.IVideo;
import org.javlo.component.image.GlobalImage;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.service.resource.Resource;

/**
 * @author pvandermaesen
 */
public class Video extends GlobalImage implements IAction, IVideo {

	public static class OrderVideo implements Comparator<Video> {

		@Override
		public int compare(Video vid1, Video vid2) {
			try {
				return -vid1.getDate().compareTo(vid2.getDate());
			} catch (ParseException e) {
				return 1;
			}
		}

	}

	@Override
	protected FilenameFilter getFileFilter() {
		return new ResourceHelper.VideoFilenameFilter();
	}

	@Override
	protected FilenameFilter getDecorationFilter() {
		return new ResourceHelper.ImageFilenameFilter();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2927642664064193844L;

	public static final String TYPE = "linked-video";

	private static final String LINK = "link";

	@Override
	public Collection<Resource> getAllResources(ContentContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { LINK, "inline" };
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		return getStyleList(ctx);
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		return "";
	}

	protected boolean isInline(ContentContext ctx) {
		return StringHelper.neverNull(getStyle(ctx)).equalsIgnoreCase("inline");
	}

	@Override
	public boolean renameResource(ContentContext ctx, File oldName, File newName) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getFileDirectory(ContentContext ctx) {
		String folder;
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		folder = URLHelper.mergePath(globalContext.getDataFolder(), staticConfig.getVideoFolder());
		return folder;
	}

	@Override
	public String createFileURL(ContentContext ctx, String url) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return URLHelper.createResourceURL(ctx, getPage(), staticConfig.getVideoFolder() + '/' + url);
	}

	@Override
	protected boolean isImageFilter() {
		return false;
	}

	@Override
	protected String getImageUploadTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("action.add-video.add");
	}

	/*
	 * @Override protected String getPreviewCode(ContentContext ctx) throws Exception { GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest()); I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, ctx.getRequest().getSession()); return renderInline(ctx, "300", "235", true) + "<div class=\"preview-info\">" + i18nAccess.getText("content.video.latest-access") + " : " + getAccess(ctx, 30) + "</div>"; }
	 */

	@Override
	protected String getPreviewCode(ContentContext ctx) throws Exception {
		return "<a href=\"" + getURL(ctx) + "\"><img src=\"" + getPreviewURL(ctx, "thumbnails") + "\" /></a>";
	}

	@Override
	protected String getRelativeFileDirectory(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return staticConfig.getVideoFolder();
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		if (getEmbedCode() != null && getEmbedCode().trim().length() == 0) {
			return super.isContentCachable(ctx);
		} else {
			return false;
		}
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (getEmbedCode() != null && getEmbedCode().trim().length() > 0 && (!getStyle().equals(LINK) || ctx.isExport())) {
			return getEmbedCode();
		} else {
			return renderInline(ctx, null, null, false);
		}
	}

	@Override
	public Map<String, String> getRenderes(ContentContext ctx) {
		if (getStyle().equals(LINK)) {
			return super.getRenderes(ctx);
		} else {
			return Collections.EMPTY_MAP;
		}
	}

	@Override
	public String getRenderer(ContentContext ctx) {
		return null;
	}

	@Override
	protected String getImageURL(ContentContext ctx) throws Exception {
		if (getLink() != null && getLink().toLowerCase().contains("youtube")) {
			return getYoutubePreview(ctx, getConfig(ctx).getProperty("image.filter", getDefaultFilter()));
		} else {
			return super.getImageURL(ctx);
		}
	}

	@Override
	public String getResourceURL(ContentContext ctx, String fileLink) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		return URLHelper.mergePath(staticConfig.getVideoFolder(), URLHelper.mergePath(getDirSelected(), fileLink));
	}

	private String getYoutubePreview(ContentContext ctx, String filter) throws Exception {
		String videoCode = URLHelper.extractParameterFromURL(getLink()).get("v");

		if (videoCode != null) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
			String fileName = "yt_" + videoCode.toLowerCase() + ".jpg";
			File imageFile = new File(URLHelper.mergePath(globalContext.getDataFolder(), staticConfig.getImageFolder(), "youtube", fileName));
			if (!imageFile.getParentFile().exists()) {
				imageFile.getParentFile().mkdirs();
			}
			if (!imageFile.exists()) {
				imageFile.createNewFile();
				URL url = new URL("http://img.youtube.com/vi/" + videoCode + "/0.jpg");
				FileOutputStream out = new FileOutputStream(imageFile);
				NetHelper.readPage(url, out);
				ResourceHelper.closeResource(out);
			}
			return URLHelper.createTransformURL(ctx, URLHelper.mergePath(staticConfig.getImageFolder(), "youtube", fileName), getConfig(ctx).getProperty("image.filter", "preview"));
		}
		return null;
	}

	@Override
	public String getPreviewURL(ContentContext ctx) throws Exception {
		if (getDecorationImage() != null && getDecorationImage().trim().length() > 1) {
			String imageLink = getResourceURL(ctx, getDecorationImage());
			return URLHelper.createTransformURL(ctx, imageLink, getConfig(ctx).getProperty("image.filter", "preview"));
		} else if (getLink() != null && getLink().toLowerCase().contains("youtube")) {
			try {
				String previewYouTube = getYoutubePreview(ctx, null);
				return previewYouTube;
			} catch (Exception e) {
				logger.warning(e.getMessage());
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected Map<String, String> getTranslatableResources(ContentContext ctx) throws Exception {
		Collection<Video> videos = getAllVideoOnPage(ctx);
		Map<String, String> outResourceList = new HashMap<String, String>();
		for (Video video : videos) {
			if (video.getResourceLabel() != null) {
				outResourceList.put(video.getId(), video.getResourceLabel());
			}
		}
		return outResourceList;
	}

	public Collection<Video> getAllVideoOnPage(ContentContext ctx) throws Exception {
		List<Video> comps = new LinkedList<Video>();
		ContentContext noAreaCtx = new ContentContext(ctx);
		noAreaCtx.setArea(null);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		noAreaCtx.setRequestContentLanguage(globalContext.getDefaultLanguages().iterator().next());
		ContentElementList content = getCurrentPage(ctx, true).getContent(noAreaCtx);
		while (content.hasNext(noAreaCtx)) {
			IContentVisualComponent comp = content.next(noAreaCtx);
			if (comp instanceof Video) {
				comps.add((Video) comp);
			}
		}
		// Collections.sort(comps, new Video.OrderVideo());
		return comps;
	}

	private String getResourceLabel() {
		if (getFileName() != null && getFileName().trim().length() > 0) {
			return getFileName();
		} else if (getLink() != null && getLink().trim().length() > 0) {
			return getLink();
		}
		return null;
	}

	@Override
	protected String getDefaultFilter() {
		return "preview";
	}

	protected String getImageFilter(ContentContext ctx) {
		return getConfig(ctx).getProperty("image.filter", "preview");
	}

	@Override
	public String getURL(ContentContext ctx) {
		if (getEmbedCode().trim().length() > 0) {
			String url = "/expcomp/" + getId() + ".html";
			return URLHelper.createStaticURL(ctx, url);
		} else {
			if (getLink() != null && getLink().trim().length() > 0) {
				return getLink();
			} else {
				if (getFileName() != null && getFileName().trim().length() > 0) {
					String fileLink = getResourceURL(ctx, getFileName());
					return URLHelper.createResourceURL(ctx, getPage(), fileLink).replace('\\', '/');
				} else {
					return null;
				}
			}
		}
	}

	public String renderInline(ContentContext ctx, String width, String height, boolean preview) throws Exception {

		String link = getLink();
		String imageFilter = getImageFilter(ctx);
		if (preview) {
			imageFilter = "thumbnails";
		}

		String accessActionURL = URLHelper.createURL(ctx) + "?webaction=video.access&comp-id=" + getId();
		ctx.getRequest().setAttribute("accessURL", accessActionURL);
		ctx.getRequest().setAttribute("comp_id", getId());

		boolean renderAsLink = (getDecorationImage() != null && getDecorationImage().trim().length() > 0) && preview;
		if (!renderAsLink) {
			renderAsLink = !preview && LINK.equals(getStyle(ctx));
		}
		if (renderAsLink) {
			if (getFileName() != null && getFileName().trim().length() > 0) {
				String fileLink = getResourceURL(ctx, getFileName());
				ctx.getRequest().setAttribute("url", URLHelper.createResourceURL(ctx, getPage(), fileLink).replace('\\', '/'));
			} else {
				if (getLink() != null && getLink().trim().length() > 0) {
					ctx.getRequest().setAttribute("url", getLink());
				}
			}

			ctx.getRequest().setAttribute("type", ResourceHelper.getFileExtensionToManType(StringHelper.getFileExtension(getFileName())));
			ctx.getRequest().setAttribute("label", getLabel());
			if (getDecorationImage() != null && getDecorationImage().trim().length() > 0) {
				String imageLink = getResourceURL(ctx, getDecorationImage());
				ctx.getRequest().setAttribute("image", URLHelper.createTransformURL(ctx, imageLink, imageFilter));
			}
			ctx.getRequest().setAttribute("width", StringHelper.neverNull(width, getConfig(ctx).getProperty("link.width", "420")));
			ctx.getRequest().setAttribute("height", StringHelper.neverNull(height, getConfig(ctx).getProperty("link.height", "315")));
			String renderer = getConfig(ctx).getRenderes().get("link");
			if (preview) {
				renderer = "link.jsp";
			}
			return executeJSP(ctx, renderer);
		} else {
			if (getFileName() != null && getFileName().trim().length() > 0) {
				String fileLink = getResourceURL(ctx, getFileName());
				ctx.getRequest().setAttribute("file", URLHelper.createResourceURL(ctx, getPage(), fileLink).replace('\\', '/'));
				ctx.getRequest().setAttribute("url", URLHelper.createResourceURL(ctx, getPage(), fileLink).replace('\\', '/'));
				ctx.getRequest().setAttribute("type", ResourceHelper.getFileExtensionToManType(StringHelper.getFileExtension(getFileName())));
				if (getDecorationImage() != null && getDecorationImage().trim().length() > 0) {
					String imageLink = getResourceURL(ctx, getDecorationImage());
					ctx.getRequest().setAttribute("image", URLHelper.createTransformURL(ctx, imageLink, imageFilter));
				}
				ctx.getRequest().setAttribute("width", StringHelper.neverNull(width, getConfig(ctx).getProperty("local.width", "420")));
				ctx.getRequest().setAttribute("height", StringHelper.neverNull(height, getConfig(ctx).getProperty("local.height", "315")));
				return executeJSP(ctx, getConfig(ctx).getRenderes().get("local"));
			} else if (link.toLowerCase().contains("youtube")) {
				String videoCode = URLHelper.extractParameterFromURL(link).get("v");
				ctx.getRequest().setAttribute("vid", videoCode);
				ctx.getRequest().setAttribute("url", "http://www.youtube.com/embed/" + videoCode);
				ctx.getRequest().setAttribute("width", StringHelper.neverNull(width, getConfig(ctx).getProperty("youyube.width", "420")));
				ctx.getRequest().setAttribute("height", StringHelper.neverNull(height, getConfig(ctx).getProperty("youyube.height", "315")));
				return executeJSP(ctx, getConfig(ctx).getRenderes().get("youtube"));
			} else if (link.toLowerCase().contains("dailymotion")) {
				if (link.split("/").length > 1) {
					String videoCode = link.split("/")[link.split("/").length - 1];
					ctx.getRequest().setAttribute("url", "http://www.dailymotion.com/embed/video/" + videoCode);
					ctx.getRequest().setAttribute("width", StringHelper.neverNull(width, getConfig(ctx).getProperty("dailymotion.width", "420")));
					ctx.getRequest().setAttribute("height", StringHelper.neverNull(height, getConfig(ctx).getProperty("dailymotion.height", "315")));
					return executeJSP(ctx, getConfig(ctx).getRenderes().get("dailymotion"));
				}
			} else if (link.toLowerCase().contains("europarltv")) {
				if (link.split("/").length > 1) {
					String videoCode = URLHelper.extractParameterFromURL(link).get("pid");
					ctx.getRequest().setAttribute("vid", videoCode);
					ctx.getRequest().setAttribute("width", StringHelper.neverNull(width, getConfig(ctx).getProperty("europarltv.width", "420")));
					ctx.getRequest().setAttribute("height", StringHelper.neverNull(height, getConfig(ctx).getProperty("europarltv.height", "315")));
					return executeJSP(ctx, getConfig(ctx).getRenderes().get("europarltv"));
				}
			}
		}

		return "<p class=\"error\">no video defined.</p>";
	}

	@Override
	protected boolean isMeta() {
		return true;
	}

	@Override
	protected boolean isDecorationImage() {
		return true;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return false;
	}

	@Override
	protected String getImageChangeTitle(ContentContext ctx) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		return i18nAccess.getText("content.video.label");
	}

	@Override
	protected String renderViewXHTMLCode(ContentContext ctx) throws Exception {
		if (getRenderer(ctx) != null && getStyle(ctx).equals(LINK)) {
			return executeCurrentRenderer(ctx);
		} else {
			return getViewXHTMLCode(ctx);
		}
	}

	public int getAccess(ContentContext ctx, int days) throws NumberFormatException, IOException {
		Calendar cal = Calendar.getInstance();
		int countAccess = 0;
		for (int i = 0; i < days; i++) {
			countAccess = countAccess + Integer.parseInt(getViewData(ctx).getProperty("access-" + StringHelper.renderDate(cal.getTime()), "0"));
			cal.roll(Calendar.DAY_OF_YEAR, false);
		}
		return countAccess;
	}

	public void addOneAccess(ContentContext ctx) throws NumberFormatException, IOException {
		String dateStr = StringHelper.renderDate(new Date());
		synchronized (getViewData(ctx)) {
			String key = "access-" + dateStr;
			int count = Integer.parseInt(getViewData(ctx).getProperty(key, "0"));
			getViewData(ctx).setProperty(key, "" + (count + 1));
			storeViewData(ctx);
		}
	}

	public static final String performAccess(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		String compId = requestService.getParameter("comp-id", null);
		if (compId != null) {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			ContentService content = ContentService.getInstance(ctx.getRequest());
			IContentVisualComponent comp = content.getComponent(ctx, compId);
			if (comp != null && comp instanceof Video) {
				((Video) comp).addOneAccess(ctx);
			}
		}
		return null;
	}

	@Override
	public Collection<String> getExternalResources(ContentContext ctx) {
		Collection<String> resources = new LinkedList<String>();
		resources.add("/js/freefw/ajax.js");
		return resources;
	}

	@Override
	public String getPreviewURL(ContentContext ctx, String filter) {
		if (filter == null) {
			filter = getImageFilter(ctx);
		}
		try {
			String decoImage = getDecorationImage();
			if (decoImage != null && decoImage.trim().length() > 0) {
				String imageLink = getResourceURL(ctx, getDecorationImage());
				return URLHelper.createTransformURL(ctx, imageLink, filter);
			} else if (getLink() != null && getLink().toLowerCase().contains("youtube")) {
				return getYoutubePreview(ctx, null);
			}
		} catch (Exception e) {
			logger.warning(e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public String getResourceURL(ContentContext ctx) {
		return getResourceURL(ctx, getDecorationImage());
	}

	@Override
	protected boolean isEmbedCode() {
		return true;
	}

	@Override
	public String getActionGroupName() {
		return "video";
	}
}
