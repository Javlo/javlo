package org.javlo.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.helper.RequestHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.image.ImageHelper;
import org.javlo.mailing.feedback.IMailingFeedback;
import org.javlo.mailing.feedback.MailingFeedbackFactory;

public class MailingFeedback extends HttpServlet {
	
	private static final String IMAGE = "/images/empty.png";
	private static final String CONTENT_TYPE = ImageHelper.getImageExtensionToManType(IMAGE);
		
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
		resp.setContentType(CONTENT_TYPE);
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ResourceHelper.writeStreamToStream(in, resp.getOutputStream());
		ResourceHelper.closeResource(in);
		ContentContext ctx;
		try {
			ctx = ContentContext.getContentContext(req, resp);			
			for (IMailingFeedback mailingFeedback : MailingFeedbackFactory.getInstance(ctx).getAllMailingFeedback()) {
				mailingFeedback.treatFeedback(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}