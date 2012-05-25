package org.javlo.module.macro;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.macro.IMacro;
import org.javlo.macro.MacroFactory;
import org.javlo.module.ModuleContext;
import org.javlo.service.RequestService;

public class MacroAction extends AbstractModuleAction {

	@Override
	public String getActionGroupName() {
		return "macro";
	}
	
	@Override
	public String prepare(ContentContext ctx, ModuleContext moduleContext) throws Exception {
		
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		MacroFactory macroFactory = MacroFactory.getInstance(staticConfig);
		
		List<IMacro> ctxMacros = new LinkedList<IMacro>();
		Collection<IMacro> macros = macroFactory.getMacros();
		for (IMacro macro : macros) {
			if (globalContext.getMacros().contains(macro.getName())) {
				ctxMacros.add(macro);
			}
		}
		
		ctx.getRequest().setAttribute("macros", ctxMacros);
		
		return super.prepare(ctx, moduleContext);
	}
	
	public static final String performExecuteMacro (RequestService requestService, StaticConfig staticConfig, ContentContext ctx) throws Exception {
		String macroName = requestService.getParameter("macro", null);
		if (macroName == null) {
			return "bad request structure : need 'macro' parameter";
		}
		MacroFactory macroFactory = MacroFactory.getInstance(staticConfig);
		IMacro macro = macroFactory.getMacro(macroName);
		if (macro == null) {
			return "macro not found : "+macroName;
		}
		return macro.perform(ctx, null);
		
	}

}
