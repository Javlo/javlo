package org.javlo.servlet;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.remote.RemoteFactory;
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
		boolean isBin = StringHelper.getFileExtension(request.getPathInfo()).equalsIgnoreCase("bin");
		GlobalContext globalContext = GlobalContext.getInstance(request);
		RemoteFactory remoteFactory = RemoteFactory.getInstance(globalContext);

		ContentContext ctx;
		try {
			ctx = ContentContext.getContentContext(request, response);
			if (!isBin) {
				XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(response.getOutputStream()));				
				RemoteResourceList list = remoteFactory.getAllResources(ctx);
				encoder.writeObject(list);				
				encoder.flush();
				encoder.close();
			} else {
				// bin encodeur
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}

	}

}
