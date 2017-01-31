package org.javlo.component.core;

import java.awt.image.BufferedImage;

import org.javlo.context.ContentContextBean;

public interface IImageFilter {

	String getId();

	String getImageFilterKey(ContentContextBean ctx);

	BufferedImage filterImage(ContentContextBean ctx, BufferedImage image);
	
	String getImageHash(ContentContextBean ctx);

}
