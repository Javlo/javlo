package org.javlo.component.core;

import java.awt.image.BufferedImage;

import org.javlo.context.ContentContext;

public interface IImageFilter {

	String getId();

	String getImageFilterKey(ContentContext ctx);

	BufferedImage filterImage(ContentContext ctx, BufferedImage image);

}
