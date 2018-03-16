/*
 * Created on 01-f�vr.-2004
 */
package org.javlo.helper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Enumeration;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentFactory;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.filter.CatchAllFilter;
import org.javlo.navigation.MenuElement;
import org.javlo.service.syncro.AbstractSynchroService;
import org.javlo.service.syncro.BaseSynchroService;
import org.javlo.service.syncro.ServerSynchroService;
import org.javlo.servlet.AccessServlet;
import org.javlo.servlet.ImageTransformServlet;
import org.javlo.servlet.SynchronisationServlet;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.UserFactory;


/**
 * @author pvandermaesen some method for help the debuging
 */
public class DebugHelper {

	/**
	 * @author pvanderm global StructureException for error any bad structure ( xml, db, ... )
	 */
	public static class AssertException extends RuntimeException {

		public AssertException() {
			super();
		}

		public AssertException(Throwable e) {
			super(e);
		}

		public AssertException(String msg) {
			super(msg);
		}

	}

	/**
	 * @author pvanderm global StructureException for error any bad structure ( xml, db, ... )
	 */
	public static class StructureException extends Exception {

		public StructureException() {
			super();
		}

		public StructureException(Throwable e) {
			super(e);
		}

		public StructureException(String msg) {
			super(msg);
		}

	}

	/**
	 * throw a exception if error throw
	 * 
	 * @param error
	 *            the error boolean value
	 * @param msg
	 *            the message
	 * @throws StructureException
	 *             if error true
	 */
	public static void checkStructure(boolean error, String msg) throws StructureException {
		if (error) {
			throw new StructureException(msg);
		}
	}

	/**
	 * throw a exception if error throw
	 * 
	 * @param error
	 *            the error boolean value
	 * @param msg
	 *            the message
	 * @throws AssertException
	 *             if error true
	 */
	public static void checkAssert(boolean error, String msg) throws AssertException {
		if (error) {
			throw new AssertException(msg);
		}
	}

	public static void writeInfo(ContentContext ctx, PrintStream out) {
		Runtime runtime = Runtime.getRuntime();
		ThreadMXBean threads = ManagementFactory.getThreadMXBean();
		out.println("****************************************************************");
		out.println("****************************************************************");
		out.println("****");
		out.println("**** TOTAL MEMORY      :  " + runtime.totalMemory() + " (" + runtime.totalMemory() / 1024 + " KB)" + " (" + runtime.totalMemory() / 1024 / 1024 + " MB)");
		out.println("**** FREE MEMORY       :  " + runtime.freeMemory() + " (" + runtime.freeMemory() / 1024 + " KB)" + " (" + runtime.freeMemory() / 1024 / 1024 + " MB)");
		out.println("**** THREAD ****");
		out.println("**** THREAD COUNT      :  " + threads.getThreadCount());
		out.println("**** THREAD STR COUNT  :  " + threads.getTotalStartedThreadCount());
		out.println("**** THREAD DMN COUNT  :  " + threads.getDaemonThreadCount());
		out.println("**** #MenuElement      :  " + MenuElement.INSTANCE);
		out.println("**** #ComponentBean    :  " + ComponentBean.INSTANCE);
		out.println("****");
		out.println("****************************************************************");
		if (ctx != null) {
			out.println("**** SESSION  *****");			
			Enumeration names = ctx.getRequest().getSession().getAttributeNames();
			while (names.hasMoreElements()) {
				String key = ""+names.nextElement();
				System.out.println(key+" = "+ctx.getRequest().getSession().getAttribute(key).getClass()+" ["+ctx.getRequest().getSession().getAttribute(key).hashCode()+"]");
			}
			out.println("****");
			out.println("**** APPLICATION  *****");
			names = ctx.getRequest().getSession().getServletContext().getAttributeNames();
			while (names.hasMoreElements()) {
				String key = ""+names.nextElement();
				System.out.println(key+" = "+ctx.getRequest().getSession().getServletContext().getAttribute(key).getClass()+" ["+ctx.getRequest().getSession().getServletContext().getAttribute(key).hashCode()+"]");
			}
			out.println("****");
			out.println("**** GlobalContext  *****");			
			for (Object key : ctx.getGlobalContext().getAttributesKeys()) {				
				System.out.println(key+" = "+ctx.getGlobalContext().getAttribute(""+key).getClass()+" ["+ctx.getGlobalContext().getAttribute(""+key).hashCode()+"]");
			}
		
			out.println("****************************************************************");
		}
		out.println("****************************************************************");
		
		
	}

	public static void updateLoggerLevel(ServletContext application) throws Exception {
		StaticConfig staticConfig = StaticConfig.getInstance(application);
		Logger.getLogger("").setLevel(staticConfig.getAllLogLevel());
		AccessServlet.logger.setLevel(staticConfig.getAccessLogLevel());		
		ImageTransformServlet.logger.setLevel(staticConfig.getAccessLogLevel());
		CatchAllFilter.logger.setLevel(staticConfig.getAccessLogLevel());
		MenuElement.logger.setLevel(staticConfig.getNavigationLogLevel());

		ServerSynchroService.logger.setLevel(staticConfig.getSynchroLogLevel());
		BaseSynchroService.logger.setLevel(staticConfig.getSynchroLogLevel());
		AbstractSynchroService.logger.setLevel(staticConfig.getSynchroLogLevel());
		SynchronisationServlet.logger.setLevel(staticConfig.getSynchroLogLevel());

		ComponentFactory.updateComponentsLogLevel(application, staticConfig.getAllComponentLogLevel());
		AbstractVisualComponent.logger.setLevel(staticConfig.getAbstractComponentLogLevel());
		UserFactory.logger.setLevel(staticConfig.getLoginLogLevel());
		AdminUserFactory.logger.setLevel(staticConfig.getLoginLogLevel());		
	}
	
	/**
	 * return the caller of the current method
	 * @return
	 */
	public static String getCaller() {
		Exception e = new Exception();
		if (e.getStackTrace().length>1) {
			return e.getStackTrace()[2].toString();
		} else { 
			return "unknow";
		}
	}
	
	public static String getCaller(int count) {
		Exception e = new Exception();		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		for (int i=2; i<Math.min(count+2, e.getStackTrace().length); i++) {
			out.println(e.getStackTrace()[i].toString());
		}
		out.close();
		return new String(outStream.toByteArray());
	}

}
