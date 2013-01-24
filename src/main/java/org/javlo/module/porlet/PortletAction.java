package org.javlo.module.porlet;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.module.core.ModulesContext;

public class PortletAction extends AbstractModuleAction {

	@Override
	public String getActionGroupName() {
		return "portlet";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		// TODO Auto-generated method stub
		return super.prepare(ctx, modulesContext);
	}

}
