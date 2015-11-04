package org.javlo.component.core;

import java.awt.image.BufferedImage;

import org.javlo.rendering.Device;

public interface IImageFilter {

	String getId();

	String getImageFilterKey(Device device);

	BufferedImage filterImage(Device device, BufferedImage image);
	
	String getImageHash(Device device);

}
