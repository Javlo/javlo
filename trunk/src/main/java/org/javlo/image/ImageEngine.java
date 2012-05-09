package org.javlo.image;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

import com.jhlabs.image.RGBAdjustFilter;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class ImageEngine {
	
	private static Logger logger = Logger.getLogger(ImageEngine.class.getName());

	public final static Color NEUTRAL_COLOR = new Color(0.5f, 0.5f, 0.5f);

	// This class overrides the setCompressionQuality() method to workaround
	// a problem in compressing JPEG images using the javax.imageio package.
	private static class MyImageWriteParam extends JPEGImageWriteParam {
		public MyImageWriteParam() {
			super(Locale.getDefault());
		}

		/*
		 * public void setCompressionQuality(float quality) { if (quality < 0.0F || quality > 1.0F) { throw new IllegalArgumentException("Quality out-of-bounds!"); } this.compressionQuality = 256 - (quality 256); }
		 */

		public void setCompressionQuality(int quality) {
			this.compressionQuality = quality;
		}
	}

	// static File source = new File("c:/trans/margaux1.jpg");
	static File source = new File("s:/tmp/source_light.jpg");

	static File filter = new File("c:/trans/layer.png");

	// static File target = new File("c:/trans/margaux1_w2.png");
	static File target = new File("s:/tmp/target_light_1.png");

	public static BufferedImage blurring(BufferedImage img) {
		/*
		 * float[] matrix = { 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, };
		 */
		float[] matrix = new float[400];
		for (int i = 0; i < matrix.length; i++) {
			matrix[i] = (float) (1f / (float) matrix.length);
		}

		BufferedImage target = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

		BufferedImageOp op = new ConvolveOp(new Kernel(20, 20, matrix));
		return op.filter(img, target);
	}

	public static BufferedImage lightBlurring(BufferedImage img) {

		float[] matrix = new float[9];
		for (int i = 0; i < matrix.length; i++) {
			matrix[i] = (float) (1f / (float) matrix.length);
		}

		BufferedImage target = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

		BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, matrix));
		return op.filter(img, target);
	}

	private static BufferedImage resize(BufferedImage bi, int width, int height) {
		float ratio = ((float) width * (float) height) / ((float) bi.getWidth() * (float) bi.getHeight());
		if (ratio < 0.5) {
			return resizeBig(bi, width, height);
		} else {
			return resizeSmall(bi, width, height);
		}
	}

	private static BufferedImage resizeSmall(BufferedImage bi, int width, int height) {

		width = Math.abs(width);
		height = Math.abs(height);

		AffineTransform tx = new AffineTransform();

		double xsc = (width) / ((double) bi.getWidth());
		double ysc = (height) / ((double) bi.getHeight());

		if ((xsc < 0.4) && (ysc < 0.4)) { /* sufisant reduction of the size */
			try {
				bi = lightBlurring(bi); /* more beautiful image */
			} catch (RuntimeException e) {
				// TODO check why this method does'nt work always
			}
		}

		tx.scale(xsc, ysc);

		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BICUBIC);

		BufferedImage image = op.filter(bi, null);

		BufferedImage imgNew = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				int rgb = image.getRGB(x, y);
				imgNew.setRGB(x, y, rgb);

			}
		}
		return imgNew;
	}

	private static BufferedImage resizeBig(BufferedImage in, int width, int height) {
		int imageWidth = in.getWidth();
		int imageHeight = in.getHeight();

		int[] pixels = in.getRGB(0, 0, imageWidth, imageHeight, null, 0, imageWidth);
		int[] outPixels = new int[width * height];

		int nw = Math.max(imageWidth / width + 1, 2);
		int nh = Math.max(imageHeight / height + 1, 2);

		int idx, i2, j2, argb, cnt;
		int[] v = new int[4];

		float kw = (float) imageWidth / (float) width;
		float kh = (float) imageHeight / (float) height;

		float nw2 = nw / 2.0F;
		float nh2 = nh / 2.0F;

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				v[0] = v[1] = v[2] = v[3] = cnt = 0;

				i2 = (int) ((i + 0.5F) * kh - nh2);
				j2 = (int) ((j + 0.5F) * kw - nw2);

				for (int k = 0; k < nh; k++) {
					for (int l = 0; l < nw; l++) {
						idx = (j2 + l) + (i2 + k) * imageWidth;

						if (idx > -1 && idx < pixels.length) {
							argb = pixels[idx];
							v[0] += (argb >> 16) & 0xFF;
							v[1] += (argb >> 8) & 0xFF;
							v[2] += argb & 0xFF;
							v[3] += (argb >> 24) & 0xFF; // alpha
							cnt++;
						}
					}
				}

				outPixels[j + i * width] = ((v[3] / cnt) << 24) | ((v[0] / cnt) << 16) | ((v[1] / cnt) << 8) | (v[2] / cnt);
			}
		}

		BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		out.setRGB(0, 0, width, height, outPixels, 0, width);

		return out;
	}

	/**
	 * get a color in a image, coord can be out of the image size.
	 * 
	 * @param image
	 *            a standard java image
	 * @param x
	 *            if < 0 -> get 0 id > width -> get width-1
	 * @param y
	 *            if < 0 -> get 0 id > length -> get length-1
	 * @return the color of a pixel
	 */
	public static int getColor(BufferedImage image, int x, int y) {
		if (x < 0) {
			return NEUTRAL_COLOR.getRGB();
		} else if (x >= image.getWidth()) {
			x = image.getWidth() - 1;
		}
		if (y < 0) {
			return NEUTRAL_COLOR.getRGB();
		} else if (y >= image.getHeight()) {
			y = image.getHeight() - 1;
		}
		return image.getRGB(x, y);
	}

	private static int getScaleColor(BufferedImage source, BufferedImage target, int x, int y) {
		double xsc = ((source.getWidth()) / ((double) target.getWidth()));
		double ysc = ((source.getHeight()) / ((double) target.getHeight()));

		float r = 0;
		float g = 0;
		float b = 0;

		float scale = 0;

		/** calcul the square * */
		float squareMultiplier = 1f;
		Color color = new Color(getColor(source, (int) (x * xsc - 1), (int) (y * ysc - 1)));
		r = r + color.getRed() * squareMultiplier;
		g = g + color.getGreen() * squareMultiplier;
		b = b + color.getBlue() * squareMultiplier;
		color = new Color(getColor(source, (int) (x * xsc + 1), (int) (y * ysc - 1)));
		r = r + color.getRed() * squareMultiplier;
		g = g + color.getGreen() * squareMultiplier;
		b = b + color.getBlue() * squareMultiplier;
		color = new Color(getColor(source, (int) (x * xsc - 1), (int) (y * ysc + 1)));
		r = r + color.getRed() * squareMultiplier;
		g = g + color.getGreen() * squareMultiplier;
		b = b + color.getBlue() * squareMultiplier;
		color = new Color(getColor(source, (int) (x * xsc + 1), (int) (y * ysc + 1)));
		r = r + color.getRed() * squareMultiplier;
		g = g + color.getGreen() * squareMultiplier;
		b = b + color.getBlue() * squareMultiplier;
		scale = scale + 4 * squareMultiplier;

		/** calcul standard square * */
		color = new Color(getColor(source, (int) (x * xsc), (int) (y * ysc - 1)));
		r = r + color.getRed();
		g = g + color.getGreen();
		b = b + color.getBlue();

		color = new Color(getColor(source, (int) (x * xsc - 1), (int) (y * ysc)));
		r = r + color.getRed();
		g = g + color.getGreen();
		b = b + color.getBlue();

		color = new Color(getColor(source, (int) (x * xsc + 1), (int) (y * ysc)));
		r = r + color.getRed();
		g = g + color.getGreen();
		b = b + color.getBlue();

		color = new Color(getColor(source, (int) (x * xsc), (int) (y * ysc + 1)));
		r = r + color.getRed();
		g = g + color.getGreen();
		b = b + color.getBlue();
		scale = scale + 4;

		/* get the center color */
		color = new Color(getColor(source, (int) (x * xsc), (int) (y * ysc)));
		r = r + color.getRed();
		g = g + color.getGreen();
		b = b + color.getBlue();
		scale = scale + 1;

		return new Color(r / scale / 255, g / scale / 255, b / scale / 255).getRGB();

	}

	public static BufferedImage resizeIn(BufferedImage bi, int width, int height) {
		
		logger.info("resizeIn width:"+width+" height:"+height);

		if (width < 0) {
			return resizeHeight(bi, height, null);
		} else if (height < 0) {
			return resizeWidth(bi, height);
		}

		if ((float) bi.getWidth() / (float) bi.getHeight() > (float) width / (float) height) {
			int newHeight = (bi.getHeight() * width) / bi.getWidth();
			return resize(bi, width, newHeight);
		} else {
			int newWidth = (bi.getWidth() * height) / bi.getHeight();
			return resize(bi, newWidth, height);
		}

	}

	public static BufferedImage resizeHeight(BufferedImage bi, int height, Color bgColor) {
		
		logger.info("resizeWidth height:"+height+" bgColor:"+bgColor);

		if ((float) height / (float) bi.getHeight() < 0.5) {
			bi = lightBlurring(bi);
		}

		int width = Math.round((float) bi.getWidth() * ((float) height / (float) bi.getHeight()));
		height = Math.round((float) bi.getHeight() * ((float) width / (float) bi.getWidth()));

		/*
		 * if (width > height) { width = height; height = Math.round((float) bi.getHeight() ((float) width / (float) bi.getWidth())); }
		 */

		AffineTransform tx = new AffineTransform();
		double xsc = (width) / ((double) bi.getWidth());
		double ysc = (height) / ((double) bi.getHeight());
		tx.scale(xsc, ysc);

		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BICUBIC);

		BufferedImage image = op.filter(bi, null);

		if (bgColor != null && image.getColorModel().hasAlpha()) {
			BufferedImage imgNew;
			if (image.getColorModel().hasAlpha()) {
				imgNew = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
			} else {
				imgNew = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
			}
			for (int x = 0; x < image.getWidth(); x++) {
				for (int y = 0; y < image.getHeight(); y++) {
					int rgb = image.getRGB(x, y);
					imgNew.setRGB(x, y, replaceAlpha(new Color(rgb, true), bgColor).getRGB());
				}
			}
			image = imgNew;
		}
