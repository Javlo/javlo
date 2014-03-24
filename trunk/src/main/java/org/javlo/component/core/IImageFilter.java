package org.javlo.component.core;

import java.awt.image.BufferedImage;

public interface IImageFilter {

	String getId();

	String getImageFilterKey();

	BufferedImage filterImage(BufferedImage image);

}
