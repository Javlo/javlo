package org.javlo.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.mailing.FeedBackMailingBean;
import org.javlo.mailing.Mailing;
import org.javlo.service.DataToIDService;

/**
 * @author pvandermaesen
 * 
 * 
 */
public class FeedbackServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(FeedbackServlet.class.getName());

	static int servletRun = 0;

	private static String LOCK = "lock";

	/**
	 * get the text and the picture and build a button
	 */
	@SuppressWarnings("unchecked")
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("image/GIF");

		String path = request.getRequestURI();
		String data = FilenameUtils.getBaseName(path).substring(4);

		DataToIDService serv = DataToIDService.getInstance(getServletContext());
		Map<String, String> params = StringHelper.uriParamToMap(serv.getData(data));
		Collection<Map.Entry<String, String>> entries = params.entrySet();
		for (Map.Entry<String, String> entry : entries) {
			System.out.println("key  : " + entry.getKey());
			System.out.println("value: " + entry.getValue());
		}

		Enumeration<String> names = request.getHeaderNames();
		String userAgent = null;
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			if (name.trim().equalsIgnoreCase("user-agent")) {
				userAgent = request.getHeader(name);
			}
		}

		String id = params.get("mailing");
		if (id != null) {
			Mailing mailing = new Mailing();
			mailing.setId(getServletContext(), id);
			FeedBackMailingBean bean = new FeedBackMailingBean();
			bean.setEmail(params.get("to"));
			bean.setAgent(userAgent);
			bean.setDate(new Date());
			bean.setUrl("");
			try {
				synchronized (LOCK) {
					mailing.addFeedBack(bean);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		String imageURL = ResourceHelper.getRealPath(getServletContext(),"/images/special/no_image.gif");
		InputStream fileStream = null;
		OutputStream out;
		try {
			out = response.getOutputStream();
			fileStream = new FileInputStream(new File(imageURL));
			if ((fileStream != null)) {
				ResourceHelper.writeStreamToStream(fileStream, out);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			ResourceHelper.closeResource(fileStream);
		}
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		processRequest(httpServletRequest, httpServletResponse);
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		processRequest(httpServletRequest, httpServletResponse);
	}

}
