package org.javlo.helper;

import org.javlo.image.ImageSize;

import junit.framework.TestCase;

public class ImageSizeTest  extends TestCase {
	
	public void testStorage() throws Exception {
		ImageSize imageSize = new ImageSize(320,200);
		String data = imageSize.storeToString();
		ImageSize newImageSize = new ImageSize(640,480);
		newImageSize.loadFromString(data);
		assertEquals(newImageSize.getWidth(), 320);
		assertEquals(newImageSize.getHeight(), 200);		
	}

}
