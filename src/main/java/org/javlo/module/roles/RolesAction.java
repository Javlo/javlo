package org.javlo.module.roles;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;
import org.javlo.template.TemplateFactory;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.Role;
import org.javlo.user.RolesContext;
import org.javlo.user.RolesFactory;

public class RolesAction extends AbstractModuleAction {

	public RolesAction() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getActionGroupName() {
		return "roles";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		RolesContext rolesContext = RolesContext.getInstance(ctx.getRequest());
		RolesFactory rolesFactory = RolesFactory.getInstance(ctx.getGlobalContext());
		Role role = rolesFactory.getInstance(ctx.getGlobalContext()).getRole(ctx.getGlobalContext(), rolesContext.getRole());
		ctx.getRequest().setAttribute("role", role);

		AdminUserFactory adminUserFactory = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
		if (role != null) {
			ctx.getRequest().setAttribute("users", adminUserFactory.getUserInfoForRoles(new String[] { role.getName() }));
		}

		List<String> templateIncluded = StringHelper.stringToCollection(role.getLocalTemplateIncluded(),",");		
		List<String> templateExcluded = StringHelper.stringToCollection(role.getLocalTemplateExcluded(),",");		
		Set<String> allTemplates = new HashSet(TemplateFactory.getTemplates(ctx.getRequest().getSession().getServletContext()).keySet());		
		allTemplates.removeAll(templateIncluded);
		allTemplates.removeAll(templateExcluded);
		Collections.sort(templateIncluded);
		Collections.sort(templateExcluded);
		List<String> allTemplatesList = new LinkedList<String>(allTemplates);
		Collections.sort(allTemplatesList);
		ctx.getRequest().setAttribute("templateExcluded", templateExcluded);
		ctx.getRequest().setAttribute("templateIncluded", templateIncluded);
		ctx.getRequest().setAttribute("templateInherited", allTemplatesList);
		
		if (role.getParentRole() != null) {
			if (role.getParentRole().getTemplateIncluded().length() > 0) {
				ctx.getRequest().setAttribute("templateIncludedInheritedSize", role.getParentRole().getTemplateIncluded().split(",").length);
			}
			if (role.getParentRole().getTemplateExcluded().length() > 0) {
				ctx.getRequest().setAttribute("templateExcludedInheritedSize", role.getParentRole().getTemplateExcluded().split(",").length);
			}
		}

		return super.prepare(ctx, modulesContext);
	}

	public static String performChangeRole(RequestService rs, ContentContext ctx, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) {
		RolesContext rolesContext = RolesContext.getInstance(ctx.getRequest());
		rolesContext.setRole(rs.getParameter("role", null));
		return null;
	}

	public static String performUpdate(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		RolesFactory rolesFactory = RolesFactory.getInstance(ctx.getGlobalContext());
		Role role = rolesFactory.getInstance(ctx.getGlobalContext()).getRole(ctx.getGlobalContext(), rs.getParameter("role", null));
		role.setMailingSenders(rs.getParameter("mailingSenders", null));		
		role.setParent(rs.getParameter("parent", ""));
		List<String> templateSelected = rs.getParameterListValues("templateSelected", Collections.EMPTY_LIST);
		if (templateSelected.size() > 0) {
			Collection<String> templateIncluded = new HashSet<String>(StringHelper.stringToCollection(role.getLocalTemplateIncluded(),","));
			Collection<String> templateExcluded = new HashSet<String>(StringHelper.stringToCollection(role.getLocalTemplateExcluded(),","));
			if (rs.getParameter("templateAction", "").equals("inherited")) {
				templateIncluded.removeAll(templateSelected);
				templateExcluded.removeAll(templateSelected);
			} else if (rs.getParameter("templateAction", "").equals("included")) {
				templateIncluded.addAll(templateSelected);
				templateExcluded.removeAll(templateSelected);
			} else if (rs.getParameter("templateAction", "").equals("excluded")) {
				templateIncluded.removeAll(templateSelected);
				templateExcluded.addAll(templateSelected);
			}			
			role.setTemplateIncluded(StringHelper.collectionToString(templateIncluded,","));
			role.setTemplateExcluded(StringHelper.collectionToString(templateExcluded,","));			
		}
		rolesFactory.clear();

		return null;
	}

}