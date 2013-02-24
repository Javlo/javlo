package org.javlo.image;

public class ImageSize {

	private int width;
	private int height;
	private int dpi = 72;

	public ImageSize(int width, int height) {
		super();
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getDpi() {
		return dpi;
	}

	public void setDpi(int dpi) {
		this.dpi = dpi;
	}

	@Override
	public String toString() {
		return "x:" + getWidth() + " y:" + getHeight();
	}

}
