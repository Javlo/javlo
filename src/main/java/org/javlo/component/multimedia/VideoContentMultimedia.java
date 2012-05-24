package org.javlo.component.multimedia;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class VideoContentMultimedia extends Multimedia {

	protected static class TranslatedVideo {

		public static class AccessOrderVideo implements Comparator<TranslatedVideo> {

			ContentContext ctx;
			int days;

			public AccessOrderVideo(ContentContext ctx, int days) {
				this.ctx = ctx;
				this.days = days;
			}

			@Override
			public int compare(TranslatedVideo vid1, TranslatedVideo vid2) {
				try {
					if (vid1 == null || vid1.getVideo() == null || vid1.getVideo().getDate() == null || vid2 == null || vid2.getVideo() == null || vid2.getVideo().getDate() == null) {
						return 1;
					}
					return vid1.getVideo().getAccess(ctx, days) - vid2.getVideo().getAccess(ctx, days);
				} catch (Exception e) {
					e.printStackTrace();
					return 1;
				}
			}

		}

		public static class OrderVideo implements Comparator<TranslatedVideo> {

			@Override
			public int compare(TranslatedVideo vid1, TranslatedVideo vid2) {
				try {
					if (vid1 == null || vid1.getVideo() == null || vid1.getVideo().getDate() == null || vid2 == null || vid2.getVideo() == null || vid2.getVideo().getDate() == null) {
						return 1;
					}
					return -vid1.getVideo().getDate().compareTo(vid2.getVideo().getDate());
				} catch (ParseException e) {
					return 1;
				}
			}

		}

		private Video video;
		private List<Video> translation = new LinkedList<Video>();

		public void addTranslation(Video video) {
			translation.add(video);
		}

		public List<Video> getTranslation() {
			return translation;
		}

		public Video getVideo() {
			return video;
		}

		public void setVideo(Video video) {
			this.video = video;
		}

	}

	public static final String TYPE = "videos-multimedia";

	protected boolean acceptResource(ContentContext ctx, MultimediaResource resource, int index) {
		Calendar currentDate = GregorianCalendar.getInstance();
		if (resource.getDate() != null) {
			currentDate.setTime(resource.getDate());
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

		if (resource.getDate() == null && (startDate != null || endDate != null)) {
			afterAccept = false;
			beforeAccept = false; // not necessary, just more "clean" :-)
		}

		return afterAccept && beforeAccept && index < getMaxListSize();
	}

	private MultimediaResource createResource(ContentContext ctx, Video video) throws Exception {
		MultimediaResource resource = new MultimediaResource();
		resource.setTitle(video.getTitle());
		resource.setRelation(getHTMLRelation(ctx));
		resource.setLocation(video.getLocation());
		resource.setDescription(video.getLabel());
		resource.setDate(video.getDate());

		String accessActionURL = URLHelper.createURL(ctx) + "?webaction=video.access&comp-id=" + video.getId();
		resource.setAccessURL(accessActionURL);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		resource.setShortDate(StringHelper.renderDate(resource.getDate(), globalContext.getShortDateFormat()));
		resource.setMediumDate(StringHelper.renderDate(resource.getDate(), globalContext.getMediumDateFormat()));
		resource.setFullDate(StringHelper.renderDate(resource.getDate(), globalContext.getFullDateFormat()));

		resource.setURL(video.getURL(ctx));
		resource.setLanguage(video.getBean(ctx).getLanguage());
		resource.setPreviewURL(video.getPreviewURL(ctx));
		return resource;
	}

	Collection<TranslatedVideo> getAllTranslatedVideo(ContentContext ctx) throws Exception {
		List<Video> videos = getAllVideoComponent(ctx);
		Map<String, TranslatedVideo> translatedVideo = new HashMap<String, TranslatedVideo>();
		for (Video video : videos) {
			if (video.getTranslatedID() == null || video.getTranslatedID().trim().length() == 0) {
				TranslatedVideo tVideo = new TranslatedVideo();
				tVideo.setVideo(video);
				translatedVideo.put(video.getId(), tVideo);
			}
		}
		for (Video video : videos) {
			if (video.getTranslatedID() != null && video.getTranslatedID().trim().length() > 0) {
				TranslatedVideo tVideo = translatedVideo.get(video.getTranslatedID());
				if (tVideo != null) {
					tVideo.addTranslation(video);
				} else {
					logger.warning("translated video not found : " + video.getTranslatedID());
				}
			}
		}

		Collection<String> mustBeRemovedVideo = new LinkedList<String>();
		Collection<TranslatedVideo> mustBeAddedVideo = new LinkedList<TranslatedVideo>();
		for (TranslatedVideo tVideo : translatedVideo.values()) {
			if (tVideo.getTranslation().size() > 0) {
				for (Video video : tVideo.getTranslation()) {
					if (video.getBean(ctx).getLanguage().equals(ctx.getRequestContentLanguage())) { // if current language is in translation, switch the video.
						mustBeRemovedVideo.add(tVideo.getVideo().getId());
						TranslatedVideo currentLangVideo = new TranslatedVideo();
						currentLangVideo.setVideo(video);
						currentLangVideo.addTranslation(tVideo.getVideo());
						for (Video otherTranslationVideo : tVideo.getTranslation()) {
							if (!otherTranslationVideo.getId().equals(video.getId())) {
								currentLangVideo.addTranslation(otherTranslationVideo);
							}
						}
						mustBeAddedVideo.add(currentLangVideo);
					}
				}
			}
		}
		for (String videoId : mustBeRemovedVideo) {
			translatedVideo.remove(videoId);
		}
		for (TranslatedVideo translatedVideoTobeAdded : mustBeAddedVideo) {
			translatedVideo.put(translatedVideoTobeAdded.getVideo().getId(), translatedVideoTobeAdded);
		}

		List<TranslatedVideo> outList = new LinkedList<TranslatedVideo>();
		outList.addAll(translatedVideo.values());
		if (isOrderByAccess(ctx)) {
			Collections.sort(outList, new TranslatedVideo.AccessOrderVideo(ctx, 30));
		} else {
			Collections.sort(outList, new TranslatedVideo.OrderVideo());
		}
		return outList;
	}

	List<Video> getAllVideoComponent(ContentContext ctx) throws Exception {
		List<Video> comps = new LinkedList<Video>();
		MenuElement[] children = getRootPage(ctx).getAllChilds();
		ContentContext noAreaCtx = new ContentContext(ctx);
		noAreaCtx.setArea(null);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		for (String lg : globalContext.getContentLanguages()) {
			noAreaCtx.setRequestContentLanguage(lg);
			for (MenuElement page : children) {
				ContentElementList content = page.getContent(noAreaCtx);
				while (content.hasNext(noAreaCtx)) {
					IContentVisualComponent comp = content.next(noAreaCtx);
					if (comp instanceof Video) {
						comps.add((Video) comp);
					}
				}
			}
		}
		// Collections.sort(comps, new Video.OrderVideo());
		return comps;
	}

	@Override
	public Collection<String> getExternalResources(ContentContext ctx) {
		Collection<String> resources = new LinkedList<String>();
		resources.add("/js/freefw/ajax.js");
		return resources;
	}

	MenuElement getRootPage(ContentContext ctx) throws Exception {
		ContentService content = ContentService.createContent(ctx.getRequest());
		MenuElement root = content.getNavigation(ctx);
		return root;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		int index = 0;

		List<MultimediaResource> allResource = new LinkedList<MultimediaResource>();

		for (TranslatedVideo tVideo : getAllTranslatedVideo(ctx)) {

			String cssClass = "video";

			MultimediaResource resource = createResource(ctx, tVideo.getVideo());
			resource.setCssClass(cssClass);
			for (Video video : tVideo.getTranslation()) {
				MultimediaResource tResource = createResource(ctx, video);
				resource.addTranslation(tResource);
				tResource.setCssClass(cssClass);
			}

			if (isRenderInfo(ctx)) {
				resource.setIndex(index);
				if (acceptResource(ctx, resource, index)) {
					index++;
					allResource.add(resource);
				}
			}
		}

		ctx.getRequest().setAttribute("size", index);
		ctx.getRequest().setAttribute("resources", allResource);

		return executeJSP(ctx, getRenderer(ctx));
	}

	@Override
	boolean isFolder() {
		return false;
	}

}
