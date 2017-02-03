package org.javlo.component.image;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

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
	public ImageBean(ContentContext ctx, IImageTitle image, String filter) throws Exception {
		super();
		this.url = image.getResourceURL(ctx);
		this.description = image.getImageDescription(ctx);
		this.link = image.getImageLinkURL(ctx);
		this.setPreviewURL(URLHelper.createTransformURL(ctx, ctx.getCurrentPage(), image.getResourceURL(ctx), filter));
	}
	public String getName() {
		return StringHelper.getFileNameFromPath(url);
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
