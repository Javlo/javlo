package org.javlo.component.image;

import java.io.Serializable;

import org.javlo.context.ContentContext;

public class ImageTitleBean implements IImageTitle, Serializable {
	
	public static final ImageTitleBean EMPTY_BEAN = new ImageTitleBean(null,null,null);
	
	private final String imageDescription;
	private final String imageURL;
	private final String imageLink;
	private final int priority;
	private boolean mobileOnly = false;
	
	public ImageTitleBean(String imageDescription, String imageURL, String imageLink, int priority) {
		super();
		this.imageDescription = imageDescription;
		this.imageURL = imageURL;
		this.imageLink = imageLink;
		this.priority = priority;
	}
	
	public ImageTitleBean(String imageDescription, String imageURL, String imageLink, int priority, boolean mobileOnly) {
		super();
		this.imageDescription = imageDescription;
		this.imageURL = imageURL;
		this.imageLink = imageLink;
		this.priority = priority;
		this.mobileOnly = mobileOnly;
	}
	
	public ImageTitleBean(String imageDescription, String imageURL, String imageLink) {
		super();
		this.imageDescription = imageDescription;
		this.imageURL = imageURL;
		this.imageLink = imageLink;
		this.priority = 5;
	}
	
	public ImageTitleBean(String imageDescription, String imageURL, String imageLink, boolean mobileOnly) {
		super();
		this.imageDescription = imageDescription;
		this.imageURL = imageURL;
		this.imageLink = imageLink;
		this.priority = 5;
		this.mobileOnly = mobileOnly;
	}
	
	public ImageTitleBean(ContentContext ctx, IImageTitle imageTitle) {
		super();
		this.imageDescription = imageTitle.getImageDescription(ctx);
		this.imageURL = imageTitle.getResourceURL(ctx);
		this.imageLink = imageTitle.getImageLinkURL(ctx);
		this.priority = imageTitle.getPriority(ctx);
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
	
	@Override
	public int getPriority(ContentContext ctx) {
		return priority;
	}
	
	@Override
	public boolean isMobileOnly(ContentContext ctx) {
		return mobileOnly;
	}
	
	public void setMobileOnly(boolean mobileOnly) {
		this.mobileOnly = mobileOnly;
	}
}
