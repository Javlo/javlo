package org.javlo.visualtesting.helper;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

public class ImageHelper {

	public static double compareImage(Path img1File, Path img2File) throws IOException {
		BufferedImage img1 = ImageIO.read(img1File.toFile());
		BufferedImage img2 = ImageIO.read(img2File.toFile());
		return compareImage(img1, img2);
	}

	public static double compareImage(BufferedImage img1, BufferedImage img2) {
		int height = Math.max(img1.getHeight(), img2.getHeight());
		int width = Math.max(img1.getWidth(), img2.getWidth());
		long matching = 0;
		long pixels = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int col1 = -1;
				if (x < img1.getWidth() && y < img1.getHeight()) {
					col1 = img1.getRGB(x, y);
				}
				int col2 = -1;
				if (x < img2.getWidth() && y < img2.getHeight()) {
					col2 = img2.getRGB(x, y);
				}
				if (Integer.valueOf(col1).equals(col2)) {
					matching++;
				}
				pixels++;
			}
		}
		return (double) matching / pixels;
	}

}
