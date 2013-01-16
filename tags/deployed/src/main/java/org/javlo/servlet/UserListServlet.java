package org.javlo.servlet;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.StringHelper;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.User;
import org.javlo.user.UserEditFilter;
import org.javlo.user.UserFactory;
import org.javlo.utils.CSVFactory;


public class UserListServlet extends HttpServlet {

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
        			
            		if (!AdminUserSecurity.getInstance().haveRight(user, AdminUserSecurity.USER_ROLE) && !AdminUserSecurity.getInstance().haveRight(user, AdminUserSecurity.ADMIN_USER_ROLE)) {
            			throw new ServletException("user:"+user.getLogin()+" have no suffisant right.");
            		}
            		
            		if (!AdminUserSecurity.getInstance().haveRight(user, AdminUserSecurity.ADMIN_USER_ROLE) && admin) {
            			throw new ServletException("user:"+user.getLogin()+" have no suffisant right for download admin user list.");
            		}
        			
                    boolean filtered = false;
                    
                    String filteredString = requestService.getParameter("filtered", "false");
                    filtered = StringHelper.isTrue(filteredString);

                    response.setContentType("application/csv");
        			if (admin) {
        				userFact = AdminUserFactory.createUserFactory(globalContext, request.getSession());
        			}

        			List<IUserInfo> users = userFact.getUserInfoList();
        			IUserInfo userInfo = userFact.createUserInfos();
        			String[] labels = BeanHelper.getAllLabels(userInfo);
                    String[][] usersArray = new String[users.size() + 1][labels.length];
                    usersArray[0] = labels;
                    CSVFactory cvsFact = new CSVFactory(usersArray);
                    
                    UserEditFilter userFilter = editContext.getUserEditFilter();

                    cvsFact.exportRowCSV(response.getOutputStream(), labels);

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
                                    	String rolesRow = StringHelper.collectionToString(localUserInfo.getRoles(), ""+IUserInfo.ROLES_SEPARATOR);
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

                        if (printRow) {
                            cvsFact.exportRowCSV(response.getOutputStream(), values);
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