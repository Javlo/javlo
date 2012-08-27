package org.javlo.module.mailing;

import java.util.List;

import org.javlo.bean.LinkToRenderer;
import org.javlo.module.core.AbstractModuleContext;

public class MailingModuleContext extends AbstractModuleContext {
	
	@Override
	public List<LinkToRenderer> getNavigation() {
		return null;
	}

	@Override
	public void init() {
	}

	@Override
	public LinkToRenderer getHomeLink() {
		return null;
	}

}
