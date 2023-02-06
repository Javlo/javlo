package org.javlo.service.pdf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.javlo.helper.StringHelper;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.pdf.ITextFSImage;
import org.xhtmlrenderer.pdf.ITextImageElement;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

import com.lowagie.text.Image;

public class ImageReplacedElementFactory implements ReplacedElementFactory {
	
	private static Logger logger = Logger.getLogger(ImageReplacedElementFactory.class.getName());

	private String relativePath;

	public ImageReplacedElementFactory(String path) {
		if (path != null && !path.endsWith("/")) {
			path += "/";
		}
		this.relativePath = path;
	}

	private static byte[] toByteArray(InputStream in) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[2048];

		while ((nRead = in.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		buffer.flush();
		return buffer.toByteArray();
	}

	private static boolean isAbsolute(String path) {
		if (path == null) {
			return false;
		}
		String lowerPath = path.toLowerCase();
		return lowerPath.startsWith("//") || lowerPath.startsWith("http://") || lowerPath.startsWith("https://");
	}

	@Override
	public ReplacedElement createReplacedElement(LayoutContext lc, BlockBox box, UserAgentCallback uac, int cssWidth, int cssHeight) {
		Element e = box.getElement();
		String nodeName = e.getNodeName();
		if (nodeName.equals("img")) {
			String imagePath = e.getAttribute("src");
			InputStream in = null;
			try {
				if (isAbsolute(imagePath)) {
					if (StringHelper.getFileExtension(imagePath).toLowerCase().equals("webp")) {
						try (InputStream inWepp = new URL(imagePath).openStream()) {
							BufferedImage img = ImageIO.read(inWepp);
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							ImageIO.write(img, "png", out);
							img.flush();
							in = new ByteArrayInputStream(out.toByteArray());
						}
					} else {
						URL url = new URL(imagePath);
						in = url.openStream();
					}
				} else {
					if (imagePath == null) {
						logger.warning("relative image found, but relative path not defined : "+imagePath);
					} else {
						in = new FileInputStream(relativePath + imagePath);
					}
				}
				if (in != null) {
					byte[] bytes = toByteArray(in);
					Image image = Image.getInstance(bytes);
					FSImage fsImage = new ITextFSImage(image);
					if (cssWidth != -1 || cssHeight != -1) {
						fsImage.scale(cssWidth, cssHeight);
					} else {
						fsImage.scale(2000, 1000);
					}

					return new ITextImageElement(fsImage);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void remove(Element e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFormSubmissionListener(FormSubmissionListener listener) {
		// TODO Auto-generated method stub

	}

}
