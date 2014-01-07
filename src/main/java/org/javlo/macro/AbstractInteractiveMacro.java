package org.javlo.macro;

import org.javlo.context.ContentContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.macro.core.IMacro;
import org.javlo.macro.core.MacroFactory;
import org.javlo.message.MessageRepository;
import org.javlo.module.macro.MacroAction;
import org.javlo.module.macro.MacroModuleContext;
import org.javlo.service.RequestService;

public abstract class AbstractInteractiveMacro implements IInteractiveMacro {

	public AbstractInteractiveMacro() {
	}

	public static String initInteractiveMacro(ContentContext ctx, String name) throws Exception {
		MacroModuleContext macroContext;
		try {
			macroContext = MacroModuleContext.getInstance(ctx.getRequest());			
			IMacro macro = MacroFactory.getInstance(ctx.getGlobalContext().getStaticConfig()).getMacro(name);
			if (macro == null) {
				return "macro not found : " + name;
			}
			macroContext.setActiveMacro(macro);
		} catch (Exception e) {
			e.printStackTrace();		
		}
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		
		MacroAction.performExecuteInteractiveMacro(rs, ctx, ctx.getGlobalContext().getStaticConfig(), MessageRepository.getInstance(ctx), I18nAccess.getInstance(ctx));
		
		return null;
	}

	@Override
	public boolean isAdmin() {
		return false;
	}

	@Override
	public boolean isPreview() {
		return false;
	}

}
