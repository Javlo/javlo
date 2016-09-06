/*
 * Created on 8 oct. 2003
 */
package org.javlo.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.javlo.component.core.IImageFilter;
import org.javlo.helper.LangHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.rendering.Device;
import org.javlo.template.Template;

/**
 * @author pvanderm
 */
public class ImageHelper {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ImageHelper.class.getName());

	public static final int NO_FILTER = 0;

	public static final int BACK_AND_WHITE_FILTER = 1;

	static final int MAX_STRING_SIZE = 18;

	static final String DB_IMAGE_NAME = "_dir_cache.jpg";

	static final Color BACK_GROUND_DIR_COLOR = Color.BLACK;

	static final int BORDER = 4;

	// static final Color BG_TITLE_COLOR = Color.decode("#99ccff");
	static final Color BG_TITLE_COLOR = Color.GRAY;

	static final Color TEXT_TITLE_COLOR = Color.BLACK;

	static final String DIR_DIR = "dir_images";

	static final float DIR_IMAGE_QUALITY = (float) 0.95;

	public static String createSpecialDirectory(int width) {
		return createSpecialDirectory(width, 0);
	}

	public static String createSpecialDirectory(Device device, String context, String filter, String area, String deviceCode, Template template, IImageFilter comp, ImageConfig.ImageParameters param) {
		context = StringHelper.createFileName(context);
		String pageIndice = "";
		if (param.getPage() > 1) {
			pageIndice = "page_" + param.getPage() + "/";
		}
		String out = context + '/' + filter + '/' + deviceCode + '/' + area + '/' + pageIndice;
		if (param.isLowDef()) {
			out = URLHelper.mergePath(out, "low") + "/";
		}

		if (template == null) {
			out += Template.EDIT_TEMPLATE_CODE;
		} else {
			out += template.getId();
		}
		String compFilterKey = null;
		if (comp != null) {
			compFilterKey = StringHelper.trimAndNullify(comp.getImageFilterKey(device));
		}
		if (compFilterKey == null) {
			out += "/none";
		} else {
			out += "/" + StringHelper.createFileName(compFilterKey);
		}

		return out;
	}

	public static String createSpecialDirectory(int width, int filter) {
		return "width_" + width + "_filter_" + filter;
	}

	/**
	 * transform a path in a string to a key. this key can be a directory name (
	 * sp. replace / and \ with _ ).
	 * 
	 * @param path
	 *            a path to a file
	 * @return a key can be a directory name.
	 */
	public static String pathToKey(String path) {
		// String res = path.replaceAll("\\\\|/|:| ", "_");
		String res = path.replaceAll("\\\\ ", "/");
		res = res.replaceAll(":", "_");
		return res;
	}

	public static BufferedImage createAbsoluteLittleImage(ServletContext servletContext, String name, int width) throws IOException {
		BufferedImage image = null;
		InputStream in = new FileInputStream(name);
		try {
			image = ImageIO.read(in);
			if (image != null) {
				image = resize(image, width);
			} else {
				logger.severe("error with file '" + name + "' when resizing.");
			}
		} catch (Throwable t) {
			logger.severe(t.getMessage());
		} finally {
			ResourceHelper.closeResource(in);
		}
		return image;
	}

	public static BufferedImage loadImage(ServletContext servletContext, String name) throws IOException {
		BufferedImage image = null;
		InputStream in = servletContext.getResourceAsStream(name);
		if (in != null) {
			try {
				image = ImageIO.read(in);
			} finally {
				ResourceHelper.closeResource(in);
			}
		} else {
			logger.warning("file not found : " + name);
		}
		return image;
	}

	public static BufferedImage createLittleImage(ServletContext servletContext, String name, int width) throws IOException {
		BufferedImage image = loadImage(servletContext, name);
		if (image != null) {
			image = resize(image, width);
		} else {
			logger.severe("error with file '" + name + "' when resizing.");
		}

		return image;
	}

	public static BufferedImage resize(BufferedImage aOrgImage, int width) {
		double scale;

		if (aOrgImage.getWidth() > aOrgImage.getHeight()) {
			if (aOrgImage.getWidth() < width) {
				scale = (double) width / (double) aOrgImage.getWidth(); // removed
																		// : 1
			} else {
				scale = (double) width / (double) aOrgImage.getWidth();
			}
		} else {
			if (aOrgImage.getHeight() < width) {
				scale = (double) width / (double) aOrgImage.getWidth(); // removed
																		// : 1
			} else {
				scale = (double) width / (double) aOrgImage.getHeight(null);
			}
		}

		// Determine size of new image.
		// One of them should equal aMaxDim.
		int scaledW = (int) Math.round(scale * aOrgImage.getWidth());
		int scaledH = (int) Math.round(scale * aOrgImage.getHeight());

		java.awt.Image img = aOrgImage.getScaledInstance(scaledW, scaledH, java.awt.Image.SCALE_SMOOTH);

		ColorModel cm = aOrgImage.getColorModel();
		WritableRaster wr = cm.createCompatibleWritableRaster(scaledW, scaledH);

		// Create BufferedImage
		BufferedImage buffImage = new BufferedImage(cm, wr, false, null);
		Graphics2D g2 = buffImage.createGraphics();
		g2.drawImage(img, 0, 0, null);
		g2.dispose();

		return buffImage;
	}

	/**
	 * research image in subdirectories
	 * 
	 * @param dir
	 *            current directory
	 * @return a list of image in a subdirectory of current directory
	 */
	static Image[] getSubImage(Directory dir) {
		Image[] res = new Image[0];
		if (dir.getImages().length > 0) {
			res = dir.getImages();
		} else {
			for (int i = 0; i < dir.getChild().length; i++) {
				res = getSubImage(dir.getChild()[i]);
				if (res.length > 0) {
					i = dir.getChild().length;
				}
			}
		}
		return res;
	}

	static String cutString(String str) {
		String res = str;
		if (res.length() > MAX_STRING_SIZE) {
			res = str.substring(0, MAX_STRING_SIZE - 1).trim() + "...";
		}
		return res;
	}

	/*
	 * public static void main(String[] args) throws Exception { File path = new
	 * File("c:\\trans\\faces"); File outPath = new
	 * File("c:\\trans\\faces\\out"); //File outPath2 = new
	 * File("c:\\trans\\faces\\out2"); outPath.mkdirs(); //outPath2.mkdirs();
	 * for (File image : path.listFiles()) { if
	 * (StringHelper.isImage(image.getName())) { try { BufferedImage bufImage =
	 * ImageIO.read(image); if (bufImage.getType() !=
	 * BufferedImage.TYPE_INT_ARGB) { BufferedImage tmp = new
	 * BufferedImage(bufImage.getWidth(), bufImage.getHeight(),
	 * BufferedImage.TYPE_INT_ARGB); tmp.getGraphics().drawImage(bufImage, 0, 0,
	 * null); bufImage = tmp; } int[] srcPixels = ((DataBufferInt)
	 * bufImage.getRaster().getDataBuffer()).getData(); MBFImage img = new
	 * MBFImage(srcPixels, bufImage.getWidth(), bufImage.getHeight());
	 * 
	 * // A simple Haar-Cascade face detector HaarCascadeDetector det1 = new
	 * HaarCascadeDetector(); //DetectedFace face1 =
	 * det1.detectFaces(img.flatten()).get(0);
	 * System.out.println(image.getName()); for (DetectedFace face :
	 * det1.detectFaces(img.flatten())) { if (face.getConfidence() > 0) { new
	 * SimpleDetectedFaceRenderer().drawDetectedFace(img, 10, face); } Point
	 * point = new
	 * Point((int)Math.round(face.getShape().minX()+(face.getShape().getWidth()/
	 * 2)),
	 * (int)Math.round(face.getShape().minY()+(face.getShape().getHeight()/2)));
	 * System.out.println("x="+point.getX());
	 * System.out.println("y="+point.getY()); }
	 * 
	 * ImageUtilities.write(img, new File(outPath.getAbsolutePath() + '/' +
	 * image.getName()));
	 * 
	 * bufImage = ImageEngine.removeAlpha(bufImage); OutputStream out = new
	 * FileOutputStream(new File(outPath.getAbsolutePath() + '/' +
	 * image.getName())); ImageOutputStream ios =
	 * ImageIO.createImageOutputStream(out); ImageWriter writer =
	 * ImageIO.getImageWritersByFormatName("jpeg").next(); ImageWriteParam param
	 * = writer.getDefaultWriteParam();
	 * param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	 * param.setCompressionQuality(0.99F); writer.setOutput(ios);
	 * writer.write(bufImage); ios.close(); out.close(); } catch (Exception e) {
	 * e.printStackTrace(); } } } }
	 */

	public static final String getImageFormat(String fileName) {
		String ext = StringHelper.getFileExtension(fileName);
		if (ext.equalsIgnoreCase("jpg")) {
			return "JPEG";
		} else if (ext.equalsIgnoreCase("png")) {
			return "PNG";
		} else if (ext.equalsIgnoreCase("gif")) {
			return "GIF";
		} else {
			return null;
		}
	}

	public static final String getImageExtensionToManType(String ext) {
		if (ext != null) {
			ext = ext.trim().toLowerCase();
			if (ext.equals("gif")) {
				return "image/GIF";
			} else if (ext.equals("png")) {
				return "image/GIF";
			} else if ((ext.equals("jpg")) || (ext.equals("jpeg"))) {
				return "image/JPEG";
			}
		}
		return null;
	}

	/**
	 * return dimension of picture in exif data, null if not found.
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static ImageSize getExifSize(InputStream in) throws IOException {
		Map<String, String> exifData = getExifData(in);
		ImageSize imageSize = null;
		try {
			imageSize = new ImageSize(Integer.parseInt(exifData.get("PixelXDimension")), Integer.parseInt(exifData.get("PixelYDimension")));
		} catch (Throwable t) {
		}
		return imageSize;
	}

	public static ImageSize getJpegSize(InputStream in) {
		ImageSize imageSize = null;
		try {
			byte[] buffer = new byte[1024 * 8];
			BufferedInputStream bufIn = new BufferedInputStream(in);
			int read = bufIn.read(buffer);

			for (int i = 0; i < read; i++) {
				if (buffer[i] == (byte) 0xff && buffer.length > i + 10) {
					if (buffer[i + 1] == (byte) 0xc0) {
						int j = 1;
						while (buffer[i + j] != 8 && j < 5) {
							j++;
						}
						if (j < 5) {
							int height = LangHelper.unsigned(buffer[i + j + 1]) * 255 + LangHelper.unsigned(buffer[i + j + 2]);
							int width = LangHelper.unsigned(buffer[i + j + 3]) * 255 + LangHelper.unsigned(buffer[i + j + 4]);
							imageSize = new ImageSize(width, height);
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return imageSize;

	}

	public static Map<String, String> getExifData(InputStream in) throws IOException {
		String rawData = ResourceHelper.loadStringFromStream(in, Charset.defaultCharset());
		Map<String, String> outData = new HashMap<String, String>();
		int index = 0;
		int exifIndex = rawData.indexOf("exif:", index);
		while (exifIndex >= 0) {
			String exifStr = rawData.substring(exifIndex, rawData.indexOf(' ', exifIndex + "exif:".length()));
			index = index + exifStr.length();
			String[] exifArray = StringUtils.split(exifStr, '=');
			if (exifArray.length == 2) {
				outData.put(exifArray[0].replace("exif:", ""), exifArray[1].replace("\"", ""));
			}
			exifIndex = rawData.indexOf("exif:", index);
		}
		return outData;
	}

	public static void main(String[] args) throws IOException {
		File src = new File("c:/trans/green/Lindi.jpg");

		int DISTANCE_MAX = ImageEngine.getColorDistance(Color.BLACK, Color.WHITE);
		System.out.println("max distance : " + ImageEngine.getColorDistance(Color.BLACK, Color.WHITE));

		ExtendedColor bgColor = new ExtendedColor(100, 100, 100);
		System.out.println(bgColor.getGreenProportion());
		

		if (!src.exists()) {
			System.out.println("*** file not found : " + src);
		} else {
			BufferedImage img = ImageIO.read(src);
			BufferedImage targetImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			int c = 0;
			for (int x = 0; x < img.getWidth(); x++) {
				for (int y = 0; y < img.getHeight(); y++) {
					ExtendedColor col = new ExtendedColor(img.getRGB(x, y), true);
					float minGreenProp = (float)0.4;
					if (col.getGreenProportion() > minGreenProp) {						
						if (col.getGreenProportion() > minGreenProp+0.1) {
							col = new ExtendedColor(0, 0, 0, 0);
						} else {
							int green = 0;
							int dec = 10;
							while (green == 0 && dec < Math.max(img.getWidth(), img.getHeight()) / 2) {
								if (new ExtendedColor(ImageEngine.getColor(img, x - dec, y, Color.green)).getGreenProportion() < minGreenProp) {
									green = new ExtendedColor(ImageEngine.getColor(img, x - dec, y, Color.green)).getGreen();
								}
								if (new ExtendedColor(ImageEngine.getColor(img, x + dec, y, Color.green)).getGreenProportion() < minGreenProp) {
									green = new ExtendedColor(ImageEngine.getColor(img, x + dec, y, Color.green)).getGreen();
								}
								if (new ExtendedColor(ImageEngine.getColor(img, x, y - dec, Color.green)).getGreenProportion() < minGreenProp) {
									green = new ExtendedColor(ImageEngine.getColor(img, x, y - dec, Color.green)).getGreen();
								}
								if (new ExtendedColor(ImageEngine.getColor(img, x, y + dec, Color.green)).getGreenProportion() < minGreenProp) {
									green = new ExtendedColor(ImageEngine.getColor(img, x, y + dec, Color.green)).getGreen();
								}
								dec++;
							}
							col = new ExtendedColor(col.getRed(), green, col.getBlue(), Math.round(255-(1-col.getGreenProportion())*255));
						}
						c++;
					}
					targetImg.setRGB(x, y, col.getRGB());
				}
			}
			System.out.println("c=" + c);
			ImageIO.write(targetImg, "png", new File("c:/trans/green/out.png"));
		}
	}
}
