package org.javlo.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.servlet.zip.ZipManagement;

import sun.awt.image.ByteArrayImageSource;

public class ClassServlet extends HttpServlet {

	private static final long serialVersionUID = -2086338711912267925L;

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ClassServlet.class.getName());

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
		try {
			String classPath = request.getPathInfo().substring(1); // remove first '/'
			String className = classPath.replace('/', '.');
			
			if (className.endsWith(".jar")) {
				
				classPath = classPath.substring(0,classPath.length()-".jar".length());
				className = classPath.replace('/', '.');
				
				Class clazz = getClass().getClassLoader().loadClass(className);				
				ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
				ObjectOutput out = new ObjectOutputStream(tmpOut);
				out.writeObject(clazz);
				out.close();
				
				response.setContentType("application/gzip");
				ZipOutputStream outZip = new ZipOutputStream(response.getOutputStream());
				
				ZipManagement.addFileInZip(outZip, classPath+".class", new ByteArrayInputStream(tmpOut.toByteArray()));

				outZip.finish();
				outZip.flush();
				outZip.close();
			} else {
				Class clazz = getClass().getClassLoader().loadClass(className);
				ObjectOutput out = new ObjectOutputStream(response.getOutputStream());
				out.writeObject(clazz);
				out.close();
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
