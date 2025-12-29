package org.javlo.utils;

import jakarta.mail.internet.InternetAddress;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.mailing.MailConfig;
import org.javlo.mailing.MailService;
import org.javlo.servlet.IVersion;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class DebugListening {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(DebugListening.class.getName());

	private static DebugListening instance = null;

	public static StaticConfig staticConfig = null;

	public static DebugListening getInstance() {
		if (instance == null) {
			instance = new DebugListening();
		}
		return instance;
	}

	private long latestSend = 0;

	private static final long DELTA_SEND = 10000;

	public static final boolean SEND_ERROR_MAIL = true;

	private ServletContext application;

	public void sendError(ContentContext ctx, StaticConfig staticConfig, Throwable t, String info) {
		HttpServletRequest request = ctx.getRequest();
		HttpSession session = request.getSession(true);		
		if (System.currentTimeMillis() - DELTA_SEND > latestSend) {
			latestSend = System.currentTimeMillis();
			GlobalContext globalContext = GlobalContext.getInstance(request);
			String userName = "undefined";
			if (session != null) {
				IUserFactory fact = AdminUserFactory.createUserFactory(globalContext, session);
				if (fact.getCurrentUser(globalContext, request.getSession()) != null) {
					userName = fact.getCurrentUser(globalContext, request.getSession()).getName();
				}
			}
			try {
				String subject = "Javlo error:" + globalContext.getContextKey() + "  host:" + request.getRemoteHost();				
				Map<String, String> errorInfo = new LinkedHashMap<String, String>();
				errorInfo.put("version", IVersion.VERSION);
				errorInfo.put("local addr", request.getLocalAddr());
				errorInfo.put("local host", request.getLocalName());
				errorInfo.put("request method", request.getMethod());
				errorInfo.put("request path info", request.getPathInfo());
				errorInfo.put("direct link", URLHelper.createAbsoluteURL(ctx, ctx.getPath()));
				if (session != null) {
					errorInfo.put("session creation time", StringHelper.renderTime(new Date(session.getCreationTime())));
					errorInfo.put("session last acces", StringHelper.renderTime(new Date(session.getLastAccessedTime())));					
				}
				errorInfo.put("time", StringHelper.renderTime(new Date()));
				errorInfo.put("user name", userName);
				errorInfo.put("administrator", globalContext.getAdministrator());
				errorInfo.put("folder", globalContext.getFolder());
				errorInfo.put("info", info);
				
				ByteArrayOutputStream arrayOut = new ByteArrayOutputStream();
				PrintStream out = new PrintStream(arrayOut);
				out.println("");
				out.println("CONTEXT INFO : ");
				globalContext.writeInfo(out);
				out.println("");
				out.println("STACK TRACE : ");
				if (t != null) {
					out.println("message : " + t.getMessage());
					t.printStackTrace(out);
				} else {
					out.println("message : NO STACK TRACE.");
				}
				out.flush();
				out.close();
				
				String adminEmail = XHTMLHelper.createAdminMail(subject, XHTMLHelper.textToXHTML(new String(arrayOut.toByteArray())), errorInfo, URLHelper.createStaticURL(ctx,  "/"), globalContext.getGlobalTitle(), "- Javlo -");

				if (SEND_ERROR_MAIL) {
					if (staticConfig.getErrorMailReport() != null) {
						MailService mailService = MailService.getInstance(new MailConfig(globalContext, StaticConfig.getInstance(request.getSession()), null));
						mailService.sendMail(new InternetAddress(globalContext.getAdministratorEmail()), new InternetAddress(staticConfig.getErrorMailReport()), subject, adminEmail, true, globalContext.getDKIMBean());
						logger.warning("SEND ERROR TO ADMINISTRATOR");
					} else {
						logger.warning("no error email defined, the error message will be displayed in log.");
						logger.warning(new String(arrayOut.toByteArray()));
					}
				}

			} catch (Exception e) {
				//e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		Throwable t = new Exception();
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		t.printStackTrace(System.out);
		out.close();
		System.out.println(new String(outStream.toByteArray()));
	}

	public void sendError(ContentContext ctx, String info) {
		sendError(ctx, null, info);
	}

	public void sendError(ContentContext ctx, Throwable t) {
		sendError(ctx, t, "");
	}

	public void sendError(ContentContext ctx, Throwable t, String info) {
		if (staticConfig == null) {
			staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession(true).getServletContext());
		}
		sendError(ctx, staticConfig, t, info);
	}

	public void sendError(ServletContext application, String message) {
		if (System.currentTimeMillis() - DELTA_SEND > latestSend) {
			latestSend = System.currentTimeMillis();

			StaticConfig staticConfig = StaticConfig.getInstance(application);

			try {
				if (staticConfig.getErrorMailReport() != null) {
					String subject = "wcms error report : " + staticConfig.getInstanceId();
					MailService mailService = MailService.getInstance(new MailConfig(null, staticConfig, null));
					mailService.sendMail(new InternetAddress(staticConfig.getErrorMailReport()), new InternetAddress(staticConfig.getErrorMailReport()), subject, message, false);
				} else {
					logger.warning("no error email defined, the error message will be displayed in log.");
					logger.warning(new String(message));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void sendError(String message) {
		sendError(application, message);
	}

	public void setAppplication(ServletContext appplication) {
		this.application = appplication;
	}

}
