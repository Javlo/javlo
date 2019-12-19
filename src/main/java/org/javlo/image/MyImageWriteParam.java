package org.javlo.image;

import java.util.Locale;

import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

// This class overrides the setCompressionQuality() method to workaround
// a problem in compressing JPEG images using the javax.imageio package.
class MyImageWriteParam extends JPEGImageWriteParam {
	public MyImageWriteParam() {
		super(Locale.getDefault());
	}

	/*
	 * public void setCompressionQuality(float quality) { if (quality < 0.0F ||
	 * quality > 1.0F) { throw new
	 * IllegalArgumentException("Quality out-of-bounds!"); } this.compressionQuality
	 * = 256 - (quality 256); }
	 */

	public void setCompressionQuality(int quality) {
		this.compressionQuality = quality;
	}
}