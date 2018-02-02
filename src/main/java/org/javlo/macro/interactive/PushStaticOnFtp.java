package org.javlo.macro.interactive;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTPClient;
import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;

public class PushStaticOnFtp implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(PushStaticOnFtp.class.getName());
	
	private static final String NAME = "push-static-on-ftp";
	
	private static TransfertStaticToFtp thread = null;

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public String prepare(ContentContext ctx) {
		ctx.getRequest().setAttribute("host", ctx.getGlobalContext().getData(getName()+"-host"));
		ctx.getRequest().setAttribute("port", ctx.getGlobalContext().getData(getName()+"-port", "21"));
		ctx.getRequest().setAttribute("username", ctx.getGlobalContext().getData(getName()+"-username"));
		ctx.getRequest().setAttribute("password", ctx.getGlobalContext().getData(getName()+"-password"));
		ctx.getRequest().setAttribute("path", ctx.getGlobalContext().getData(getName()+"-path"));		
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
		return "/jsp/macros/push-static-on-ftp.jsp";
	}

	@Override
	public String getActionGroupName() {
		return getName();
	}

	public static String performPush(RequestService rs, ContentContext ctx, EditContext editCtx, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws MalformedURLException, Exception {
		String host = rs.getParameter("host", "");
		String port = rs.getParameter("port", "");
		String username = rs.getParameter("username", "");
		String password = rs.getParameter("password", "");
		String path = rs.getParameter("path", "");
		
		ctx.getGlobalContext().setData(NAME+"-host",  host);
		ctx.getGlobalContext().setData(NAME+"-port", "21");
		ctx.getGlobalContext().setData(NAME+"-username", username);
		if (StringHelper.isTrue(rs.getParameter("storepassword"))) {
			ctx.getGlobalContext().setData(NAME+"-password", password);
		} else {
			ctx.getGlobalContext().setData(NAME+"-password", null);
		}
		ctx.getGlobalContext().setData(NAME+"-path", path);	

		if (StringHelper.isEmpty(host) || !StringHelper.isDigit(port)) {
			return "bad host or port";
		}

		FTPClient ftp = new FTPClient();		
		ftp.connect(host, Integer.parseInt(port));
		if (!ftp.isConnected()) {
			return "could not connect to : " + host + ":" + port;
		} else {
			if (!StringHelper.isEmpty(username)) {
				if (!ftp.login(username, password)) {
					return "could not log with username:" + username;
				} else if (!ftp.changeWorkingDirectory(path)) {
					return "path not found : " + path;
				}
			}
			
			File folder = new File(URLHelper.mergePath(ctx.getGlobalContext().getFolder(), "_static_temp"));
			if (thread != null && thread.running) {
				return "Thread already lauched, please wait...";
			}	
			
			String url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE).getContextForAbsoluteURL(), "/");
			logger.info("download : "+url);
			thread = new TransfertStaticToFtp(folder, new URL(url), host, Integer.parseInt(port), username, password, path);
			thread.start();			
			messageRepository.setGlobalMessage(new GenericMessage("Push thread lauched.", GenericMessage.INFO));
			ctx.setClosePopup(true);
		}
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
	
	public static void main(String[] args) throws MalformedURLException {		
		TransfertStaticToFtp thread = new TransfertStaticToFtp(new File("c:/trans/temp_static"), new URL("http://localhost/javlo/empty/"), "localhost", 21, "test", null, "/javlo_static");
		thread.run();
	}

	
}
