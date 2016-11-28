package org.javlo.component.multimedia;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.servlet.ImageTransformServlet;
import org.javlo.ztatic.StaticInfo.StaticInfoBean;

public class MultimediaResource {

	public static class SortByDate implements Comparator<MultimediaResource> {

		boolean reverse = false;

		public SortByDate(boolean reverse) {
			this.reverse = reverse;
		}

		@Override
		public int compare(MultimediaResource o1, MultimediaResource o2) {
			int out = 0;
			if (o1.getDate() != null || o2.getDate() != null) {

				if (o1.getDate() == null) {
					out = 1;
				} else if (o2.getDate() == null) {
					out = -1;
				} else if (o1.getDate().equals(o2.getDate())) {
					out = o2.getIndex() - o1.getIndex();
				} else {
					out = o1.getDate().compareTo(o2.getDate());
				}
			}
			if (reverse) {
				return out;
			} else {
				return -out;
			}
		}

	}

	public static class SortByName implements Comparator<MultimediaResource> {

		boolean reverse = false;

		public SortByName(boolean reverse) {
			this.reverse = reverse;
		}

		@Override
		public int compare(MultimediaResource o1, MultimediaResource o2) {
			int out = 0;
			String name1 = StringHelper.neverNull(StringHelper.getFileNameFromPath(o1.getURL()));
			String name2 = StringHelper.neverNull(StringHelper.getFileNameFromPath(o2.getURL()));
			out = name1.compareTo(name2);
			if (reverse) {
				return -out;
			} else {
				return out;
			}
		}

	}

	public static class SortByIndex implements Comparator<MultimediaResource> {

		boolean reverse = false;

		public SortByIndex(boolean reverse) {
			this.reverse = reverse;
		}

		@Override
		public int compare(MultimediaResource o1, MultimediaResource o2) {
			if (reverse) {
				return o2.getIndex() - o1.getIndex();
			} else {
				return -(o2.getIndex() - o1.getIndex());

			}
		}

	}

	private String id;
	private String URL;
	private String absoluteURL;
	private String previewURL;
	private String absolutePreviewURL;
	private String path;
	private String description;
	private String fullDescription;
	private String shortDate;
	private String copyright;
	private String mediumDate;
	private String fullDate;
	private String name;
	private Date date;
	private String cssClass;
	private String title;
	private String relation;
	private String accessToken;
	private int index;
	private String location;
	private String gpsPosition;
	private String language;
	private String accessURL;
	private boolean freeAccess = true;
	private final List<MultimediaResource> translation = new LinkedList<MultimediaResource>();
	private List<String> tags;
	private StaticInfoBean staticInfo;

	public void addTranslation(MultimediaResource resource) {
		this.translation.add(resource);
	}

	public String getAccessURL() {
		return accessURL;
	}

	public String getCssClass() {
		return cssClass;
	}

	public Date getDate() {
		return date;
	}

	public String getDescription() {
		return description;
	}

	public String getFullDate() {
		return fullDate;
	}

	public int getIndex() {
		return index;
	}

	public String getLanguage() {
		return language;
	}

	public String getLocation() {
		return location;
	}

	public String getMediumDate() {
		return mediumDate;
	}

	public String getPath() {
		return path;
	}

	public String getPreviewURL() {
		return previewURL;
	}

	public String getRelation() {
		return relation;
	}

	public String getShortDate() {
		return shortDate;
	}

	public String getTitle() {
		return title;
	}

	public List<MultimediaResource> getTranslation() {
		return translation;
	}

	public String getURL() {
		return URL;
	}

