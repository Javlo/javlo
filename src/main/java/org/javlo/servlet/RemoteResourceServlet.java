package org.javlo.servlet;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.remote.IRemoteResource;
import org.javlo.remote.LocalResourceFactory;

public class RemoteResourceServlet extends HttpServlet {

	private static final long serialVersionUID = -2086338711912267925L;

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(RemoteResourceServlet.class.getName());

	@Override
	public void init() throws ServletException {
		super.init();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		LocalResourceFactory localFactory = LocalResourceFactory.getInstance(globalContext);

		String uri = request.getPathInfo().trim();
		if (!StringHelper.getFileExtension(uri).equalsIgnoreCase("xml")) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND, "bad file format : use xml.");
			return;
		}

		uri = uri.substring(0, uri.length() - ".xml".length()); // remove extension

		String[] paths = StringUtils.split(uri, "/");
		if (paths.length < 1 || paths.length > 2) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND, "bad path structure : sample :/type/category.xml.");
			return;
		}

		Serializable obj = null;
		try {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			if (paths.length == 1) { // command
				String command = paths[0];
				if (command.equalsIgnoreCase("types")) {
					obj = localFactory.getTypes();
				} else if (command.equalsIgnoreCase("all")) {
					obj = localFactory.getResourcesForProxy(ctx, null, null);
				}
			} else {
				String type = paths[0];
				String category = paths[1];
				if (category.equals("categories")) { // ask list of category
					ArrayList<String> categories = new ArrayList<String>();
					if (localFactory.getResourcesForProxy(ctx, type, null) != null) {
						List<IRemoteResource> resources = localFactory.getResourcesForProxy(ctx, type, null).getList();
						for (IRemoteResource bean : resources) {
							if (!categories.contains(bean.getCategory())) {
								categories.add(bean.getCategory());
							}
						}
					} else {
						logger.severe("no resources found for : "+type);
					}
					obj = categories;
				} else {
					obj = localFactory.getResourcesForProxy(ctx, type, category);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
			return;
		}

		if (obj == null) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND, "path not found.");
			return;
		}

		ContentContext ctx;
		try {
			ctx = ContentContext.getContentContext(request, response);
			XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(response.getOutputStream()));
			encoder.writeObject(obj);
			encoder.flush();
			encoder.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}

	}

}
