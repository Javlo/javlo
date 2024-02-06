 package org.javlo.servlet;

 import jakarta.servlet.ServletException;
 import jakarta.servlet.http.HttpServlet;
 import jakarta.servlet.http.HttpServletRequest;
 import jakarta.servlet.http.HttpServletResponse;
 import net.glxn.qrgen.QRCode;
 import net.glxn.qrgen.image.ImageType;
 import org.apache.commons.lang3.StringUtils;
 import org.javlo.component.core.IContentVisualComponent;
 import org.javlo.component.core.ILink;
 import org.javlo.context.ContentContext;
 import org.javlo.context.GlobalContext;
 import org.javlo.helper.StringHelper;
 import org.javlo.helper.URLHelper;
 import org.javlo.navigation.MenuElement;
 import org.javlo.service.ContentService;

 import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.util.logging.Logger;

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
		if (splittedPath.length < 2 && request.getParameter("link") == null) {
			logger.warning("bad request structure : " + request.getPathInfo());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		String category = splittedPath[0];
		String file = splittedPath[1];
		String[] splittedFile = StringUtils.split(file, ".");


		
		try {
			ContentContext ctx = ContentContext.getContentContext(request, response);

			if (request.getParameter("link") != null) {
				if (StringHelper.isTrue(ctx.getGlobalContext().getStaticConfig().getProperty("qrcode.free", null), false)) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					String data = request.getParameter("link");
					QRCode.from(data).to(ImageType.PNG).withSize(1024, 1024).writeTo(out); //74 = estimation of margin
					BufferedImage image = ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
					if (StringHelper.isTrue(request.getParameter("remove-border"), false)) {
						image = removeBorder(image);
					}
					ImageIO.write(image, "png", response.getOutputStream());
				}
			} else {

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
				} else if (category.equals("data")) {
					data = (String) ctx.getGlobalContext().getTimeAttribute(splittedFile[0]);
				} else if (category.equals("page")) {
					String url = request.getPathInfo().replaceFirst("/page/", "");
					String lg = url.substring(0, 2);
					url = url.substring(2);
					ByteArrayOutputStream out = new ByteArrayOutputStream();

					ContentContext pageCtx = new ContentContext(ctx);
					pageCtx.setAbsoluteURL(true);
					pageCtx.setAllLanguage(lg);

					pageCtx.setPath(url);
					MenuElement page = ctx.getCurrentPage();

					if (page.isRoot()) {
						logger.warning("page root ? : " + url);
					}

					if (page == null) {
						logger.warning("page not found : " + url);
						response.setStatus(HttpServletResponse.SC_NOT_FOUND);
						return;
					} else {
						String shortUrl = URLHelper.createStaticURL(ctx.getContextForAbsoluteURL(), pageCtx.getCurrentPage().getShortLanguageURL(pageCtx));
						QRCode.from(shortUrl).to(ImageType.PNG).withSize(1024 + 74, 1024 + 74).writeTo(out); //74 = estimation of margin
						BufferedImage image = ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
						//image = removeBorder(image);
						ImageIO.write(image, "png", response.getOutputStream());
					}
				}

				if (data != null) {
					int qrSize = ctx.getCurrentTemplate().getQRCodeSize();

					ByteArrayOutputStream out = new ByteArrayOutputStream();
					QRCode.from(data).to(ImageType.PNG).withSize(qrSize + 74, qrSize + 74).writeTo(out); //74 = estimation of margin

					BufferedImage image = ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
					image = removeBorder(image);
					ImageIO.write(image, splittedFile[1], response.getOutputStream());
				}
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
