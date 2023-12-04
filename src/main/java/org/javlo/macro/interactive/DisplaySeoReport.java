package org.javlo.macro.interactive;

import java.util.Map;
import java.util.logging.Logger;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.module.dashboard.ReportFactory;

public class DisplaySeoReport implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(DisplaySeoReport.class.getName());

	private static final String NAME = "display-seo-report";

	private static Thread thread = null;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String prepare(ContentContext ctx) {
		ctx.getRequest().setAttribute("host", ctx.getGlobalContext().getData(getName() + "-host"));
		ctx.getRequest().setAttribute("port", ctx.getGlobalContext().getData(getName() + "-port", "21"));
		ctx.getRequest().setAttribute("username", ctx.getGlobalContext().getData(getName() + "-username"));
		ctx.getRequest().setAttribute("password", ctx.getGlobalContext().getData(getName() + "-password"));
		ctx.getRequest().setAttribute("path", ctx.getGlobalContext().getData(getName() + "-path"));
		ctx.getRequest().setAttribute("email", ctx.getGlobalContext().getData(getName() + "-email"));
		try {
			ctx.getRequest().setAttribute("report", ReportFactory.getReport(ctx));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		return null;
	}

	@Override
	public boolean isAdmin() {
		return false;
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/display_seo_reprot.jsp";
	}

	@Override
	public String getActionGroupName() {
		return getName();
	}

	@Override
	public String getInfo(ContentContext ctx) {
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

	@Override
	public boolean isAdd() {
		return false;
	}

	@Override
	public boolean isInterative() {
		return true;
	}

	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return ctx.getCurrentEditUser() != null;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public void init(ContentContext ctx) {
	}

	@Override
	public String getModalSize() {
		return DEFAULT_MAX_MODAL_SIZE;
	}

	@Override
	public String getIcon() {
		return "fa fa-signal";
	}
	
	@Override
	public String getUrl() {
		return null;
	}
	
	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}

	@Override
	public int getType() {
		return TYPE_TOOLS;
	}

}
