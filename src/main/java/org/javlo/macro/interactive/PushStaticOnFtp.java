package org.javlo.macro.interactive;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
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
import org.javlo.mailing.MailConfig;
import org.javlo.mailing.MailService;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;

public class PushStaticOnFtp implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(PushStaticOnFtp.class.getName());
	
	private static final String NAME = "push-static-on-ftp";
	
	private static Thread thread = null;


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
		ctx.getRequest().setAttribute("email", ctx.getGlobalContext().getData(getName()+"-email"));
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
	
	@Override
	public String getInfo(ContentContext ctx) {	
		return null;
	}

	public static String performPush(RequestService rs, ContentContext ctx, EditContext editCtx, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws MalformedURLException, Exception {
		String host = rs.getParameter("host", "");
		String port = rs.getParameter("port", "");
		String username = rs.getParameter("username", "");
		String password = rs.getParameter("password", "");
		String email = rs.getParameter("email", "");
		String path = rs.getParameter("path", "");
		boolean zipOnly = StringHelper.isTrue(rs.getParameter("ziponly"));
		boolean here = StringHelper.isTrue(rs.getParameter("here"));
		
		ctx.getGlobalContext().setData(NAME+"-host",  host);
		ctx.getGlobalContext().setData(NAME+"-port", "21");
		ctx.getGlobalContext().setData(NAME+"-username", username);
		ctx.getGlobalContext().setData(NAME+"-email", email);
		if (StringHelper.isTrue(rs.getParameter("storepassword"))) {
			ctx.getGlobalContext().setData(NAME+"-password", password);
		} else {
			ctx.getGlobalContext().setData(NAME+"-password", null);
		}
		ctx.getGlobalContext().setData(NAME+"-path", path);
		
		MailService mailService = null;
		if (StringHelper.isMail(email)) {
			mailService = MailService.getInstance(new MailConfig(ctx.getGlobalContext(), ctx.getGlobalContext().getStaticConfig(), null ));
		}
		
		File folder = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), "_static_temp"));
		String url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE).getContextForAbsoluteURL(), "/");
		if (here) {
			url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE).getContextForAbsoluteURL(), ctx.getCurrentPage());
		}
		
		if (zipOnly) {
			File zipFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), "_static_export/"+StringHelper.stringToFileName(globalContext.getContextKey()+"_"+StringHelper.renderSortableTime(new Date()))+".zip"));
			zipFile.getParentFile().mkdirs();
			zipFile.createNewFile();
			thread = new TransfertStaticToZip(ctx,  folder, new URL(url), zipFile, path, mailService, email);
		} else {
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
				if (thread != null && thread.isAlive()) {
					return "Thread already lauched, please wait...";
				}	
				logger.info("download : "+url);
				thread = new TransfertStaticToFtp(ctx, folder, new URL(url), host, Integer.parseInt(port), username, password, path, mailService, email);
			}
		}
		if (thread != null) {
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
	
	
}
