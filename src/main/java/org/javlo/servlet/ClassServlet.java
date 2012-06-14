package org.javlo.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.helper.ResourceHelper;
import org.javlo.servlet.zip.ZipManagement;

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

			if (!className.startsWith("org.javlo")) {
				throw new SecurityException("only javlo package can be access.");
			}

			if (className.endsWith(".jar")) {

				classPath = classPath.substring(0, classPath.length() - ".jar".length());
				className = classPath.replace('/', '.');

				System.out.println("***** ClassServlet.process : classPath = "+classPath); //TODO: remove debug trace
				
				Class clazz = getClass().getClassLoader().loadClass(className);
				InputStream in = clazz.getClassLoader().getResourceAsStream('/'+classPath+".class");
				ByteArrayOutputStream tmpOut = new ByteArrayOutputStream();
				ResourceHelper.writeStreamToStream(in, tmpOut);
				/*ObjectOutput out = new ObjectOutputStream(tmpOut);
				out.writeObject(clazz);				out.close();*/

				Manifest manifest = new Manifest();
				manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

				response.setContentType("application/gzip");
				JarOutputStream outZip = new JarOutputStream(response.getOutputStream(), manifest);

				ZipManagement.addFileInZip(outZip, classPath + ".class", new ByteArrayInputStream(tmpOut.toByteArray()));

				outZip.finish();
				outZip.flush();
				outZip.close();
			} else if (className.endsWith(".class")) {
				classPath = classPath.substring(0, classPath.length() - ".class".length());
				className = classPath.replace('/', '.');

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
