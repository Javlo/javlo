package org.javlo.user;

import javax.servlet.http.HttpServletRequest;

import org.javlo.context.GlobalContext;

public class RolesContext {
	
	private String role = null;
	
	private static final String KEY = "rolesContext";

	public RolesContext() {
		// TODO Auto-generated constructor stub
	}
	
	public static RolesContext getInstance(HttpServletRequest request) {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		RolesContext outContext = (RolesContext)request.getSession().getAttribute(KEY);
		if (outContext == null) {
			outContext = new RolesContext();
			if (globalContext.getAdminUserRoles().size() > 0) {				
				outContext.setRole(globalContext.getAdminUserRoles().iterator().next());
			}
			request.getSession().setAttribute(KEY, outContext);
		}
		return outContext;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

}
