package org.javlo.macro.interactive.module;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.macro.interactive.AbstractDisplayModule;
import org.javlo.module.taxonomy.TaxonomyAction;

public class TaxonomyMacroModule extends AbstractDisplayModule {
	
	private TaxonomyAction action = null;

	@Override
	public String getName() {
		return "taxonomy";
	}

	@Override
	protected AbstractModuleAction getModuleAction(ContentContext ctx) {
		if (action == null) {
			action = new TaxonomyAction();
		}
		return action;
	}
	
	@Override
	public String getIcon() {	
		return "bi bi-diagram-3";
	}

}
