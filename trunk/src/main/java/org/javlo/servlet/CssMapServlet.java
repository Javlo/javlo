package org.javlo.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.javlo.context.GlobalContext;

public class CssMapServlet extends HttpServlet {

	public CssMapServlet() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void doGet(HttpServletRequest httpRequest, javax.servlet.http.HttpServletResponse resp) throws ServletException, IOException {
		String path = httpRequest.getServletPath();
		GlobalContext globalContext = GlobalContext.getInstance(httpRequest);
		if (path.startsWith('/' + globalContext.getContextKey())) {
			path = path.replaceFirst('/' + globalContext.getContextKey(), "");
		}
		String cssPath = path;
		path = path.substring(0, path.length()-".css.map".length())+".less";		
		System.out.println("***** CssMapServlet.doGet : path = "+path); //TODO: remove debug trace
		File lessFile = new File(httpRequest.getSession().getServletContext().getRealPath(path));
		System.out.println("***** CssMapServlet.doGet : lessFile = "+lessFile); //TODO: remove debug trace
		if (!lessFile.exists()) {
			System.out.println("***** CssMapServlet.doGet : LESS FILE NOT FOUND."); //TODO: remove debug trace
			resp.setStatus(404);
			return;
		} else {	
			resp.setContentType("application/json");
			PrintStream out = new PrintStream(resp.getOutputStream());
			System.out.println("***** CssMapServlet.doGet : RENDER FILE."); //TODO: remove debug trace
			out.println("{version\":3,\"file\":\""+cssPath+"\",\"sources\":[\""+path+"\"],\"names\":[]}");
			out.close();			
		}
	}
	

}
