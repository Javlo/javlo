package org.javlo.module.core;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.javlo.actions.IModuleAction;

public class ExternalModule extends Module {

	public ExternalModule(File configFile, Locale locale, String modulePath, String URIPrefix) throws IOException {
		super(configFile, locale, modulePath, URIPrefix);
	}
	
	@Override
	public String getModuleFolder() {	
		return ModulesContext.EXTERNAL_MODULES_FOLDER;
	}
	
	public void setAction(IModuleAction action) {
		this.action = action;
	}	
	
	@Override
	protected void loadAction() {
		// action loaded from ModulesContext
	}

}
