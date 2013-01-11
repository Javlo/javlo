/*
 * Created on 8 oct. 2003
 */
package org.javlo.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
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

	public static String createSpecialDirectory(String context, String filter, String area, String deviceCode, Template template) {
		context = StringHelper.createFileName(context);
		if (template == null) {
			return context + '/' + filter + '/' + deviceCode + '/' + area + '/' + Template.EDIT_TEMPLATE_CODE;
		} else {
			return context + '/' + filter + '/' + deviceCode + '/' + area + '/' + template.getId();
		}
	}

	public static String createSpecialDirectory(int width, int filter) {
		return "width_" + width + "_filter_" + filter;
	}

	/**
	 * transform a path in a string to a key. this key can be a directory name ( sp. replace / and \ with _ ).
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

	public static InputStream loadFileFromDisk(HttpServletRequest request, String cacheDir, String name, int width, int filter) {
		String realFile = request.getSession().getServletContext().getRealPath(cacheDir + "/" + createSpecialDirectory(width, filter) + "/" + pathToKey(name));
		InputStream stream = null;
		File file = new File(realFile);
		if (file.exists()) {
			try {
				stream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				// do nothing just return null
			}
		} else {
			// System.out.println("can not load form disk : " +
			// file.getAbsolutePath());
		}
		return stream;
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

	public static boolean thumbnailExist(HttpServletRequest request, String cacheDir, String name, int width, int filter) {
		String realPath = request.getSession().getServletContext().getRealPath(cacheDir + "/" + createSpecialDirectory(width, filter) + "/" + DIR_DIR + "/" + pathToKey(name));
		File file = new File(realPath);
		if (!file.exists()) {
			// System.out.println("file not found in disk cache : " +
			// file.getAbsolutePath());
		}
		return file.exists();
	}

	public static void main(String[] args) {
		String path = "c:\\p\\photos";
		System.out.println("path=" + path);
		System.out.println("key=" + pathToKey(path));
	}

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

}
