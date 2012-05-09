/*
 * Created on Aug 13, 2003
 */
package org.javlo.actions;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.admin.AdminAction;
import org.javlo.component.core.ComponentFactory;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.ecom.EcomActions;
import org.javlo.helper.ConfigHelper;
import org.javlo.helper.StringHelper;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.Module;
import org.javlo.module.ModuleContext;

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

	static final String formatActionComponentName(String name) {
		String workingName = name.substring(name.indexOf(".") + 1);
		return formatActionName(workingName);
	}

	static final String formatActionName(String name) {
		String start = name.substring(0, 1);
		String end = "";
		if (name.length() > 1) {
			end = name.substring(1, name.length());
		}
		return start.toUpperCase() + end.toLowerCase();
	}

	public static IAction getAction(HttpServletRequest request, String group) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		IAction outAction = getActionComponent(request, group);
		if (outAction != null) {
			return getActionModule(request, group);
		} else {
			return outAction;
		}
	}

	public static IAction getActionModule(HttpServletRequest request, String group) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		ModuleContext moduleContext = ModuleContext.getInstance(globalContext, request.getSession());
		Collection<Module> modules = moduleContext.getAllModules();
		IAction action = null;
		for (Module module : modules) {
			action = module.getAction();
			if (group.equals(action.getActionGroupName())) {
				return action;
			}
		}
		return action;
	}

	public static IAction getActionComponent(HttpServletRequest request, String group) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		Object[] comp = ComponentFactory.getComponents(globalContext);
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

		return action;
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
		IAction[] outActionGroup = new IAction[6];
		outActionGroup[0] = new SearchActions();
		outActionGroup[1] = new MailingActions();
		outActionGroup[2] = new AdminAction();
		outActionGroup[3] = new EcomActions();
		outActionGroup[4] = new ViewActions();
		outActionGroup[5] = new TimeTravelerActions();
		return outActionGroup;
	}

	static public final String perform(String actionName, HttpServletRequest request, HttpServletResponse response) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		logger.fine("perform action : " + actionName);

		String group = getActionGroup(actionName);
		String message = null;

		try {
			IAction action;
			if (group == null) {
				ModuleContext moduleContext = ModuleContext.getInstance(globalContext, request.getSession());
				action = moduleContext.getCurrentModule().getAction();
			} else {
				action = getAction(request, group);
			}

			if (action != null) {
				message = invokeAction(request, response, action, actionName);
			} else {
				message = "actions class not found : " + action;
				logger.severe(message);;
			}
		} catch (Throwable t) {
			if (t.getMessage() != null) {
				message = "error in action '" + actionName + "' : " + t.getMessage();
			} else {
				message = "error in action '" + actionName + "' contact administrator, current time : " + StringHelper.renderTime(new Date());
			}
			logger.severe(message);
			logger.warning(message);
			t.printStackTrace();
		}

		ContentContext ctx = ContentContext.getContentContext(request, response);		
		MessageRepository msgRepo = MessageRepository.getInstance(ctx);		
		if (message != null) {
			msgRepo.setGlobalMessageAndNotification(ctx,new GenericMessage(message, GenericMessage.ERROR));
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
			if (method != null) {
				Class<?>[] clazzes = method.getParameterTypes();
				Object[] paramsInstance = new Object[clazzes.length];
				for (int i = 0; i < clazzes.length; i++) {
					paramsInstance[i] = ConfigHelper.smartInstance(clazzes[i], request, response);
				}
				if (method.getReturnType().isAssignableFrom(String.class)) {
					return (String)method.invoke(action, paramsInstance);
				} else {
					message = "bad return type for a action method (must be a String) : "+method;
					logger.severe(message);					
				}
			} else {
				message = "method not found : " + methodName;
				logger.warning(message);				
			}
		} catch (Exception e) {
			e.printStackTrace();
			message = "method not found : " + methodName + " on : " + action.getClass().getCanonicalName();
			logger.fine(message);			
		}
		return message;
	}
	
}
