package org.javlo.servlet;

import java.io.IOException;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.data.rest.IRestFactory;
import org.javlo.data.rest.IRestItem;
import org.javlo.data.rest.RestContainerFactory;
import org.javlo.helper.StringHelper;
import org.javlo.utils.JSONMap;

public class RestDataServlet extends HttpServlet {

	private static Logger logger = Logger.getLogger(RestDataServlet.class.getName());

	private static final long serialVersionUID = 1L;

	public RestDataServlet() {
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		process(req, resp, false);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		process(req, resp, true);
	}

	public void process(HttpServletRequest request, HttpServletResponse response, boolean post) throws ServletException {

		if (!StaticConfig.getInstance(request.getSession().getServletContext()).isRestServer()) {
			logger.warning("no rest server");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		String path = request.getPathInfo();
		String[] pathItem = StringUtils.split(path, '/');
		if (!StaticConfig.getInstance(request.getSession().getServletContext()).isRestServlet()) {
			logger.warning("rest servlet not activated, change static-config to use.");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else if (pathItem == null || pathItem.length < 2) {
			logger.warning("bad rest url format : min size : 2");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		} else {
			String restFactoryName = pathItem[0];
			IRestFactory restFactory = RestContainerFactory.getRestFactory(restFactoryName);
			if (restFactory == null) {
				logger.warning("RestFactory not found : " + restFactoryName);
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			} else {
				ContentContext ctx;
				try {
					response.setContentType("application/json; charset=" + ContentContext.CHARACTER_ENCODING);
					ctx = ContentContext.getContentContext(request, response);
					if (path.length() > pathItem[0].length() + 2) {
						path = path.substring(pathItem[0].length() + 2);
					} else {
						path = "";
					}
					
					String maxStr = ctx.getRequestService().getParameter("max");
					int max = 10;
					if (StringHelper.isDigit(maxStr)) {
						max = Integer.parseInt(maxStr);
						if (max < 0) {
							max = Integer.MAX_VALUE;
						}						
					}
					
					IRestItem item = restFactory.search(ctx, path, request.getQueryString(), max);
					if (item == null) {
						logger.warning("Rest item not found : " + path);
						response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					} else {
						JSONMap.JSON.toJson(item.getContentAsMap(ctx), response.getWriter());
					}
					response.flushBuffer();
				} catch (ResponseErrorException e) {
					logger.warning("status : "+e.getResponseStatus());
					response.setStatus(e.getResponseStatus());
				} catch (Exception e) {
					throw new ServletException(e);
				}
			}
		}
	}
}
