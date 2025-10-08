/** 
 * Created on Aug 13, 2003
 */
package org.javlo.actions;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.service.RequestService;
import org.javlo.service.google.translation.GoogleSheetService;
import org.javlo.service.notification.NotificationService;
import org.javlo.user.User;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author pvandermaesen list of actions for search in cms.
 */
public class GoogleSheetAction implements IAction {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(GoogleSheetAction.class.getName());


	@Override
	public String getActionGroupName() {
		return "gsheet";
	}

	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return true;
	}

	public static String performHtml(RequestService rs, ContentContext ctx, GlobalContext globalContext, NotificationService notif, User user, HttpSession session) throws ParseException, GeneralSecurityException, IOException {
		String project = rs.getParameter("project");
		String sheet = rs.getParameter("sheet");
		String googleId = rs.getParameter("spreadsheetId");
		String cssClass = rs.getParameter("cssClass");

		logger.info("project : " + project);
		logger.info("sheet : " + sheet);
		logger.info("googleId : " + googleId);

		if (cssClass == null || cssClass.isEmpty()) {
			cssClass = "spreadsheet-table"; // valeur par défaut
		}

		String credentialPath = ctx.getGlobalContext().getCredentialPath(project);
		if (credentialPath == null) {
			logger.warning("project not found : " + project);
			return "project not found : " + project;
		} else {
			logger.info("project found : " + credentialPath);
		}

		GoogleSheetService service = new GoogleSheetService(credentialPath, googleId);

		HttpServletResponse response = ctx.getResponse();
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		String htmlContent = service.readAllAsHTML(sheet, cssClass);
		response.getWriter().write(htmlContent);
		response.getWriter().flush();
		response.getWriter().close();
		ctx.setStopRendering(true);

		return null;
	}

	public static String performCsv(RequestService rs, ContentContext ctx, GlobalContext globalContext, NotificationService notif, User user, HttpSession session) throws ParseException, GeneralSecurityException, IOException {
		String project = rs.getParameter("project");
		String sheet = rs.getParameter("sheet");
		String googleId = rs.getParameter("spreadsheetId");

		logger.info("project : " + project);
		logger.info("sheet : " + sheet);
		logger.info("googleId : " + googleId);

		String credentialPath = ctx.getGlobalContext().getCredentialPath(project);
		if (credentialPath == null) {
			logger.warning("project not found : " + project);
			return "project not found : " + project;
		} else {
			logger.info("project found : " + credentialPath);
		}

		GoogleSheetService service;
		try {
			service = new GoogleSheetService(credentialPath, googleId);
		} catch (Exception e) {
			logger.severe("Failed to initialize GoogleSheetService: " + e.getMessage());
			return "Error initializing GoogleSheetService";
		}

		HttpServletResponse response = ctx.getResponse();
		try {
			response.setContentType("text/csv");
			response.setHeader("Content-Disposition", "attachment; filename=\"data.csv\"");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(service.readAllAsCSV(sheet));
			response.getWriter().flush();
			response.getWriter().close();
		} catch (IOException e) {
			logger.severe("Error writing CSV to response: " + e.getMessage());
			return "Error writing CSV to response";
		}

		ctx.setStopRendering(true);

		return null;
	}

	public static String performUpdateCell(RequestService rs, ContentContext ctx) throws ParseException, GeneralSecurityException, IOException {
		String project = rs.getParameter("project");
		String sheet = rs.getParameter("sheet");
		String googleId = rs.getParameter("spreadsheetId");
		String cell = rs.getParameter("cell");
		String value = rs.getParameter("value");

		logger.info("project : "+project);
		logger.info("sheet : "+sheet);
		logger.info("googleId : "+googleId);
		logger.info("cell : "+cell);
		logger.info("value : "+value);

		GoogleSheetService service = new GoogleSheetService(ctx.getGlobalContext().getCredentialPath(project), googleId);
		
		List<List<Object>> values = new LinkedList<>();
		List<Object> list = new LinkedList<>();
		list.add(value);
		values.add(list);
		service.writeRange(cell, values);

		return null;
	}

}