//				if (bgColor != null) {
//					imgNew.setRGB(x, y, replaceAlpha(new Color(rgb, true), bgColor).getRGB());
//				} else {
//					imgNew.setRGB(x, y, rgb);
//				}
//		return imgNew;

		return image;
	}

	public static BufferedImage resizeWidth(BufferedImage bi, int width) {
		return resizeWidth(bi, width, 0, 0, 0, 0, null);
	}

	public static BufferedImage resizeWidth(BufferedImage bi, int width, int mt, int mr, int ml, int mb, Color bgColor) {
		
		logger.info("resizeWidth with:"+width+" bgColor:"+bgColor);
		
		if ((float) width / (float) bi.getWidth() < 0.5) {
			bi = lightBlurring(bi);
		}

		int height = Math.round((float) bi.getHeight() * ((float) width / (float) bi.getWidth()));
		width = Math.round((float) bi.getWidth() * ((float) height / (float) bi.getHeight()));

		/*
		 * if (height > width) { height = width; width = Math.round((float) bi.getWidth() ((float) height / (float) bi.getHeight())); }
		 */

		AffineTransform tx = new AffineTransform();
		double xsc = (width) / ((double) bi.getWidth());
		double ysc = (height) / ((double) bi.getHeight());
		tx.scale(xsc, ysc);

		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BICUBIC);

		BufferedImage image = op.filter(bi, null);

