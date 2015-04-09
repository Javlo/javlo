package org.javlo.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ColorImageEngine {

	static Color mixedColor(Color color1, Color color2, float ratioColor1) {
		float alpha = (color1.getAlpha() * ratioColor1 + color2.getAlpha() * (1 - ratioColor1));
		float red = (color1.getRed() * ratioColor1 + color2.getRed() * (1 - ratioColor1));
		float green = (color1.getGreen() * ratioColor1 + color2.getGreen() * (1 - ratioColor1));
		float blue = (color1.getBlue() * ratioColor1 + color2.getBlue() * (1 - ratioColor1));
		return new Color(red / 255, green / 255, blue / 255, alpha / 255);
	}

	/**
	 * get a distance between two color between 0 and 100
	 * 
	 * @param color1
	 * @param color2
	 * @return a int between 0 and 100
	 */
	static int getColorDistance(Color color1, Color color2) {
		int red = Math.abs(color1.getRed() - color2.getRed());
		int green = Math.abs(color1.getGreen() - color2.getGreen());
		int blue = Math.abs(color1.getBlue() - color2.getBlue());
		int alpha = Math.abs(color1.getAlpha() - color2.getAlpha());
		return Math.round(((float) (red + green + blue + alpha) / (float) (255 * 4)) * 100);
	}

	public static BufferedImage getDegrade(boolean vertical, int margin, int size, Color startColor, Color endColor, int alphaMax) {
		BufferedImage outImage;
		Color noColor = new Color(0, 0, 0, 0);
		if (vertical) {
			outImage = new BufferedImage(1, size + margin, BufferedImage.TYPE_4BYTE_ABGR);
			for (int p = 0; p < margin; p++) {
				outImage.setRGB(0, p, noColor.getRGB());
			}
			for (int p = margin; p < size + margin; p++) {
				Color c;
				if (endColor != null && startColor != null) {
					c = mixedColor(startColor, endColor, (float) (p - margin) / (float) size);
				} else {
					if (endColor == null) {
						int alpha = 255 - Math.round(((float) (p - margin) / (float) size) * alphaMax);
						c = new Color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), alpha);
					} else {
						int alpha = 255 - Math.round((1 - ((float) (p - margin) / (float) size)) * alphaMax);
						c = new Color(endColor.getRed(), endColor.getGreen(), endColor.getBlue(), alpha);
					}
				}
				outImage.setRGB(0, p, c.getRGB());
			}
		} else {
			outImage = new BufferedImage(size+margin, 1, BufferedImage.TYPE_4BYTE_ABGR);			
			for (int p = 0; p < margin; p++) {
				outImage.setRGB(p, 0, noColor.getRGB());
			}
			for (int p = margin; p < size + margin; p++) {
				Color c;
				if (endColor != null && startColor != null) {
					c = mixedColor(startColor, endColor, (float) (p - margin) / (float) size);
				} else {
					if (endColor == null) {
						int alpha = 255 - Math.round(((float) (p - margin) / (float) size) * alphaMax);
						c = new Color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), alpha);
					} else {
						int alpha = 255 - Math.round((1 - ((float) (p - margin) / (float) size)) * alphaMax);
						c = new Color(endColor.getRed(), endColor.getGreen(), endColor.getBlue(), alpha);
					}
				}
				outImage.setRGB(p, 0, c.getRGB());
			}
		}
		return outImage;
	}

	private static final Color getMixedColor(Color color, Color bgColor) {
		Color outColor = color;
		if (getColorDistance(color, Color.WHITE) < 50) {
			Color finalColor = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), color.getAlpha());
			// color = mixedColor(color, finalColor,getColorDistance(color,Color.WHITE)/100);
			return finalColor;
		}
		return outColor;
	}

	public static final BufferedImage getCornerLine(int size, File leftImage, File rightImage, Color bodyColor) throws IOException {
		BufferedImage leftCorner = ImageIO.read(leftImage);
		BufferedImage rightCorner = ImageIO.read(rightImage);
		BufferedImage outImage = new BufferedImage(size, Math.max(leftCorner.getHeight(), rightCorner.getHeight()), BufferedImage.TYPE_4BYTE_ABGR);
		for (int x = 0; x < leftCorner.getWidth(); x++) {
			for (int y = 0; y < outImage.getHeight(); y++) {
				outImage.setRGB(x, y, getMixedColor(new Color(leftCorner.getRGB(x, y), true), bodyColor).getRGB());
			}
		}
		for (int x = 0; x < rightCorner.getWidth(); x++) {
			for (int y = 0; y < outImage.getHeight(); y++) {
				outImage.setRGB(x + (size - rightCorner.getWidth()), y, getMixedColor(new Color(rightCorner.getRGB(x, y), true), bodyColor).getRGB());
			}
		}
		for (int x = 0; x < size - leftCorner.getWidth() - rightCorner.getWidth(); x++) {
			for (int y = 0; y < outImage.getHeight(); y++) {
				outImage.setRGB(x + leftCorner.getWidth(), y, getMixedColor(new Color(rightCorner.getRGB(0, y), true), bodyColor).getRGB());
			}
		}
		return outImage;
	}

	public static void main(String[] args) {
		File leftImage = new File("/tmp/left_corner.png");
		File rightImage = new File("/tmp/right_corner.png");
		BufferedImage result;
		try {
			result = getCornerLine(800, leftImage, rightImage, Color.decode("#ff0000"));
			ImageIO.write(result, "png", new File("/tmp/result.png"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}