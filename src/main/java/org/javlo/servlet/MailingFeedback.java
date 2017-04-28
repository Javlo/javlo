package org.javlo.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.helper.RequestHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.image.ImageHelper;

public class MailingFeedback extends HttpServlet {
	
	private static final String IMAGE = "/images/empty.png";
	
	private byte[] data = null;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {		
		if (data == null) {
			File imageFile = new File(ResourceHelper.getRealPath(getServletContext(),IMAGE));
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ResourceHelper.writeFileToStream(imageFile, out);
			data = out.toByteArray();
			out.close();
		}		
		resp.setHeader("Cache-Control", "no-cache");
		resp.setHeader("Accept-Ranges", "bytes");
		resp.setContentType(getServletInfo());
		resp.setContentType(ImageHelper.getImageExtensionToManType(IMAGE));
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ResourceHelper.writeStreamToStream(in, resp.getOutputStream());
		ResourceHelper.closeResource(in);
		ContentContext ctx;
		try {
			ctx = ContentContext.getContentContext(req, resp);
			RequestHelper.traceMailingFeedBack(ctx);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}