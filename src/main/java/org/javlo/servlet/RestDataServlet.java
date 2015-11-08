package org.javlo.servlet;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.javlo.context.ContentContext;
import org.javlo.data.rest.IRestFactory;
import org.javlo.data.rest.IRestItem;
import org.javlo.data.rest.RestContainerFactory;
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
		String path = request.getRequestURI();
		String[] pathItem = StringUtils.split(path, '/');
		if (pathItem.length < 2) {
			logger.warning("bad rest url format : min size : 2");
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);			
		} else {
			String restFactoryName = pathItem[1];			
			IRestFactory restFactory = RestContainerFactory.getRestFactory(restFactoryName);
			if (restFactory == null) {
				logger.warning("RestFactory not found : "+restFactoryName);
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);				
			} else {
				ContentContext ctx;
				try {
					response.setContentType("application/json; charset=" + ContentContext.CHARACTER_ENCODING);					
					ctx = ContentContext.getContentContext(request, response);
					path = path.substring(pathItem[0].length()+pathItem[1].length()+2);
					IRestItem item = restFactory.search(ctx, path);
					if (item == null) {
						logger.warning("Rest item not found : "+path);
						response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					} else {
						JSONMap.JSON.toJson(item.getContentAsMap(ctx), response.getWriter());						
					}
					response.flushBuffer();
				} catch (Exception e) {					
					throw new ServletException(e);
				}				
			}
		}
	}
}
