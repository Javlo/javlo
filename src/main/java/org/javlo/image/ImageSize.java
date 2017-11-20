package org.javlo.image;

import org.javlo.helper.IStringSeralizable;
import org.javlo.helper.StringHelper;

public class ImageSize implements IStringSeralizable {

	private int width=-1;
	private int height=-1;
	private int dpi = 72;
	
	public ImageSize() {
	}

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

	@Override
	public boolean loadFromString(String data) {
		if (StringHelper.isEmpty(data)) {
			return false;
		}
		String[] datas = StringHelper.stringToArray(data, ",");
		if (datas.length >= 2) {
			setWidth(Integer.parseInt(datas[0]));
			setHeight(Integer.parseInt(datas[1]));
			if (data.length() == 2) {
				return true;
			}
		} 
		if (datas.length >= 3) {			
			setDpi(Integer.parseInt(datas[2]));
			return true;
		}
		return false;
	}

	@Override
	public String storeToString() {
		return ""+getWidth()+','+getHeight()+','+getDpi();
	}

}

