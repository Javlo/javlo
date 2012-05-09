package org.javlo.module.file;

import org.javlo.actions.IModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.module.ModuleContext;

public class FileAction implements IModuleAction {

	@Override
	public String getActionGroupName() {
		return "file";
	}

	@Override
	public String prepare(ContentContext ctx, ModuleContext moduleContext) throws Exception {
//		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
//		ctx.getRequest().setAttribute("root", URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getStaticFolder()));
		return null;
	}

}
