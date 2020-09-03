package org.javlo.component.core;

import java.awt.image.BufferedImage;

import javax.servlet.ServletContext;

import org.javlo.context.ContentContextBean;

public interface IImageFilter {

	String getId();

	String getImageFilterKey(ContentContextBean ctx);

	BufferedImage filterImage(ServletContext application, ContentContextBean ctx, BufferedImage image);
	
	String getImageHash(ContentContextBean ctx);

}
