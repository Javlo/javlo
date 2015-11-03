package org.javlo.image;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.IndexColorModel;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

import org.imgscalr.Scalr;
import org.javlo.helper.StringHelper;

import com.jhlabs.image.RGBAdjustFilter;

public class ImageEngine {

	private static Logger logger = Logger.getLogger(ImageEngine.class.getName());

	public final static Color NEUTRAL_COLOR = new Color(0.5f, 0.5f, 0.5f);
	
	public final static Color TRANSPARENT_COLOR = new Color(0,0,0,1);

	// This class overrides the setCompressionQuality() method to workaround
	// a problem in compressing JPEG images using the javax.imageio package.
	private static class MyImageWriteParam extends JPEGImageWriteParam {
		public MyImageWriteParam() {
			super(Locale.getDefault());
		}

		/*
		 * public void setCompressionQuality(float quality) { if (quality < 0.0F
		 * || quality > 1.0F) { throw new
		 * IllegalArgumentException("Quality out-of-bounds!"); }
		 * this.compressionQuality = 256 - (quality 256); }
		 */

		public void setCompressionQuality(int quality) {
			this.compressionQuality = quality;
		}
	}

	public static BufferedImage loadImage(File file) throws IOException {
		BufferedImage outImage = ImageIO.read(file);
		return outImage;
	}

	public static void storeImage(BufferedImage img, File file) throws IOException {
		ImageIO.write(img, StringHelper.getFileExtension(file.getName()).toLowerCase(), file);
		return;
	}

	public static BufferedImage blurring(BufferedImage img) {
		/*
		 * float[] matrix = { 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,
		 * 0f, 0f, 0f, 0f, };
		 */
		float[] matrix = new float[400];
		for (int i = 0; i < matrix.length; i++) {
			matrix[i] = (1f / matrix.length);
		}

		BufferedImage target = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

		BufferedImageOp op = new ConvolveOp(new Kernel(20, 20, matrix));
		return op.filter(img, target);
	}
	
	 private static int colorToRGB(int alpha, int red, int green, int blue) {
         int newPixel = 0;
         newPixel += alpha;
         newPixel = newPixel << 8;
         newPixel += red; newPixel = newPixel << 8;
         newPixel += green; newPixel = newPixel << 8;
         newPixel += blue;
         return newPixel;
     }
	
	// The luminance method
	public static BufferedImage luminosity(BufferedImage original) {
	 
	    int alpha, red, green, blue;
	    int newPixel;
	 
	    BufferedImage lum = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
	 
	    for(int i=0; i<original.getWidth(); i++) {
	        for(int j=0; j<original.getHeight(); j++) {
	 
	            // Get pixels by R, G, B
	            alpha = new Color(original.getRGB(i, j)).getAlpha();
	            red = new Color(original.getRGB(i, j)).getRed();
	            green = new Color(original.getRGB(i, j)).getGreen();
	            blue = new Color(original.getRGB(i, j)).getBlue();
	 
	            red = (int) (0.21 * red + 0.71 * green + 0.07 * blue);
	            // Return back to original format
	            newPixel = colorToRGB(alpha, red, red, red);
	 
	            // Write pixels into image
	            lum.setRGB(i, j, newPixel);
	 
	        }
	    }
	 
	    return lum;
	 
	}
	
	// The average grayscale method
	public static BufferedImage avg(BufferedImage original) {
	 
	    int alpha, red, green, blue;
	    int newPixel;
	 
	    BufferedImage avg_gray = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
	    int[] avgLUT = new int[766];
	    for(int i=0; i<avgLUT.length; i++) avgLUT[i] = (int) (i / 3);
	 
	    for(int i=0; i<original.getWidth(); i++) {
	        for(int j=0; j<original.getHeight(); j++) {
	 
	            // Get pixels by R, G, B
	            alpha = new Color(original.getRGB(i, j)).getAlpha();
	            red = new Color(original.getRGB(i, j)).getRed();
	            green = new Color(original.getRGB(i, j)).getGreen();
	            blue = new Color(original.getRGB(i, j)).getBlue();
	 
	            newPixel = red + green + blue;
	            newPixel = avgLUT[newPixel];
	            // Return back to original format
	            newPixel = colorToRGB(alpha, newPixel, newPixel, newPixel);
	 
	            // Write pixels into image
	            avg_gray.setRGB(i, j, newPixel);
	 
	        }
	    }
	 
	    return avg_gray;
	 
	}
	
