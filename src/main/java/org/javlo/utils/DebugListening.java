package org.javlo.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;
import java.util.logging.Logger;

import javax.mail.internet.InternetAddress;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.mailing.MailConfig;
import org.javlo.mailing.MailService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;

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
				if (fact.getCurrentUser(request.getSession()) != null) {
					userName = fact.getCurrentUser(request.getSession()).getName();
				}
			}
			ByteArrayOutputStream arrayOut = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(arrayOut);

			try {
				String subject = "Javlo error : " + globalContext.getContextKey() + "  host:" + request.getRemoteHost();
				out.println(subject);
				out.println("");
				out.println("local addr           : " + request.getLocalAddr());
				out.println("local host           : " + request.getLocalName());
				out.println("request method       : " + request.getMethod());
				out.println("request path info    : " + request.getPathInfo());
				out.println("direct link          : " + URLHelper.createAbsoluteURL(ctx, ctx.getPath()));
				if (session != null) {
					out.println("session creation time : " + StringHelper.renderTime(new Date(session.getCreationTime())));
					out.println("session last acces    : " + StringHelper.renderTime(new Date(session.getLastAccessedTime())));
				} else {
					out.println("session null");
				}
				out.println("time                  : " + StringHelper.renderTime(new Date()));
				out.println("");
				out.println("user name             : " + userName);

				out.println("administrator         : " + globalContext.getAdministrator());
				out.println("folder                : " + globalContext.getFolder());

				out.println("info                  : " + info);
				out.println("");
				out.println("CONTEXT INFO : ");
				globalContext.writeInfo(session, out);
				out.println("");
				out.println("STACK TRACE : ");
				if (t != null) {
					out.println("message : " + t.getMessage());
					t.printStackTrace(out);
				} else {
					out.println("message : NO STACK TRACE.");
				}
				
				out.close();

				if (SEND_ERROR_MAIL) {
					if (staticConfig.getErrorMailReport() != null) {
						MailService mailService = MailService.getInstance(new MailConfig(globalContext, StaticConfig.getInstance(request.getSession()), null));
						mailService.sendMail(new InternetAddress(staticConfig.getErrorMailReport()), new InternetAddress(staticConfig.getErrorMailReport()), subject, new String(arrayOut.toByteArray()), false);
						logger.warning("SEND ERROR TO ADMINISTRATOR");
					} else {
						logger.warning("no error email defined, the error message will be displayed in log.");
						logger.warning(new String(arrayOut.toByteArray()));
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
