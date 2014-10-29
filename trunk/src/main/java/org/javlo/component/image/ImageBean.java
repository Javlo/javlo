package org.javlo.component.image;

public class ImageBean {
	
	private String url;
	private String previewURL;
	private String description;
	private String link;
	
	public ImageBean(String url, String previewURL, String descrition, String link) {
		super();
		this.url = url;
		this.description = descrition;
		this.link = link;
		this.setPreviewURL(previewURL);
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getDescription() {
		return description;
	}
	public void setDescrition(String description) {
		this.description = description;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getPreviewURL() {
		return previewURL;
	}
	public void setPreviewURL(String previewURL) {
		this.previewURL = previewURL;
	}
}
