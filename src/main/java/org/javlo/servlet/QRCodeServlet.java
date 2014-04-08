package org.javlo.servlet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

import org.codehaus.plexus.util.StringUtils;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.ILink;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.service.ContentService;

public class QRCodeServlet extends HttpServlet {

	private static Logger logger = Logger.getLogger(QRCodeServlet.class.getName());
	
	private static BufferedImage removeBorder(BufferedImage image) {
		int firstColor = image.getRGB(1, 1);
		for (int x=0; x<image.getWidth(); x++) {
			if (image.getRGB(x, 1) != firstColor) {
				return image; // no border
			}
		}
		for (int x=0; x<image.getWidth(); x++) {
			for (int y=0; y<image.getWidth(); y++) {				
				if (image.getRGB(x, y) != firstColor) {					
					return image.getSubimage(x, x, image.getWidth()-2*x, image.getHeight()-2*x);
				}
			}
		}
		return image;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {

		String[] splittedPath = StringUtils.split(request.getPathInfo(), "/");
		if (splittedPath.length != 2) {
			logger.warning("bad request structure : " + request.getPathInfo());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		String category = splittedPath[0];
		String file = splittedPath[1];
		String[] splittedFile = StringUtils.split(file, ".");
		if (splittedFile.length != 2) {
			logger.warning("bad file structure : " + file);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		try {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			
			if (category.endsWith("_preview")) {
				ctx.setRenderMode(ContentContext.PREVIEW_MODE);
				category = category.substring(0, category.lastIndexOf("_preview"));
			}

			String data = null;

			if (category.equals("link")) {
				ContentService content = ContentService.getInstance(GlobalContext.getInstance(request));
				IContentVisualComponent comp = content.getComponent(ctx, splittedFile[0]);
				if (comp == null) {
					logger.warning("component not found : " + splittedFile[0]);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return;
				} else if (!(comp instanceof ILink)) {
					logger.warning("component not link : " + splittedFile[0]);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return;
				}
				ctx = ctx.getContextForAbsoluteURL();
				data = ((ILink) comp).getURL(ctx);
			}

			if (data != null) {				
				int qrSize = ctx.getCurrentTemplate().getQRCodeSize(); 
				
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				QRCode.from(data).to(ImageType.PNG).withSize(qrSize+74,qrSize+74).writeTo(out); //74 = estimation of margin
				
				BufferedImage image = ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
				image = removeBorder(image);
				ImageIO.write(image, splittedFile[1], response.getOutputStream());
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
	}

	public static void main(String[] args) {

		try {
			//OutputStream out;
			//out = new FileOutputStream(new File("c:/trans/test.png"));
			//QRCode.from("http://www.javlo.be").to(ImageType.PNG).writeTo(out);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			QRCode.from("http://www.javlo.be").to(ImageType.PNG).withSize(300, 300).writeTo(out);
			BufferedImage image = removeBorder(ImageIO.read(new ByteArrayInputStream(out.toByteArray())));
			ImageIO.write(image, "png", new File("c:/trans/test2.png"));
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