	// The desaturation method
	public static BufferedImage desaturation(BufferedImage original) {
	 
	    int alpha, red, green, blue;
	    int newPixel;
	 
	    int[] pixel = new int[3];
	 
	    BufferedImage des = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
	    int[] desLUT = new int[511];
	    for(int i=0; i<desLUT.length; i++) desLUT[i] = (int) (i / 2);
	 
	    for(int i=0; i<original.getWidth(); i++) {
	        for(int j=0; j<original.getHeight(); j++) {
	 
	            // Get pixels by R, G, B
	            alpha = new Color(original.getRGB(i, j)).getAlpha();
	            red = new Color(original.getRGB(i, j)).getRed();
	            green = new Color(original.getRGB(i, j)).getGreen();
	            blue = new Color(original.getRGB(i, j)).getBlue();
	 
	            pixel[0] = red;
	            pixel[1] = green;
	            pixel[2] = blue;
	 
	            int newval = (int) (findMax(pixel) + findMin(pixel));
	            newval = desLUT[newval];
	 
	            // Return back to original format
	            newPixel = colorToRGB(alpha, newval, newval, newval);
	 
	            // Write pixels into image
	            des.setRGB(i, j, newPixel);
	 
	        }
	    }
	 
	    return des;
	 
	}    
	 
	private static int findMin(int[] pixel) {
	 
	    int min = pixel[0];
	 
	    for(int i=0; i<pixel.length; i++) {
	        if(pixel[i] < min)
	                min = pixel[i];
	    }
	 
	    return min;
	 
	}
	 
	private static int findMax(int[] pixel) {
	 
	    int max = pixel[0];
	 
	    for(int i=0; i<pixel.length; i++) {
	        if(pixel[i] > max)
	                max = pixel[i];
	    }
	 
	    return max;
	 
	}

	public static BufferedImage lightBlurring(BufferedImage img) {

		float[] matrix = new float[9];
		for (int i = 0; i < matrix.length; i++) {
			matrix[i] = (1f / matrix.length);
		}

		BufferedImage target = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());

		BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, matrix));
		return op.filter(img, target);
	}

	public static BufferedImage resize(BufferedImage bi, Integer width, Integer height, boolean hq) {		
		return scale(bi,width,height,hq);
	}
	
	public static BufferedImage zoom(BufferedImage img, double zoom, int interestX, int interestY) {
		
		int realInterestX = (interestX * img.getWidth()) / 1000;
		int realInterestY = (interestY * img.getHeight()) / 1000;
		
		if (zoom < 1) {
			return img;
		}
		int topX = (int)Math.round(realInterestX-realInterestX/zoom);
		int bottomX = (int)Math.round(realInterestX+(img.getWidth()-realInterestX)/zoom);
		
		int topY = (int)Math.round(realInterestY-realInterestY/zoom);
		int bottomY = (int)Math.round(realInterestY+(img.getHeight()-realInterestY)/zoom);
		
		return cropImage(img, bottomX-topX, bottomY-topY, topX, topY);
	}
	
	public static BufferedImage scale(BufferedImage img, Integer inTargetWidth, Integer inTargetHeight, boolean hq) {
		if (hq) {		
			return Scalr.resize(img, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH,inTargetWidth, inTargetHeight);
		} else {
			return Scalr.resize(img, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_TO_WIDTH,inTargetWidth, inTargetHeight);
		}		
	}
	
	public static BufferedImage _scale(BufferedImage img, Integer inTargetWidth, Integer inTargetHeight) {
				
		if (inTargetWidth == null && inTargetHeight == null) {
			return img;			
		}
		
		int targetWidth;
		if (inTargetWidth == null) {
			targetWidth = (img.getWidth()*inTargetHeight)/img.getHeight();
		} else {
			targetWidth = inTargetWidth;
		}
		
		int targetHeight;
		if (inTargetHeight == null) {
			targetHeight = (img.getHeight()*targetWidth)/img.getWidth();
		} else {
			targetHeight = inTargetHeight;
		}

	    int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
	    BufferedImage ret = img;
	    BufferedImage scratchImage = null;
	    Graphics2D g2 = null;

	    int w = img.getWidth();
	    int h = img.getHeight();

	    int prevW = w;
	    int prevH = h;

	    do {
	        if (w > targetWidth) {
	            w /= 2;	            
	        }
	        w = (w < targetWidth) ? targetWidth : w;

	        if (h > targetHeight) {
	            h /= 2;	            
	        }
	        h = (h < targetHeight) ? targetHeight : h;

	        if (scratchImage == null) {
	            scratchImage = new BufferedImage(w, h, type);
	            g2 = scratchImage.createGraphics();
	        }

	        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	        g2.drawImage(ret, 0, 0, w, h, 0, 0, prevW, prevH, null);

	        prevW = w;
	        prevH = h;
	        ret = scratchImage;
	    } while (w != targetWidth || h != targetHeight);

	    if (g2 != null) {
	        g2.dispose();
	    }

	    if (targetWidth != ret.getWidth() || targetHeight != ret.getHeight()) {
	        scratchImage = new BufferedImage(targetWidth, targetHeight, type);
	        g2 = scratchImage.createGraphics();
	        g2.drawImage(ret, 0, 0, null);
	        g2.dispose();
	        ret = scratchImage;
	    }

	    return ret;

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

		if (bi.getType() == BufferedImage.TYPE_4BYTE_ABGR) {			
			BufferedImage imgNew = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			for (int x = 0; x < image.getWidth(); x++) {
				for (int y = 0; y < image.getHeight(); y++) {
					int rgb = image.getRGB(x, y);
					imgNew.setRGB(x, y, rgb);
				}
			}
			return imgNew;
		} else {
			return image;
		}

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

		BufferedImage out;
		if (in.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
			out = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		} else {
			out = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		}
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
	
	/**
	 * if color null return transparent.
	 * @param color
	 * @return
	 */
	public static Color neverNullColor(Color color) {
		if (color != null) {
			return color;
		} else { 
			return TRANSPARENT_COLOR;
		}
	}
	
	public static int getColor(BufferedImage image, int x, int y, Color outCol) {
		if (outCol == null) {
			outCol = TRANSPARENT_COLOR;
		}
		if (x < 0) {
			return outCol.getRGB();
		} else if (x >= image.getWidth()) {
			return outCol.getRGB();
		}
		if (y < 0) {
			return outCol.getRGB();
		} else if (y >= image.getHeight()) {
			return outCol.getRGB();
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

	public static BufferedImage resizeHeight(BufferedImage bi, int height, Color bgColor, boolean hq) {
		int width = Math.round(bi.getWidth() * ((float) height / (float) bi.getHeight()));
		height = Math.round(bi.getHeight() * ((float) width / (float) bi.getWidth()));
		BufferedImage image = resize(bi, width, height,hq);

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
		return image;
	}

	public static BufferedImage resizeWidth(BufferedImage bi, int width, boolean hq) {
		return resizeWidth(bi, width, 0, 0, 0, 0, null, hq);
	}

	public static BufferedImage resizeWidth(BufferedImage bi, int width, int mt, int mr, int ml, int mb, Color bgColor, boolean hq) {
		int height = Math.round(bi.getHeight() * ((float) width / (float) bi.getWidth()));
		width = Math.round(bi.getWidth() * ((float) height / (float) bi.getHeight()));
		BufferedImage image = resize(bi, width, height, hq);

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
			image = outImage;
		} 
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
		float alpha = (color.getAlpha()) / 255f;

		float red = color.getRed() * alpha + bg.getRed() * (1 - alpha);
		float green = color.getGreen() * alpha + bg.getGreen() * (1 - alpha);
		float blue = color.getBlue() * alpha + bg.getBlue() * (1 - alpha);
		return new Color(red / 255, green / 255, blue / 255);
	}
	
	static Color replaceAlpha(int color, int bg) {
		return replaceAlpha(new Color(color), new Color(bg));
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
		filter.rFactor = adjustColor.getRed() / 255f;
		filter.gFactor = adjustColor.getGreen() / 255f;
		filter.bFactor = adjustColor.getBlue() / 255f;
		return filter.filter(image, null);
	}

	public static BufferedImage borderCorner(BufferedImage image, Color bg) {
		return borderCorner(image, Math.min(image.getHeight(), image.getWidth()) / 4, bg);
	}

	public static BufferedImage replaceAlpha(BufferedImage image, Color bg) {
		if (bg == null) {
			return image;
		}
		BufferedImage imgNew = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
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

	/**
	 * replace bg color with transparency
	 * 
	 * @param image
	 * @param bg
	 * @return image with transparency
	 */
	public static BufferedImage createAlpha(BufferedImage image, Color bg) {
		if (bg == null) {
			return image;
		}
		BufferedImage imgNew = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				int rgb = image.getRGB(x, y);
				Color color = new Color(rgb);
				if (color.getRed() == bg.getRed() && color.getGreen() == bg.getGreen() && color.getBlue() == bg.getBlue()) {
					color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
				}

				imgNew.setRGB(x, y, color.getRGB());
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

	public static BufferedImage applyFilter(BufferedImage source, BufferedImage filter, boolean cropResize, boolean addBorder, int mt, int ml, int mr, int mb, int fzx, int fzy, boolean isFocus, Color bgColor, boolean hq) {

		BufferedImage workImage = null;

		if (bgColor == null) {
			bgColor = Color.WHITE;
		}

		int workWith = filter.getWidth() - (ml + mr);
		int workHeight = filter.getHeight() - (mt + mb);

		if (!cropResize) {
			workImage = resize(source, workWith, workHeight, hq);
		} else {
			workImage = ImageEngine.resize(source, filter.getWidth(), filter.getHeight(), cropResize, addBorder, mt, ml, mr, mb, bgColor, fzx, fzy, isFocus, hq);
			/*
			 * if ((float) source.getWidth() / (float) source.getHeight() <
			 * (float) workWith / (float) workHeight) { int height =
			 * (source.getHeight() * workWith) / source.getWidth(); workImage =
			 * resize(source, workWith, height); } else { int width =
			 * (source.getWidth() * workHeight) / source.getHeight();
			 * //workImage = resize(source, width, workHeight);
			 * 
			 * WARNING : test si on fait pas 2x la marge avec cette méthode the
			 * resize }
			 */
		}
		BufferedImage outImage = new BufferedImage(filter.getWidth(), filter.getHeight(), source.getType());
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

		workImage = new BufferedImage(width, height, source.getType());

		for (int x = 0; x < workImage.getWidth(); x++) {
			for (int y = 0; y < workImage.getHeight(); y++) {
				workImage.setRGB(x, y, source.getRGB(x + startX, y + startY));
			}
		}

		return workImage;
	}
	
	private static final void fillImage(BufferedImage image, Color color) {
		Graphics2D graphics = image.createGraphics();
		graphics.setPaint (color );
		graphics.fillRect ( 0, 0, image.getWidth(), image.getHeight() );
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
	 *            true if image must be croped or false if image must be
	 *            deformed
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
	public static BufferedImage resize(BufferedImage source, int inWidth, int inHeight, boolean cropResize, boolean addBorder, int mt, int ml, int mr, int mb, Color bgColor, int interestX, int interestY, boolean focusZone, boolean hq) {

		logger.fine("resize with:" + inWidth + " height:" + inHeight + " bgColor:" + bgColor);

		if (inWidth < 0) {
			inWidth = Math.abs(source.getWidth() * inHeight / source.getHeight());
		} else if (inHeight < 0) {
			inHeight = Math.abs(source.getHeight() * inWidth / source.getWidth());
		}

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

			BufferedImage outImage = new BufferedImage(newWidth, newHeight, source.getType());
			if (bgColor != null) {
				fillImage(outImage, bgColor);
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
			workImage = resize(source, workWith, workHeight, hq);			
		} else {
			if ((float) source.getWidth() / (float) source.getHeight() < (float) workWith / (float) workHeight) {
				int height = (source.getHeight() * workWith) / source.getWidth();
				workImage = resize(source, workWith, height, hq);
			} else {
				int width = (source.getWidth() * workHeight) / source.getHeight();
				workImage = resize(source, width, workHeight, hq);
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

		BufferedImage outImage = new BufferedImage(inWidth, inHeight, source.getType());
		for (int x = deltaX; x < inWidth + deltaX; x++) {
			for (int y = deltaY; y < inHeight + deltaY; y++) {
				int targetY = y - deltaY;
				int targetX = x - deltaX;
				Integer imageColor = null;
				if (bgColor != null) {
					imageColor = bgColor.getRGB();
				}
				if ((targetX >= ml) && (targetX < workWith + ml) && (targetY >= mt) && (targetY < workHeight + mt)) {					
					imageColor =  getColor(workImage, x+ml, y+mt, bgColor);
				}
				Integer mixedColor = imageColor;
				if (bgColor != null) {
					mixedColor = replaceAlpha(new Color(imageColor), bgColor).getRGB();
				}
				if (mixedColor != null) {
					outImage.setRGB(targetX, targetY, mixedColor);
				}
			}
		}
		
		return outImage;
	}
	
	public static void insertImage(BufferedImage source, BufferedImage target, int posx, int posy) {
		for (int x = 0; x < source.getWidth(); x++) {
			for (int y = 0; y < source.getHeight(); y++) {
				int rgb = source.getRGB(x, y);
				target.setRGB(posx+x, posy+y, rgb);					
			}
		}
	}

	public static BufferedImage applyBgColor(BufferedImage image, Color bgColor) {
		org.javlo.helper.LocalLogger.stepCount("transform", "start - transformation - 2.0.1");
		BufferedImage outImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		org.javlo.helper.LocalLogger.stepCount("transform", "start - transformation - 2.0.2");
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				Color filterColor = new Color(image.getRGB(x, y), true);
				if (filterColor.getAlpha() < 255) {
					filterColor = replaceAlpha(filterColor, bgColor);
				}
				outImage.setRGB(x, y, filterColor.getRGB());
			}
		}
		org.javlo.helper.LocalLogger.stepCount("transform", "start - transformation - 2.0.3");
		return outImage;
	}

	private static int makeARGB(int a, int r, int g, int b) {
		return a << 24 | r << 16 | g << 8 | b;
	}

	public static BufferedImage resizeImage(BufferedImage in, int width, int height) throws IOException {

		logger.fine("resizeImage with:" + width + " height:" + height);

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

	public static BufferedImage cropImage(BufferedImage image, int width, int height, int inX, int inY) {
		BufferedImage outImage = new BufferedImage(width, height, image.getType());
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				if (x >= inX && x < inX + width) {
					if (y >= inY && y < inY + height) {
						outImage.setRGB(x - inX, y - inY, image.getRGB(x, y));
					}
				}
			}
		}
		return outImage;
	}

	public static void compressJpegFile(BufferedImage image, OutputStream out, double compressionQuality) {
		Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
		if (iter.hasNext()) {
			ImageWriter writer = iter.next();
			ImageWriteParam param = writer.getDefaultWriteParam();

			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality((float) compressionQuality);

			writer.setOutput(out);
			IIOImage img = new IIOImage(image, null, null);
			try {
				writer.write(null, img, param);
			} catch (IOException e) {
				e.printStackTrace();
			}
			writer.dispose();
		}
	}

	/**
	 * rotate the image with angle in degree. note than result can be bigger
	 * than source.
	 * 
	 * @param image
	 * @param angle
	 * @return
	 */
	public static BufferedImage rotate(BufferedImage image, int angle, Color bg) {

		int targetType = image.getType();
		if (bg != null) {
			targetType = BufferedImage.TYPE_3BYTE_BGR;
		}

		double radAngle = Math.toRadians(angle);
		double cos = Math.cos(radAngle);
		double sin = Math.sin(radAngle);
		int newWidth = (int) Math.round(Math.abs(image.getWidth() * cos) + Math.abs(image.getHeight() * sin));
		int newHeight = (int) Math.round(Math.abs(image.getWidth() * sin) + Math.abs(image.getHeight() * cos));

		int maxBorder = Math.max(image.getHeight(), image.getWidth());

		BufferedImage outImage = new BufferedImage(maxBorder * 2, maxBorder * 2, targetType);

		Graphics2D graphics = outImage.createGraphics();
		if (bg != null) {
			graphics.setColor(bg);
			graphics.fillRect(0, 0, outImage.getWidth(), outImage.getHeight());
		}

		BufferedImage workImage = new BufferedImage(maxBorder * 2, maxBorder * 2, targetType);

		Graphics2D wgr = workImage.createGraphics();
		wgr.drawImage(image, workImage.getWidth() / 2 - image.getWidth() / 2, workImage.getHeight() / 2 - image.getHeight() / 2, null);

		// Rotation information
		AffineTransform tx = AffineTransform.getRotateInstance(radAngle, maxBorder, maxBorder);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

		// Drawing the rotated image at the required drawing locations
		graphics.drawImage(op.filter(workImage, null), 0, 0, null);

		int startX = (outImage.getWidth() - newWidth) / 2;
		int startY = (outImage.getHeight() - newHeight) / 2;

		return outImage.getSubimage(startX, startY, newWidth, newHeight);
	}

	/**
	 * create transparent dash on 1 pixel on 2
	 * 
	 * @param image
	 * @return a image width same width and same height.
	 */
	public static BufferedImage dashed(BufferedImage image, int size) {
		BufferedImage outImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				if (x % size == 0 && y % size == 0) {
					outImage.setRGB(x, y, image.getRGB(x, y));
				}
			}
		}
		return outImage;
	}
	
	/**
	 * create transparent dash
	 * 
	 * @param image
	 * @return a image width same width and same height.
	 */
	public static BufferedImage resizeDashed(BufferedImage image, int factor) {
		BufferedImage outImage = new BufferedImage(image.getWidth()*factor, image.getHeight()*factor, BufferedImage.TYPE_4BYTE_ABGR);
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				outImage.setRGB(x*factor, y*factor, image.getRGB(x, y));				
			}
		}
		return outImage;
	}
	
	public static BufferedImage grayscale(BufferedImage inImage) {
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);  
		ColorConvertOp op = new ColorConvertOp(cs, null);  
		return op.filter(inImage, null); 
	}
	
	/**
	 * the "mathematical" distance between two color.
	 * @param c1
	 * @param c2
	 * @return
	 */
	public static int getColorDistance(Color c1, Color c2) {
		return Math.abs(c1.getRed()-c2.getRed())+Math.abs(c1.getGreen()-c2.getGreen())+Math.abs(c1.getBlue()-c2.getBlue());
	}
	
	/**
	 * search a color inside a image.
	 * @param image
	 * @param color
	 * @return the distance from all pixels to color (0:no difference (all pixel are the same and is exactly the color value) 1 : max difference (image in black and color is white)).
	 */
	public static double closeColor(BufferedImage image, Color color) {		
		double colorDistance = 0;		
		int STEP = 20;
		if (image.getWidth()*4<STEP || image.getHeight()*4<STEP) {
			STEP = 1;
		}
		int maxDistance = getColorDistance(Color.WHITE, Color.BLACK);
		int imageSize = ((STEP+1)*(STEP+1));
		int c = 0;		
		for (int x = 0; x < image.getWidth(); x+=image.getWidth()/STEP) {
			for (int y = 0; y < image.getHeight(); y+=image.getHeight()/STEP) {
				Color imageColor = new Color(image.getRGB(x, y));				
				colorDistance = colorDistance + ((double)getColorDistance(color,imageColor)/maxDistance)/imageSize;
				c++;
			}
		}		
		return colorDistance;
	}
	
	/**
	*
	* @param img Image to modify
	* @param sepiaIntensity From 0-255, 30 produces nice results
	* @throws Exception
	*/
	public static void applySepiaFilter(BufferedImage img, int sepiaIntensity) {
	    // Play around with this. 20 works well and was recommended
	    // by another developer. 0 produces black/white image
	    int sepiaDepth = 20;

	    int w = img.getWidth();
	    int h = img.getHeight();

	    WritableRaster raster = img.getRaster();

	    // We need 3 integers (for R,G,B color values) per pixel.
	    int[] pixels = new int[w*h*3];
	    raster.getPixels(0, 0, w, h, pixels);

	    // Process 3 ints at a time for each pixel.
	    // Each pixel has 3 RGB colors in array
	    for (int i=0;i<pixels.length; i+=3) {
	        int r = pixels[i];
	        int g = pixels[i+1];
	        int b = pixels[i+2];

	        int gry = (r + g + b) / 3;
	        r = g = b = gry;
	        r = r + (sepiaDepth * 2);
	        g = g + sepiaDepth;

	        if (r>255) r=255;
	        if (g>255) g=255;
	        if (b>255) b=255;

	        // Darken blue color to increase sepia effect
	        b-= sepiaIntensity;

	        // normalize if out of bounds
	        if (b<0) b=0;
	        if (b>255) b=255;

	        pixels[i] = r;
	        pixels[i+1]= g;
	        pixels[i+2] = b;
	    }
	    raster.setPixels(0, 0, w, h, pixels);
	}
	
	public static BufferedImage flip(BufferedImage image, boolean verticaly) {
		BufferedImage outImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		for (int x = 0; x < image.getWidth(); x+=1) {
			for (int y = 0; y < image.getHeight(); y+=1) {
				if (verticaly) {
					outImage.setRGB(x, image.getHeight()-y-1, image.getRGB(x, y));
				} else {
					outImage.setRGB(image.getWidth()-x-1, y, image.getRGB(x, y));
				}				
			}
		}		
		return outImage;
	}

	
	public static void main(String[] args) throws Exception {
		File source = new File("C:/trans/test.jpg");
		File target = new File("c:/trans/out.png");

	    BufferedImage src = luminosity(ImageIO.read(source));
	    ImageIO.write(src, "png", target);
	}

	public static BufferedImage convertRGBAToIndexed(BufferedImage src) {
	    BufferedImage outImage = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_INDEXED);
	    Graphics g = outImage.getGraphics();
	    g.setColor(new Color(231, 20, 189));
	    g.fillRect(0, 0, outImage.getWidth(), outImage.getHeight());
	    outImage = makeTransparent(outImage, 0, 0);
	    outImage.createGraphics().drawImage(src, 0, 0, null);
	    return outImage;
	}

	private static BufferedImage makeTransparent(BufferedImage image, int x, int y) {
	    ColorModel cm = image.getColorModel();
	    if (!(cm instanceof IndexColorModel)) {
	        return image;
	    }
	    IndexColorModel icm = (IndexColorModel) cm;
	    WritableRaster raster = image.getRaster();
	    int pixel = raster.getSample(x, y, 0); // pixel is offset in ICM's palette
	    int size = icm.getMapSize();
	    byte[] reds = new byte[size];
	    byte[] greens = new byte[size];
	    byte[] blues = new byte[size];
	    icm.getReds(reds);
	    icm.getGreens(greens);
	    icm.getBlues(blues);
	    IndexColorModel icm2 = new IndexColorModel(8, size, reds, greens, blues, pixel);
	    return new BufferedImage(icm2, raster, image.isAlphaPremultiplied(), null);
	}
}
