package org.javlo.macro.interactive;

import java.util.Map;
import java.util.logging.Logger;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;

public abstract class AbstractDisplayModule implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(AbstractDisplayModule.class.getName());

	@Override
	public abstract String getName();
	
	protected abstract AbstractModuleAction getModuleAction(ContentContext ctx);

	@Override
	public String prepare(ContentContext ctx) {
		AbstractModuleAction moduleAction = getModuleAction(ctx);
		try {
			ModulesContext modulesContext = ModulesContext.getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext());
			Module module = modulesContext.getModule(getName());
			if (module == null) {
				logger.severe("module not found : "+getName());
			} else {
				modulesContext.setCurrentModule(module.getName());
			}
			moduleAction.prepare(ctx, ModulesContext.getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext()));
			ctx.getRequest().setAttribute("currentModule", module);
			ctx.getRequest().setAttribute("params", getParams(ctx));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String getParams(ContentContext ctx) {
		return "";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		return null;
	}

	@Override
	public boolean isAdmin() {
		return false;
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/display_module.jsp";
	}

	@Override
	public String getActionGroupName() {
		return getName();
	}

	@Override
	public String getInfo(ContentContext ctx) {
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

	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return ctx.getCurrentEditUser() != null;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public void init(ContentContext ctx) {
	}

	@Override
	public String getModalSize() {
		return DEFAULT_MAX_MODAL_SIZE;
	}

	@Override
	public String getIcon() {
		return "fa fa-signal";
	}
	
	@Override
	public String getUrl() {
		return null;
	}
	
	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}

}
