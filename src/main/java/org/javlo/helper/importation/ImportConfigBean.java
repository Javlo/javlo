package org.javlo.helper.importation;

import org.javlo.component.core.ComponentBean;

public class ImportConfigBean {
	
	private boolean imagesAsGallery = false;
	
	private boolean imagesAsImages = false;
	
	private String area = ComponentBean.DEFAULT_AREA;
	
	private boolean beforeContent = true;

	public boolean isImagesAsGallery() {
		return imagesAsGallery;
	}

	public void setImagesAsGallery(boolean imagesAsGallery) {
		this.imagesAsGallery = imagesAsGallery;
	}

	public boolean isImagesAsImages() {
		return imagesAsImages;
	}

	public void setImagesAsImages(boolean imagesAsImages) {
		this.imagesAsImages = imagesAsImages;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public boolean isBeforeContent() {
		return beforeContent;
	}

	public void setBeforeContent(boolean beforeContent) {
		this.beforeContent = beforeContent;
	}
	
}
