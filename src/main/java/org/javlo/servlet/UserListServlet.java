package org.javlo.servlet;

import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.component.web2.EventRegistration;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.User;
import org.javlo.user.UserEditFilter;
import org.javlo.user.UserFactory;
import org.javlo.utils.CSVFactory;
import org.javlo.utils.XLSTools;

public class UserListServlet extends HttpServlet {

	private static Logger logger = Logger.getLogger(UserListServlet.class.getName());

	private static final long serialVersionUID = 1L;

	public void init() throws ServletException {
		super.init();
	}

	public void destroy() {
		super.destroy();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		process(request, response);
	}

	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			GlobalContext globalContext = GlobalContext.getInstance(request);
			EditContext editContext = EditContext.getInstance(globalContext, request.getSession());

			if (editContext.getUserPrincipal() != null) {
				IUserFactory userFact = UserFactory.createUserFactory(globalContext, request.getSession());
				User user = userFact.getCurrentUser(request.getSession());

				// TODO: check role
				if (user != null) {

					RequestService requestService = RequestService.getInstance(request);

					boolean admin = requestService.getParameter("admin", null) != null;
					String eventId = requestService.getParameter("event", null);
					List<String> confirmedUser = null;
					if (eventId != null) {
						EventRegistration event = null;
						ContentContext ctx = ContentContext.getContentContext(request, response);
						ContentService content = ContentService.getInstance(ctx.getRequest());
						event = (EventRegistration) content.getComponent(ctx, eventId);
						if (event == null) {
							logger.warning("event not found : " + eventId);
							response.setStatus(HttpServletResponse.SC_NO_CONTENT);
							return;
						}
						confirmedUser = event.getConfirmedUser(ctx);
					}

					if (!AdminUserSecurity.getInstance().canRole(user, AdminUserSecurity.USER_ROLE) && !AdminUserSecurity.getInstance().haveRight(user, AdminUserSecurity.ADMIN_USER_ROLE)) {
						throw new ServletException("user:" + user.getLogin() + " have no suffisant right.");
					}

					if (!AdminUserSecurity.getInstance().haveRight(user, AdminUserSecurity.ADMIN_USER_ROLE) && admin) {
						throw new ServletException("user:" + user.getLogin() + " have no suffisant right for download admin user list.");
					}

					boolean filtered = false;

					String filteredString = requestService.getParameter("filtered", "false");
					filtered = StringHelper.isTrue(filteredString);

					if (admin) {
						userFact = AdminUserFactory.createUserFactory(globalContext, request.getSession());
					}

					List<IUserInfo> users = userFact.getUserInfoList();
					IUserInfo userInfo = userFact.createUserInfos();
					String[] labels = BeanHelper.getAllLabels(userInfo);
					String[][] usersArray = new String[users.size() + 1][labels.length];
					usersArray[0] = labels;
					String separtor = ",";
					CSVFactory csvFact = new CSVFactory(usersArray);
					if (StringHelper.isTrue(requestService.getParameter("excel", null))) {
						separtor = ";";
					}

					UserEditFilter userFilter = editContext.getUserEditFilter();

					csvFact.appendRow(labels);
					for (IUserInfo localUserInfo : users) {
						String[] values = (String[]) BeanHelper.getAllValues(localUserInfo);
						boolean printRow = true;
						if (filtered) {
							labels = BeanHelper.getAllLabels(localUserInfo);
							for (int j = 0; j < labels.length; j++) {
								if (labels[j].equals("rolesRaw")) {
									labels[j] = "roles";
								}
								if (userFilter.getFieldContain(labels[j]).trim().length() > 0) {
									if (labels[j].equals("roles")) {
										String rolesRow = StringHelper.collectionToString(localUserInfo.getRoles(), "" + IUserInfo.ROLES_SEPARATOR);
										if (rolesRow.toLowerCase().indexOf(userFilter.getFieldContain(labels[j]).toLowerCase()) < 0) {
											printRow = false;
										}
									} else {
										if (values[j].toLowerCase().indexOf(userFilter.getFieldContain(labels[j]).toLowerCase()) < 0) {
											printRow = false;
										}
									}
								}
							}
						}
						if (confirmedUser != null && printRow) {
							if (!confirmedUser.contains(localUserInfo.getLogin())) {
								printRow = false;
							}
						}
						if (printRow) {
							csvFact.appendRow(values);
						}
					}
					String fileExt = StringHelper.getFileExtension(request.getRequestURI());
 					if (fileExt.toLowerCase().equals("csv")) {
						response.setContentType("application/csv");
						csvFact.exportCSV(response.getOutputStream(), separtor);						
					} else if (fileExt.toLowerCase().equals("xls") || fileExt.toLowerCase().equals("xlsx")) {
						response.setContentType(ResourceHelper.getFileExtensionToMineType(fileExt));
						if (fileExt.equals("xls")) {							
							XLSTools.writeXLS(XLSTools.getCellArray(csvFact.getArray()), response.getOutputStream());
						} else 	{
							XLSTools.writeXLSX(XLSTools.getCellArray(csvFact.getArray()), response.getOutputStream());
						}
					}
					response.getOutputStream().flush();
					return;
				}
			}

			response.setContentType("text/html; charset=" + ContentContext.CHARACTER_ENCODING);
			getServletContext().getRequestDispatcher("/jsp/edit/login.jsp").include(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}