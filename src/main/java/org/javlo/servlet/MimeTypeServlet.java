package org.javlo.servlet;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class MimeTypeServlet  extends HttpServlet {

	private static Logger logger = Logger.getLogger(MimeTypeServlet.class.getName());
	
	private static String svg = null;
	
	private synchronized String getImage() throws IOException {
		if (svg == null) {
			File svgFile = new File(getServletContext().getRealPath(URLHelper.mergePath(URLHelper.MINETYPE_FOLDER, "generic.svg")));
			if (!svgFile.exists()) {
				logger.warning("file not found : "+svgFile);
				return null;
			} else {
				svg = FileUtils.readFileToString(svgFile, ContentContext.CHARACTER_ENCODING);
			}
		}
		return svg;
	}
	
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		String ext = StringHelper.getFileNameWithoutExtension(StringHelper.getFileNameFromPath(request.getRequestURI()));
		try {
			response.setHeader("Cache-Control", "public,max-age=6000");
			response.setHeader("Content-Type", "image/svg+xml;charset="+ContentContext.CHARACTER_ENCODING);			
			ResourceHelper.writeStringToStream(getImage().replace("[ext]", ext), response.getOutputStream(), ContentContext.CHARACTER_ENCODING);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
	}

}
