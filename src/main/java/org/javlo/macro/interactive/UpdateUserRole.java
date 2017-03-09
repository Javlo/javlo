package org.javlo.macro.interactive;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextFactory;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;

public class UpdateUserRole implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(UpdateUserRole.class.getName());

	@Override
	public String getName() {
		return "update-user-role";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		return null;
	}

	@Override
	public boolean isAdmin() {
		return true;
	}

	@Override
	public String getActionGroupName() {
		return getName();
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/update-user-role.jsp";
	}

	@Override
	public String prepare(ContentContext ctx) {
		List<String> roles = new LinkedList<String>();
		IUserFactory userFactory = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
		roles.addAll(userFactory.getAllRoles(ctx.getGlobalContext(), ctx.getRequest().getSession()));
		Collections.sort(roles);
		ctx.getRequest().setAttribute("roles", roles);
		return null;
	}

	public static String performUpdate(RequestService rs, EditContext editCtx, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		IUserFactory userFactory = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
		if (!AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser())) {
			logger.severe("user : " + ctx.getCurrentEditUser() + " try to execute unauthorized macro.");
			return "security exception !";
		}
		Set<String> roles = new HashSet<String>();
		roles.addAll(rs.getParameterListValues("role", Collections.EMPTY_LIST));
		boolean add = rs.getParameter("add", null) != null;
		
		int count=0;
		
		if (ctx.getGlobalContext().isMaster()) {			
			for (GlobalContext site : GlobalContextFactory.getAllGlobalContext(ctx.getRequest().getSession().getServletContext())) {
				logger.info("[M] add:" + add + "  site:"+site.getContextKey()+"  roles:" + roles);
				userFactory = AdminUserFactory.createUserFactory(site, ctx.getRequest().getSession());
				for (IUserInfo user : userFactory.getUserInfoList()) {
					int backupRolesSize = user.getRoles().size();
					if (add) {
						user.addRoles(roles);
					} else {
						user.removeRoles(roles);
					}
					if (user.getRoles().size() != backupRolesSize) {
						count++;
					}
				}
				if (count>0) {
					userFactory.store();
				}
			}
		} else {
			logger.info("add:" + add + "  site:"+ctx.getGlobalContext().getContextKey()+"  roles:" + roles);
			for (IUserInfo user : userFactory.getUserInfoList()) {
				int backupRolesSize = user.getRoles().size();
				if (add) {
					user.addRoles(roles);
				} else {
					user.removeRoles(roles);
				}
				if (user.getRoles().size() != backupRolesSize) {
					count++;					
				}
			}
			if (count>0) {
				userFactory.store();
			}
		}		
		messageRepository.setGlobalMessage(new GenericMessage(count+" user(s) modified", GenericMessage.INFO));
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
	
	@Override
	public boolean isAdd() {
		return false;
	}
	
	@Override
	public boolean isInterative() {	
		return true;
	}
}
