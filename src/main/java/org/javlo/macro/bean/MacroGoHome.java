package org.javlo.macro.bean;

import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;
import org.javlo.macro.core.MacroBean;

public class MacroGoHome extends MacroBean {

	public MacroGoHome() {
		super();
		setName("go-home");
	}
	
	@Override
	public void init(ContentContext ctx) {
		super.init(ctx);
		setUrl(URLHelper.createURL(ctx, "/"));
	}
	
	@Override
	public boolean isInterative() {
		return false;
	}
	
	@Override
	public String getIcon() {
		return "fa fa-home";
	}
	
	@Override
	public int getPriority() {
		return 10;
	}
	
}
