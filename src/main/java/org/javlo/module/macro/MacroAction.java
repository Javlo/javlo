package org.javlo.module.macro;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.IMacro;
import org.javlo.macro.MacroFactory;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;

public class MacroAction extends AbstractModuleAction {

	@Override
	public String getActionGroupName() {
		return "macro";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext moduleContext) throws Exception {

		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		MacroFactory macroFactory = MacroFactory.getInstance(staticConfig);
		MacroModuleContext macroContext = MacroModuleContext.getInstance(ctx.getRequest());

		List<IMacro> allMacros = new LinkedList<IMacro>();
		Collection<IMacro> macros = macroFactory.getMacros();
		for (IMacro macro : macros) {
			if (globalContext.getMacros().contains(macro.getName())) {
				allMacros.add(macro);
			}
		}

		if (macroContext.getActiveMacro() != null) {
			ctx.getRequest().setAttribute("macro", macroContext.getActiveMacro());
			String macroRenderer = macroContext.getActiveMacro().getRenderer();
			macroRenderer = ResourceHelper.createModulePath(ctx, macroRenderer);
			ctx.getRequest().setAttribute("macroRenderer", macroRenderer);
		}

		ctx.getRequest().setAttribute("macros", allMacros);

		return super.prepare(ctx, moduleContext);
	}

	public static final String performExecuteMacro(RequestService requestService, StaticConfig staticConfig, ContentContext ctx) throws Exception {
		String macroName = requestService.getParameter("macro", null);
		if (macroName == null) {
			return "bad request structure : need 'macro' parameter";
		}
		MacroFactory macroFactory = MacroFactory.getInstance(staticConfig);
		IMacro macro = macroFactory.getMacro(macroName);
		if (macro == null) {
			return "macro not found : " + macroName;
		}
		return macro.perform(ctx, null);

	}

	public static String performExecuteInteractiveMacro(RequestService rs, ContentContext ctx, StaticConfig staticConfig, MessageRepository messageRepository, I18nAccess i18nAccess) throws FileNotFoundException, InstantiationException, IllegalAccessException, IOException, ModuleException {
		MacroModuleContext macroContext = MacroModuleContext.getInstance(ctx.getRequest());
		String macroName = rs.getParameter("macro", null);
		if (macroName == null) {
			return "bad request structure : need 'macro'.";
		}
		IMacro macro = MacroFactory.getInstance(staticConfig).getMacro(macroName);
		if (macro == null) {
			return "macro not found : " + macroName;
		}
		macroContext.setActiveMacro(macro);
		return null;
	}

	public static String performCloseMacro(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws FileNotFoundException, InstantiationException, IllegalAccessException, IOException, ModuleException {
		MacroModuleContext macroContext = MacroModuleContext.getInstance(ctx.getRequest());
		macroContext.setActiveMacro(null);
		return null;
	}
}
