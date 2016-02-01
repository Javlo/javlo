package org.javlo.navigation;

import org.javlo.context.GlobalContext;

public class NavigationWithContent {
	
	private GlobalContext globalContext;
	private MenuElement page;
	
	public NavigationWithContent(GlobalContext globalContext, MenuElement page) {
		super();
		this.globalContext = globalContext;
		this.page = page;
	}
	public GlobalContext getGlobalContext() {
		return globalContext;
	}
	public void setGlobalContext(GlobalContext globalContext) {
		this.globalContext = globalContext;
	}
	public MenuElement getPage() {
		return page;
	}
	public void setPage(MenuElement page) {
		this.page = page;
	}
}