	public void setAccessURL(String accessURL) {
		this.accessURL = accessURL;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setFullDate(String fullDate) {
		this.fullDate = fullDate;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setMediumDate(String mediumDate) {
		this.mediumDate = mediumDate;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setPreviewURL(String previewURL) {
		this.previewURL = previewURL;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public void setShortDate(String shortDate) {
		this.shortDate = shortDate;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public List<String> getTags() {
		if (tags == null) {
			return Collections.EMPTY_LIST;
		}
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public void renderDate(ContentContext ctx) {
		try {
			setShortDate(StringHelper.renderShortDate(ctx, getDate()));
			setMediumDate(StringHelper.renderMediumDate(ctx, getDate()));
			setFullDate(StringHelper.renderFullDate(ctx, getDate()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getFullDescription() {
		return fullDescription;
	}

	public void setFullDescription(String fullDescription) {
		this.fullDescription = fullDescription;
	}

	/**
	 * return type : unknow, html, image, video
	 * 
	 * @return
	 */
	public String getType() {
		String url = getURL();
		if (url == null) {
			return "unknow";
		}
		if (StringHelper.isImage(url)) {
			return "image";
		} else if (StringHelper.isVideo(url)) {
			return "video";
		} else if (StringHelper.isURL(url) || StringHelper.isHTML(url)) {
			return "html";
		} else {
			return "unknow";
		}
	}

	/**
	 * return type : unknow, html, youtube, vimeo, dailymotion, europarltv, mp4,
	 * jpg, gif, png, image-unkown
	 * 
	 * @return
	 */
	public String getSubType() {
		String url = getURL();
		if (url == null) {
			return "unknow";
		}
		String lowerURL = url.trim().toLowerCase();
		if (StringHelper.isImage(url)) {
			if (lowerURL.endsWith(".jpg") || lowerURL.endsWith(".jpeg")) {
				return "jpg";
			} else if (lowerURL.endsWith(".png")) {
				return "png";
			} else if (lowerURL.endsWith(".gif")) {
				return "gif";
			} else {
				return "image-unkown";
			}
		}
		if (lowerURL.endsWith(".mp4")) {
			return "mp4";
		} else {
			if (lowerURL.contains("vimeo.")) {
				return "vimeo";
			} else if (lowerURL.contains("dailymotion.")) {
				return "dailymotion";
			} else if (lowerURL.contains("europarltv.")) {
				return "europarltv";
			} else if (lowerURL.contains("youtu")) {
				return "youtube";
			}
		}
		if (StringHelper.isURL(url) || StringHelper.isHTML(url)) {
			return "html";
		} else {
			return "unknow";
		}
	}
	
	/**
	 * get code of the video (sp. v=code in youtube video).
	 * @return
	 */
	public String getCode() {
		String subtype = getSubType();
		if (subtype.equals("youtube")) {
			return URLHelper.getParams(getURL()).get("v");
		} else if (subtype.equals("dailymotion") || subtype.equals("video")) {
			String code = getURL();
			code = StringHelper.getFileNameFromPath(code);
			return code;
		}
		return null;
	}

	public boolean isImage() {
		return getType().equals("image");
	}

	public boolean isVideo() {
		return getType().equals("video");
	}

	public boolean isHtml() {
		return getType().equals("html");
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGpsPosition() {
		return gpsPosition;
	}

	public void setGpsPosition(String gpsPosition) {
		this.gpsPosition = gpsPosition;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public String getAbsoluteURL() {
		if (getAccessToken() != null) {
			return URLHelper.addParam(absoluteURL, ImageTransformServlet.RESOURCE_TOKEN_KEY, getAccessToken());
		} else {
			return absoluteURL;
		}
	}

	public void setAbsoluteURL(String absoluteURL) {
		this.absoluteURL = absoluteURL;
	}

	public String getAbsolutePreviewURL() {
		if (getAccessToken() != null) {
			return URLHelper.addParam(absolutePreviewURL, ImageTransformServlet.RESOURCE_TOKEN_KEY, getAccessToken());
		} else {
			return absolutePreviewURL;
		}
	}

	public void setAbsolutePreviewURL(String absolutePreviewURL) {
		this.absolutePreviewURL = absolutePreviewURL;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public boolean isFreeAccess() {
		return freeAccess;
	}

	public void setFreeAccess(boolean publik) {
		this.freeAccess = publik;
	}
	
	public StaticInfoBean getStaticInfo() {
		return staticInfo;
	}
	
	public void setStaticInfo(StaticInfoBean staticInfo) {
		this.staticInfo = staticInfo;
	}
}