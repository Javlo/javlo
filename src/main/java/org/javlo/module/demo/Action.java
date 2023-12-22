package org.javlo.module.demo;

import java.lang.reflect.Method;

import jakarta.servlet.http.HttpSession;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.security.annotation.HasAllRole;
import org.javlo.security.annotation.HasAnyRole;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;

public class Action extends AbstractModuleAction {

	@Override
	public String getActionGroupName() {
		return "demo";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext moduleContext) {
		if (ctx.getRequest().getAttribute("demoMessage") == null) {
			ctx.getRequest().setAttribute("demoMessage", "message from action prepare");
		}
		return null;
	}

	public static final String performTest(ContentContext ctx) {
		ctx.getRequest().setAttribute("demoMessage", "test performed");
		return null;
	}
	
	@HasAnyRole (roles = {AdminUserSecurity.CONTENT_ROLE})
	public String performTestAnyRoles(ContentContext ctx, RequestService rs) throws Exception {
		System.out.println(">>>>>>>>> Action.performTestAnyRoles : ACCESS"); //TODO: remove debug trace
		return null;
	}
	
	@HasAllRole (roles = {AdminUserSecurity.CONTENT_ROLE})
	public static String performTestAllRoles(ContentContext ctx, RequestService rs) throws Exception {
		System.out.println(">>>>>>>>> Action.performTestAllRoles : ACCESS"); //TODO: remove debug trace
		return null;
	}

	@Override
	public Boolean haveRight(HttpSession session, User user) throws ModuleException {
		return true;
	}
	
	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return true;
	}
	
	
	public static void main(String[] args) {
		Action testClass = new Action();
		for (Method m : Action.class.getMethods()) {
			System.out.println(m.getName());
			HasAnyRole a = m.getAnnotation(HasAnyRole.class);			
			System.out.println("a : "+a);				
			System.out.println("");
		}
	}

}