//				if (bgColor != null) {
//					imgNew.setRGB(x, y, replaceAlpha(new Color(rgb, true), bgColor).getRGB());
//				} else {
//					imgNew.setRGB(x, y, rgb);
//				}

		if (bgColor != null && image.getColorModel().hasAlpha() && (mt > 0 || ml > 0 || mr > 0 || mb > 0)) {
			int inWidth = image.getWidth() + ml + mr;
			int inHeight = image.getHeight() + mt + mb;

			BufferedImage outImage = new BufferedImage(inWidth, inHeight, BufferedImage.TYPE_INT_ARGB);
			for (int x = 0; x < inWidth; x++) {
				for (int y = 0; y < inHeight; y++) {
					Color imageColor = bgColor;
					if ((x >= ml) && (x < image.getWidth() + ml) && (y >= mt) && (y < image.getHeight() + mt)) {
						imageColor = new Color(image.getRGB(x - ml, y - mt), true);
					}
					Color mixedColor = replaceAlpha(imageColor, bgColor);
					outImage.setRGB(x, y, mixedColor.getRGB());
				}
			}
//			imgNew = outImage;
			image = outImage;
		}

//		return imgNew;
		return image;
	}

	public static BufferedImage drawBorderCorner(BufferedImage image, int radius, Color bg, int size) {
		BufferedImage imgNew = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				int rgb = image.getRGB(x, y);
				if (PositionHelper.isOutRoundBorder(x, y, image.getWidth(), image.getHeight(), radius)) {
					if (!PositionHelper.isOutRoundBorder(x, y, image.getWidth(), image.getHeight(), radius - size)) {
						rgb = bg.getRGB();
					}
				}
				imgNew.setRGB(x, y, rgb);
			}
		}
		return imgNew;
	}

	static Color mixedColor(int inColor1, int inColor2, float ratioColor1) {
		Color color1 = new Color(inColor1);
		Color color2 = new Color(inColor2);
		return mixedColor(color1, color2, ratioColor1);
	}

	static Color mixedColor(Color color1, Color color2, float ratioColor1) {
		float alpha = (color1.getAlpha() * ratioColor1 + color2.getAlpha() * (1 - ratioColor1));
		float red = (color1.getRed() * ratioColor1 + color2.getRed() * (1 - ratioColor1));
		float green = (color1.getGreen() * ratioColor1 + color2.getGreen() * (1 - ratioColor1));
		float blue = (color1.getBlue() * ratioColor1 + color2.getBlue() * (1 - ratioColor1));
		return new Color(red / 255, green / 255, blue / 255, alpha / 255);
	}

	static Color replaceAlpha(Color color, Color bg) {
		float alpha = (float) (color.getAlpha()) / 255f;
		float red = color.getRed() * alpha + bg.getRed() * (1 - alpha);
		float green = color.getGreen() * alpha + bg.getGreen() * (1 - alpha);
		float blue = color.getBlue() * alpha + bg.getBlue() * (1 - alpha);
		return new Color(red / 255, green / 255, blue / 255);
	}

	static Color randomColor() {
		return new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
	}

	public static BufferedImage borderCorner(BufferedImage image, int radius, Color bg) {
		BufferedImage imgNew = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				int rgb = image.getRGB(x, y);
				if (PositionHelper.isOutRoundBorder(x, y, image.getWidth(), image.getHeight(), radius)) {
					if (PositionHelper.isOutRoundBorder(x, y, image.getWidth(), image.getHeight(), radius - 4)) {
						rgb = bg.getRGB();
					} else if (PositionHelper.isOutRoundBorder(x, y, image.getWidth(), image.getHeight(), radius - 2)) {
						rgb = mixedColor(bg, new Color(rgb), 0.65f).getRGB();
						// rgb = Color.green.getRGB();
					} else {
						rgb = mixedColor(bg, new Color(rgb), 0.35f).getRGB();
						// rgb = Color.blue.getRGB();
					}

				}
				imgNew.setRGB(x, y, rgb);
			}
		}
		return imgNew;
	}

	public static BufferedImage RBGAdjust(BufferedImage image, Color adjustColor) {
		if (adjustColor == null) {
			return image;
		}
		RGBAdjustFilter filter = new RGBAdjustFilter();
		filter.rFactor = (float) adjustColor.getRed() / 255f;
		filter.gFactor = (float) adjustColor.getGreen() / 255f;
		filter.bFactor = (float) adjustColor.getBlue() / 255f;
		return filter.filter(image, null);
	}

	public static BufferedImage borderCorner(BufferedImage image, Color bg) {
		return borderCorner(image, Math.min(image.getHeight(), image.getWidth()) / 4, bg);
	}

	public static BufferedImage replaceAlpha(BufferedImage image, Color bg) {
		if (bg == null) {
			return image;
		}
		BufferedImage imgNew = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				int rgb = image.getRGB(x, y);
				Color color = new Color(rgb, true);
				Color mixedColor = replaceAlpha(color, bg);
				imgNew.setRGB(x, y, mixedColor.getRGB());
			}
		}
		return imgNew;
	}

	public static BufferedImage removeAlpha(BufferedImage image) {
		BufferedImage imgNew = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				int rgb = image.getRGB(x, y);
				imgNew.setRGB(x, y, rgb);
			}
		}
		return imgNew;
	}

	public static BufferedImage applyFilter(BufferedImage source, BufferedImage filter, boolean cropResize, boolean addBorder, int mt, int ml, int mr, int mb, int fzx, int fzy, boolean isFocus, Color bgColor) {

		BufferedImage workImage = null;

		if (bgColor == null) {
			bgColor = Color.WHITE;
		}

		int workWith = filter.getWidth() - (ml + mr);
		int workHeight = filter.getHeight() - (mt + mb);

		if (!cropResize) {
			workImage = resize(source, workWith, workHeight);
		} else {
			workImage = ImageEngine.resize(source, filter.getWidth(), filter.getHeight(), cropResize, addBorder, mt, ml, mr, mb, bgColor, fzx, fzy, isFocus);
			/*
			 * if ((float) source.getWidth() / (float) source.getHeight() < (float) workWith / (float) workHeight) { int height = (source.getHeight() * workWith) / source.getWidth(); workImage = resize(source, workWith, height); } else { int width = (source.getWidth() * workHeight) / source.getHeight(); //workImage = resize(source, width, workHeight);
			 * 
			 * WARNING : test si on fait pas 2x la marge avec cette méthode the resize }
			 */
		}
		BufferedImage outImage = new BufferedImage(filter.getWidth(), filter.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		for (int x = 0; x < filter.getWidth(); x++) {
			for (int y = 0; y < filter.getHeight(); y++) {
				Color filterColor = new Color(filter.getRGB(x, y), true);
				Color imageColor = bgColor;
				if ((x >= ml) && (x < workWith + ml) && (y >= mt) && (y < workHeight + mt)) {
					imageColor = new Color(workImage.getRGB(x - ml, y - mt), true);
				}
				Color mixedColor = replaceAlpha(filterColor, imageColor);
				outImage.setRGB(x, y, mixedColor.getRGB());
			}
		}
		return outImage;
	}

	public static BufferedImage centerInterest(BufferedImage source, int interestX, int interestY, int minWidth, int minHeight) {
		if ((minWidth > source.getWidth()) || (minWidth == 0)) {
			minWidth = source.getWidth();
		}
		if ((minHeight > source.getHeight()) || (minHeight == 0)) {
			minHeight = source.getHeight();
		}
		BufferedImage workImage = null;
		int realInterestX = (interestX * source.getWidth()) / 1000;
		int realInterestY = (interestY * source.getHeight()) / 1000;

		int startX = realInterestX - (source.getWidth() - realInterestX);
		if (startX < 0) {
			startX = 0;
		}
		int endX = realInterestX + realInterestX;
		if (endX > source.getWidth()) {
			endX = source.getWidth();
		}

		int startY = realInterestY - (source.getHeight() - realInterestY);
		if (startY < 0) {
			startY = 0;
		}
		int endY = realInterestY + realInterestY;
		if (endY > source.getHeight()) {
			endY = source.getHeight();
		}

		int width = endX - startX;
		if (minWidth > width) {
			int recupLeft = startX - minWidth / 2;
			if (recupLeft < 0) {
				recupLeft = startX;
				startX = 0;
			} else {
				startX = startX - recupLeft;
			}

			endX = endX + (minWidth - recupLeft);
		}

		int height = endY - startY;
		if (minHeight > height) {
			int recupTop = startY - minHeight / 2;
			if (recupTop < 0) {
				recupTop = startY;
				startY = 0;
			} else {
				startY = startY - recupTop;
			}
			endY = endY + (minHeight - recupTop);
		}

		width = endX - startX;
		height = endY - startY;

		if (width > source.getWidth()) {
			width = source.getWidth();
		}
		if (height > source.getHeight()) {
			height = source.getHeight();
		}

		workImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);

		for (int x = 0; x < workImage.getWidth(); x++) {
			for (int y = 0; y < workImage.getHeight(); y++) {
				workImage.setRGB(x, y, source.getRGB(x + startX, y + startY));
			}
		}

		return workImage;
	}

	/**
	 * resize a picture
	 * 
	 * @param source
	 *            the source image
	 * @param inWidth
	 *            new with
	 * @param inHeight
	 *            new height
	 * @param cropResize
	 *            true if image must be croped or false if image must be deformed
	 * @param mt
	 *            margin top
	 * @param ml
	 *            margin left
	 * @param mr
	 *            margin right
	 * @param mb
	 *            margin bottom
	 * @param bgColor
	 *            background color (for margin and alpha)
	 * @param interestX
	 *            x position of the interest zone in the picture
	 * @param interestY
	 *            y position of the interest zone in the picture
	 * @return a resized image
	 */
	public static BufferedImage resize(BufferedImage source, int inWidth, int inHeight, boolean cropResize, boolean addBorder, int mt, int ml, int mr, int mb, Color bgColor, int interestX, int interestY, boolean focusZone) {
		
		logger.info("resize with:"+inWidth+" height:"+inHeight+" bgColor:"+bgColor);
		
		if (addBorder) {
			int newHeight;
			int newWidth;
			int borderHeight = 0;
			int borderWidth = 0;
			if ((float) source.getWidth() / (float) source.getHeight() > (float) inWidth / (float) inHeight) {
				newWidth = source.getWidth();
				newHeight = (source.getWidth() * inHeight) / inWidth;
				borderHeight = Math.abs((source.getHeight() - newHeight) / 2);
			} else {
				newWidth = (source.getHeight() * inWidth) / inHeight;
				newHeight = source.getHeight();
				borderWidth = Math.abs((source.getWidth() - newWidth) / 2);
			}

			BufferedImage outImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_4BYTE_ABGR);
			if (bgColor != null) {
				for (int x = 0; x < outImage.getWidth(); x++) {
					for (int y = 0; y < outImage.getHeight(); y++) {
						outImage.setRGB(x, y, bgColor.getRGB());
					}
				}
			}

			for (int x = 0; x < source.getWidth(); x++) {
				for (int y = 0; y < source.getHeight(); y++) {
					int color = source.getRGB(x, y);
					outImage.setRGB(x + borderWidth, y + borderHeight, color);
				}
			}
			source = outImage;
		} else if (focusZone) {
			source = centerInterest(source, interestX, interestY, inWidth, inHeight);
		}

		BufferedImage workImage;

		int workWith = inWidth - (ml + mr);
		int workHeight = inHeight - (mt + mb);

		if (!cropResize) {
			workImage = resize(source, workWith, workHeight);
		} else {
			if ((float) source.getWidth() / (float) source.getHeight() < (float) workWith / (float) workHeight) {
				int height = (source.getHeight() * workWith) / source.getWidth();
				workImage = resize(source, workWith, height);
			} else {
				int width = (source.getWidth() * workHeight) / source.getHeight();
				workImage = resize(source, width, workHeight);
			}
		}

		// repositioning the image
		int realInterestX = (interestX * workImage.getWidth()) / 1000;
		int deltaX = realInterestX - inWidth / 2;
		if (deltaX < 0) {
			deltaX = 0;
		} else if (deltaX + inWidth > workImage.getWidth()) {
			deltaX = workImage.getWidth() - inWidth;
		}

		int realInterestY = (interestY * workImage.getHeight()) / 1000;
		int deltaY = realInterestY - inHeight / 2;
		if (deltaY < 0) {
			deltaY = 0;
		} else if (deltaY + inHeight > workImage.getHeight()) {
			deltaY = workImage.getHeight() - inHeight;
		}

		BufferedImage outImage = new BufferedImage(inWidth, inHeight, BufferedImage.TYPE_4BYTE_ABGR);
		for (int x = deltaX; x < inWidth + deltaX; x++) {
			for (int y = deltaY; y < inHeight + deltaY; y++) {
				int targetY = y - deltaY;
				int targetX = x - deltaX;
				Color imageColor = bgColor;
				if ((targetX >= ml) && (targetX < workWith + ml) && (targetY >= mt) && (targetY < workHeight + mt)) {
					imageColor = new Color(workImage.getRGB(x - ml, y - mt), true);
				}
				Color mixedColor = imageColor;
				if (bgColor != null) {
					mixedColor = replaceAlpha(imageColor, bgColor);
				}
				if (mixedColor != null) {
					outImage.setRGB(targetX, targetY, mixedColor.getRGB());
				}
			}
		}
		return outImage;
	}

	public static BufferedImage applyBgColor(BufferedImage image, Color bgColor) {
		org.javlo.helper.Logger.stepCount("transform", "start - transformation - 2.0.1");
		BufferedImage outImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		org.javlo.helper.Logger.stepCount("transform", "start - transformation - 2.0.2");
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				Color filterColor = new Color(image.getRGB(x, y), true);
				if (filterColor.getAlpha() < 255) {
					filterColor = replaceAlpha(filterColor, bgColor);
				}
				outImage.setRGB(x, y, filterColor.getRGB());
			}
		}
		org.javlo.helper.Logger.stepCount("transform", "start - transformation - 2.0.3");
		return outImage;
	}

	private static int makeARGB(int a, int r, int g, int b) {
		return a << 24 | r << 16 | g << 8 | b;
	}

	public static BufferedImage resizeImage(BufferedImage in, int width, int height) throws IOException {
		
		logger.info("resizeImage with:"+width+" height:"+height);

		int imageWidth = in.getWidth();
		int imageHeight = in.getHeight();

		int[] pixels = in.getRGB(0, 0, imageWidth, imageHeight, null, 0, imageWidth);
		int[] outPixels = new int[width * height];

		int nw = Math.max(imageWidth / width + 1, 2);
		int nh = Math.max(imageHeight / height + 1, 2);

		int idx, i2, j2, argb, cnt;
		int[] v = new int[3];

		float kw = (float) imageWidth / (float) width;
		float kh = (float) imageHeight / (float) height;

		float nw2 = nw / 2.0F;
		float nh2 = nh / 2.0F;

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				v[0] = v[1] = v[2] = cnt = 0;

				i2 = (int) ((i + 0.5F) * kh - nh2);
				j2 = (int) ((j + 0.5F) * kw - nw2);

				for (int k = 0; k < nh; k++) {
					for (int l = 0; l < nw; l++) {
						idx = (j2 + l) + (i2 + k) * imageWidth;

						if (idx > -1 && idx < pixels.length) {
							argb = pixels[idx];
							v[0] += (argb >> 16) & 0xFF;
							v[1] += (argb >> 8) & 0xFF;
							v[2] += argb & 0xFF;
							cnt++;
						}
					}
				}

				outPixels[j + i * width] = 0xFF000000 | ((v[0] / cnt) << 16) | ((v[1] / cnt) << 8) | (v[2] / cnt);
			}
		}

		BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		outputImage.setRGB(0, 0, width, height, outPixels, 0, width);

		return outputImage;
	}

	public static BufferedImage web2(BufferedImage image, Color bgColor, int height, int separation) {

		if ((height < 1) || (height > image.getHeight())) {
			height = image.getHeight();
		}

		BufferedImage outImage = new BufferedImage(image.getWidth(), image.getHeight() + height + separation, BufferedImage.TYPE_4BYTE_ABGR);
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				Color filterColor = new Color(image.getRGB(x, y), true);
				outImage.setRGB(x, y, filterColor.getRGB());
			}
		}
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = image.getHeight(); y < image.getHeight() + separation; y++) {
				if (bgColor != null) {
					outImage.setRGB(x, y, bgColor.getRGB());
				} else {
					outImage.setRGB(x, y, makeARGB(0, 0, 0, 0));
				}
			}
		}
		float alpha;
		float step = (float) 150 / (float) height;
		for (int x = 0; x < image.getWidth(); x++) {
			alpha = 150;
			for (int y = image.getHeight() + separation; y < image.getHeight() + height + separation; y++) {
				alpha = alpha - step;
				Color c = new Color(image.getRGB(x, 2 * image.getHeight() - 1 - (y - separation)), true);
				int color = makeARGB(Math.round(alpha), c.getRed(), c.getGreen(), c.getBlue());
				if (bgColor != null) {
					outImage.setRGB(x, y, replaceAlpha(new Color(color, true), bgColor).getRGB());
				} else {
					outImage.setRGB(x, y, color);
				}
			}
		}
		return outImage;
	}

	public static void compressJpegFile(BufferedImage image, OutputStream out, double compressionQuality) {
		try {

			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(image);
			param.setQuality((float) compressionQuality, false); // 90%
			// quality
			// JPEG
			encoder.setJPEGEncodeParam(param);
			encoder.encode(image);
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			System.out.println("start...");
			BufferedImage image = ImageIO.read(source);
			image = resizeImage(image, 300, 420);
			// image = lightBlurring(image);
			// image = blurring(image);
			// image = applyFilter(image,filterImage, true, 50,50,50,50,null);
			// image = drawBorderCorner (image, Math.min(image.getHeight(),
			// image.getWidth())/4, Color.RED, 50);

			ImageIO.write(image, "png", target);
			System.out.println("end.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
