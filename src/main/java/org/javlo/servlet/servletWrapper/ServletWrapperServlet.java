package org.javlo.servlet.servletWrapper;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.RequestHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.RequestService;
import org.javlo.tracking.Track;
import org.javlo.tracking.Tracker;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;

/**
 * @author pvandermaesen
 * 
 * 
 */
public class ServletWrapperServlet extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ServletWrapperServlet.class.getName());

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest, HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		processRequest(httpServletRequest, httpServletResponse);
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest, HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
		processRequest(httpServletRequest, httpServletResponse);
	}

	/**
	 * get the text and the picture and build a button
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {

		ContentContext ctx;
		try {
			ctx = ContentContext.getContentContext(request, response);
			RequestHelper.traceMailingFeedBack(ctx);
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new ServletException(e1.getMessage());
		}

		/* TRACKING */
		GlobalContext globalContext = GlobalContext.getInstance(request);
		IUserFactory fact = UserFactory.createUserFactory(globalContext, request.getSession());
		User user = fact.getCurrentUser(request.getSession());
		String userName = null;
		if (user != null) {
			userName = user.getLogin();
		}
		try {
			Tracker tracker = Tracker.getTracker(globalContext, request.getSession());
			Track track = new Track(userName, "view picture", request.getRequestURI(), System.currentTimeMillis(), request.getHeader("referer"), request.getHeader("User-Agent"));
			track.setIP(request.getRemoteAddr());
			track.setSessionId(request.getSession().getId());
			tracker.addTrack(track);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		/* END TRACKING */

		// TODO: check if that work for caching
		/*
		 * Date toDay = new Date(); Calendar cal = Calendar.getInstance(); cal.setTime(toDay); cal.roll(Calendar.DAY_OF_YEAR, true); response.setHeader("Expires", cal.getTime().toString());
		 */

		try {
			RequestService requestService = RequestService.getInstance(ctx.getRequest());
			String requestManagerkey = requestService.getParameter(URLHelper.REQUEST_MANAGER_PARAMATER_KEY, null);
			if (requestManagerkey != null) {
				Object requestManagerObj = request.getSession().getAttribute(requestManagerkey);
				if (requestManagerObj != null && requestManagerObj instanceof IServletWrapper) {
					IServletWrapper requestManager = (IServletWrapper) requestManagerObj;
					requestManager.processRequest(request, response);
				}
			} else {
				logger.warning("wrapper not found.");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
