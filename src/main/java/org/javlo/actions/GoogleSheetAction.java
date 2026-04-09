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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author pvandermaesen list of actions for search in cms.
 */
public class GoogleSheetAction implements IAction {

	protected static Logger logger = Logger.getLogger(GoogleSheetAction.class.getName());

	@Override
	public String getActionGroupName() {
		return "gsheet";
	}

	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return true;
	}

	/**
	 * Resolves and validates the credential path for the given project.
	 * Returns null and logs a warning if the project is not found.
	 */
	private static String resolveCredentialPath(ContentContext ctx, String project) {
		String credentialPath = ctx.getGlobalContext().getCredentialPath(project);
		if (credentialPath == null) {
			logger.warning("project not found : " + project);
		} else {
			logger.info("project found : " + credentialPath);
		}
		return credentialPath;
	}

	public static String performHtml(RequestService rs, ContentContext ctx, GlobalContext globalContext, NotificationService notif, User user, HttpSession session) throws ParseException, GeneralSecurityException, IOException {
		String project = rs.getParameter("project");
		String sheet = rs.getParameter("sheet");
		String googleId = rs.getParameter("spreadsheetId");
		String cssClass = rs.getParameter("cssClass");

		logger.info("project : " + project + ", sheet : " + sheet + ", googleId : " + googleId);

		if (cssClass == null || cssClass.isEmpty()) {
			cssClass = "spreadsheet-table";
		}

		String credentialPath = resolveCredentialPath(ctx, project);
		if (credentialPath == null) {
			return "project not found : " + project;
		}

		GoogleSheetService service = new GoogleSheetService(credentialPath, googleId);

		HttpServletResponse response = ctx.getResponse();
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(service.readAllAsHTML(sheet, cssClass, ctx.getGlobalContext().getSpecialConfig().isGsheetSecured()));
		response.getWriter().flush();
		response.getWriter().close();
		ctx.setStopRendering(true);

		return null;
	}

	public static String performCsv(RequestService rs, ContentContext ctx, GlobalContext globalContext, NotificationService notif, User user, HttpSession session) throws ParseException, GeneralSecurityException, IOException {
		String project = rs.getParameter("project");
		String sheet = rs.getParameter("sheet");
		String googleId = rs.getParameter("spreadsheetId");

		logger.info("project : " + project + ", sheet : " + sheet + ", googleId : " + googleId);

		String credentialPath = resolveCredentialPath(ctx, project);
		if (credentialPath == null) {
			return "project not found : " + project;
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
			response.getWriter().write(service.readAllAsCSV(sheet, ctx.getGlobalContext().getSpecialConfig().isGsheetSecured()));
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

		logger.info("project : " + project + ", sheet : " + sheet + ", cell : " + cell + ", value : " + value);

		String credentialPath = resolveCredentialPath(ctx, project);
		if (credentialPath == null) {
			return "project not found : " + project;
		}

		GoogleSheetService service = new GoogleSheetService(credentialPath, googleId);

		List<List<Object>> values = new LinkedList<>();
		List<Object> list = new LinkedList<>();
		list.add(value);
		values.add(list);
		service.writeRange(cell, values);

		return null;
	}

	/**
	 * Updates a full row in a Google Sheet.
	 *
	 * Parameters:
	 *   project       - credential project name
	 *   spreadsheetId - Google spreadsheet ID
	 *   sheet         - sheet tab name
	 *   row           - 1-based row number to update
	 *   startColumn   - (optional) starting column letter, default "A"
	 *   values        - comma-separated list of cell values for the row
	 *
	 * Example: sheet=Sheet1, row=3, values=Alice,30,Paris
	 */
	public static String performUpdateRow(RequestService rs, ContentContext ctx) throws ParseException, GeneralSecurityException, IOException {
		String project = rs.getParameter("project");
		String sheet = rs.getParameter("sheet");
		String googleId = rs.getParameter("spreadsheetId");
		String rowParam = rs.getParameter("row");
		String startColumn = rs.getParameter("startColumn");
		String valuesParam = rs.getParameter("values");

		logger.info("project : " + project + ", sheet : " + sheet + ", row : " + rowParam + ", values : " + valuesParam);

		if (rowParam == null || rowParam.isEmpty()) {
			return "missing parameter: row";
		}
		if (valuesParam == null || valuesParam.isEmpty()) {
			return "missing parameter: values";
		}

		int rowNumber;
		try {
			rowNumber = Integer.parseInt(rowParam.trim());
		} catch (NumberFormatException e) {
			return "invalid row number: " + rowParam;
		}

		if (startColumn == null || startColumn.isEmpty()) {
			startColumn = "A";
		}

		String credentialPath = resolveCredentialPath(ctx, project);
		if (credentialPath == null) {
			return "project not found : " + project;
		}

		List<Object> rowValues = Arrays.asList((Object[]) valuesParam.split(",", -1));

		GoogleSheetService service = new GoogleSheetService(credentialPath, googleId);
		service.writeRow(sheet, rowNumber, startColumn, rowValues);

		return null;
	}

}
