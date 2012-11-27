package org.javlo.module.macro;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.javlo.bean.LinkToRenderer;
import org.javlo.context.GlobalContext;
import org.javlo.macro.IMacro;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;

public class MacroModuleContext extends AbstractModuleContext {

	public static final String MODULE_NAME = "macro";

	private IMacro activeMacro;

	public static MacroModuleContext getInstance(HttpServletRequest request) throws FileNotFoundException, InstantiationException, IllegalAccessException, IOException, ModuleException {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		Module module = ModulesContext.getInstance(request.getSession(), globalContext).searchModule(MODULE_NAME);
		return (MacroModuleContext) AbstractModuleContext.getInstance(request.getSession(), globalContext, module, MacroModuleContext.class);
	}

	@Override
	public List<LinkToRenderer> getNavigation() {
		return null;
	}

	@Override
	public LinkToRenderer getHomeLink() {
		return null;
	}

	@Override
	public void init() {
	}

	public IMacro getActiveMacro() {
		return activeMacro;
	}

	public void setActiveMacro(IMacro activeMacro) {
		this.activeMacro = activeMacro;
	}

}
