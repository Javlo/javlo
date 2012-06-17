package org.javlo.servlet;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.Serializable;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.remote.LocalResourceFactory;
import org.javlo.remote.RemoteResourceList;

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
		
		String[] paths = request.getPathInfo().split("/");
		if (paths.length < 1 || paths.length > 2) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND, "bad path structure : sample :/type/category.xml.");
			return;
		}
		
		Serializable obj=null;
		
		if (paths.length == 1) { // command
			String command = paths[0];
			if (command.equalsIgnoreCase("types")) {
				obj = localFactory.getTypes();
			}
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
			encoder.close();		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}

	}

}
