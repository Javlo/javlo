/*
 * Created on Aug 13, 2003
 */
package org.javlo.actions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.javlo.component.core.ComponentFactory;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.LangHelper;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IMacro;
import org.javlo.macro.core.MacroFactory;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.security.annotation.HasAllRole;
import org.javlo.security.annotation.HasAnyRole;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;
import org.javlo.user.exception.JavloSecurityException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * @author pvandermaesen manage the actions for wcms.
 */
public class ActionManager {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ActionManager.class.getName());

	static final String METHOD_PREFIX = "perform";

	static final String BEFORE_ACTION_METHOD = "beforeAction";

	static final String ACTION_SEPARATOR = ".";

	static IAction[] actionGroup = null;
	
	static IAction unsecureActionGroup = null;

	static final String removeGroup(String actionName) {
		return actionName.substring(actionName.indexOf(".") + 1);
	}

	static final String formatActionComponentName(String name) {
		String workingName = removeGroup(name);
		return formatActionName(workingName);
	}

	static final String formatActionName(String name) {
		if (name == null || name.length() == 0) {
			return "";
		}
		String start = name.substring(0, 1);
		String end = "";
		if (name.length() > 1) {
			end = name.substring(1, name.length());
		}
		return start.toUpperCase() + end.toLowerCase();
	}

	private static IAction getAction(ContentContext ctx, String group) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, ModuleException {

		IAction outAction = getActionModule(ctx.getRequest(), group);
		if (outAction == null) {
			outAction = getActionComponent(ctx, group);
		} else {
			ModulesContext.getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext()).setCurrentModuleByActionGroup(group);
		}
		if (outAction == null) {
			outAction = getActionMacro(ctx, group);
		}
		return outAction;
	}

	public static IAction getActionModule(HttpServletRequest request, String group) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, ModuleException {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		ModulesContext moduleContext = ModulesContext.getInstance(request.getSession(), globalContext);
		Collection<Module> modules = moduleContext.getModules();
		IAction action = null;
		for (Module module : modules) {
			action = module.getAction();
			if (group.equals(action.getActionGroupName())) {
				return action;
			}
		}
		return null;
	}

	public static IAction getActionComponent(ContentContext ctx, String group) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		Object[] comp = ComponentFactory.getComponents(ctx);
		IAction action = null;
		for (int i = 0; (i < comp.length) && (action == null); i++) {
			if (comp[i] instanceof IAction) {
				action = (IAction) comp[i];
				if (!group.equals(action.getActionGroupName())) {
					action = null;
				}
			}
		}

		IAction[] specialAction = getSpecialActionGroup();
		for (int i = 0; (i < specialAction.length) && (action == null); i++) {
			action = specialAction[i];
			if (!group.equals(action.getActionGroupName())) {
				action = null;
			}
		}
		
		if (action == null) {
			if (group.equals(getUnsecureActionGroup().getActionGroupName())) {
				action = getUnsecureActionGroup();
			}
		}

		return action;
	}
	
	public static IAction getUnsecureActionGroup() {
		if (unsecureActionGroup == null) {
			unsecureActionGroup = new UnsecureAction();
		}
		return unsecureActionGroup;
	}

	public static IAction getActionMacro(ContentContext ctx, String group) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		for (IMacro macro : MacroFactory.getInstance(ctx).getMacros()) {
			if (macro instanceof IAction) {
				IAction action = (IAction) macro;
				if (action.getActionGroupName().equals(group)) {
					return action;
				}
			}
		}
		return null;
	}

	public static String getActionGroup(String actionName) {
		String grp = null;
		StringTokenizer tokenizer = new StringTokenizer(actionName, ACTION_SEPARATOR);
		if (tokenizer.hasMoreTokens()) {
			grp = tokenizer.nextToken();
			if (!tokenizer.hasMoreTokens()) {
				grp = null;
			}
		}
		return grp;
	}

	/**
	 * retrun special action group for search or other element
	 * 
	 * @return a list of IAction implementation
	 */
	private static IAction[] getSpecialActionGroup() {
		if (actionGroup == null) {
			IAction[] outActionGroup = new IAction[6];
			outActionGroup[0] = new SearchActions();
			outActionGroup[1] = new ViewActions();
			outActionGroup[2] = new TimeTravelerActions();
			outActionGroup[3] = new DataAction();
			outActionGroup[4] = new MobileAction();
			outActionGroup[5] = new GoogleSheetAction();
			actionGroup = outActionGroup;
		}
		return actionGroup;
	}

	static public final String perform(String actionName, HttpServletRequest request, HttpServletResponse response, boolean unsecure) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		logger.fine("perform action : " + actionName);

		/*
		 * AdminUserSecurity adminUserSecurity =
		 * AdminUserSecurity.getInstance(request.getSession().getServletContext());
		 * IUserFactory userFactory =
		 * AdminUserFactory.createAdminUserFactory(globalContext, request.getSession());
		 * if
		 * (!adminUserSecurity.haveRight(userFactory.getCurrentUser(request.getSession()
		 * ), removeGroup(actionName).toLowerCase() )) { I18nAccess i18nAccess =
		 * I18nAccess.getInstance(request); ContentContext ctx =
		 * ContentContext.getContentContext(request, response); MessageRepository
		 * msgRepo = MessageRepository.getInstance(ctx);
		 * msgRepo.setGlobalMessageAndNotification(ctx,new
		 * GenericMessage(i18nAccess.getText("global.message.noright")+
		 * " ("+actionName+')', GenericMessage.ERROR)); return null; }
		 */

		String group = getActionGroup(actionName);
		String message = null;
		
		if (unsecure && !UnsecureAction.TYPE.equals(group)) {
			return "security error !";
		}

		ContentContext ctx = ContentContext.getContentContext(request, response);

		try {
			IAction action;
			ModulesContext moduleContext = ModulesContext.getInstance(request.getSession(), globalContext);
			if (group == null) {
				action = moduleContext.getCurrentModule().getAction();
			} else {
				action = getAction(ctx, group);
			}
			AdminUserFactory adminUserFactory = AdminUserFactory.createUserFactory(globalContext, request.getSession());
			User currentUser = adminUserFactory.getCurrentUser(request.getSession());

			if (action != null) {
				/** security **/
				if (action instanceof IModuleAction) { // if module action
					// if (currentUser == null) {
					// ctx.setNeedRefresh(true);
					// }
					if (!AdminUserSecurity.getInstance().isAdmin(currentUser)) {
						if (!moduleContext.getCurrentModule().haveRight(request.getSession(), currentUser)) {
							I18nAccess i18nAccess = I18nAccess.getInstance(request);
							MessageRepository msgRepo = MessageRepository.getInstance(ctx);
							msgRepo.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("global.message.noright") + " (" + actionName + ')', GenericMessage.ERROR), true);
							return null;
						}
					}
				}
				if (action.haveRight(ctx, actionName)) {
					message = invokeAction(request, response, action, actionName);
					logger.fine("executed action : '" + actionName + "' return : " + message);
				} else {
					logger.warning("executed action refused : '" + actionName + "' user : " + currentUser + " on " + globalContext.getContextKey());
					message = "security error.";
				}
			} else {
				message = "actions class not found : " + actionName + " - group:" + group + "  (user:" + ctx.getCurrentEditUser() + ")";
				logger.severe(message);
				if (!ctx.isAsViewMode() && currentUser == null && actionName != null) {
					ctx.setNeedRefresh(true);
				}
			}
		} catch (Throwable t) {
			if (t.getCause() instanceof JavloSecurityException) {
				message = t.getMessage();
				logger.warning(message);
				t.printStackTrace();
			} else {
				t.printStackTrace();
				if (t.getCause() != null && t.getCause().getMessage() != null) {
					message = "error in action '" + actionName + "' : " + t.getCause().getMessage();
				} else {
					message = "error in action '" + actionName + "' contact administrator, current time : " + StringHelper.renderTime(new Date());
				}
				logger.severe(message);
			}
		}

		MessageRepository msgRepo = MessageRepository.getInstance(ctx);
		if (message != null) {
			msgRepo.setGlobalMessageAndNotification(ctx, new GenericMessage(message, GenericMessage.ERROR), true);
		}

		String newModule = RequestService.getInstance(request).getParameter("module", null);
		if (newModule != null) {
			ModulesContext.getInstance(request.getSession(), GlobalContext.getInstance(request)).setCurrentModule(newModule);
		}

		return message;
	}

	private static String invokeAction(HttpServletRequest request, HttpServletResponse response, IAction action, String actionName) {
		Method method = null;
		String methodName = METHOD_PREFIX + formatActionComponentName(actionName);
		String message = null;
		try {
			Method[] methods = action.getClass().getDeclaredMethods();
			for (Method m : methods) {
				if (m.getName().equalsIgnoreCase(methodName)) {
					method = m;
					break;
				}
			}
			if (method == null) {
				methods = action.getClass().getSuperclass().getMethods();
				for (Method m : methods) {
					if (m.getName().equalsIgnoreCase(methodName)) {
						method = m;
						break;
					}
				}
			}
			if (method != null) {
				Class<?>[] clazzes = method.getParameterTypes();
				Object[] paramsInstance = new Object[clazzes.length];
				for (int i = 0; i < clazzes.length; i++) {
					paramsInstance[i] = LangHelper.smartInstance(request, response, clazzes[i]);
				}
				if (method.getReturnType().isAssignableFrom(String.class)) {
					HasAllRole hasAllRole = method.getAnnotation(HasAllRole.class);
					if (hasAllRole != null && hasAllRole.roles() != null && hasAllRole.roles().length > 0) {
						ContentContext ctx = ContentContext.getContentContext(request, response);
						AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance();
						for (String role : hasAllRole.roles()) {
							if (!adminUserSecurity.canRole(ctx.getCurrentUser(), role)) {
								logger.warning(ctx.getGlobalContext().getContextKey() + "-HasAllRole : security error user have not access to method : " + method.getName() + " role:" + role + " user:" + ctx.getCurrentUserId());
								return "security error.";
							}
						}
					}

					HasAnyRole hasAnyRole = method.getAnnotation(HasAnyRole.class);
					if (hasAnyRole != null && hasAnyRole.roles() != null && hasAnyRole.roles().length > 0) {
						ContentContext ctx = ContentContext.getContentContext(request, response);
						AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance();
						boolean hasRole = false;
						for (String role : hasAnyRole.roles()) {
							if (adminUserSecurity.canRole(ctx.getCurrentUser(), role)) {
							}
						}
						if (!hasRole) {
							logger.warning(ctx.getGlobalContext().getContextKey() + "-hasAnyRole : security error user have not access to method : " + method.getName() + " user:" + ctx.getCurrentUserId());
							return "security error.";
						}
					}

					return (String) method.invoke(action, paramsInstance);
				} else {
					message = "bad return type for a action method (must be a String) : " + method;
					logger.severe(message);
				}
			} else {
				message = "method not found : " + methodName + " on " + action.getClass().getCanonicalName();
				logger.warning(message);
			}
		} catch (

		Exception e) {
			if (e.getCause() != null && e.getCause() instanceof JavloSecurityException) {
				message = e.getCause().getMessage();
				logger.warning(message);
			} else {
				e.printStackTrace();
				String errorNum = StringHelper.getRandomId();
				message = "error - method : " + methodName + " on : " + action.getClass().getCanonicalName() + "  msg:" + e.getMessage() + " num:" + errorNum;
				try {
					I18nAccess i18nAccess = I18nAccess.getInstance(ContentContext.getContentContext(request, response));
					message = i18nAccess.getViewText("message.error.technical-error", message) + " [" + errorNum + ']';
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				logger.warning(message);
			}
		}
		logger.warning("action '" + actionName + "' message : " + message);
		return message;
	}

}
