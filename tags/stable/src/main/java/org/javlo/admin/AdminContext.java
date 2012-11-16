/*
 * Created on 20 ao�t 2003
 */
package org.javlo.admin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;


/**
 * @author pvanderm 
 */
public class AdminContext {
	
	private static final String SESSION_KEY = AdminContext.class.getName();
	
	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(AdminContext.class.getName());
	
	private int currentView = 0;
	private String commandRenderer = "global_command.jsp";
	private String renderer = "global.jsp";
	
	static final String[][] views = { { ""+0, "global" }, { ""+1, "template" }, { ""+2, "component" }, { ""+3, "static" }, { ""+4, "config" } };
	
	private String fileToEdit = null;
	
	private String URIFileToEdit = null;
	
	private String activeResource = null;

	private AdminContext(HttpSession session) {
	};
	
	public static final AdminContext getInstance(HttpSession session) {		
		AdminContext ctx = (AdminContext)session.getAttribute(SESSION_KEY);		
		if (ctx == null) {
			ctx = new AdminContext(session);
            session.setAttribute(SESSION_KEY, ctx);
		}
		return ctx;
	}

	public String[][] getViews() {
	    return views;
	}

	public int getCurrentView() {
		return currentView;
	}

	public void setCurrentView(int currentView) {
		this.currentView = currentView;
	}

	public String getCommandRenderer() {
		return commandRenderer;
	}

	public void setCommandRenderer(String commandRenderer) {
		this.commandRenderer = commandRenderer;
	}

	public String getRenderer() {
		return renderer;
	}

	public void setRenderer(String renderer) {
		this.renderer = renderer;
	}

	public String getFileToEdit() {
		return fileToEdit;
	}

	public void setFileToEdit(String fileToEdit) {
		this.fileToEdit = fileToEdit;
	}
	
	public String readFileToEdit(HttpSession session) throws IOException {
		String editFile = getFileToEdit();
		if (editFile != null) {			
			File file = new File(editFile);			
			if (file.exists()) {
				return FileUtils.readFileToString(file, ContentContext.CHARACTER_ENCODING);
			} else {
				return "";
			}
		}
		return null;
	}
	
	public void writeStreamToEdit(ServletContext application, InputStream in) throws IOException {
		String editFile = getFileToEdit();
		if (editFile != null) {
			StaticConfig staticConfig = StaticConfig.getInstance(application);
			File file = new File(editFile);
			logger.info("replace file with stream : "+file);
			if (file.exists()) {
				OutputStream out = new FileOutputStream(file);		
				
				ResourceHelper.writeStreamToStream(in, out);
				
				/*int available = in.available();
				byte[] buffer = new byte[available];
				int read = in.read(buffer);
				out.write(buffer);
				while (read > 0) {					
					available = in.available();
					buffer = new byte[available];					
					read = in.read(buffer);
					out.write(buffer);
				}*/
				out.close();
			}
		}
	}
	
	public void saveFileToEdit(HttpSession session, String content) throws IOException {
		String editFile = getFileToEdit();
		if (editFile != null) {			
			File file = new File(editFile);			
			if (!file.exists()) {
				file.createNewFile();
			}
			FileUtils.writeStringToFile(file, content, ContentContext.CHARACTER_ENCODING);
		}
	}

	public String getActiveResource() {
		return activeResource;
	}

	public void setActiveResource(String activeResource) {
		this.activeResource = activeResource;
	}

	public void setURIFileToEdit(String uRIFileToEdit) {
		URIFileToEdit = uRIFileToEdit;
	}

	public String getURIFileToEdit() {
		return URIFileToEdit;
	}
	

}

