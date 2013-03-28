package org.javlo.component.image;

import java.io.Serializable;

import org.javlo.context.ContentContext;

public class ImageTitleBean implements IImageTitle, Serializable {
	
	private final String imageDescription;
	private final String imageURL;
	private final String imageLink;
	
	public ImageTitleBean(String imageDescription, String imageURL, String imageLink) {
		super();
		this.imageDescription = imageDescription;
		this.imageURL = imageURL;
		this.imageLink = imageLink;
	}
	
	public ImageTitleBean(ContentContext ctx, IImageTitle imageTitle) {
		super();
		this.imageDescription = imageTitle.getImageDescription(ctx);
		this.imageURL = imageTitle.getResourceURL(ctx);
		this.imageLink = imageTitle.getImageLinkURL(ctx);
	}
	
	@Override
	public String getImageDescription(ContentContext ctx) {
		return imageDescription;
	}
	
	@Override
	public String getResourceURL(ContentContext ctx) {
		return imageURL;
	}

	@Override
	public boolean isImageValid(ContentContext ctx) {
		return true;
	}

	@Override
	public String getImageLinkURL(ContentContext ctx) {
		return imageLink;
	}

	
}
