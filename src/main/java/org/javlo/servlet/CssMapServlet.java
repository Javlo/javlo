package org.javlo.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class CssMapServlet extends HttpServlet {

	public CssMapServlet() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void doGet(HttpServletRequest httpRequest, jakarta.servlet.http.HttpServletResponse resp) throws ServletException, IOException {
		String path = httpRequest.getServletPath();
		GlobalContext globalContext = GlobalContext.getInstance(httpRequest);
		if (path.startsWith('/' + globalContext.getContextKey())) {
			path = path.replaceFirst('/' + globalContext.getContextKey(), "");
		}
		String cssPath = path;
		path = path.substring(0, path.length()-".css.map".length())+".less";		
		File lessFile = new File(ResourceHelper.getRealPath(httpRequest.getServletContext(),path));
		if (!lessFile.exists()) {			
			resp.setStatus(404);
			return;
		} else {	
			resp.setContentType("application/json");
			PrintStream out = new PrintStream(resp.getOutputStream());			
			out.println("{version\":3,\"file\":\""+cssPath+"\",\"sources\":[\""+path+"\"],\"names\":[]}");
			out.close();			
		}
	}
	

}
