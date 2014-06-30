package org.javlo.servlet;

import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;

public class I18n extends HttpServlet {

	public I18n() {
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String viewKey = request.getParameter("viewKey");
		String editKey = request.getParameter("editKey");
		String key = request.getParameter("key");
		
		ContentContext ctx;
		try {
			ctx = ContentContext.getContentContext(request, response);
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());		
			PrintStream out = new PrintStream(response.getOutputStream());
			if (key != null) {
				out.print(i18nAccess.getAllText(key, request.getParameter("default")));
			} else if (viewKey != null) {
				out.print(i18nAccess.getViewText(viewKey, request.getParameter("default")));
			} else if (editKey != null) {
				out.print(i18nAccess.getViewText(editKey, request.getParameter("default")));
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

}
