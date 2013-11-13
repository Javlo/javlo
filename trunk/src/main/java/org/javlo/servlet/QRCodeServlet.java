package org.javlo.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

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
				if (splittedFile[1].equalsIgnoreCase("png")) {
					QRCode.from(data).to(ImageType.PNG).withSize(qrSize,qrSize).writeTo(response.getOutputStream());
				} else if (splittedFile[1].equalsIgnoreCase("gif")) {
					QRCode.from(data).to(ImageType.GIF).withSize(qrSize,qrSize).writeTo(response.getOutputStream());
				} else if (splittedFile[1].equalsIgnoreCase("jpg")) {
					QRCode.from(data).to(ImageType.JPG).withSize(qrSize,qrSize).writeTo(response.getOutputStream());
				} else {
					logger.warning("bad image format : " + splittedFile[1]);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
	}

	public static void main(String[] args) {

		try {
			OutputStream out;
			out = new FileOutputStream(new File("c:/trans/test.png"));
			QRCode.from("http://www.penthouse.com").to(ImageType.PNG).writeTo(out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
