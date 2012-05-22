/*
 * Created on Aug 13, 2003
 */
package org.javlo.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.javlo.component.core.ComponentBean;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.mailing.Mailing;
import org.javlo.mailing.MailingContext;
import org.javlo.mailing.MailingFactory;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.DataToIDService;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.syncro.SynchronisationThread;
import org.javlo.servlet.ContentOnlyServlet;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;
import org.javlo.thread.AbstractThread;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.user.exception.UserAllreadyExistException;
import org.springframework.util.StringUtils;

/**
 * @author pvandermaesen list of actions for mailing.
 */
public class MailingActions implements IAction {

	public static final String MAILING_FEEDBACK_PARAM_NAME = "_mfb";

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(MailingActions.class.getName());

	/**
	 * DEBUG FUNCTION
	 * 
	 * @param ctx
	 * @param path
	 * @param params
	 * @return
	 */
	public static String createURL(ContentContext ctx, String path, Map params) {
		StringBuffer finalURL = new StringBuffer();
		finalURL.append(path);
		char sep = '?';
		if (path.indexOf('?') >= 0) {
			sep = '&';
		}
		Iterator keys = params.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			String value = (String) params.get(key);
			finalURL.append(sep);
			finalURL.append(key);
			finalURL.append('=');
			finalURL.append(value);
			sep = '&';
		}
		return finalURL.toString();
	}

	public static void main(String[] args) {
		Map<String, String> params = new Hashtable<String, String>();

		params.put("template", "");
		params.put(MAILING_FEEDBACK_PARAM_NAME, "##data##");
		String url = createURL(null, "", params);
		System.out.println("url=" + url);
	}

	public static final String performBackcreate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		MailingContext mailingContext = MailingContext.getInstance(request.getSession());
		mailingContext.setDisplayStep(true);
		switch (mailingContext.getPosition()) {
		case 1:
			mailingContext.setCommandRenderer(MailingContext.HOME_COMMAND_RENDERER);
			mailingContext.setRenderer(MailingContext.HOME_RENDERER);
			break;
		case 2:
			mailingContext.setCommandRenderer(null);
			mailingContext.setRenderer("/jsp/mailing/create_content.jsp");
			break;
		case 3:
			mailingContext.setCommandRenderer(null);
			mailingContext.setRenderer("/jsp/mailing/mailing_command.jsp");
			break;
		case 4:
			mailingContext.setCommandRenderer(null);
			mailingContext.setRenderer("/jsp/mailing/mailing_preview.jsp");
			break;
		default:
			mailingContext.setCommandRenderer(MailingContext.HOME_COMMAND_RENDERER);
			mailingContext.setRenderer(MailingContext.HOME_RENDERER);
			break;
		}
		mailingContext.setView(MailingContext.MAILING_VIEW);
		return null;
	}

	public static String performCancel(HttpServletRequest request, HttpServletResponse response) throws Exception {
		MailingContext mailingContext = MailingContext.getInstance(request.getSession());
		mailingContext.setRenderer(MailingContext.HOME_RENDERER);
		mailingContext.setCommandRenderer(MailingContext.HOME_COMMAND_RENDERER);
		mailingContext.setCurrentMailing(null);
		mailingContext.setPosition(1);
		mailingContext.setCurrentContent("");
		return "";
	}

	public static String performCancelmailing(HttpServletRequest request, HttpServletResponse response) throws Exception {
		performCancel(request, response);
		GlobalContext globalContext = GlobalContext.getInstance(request); EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
		editCtx.setCurrentView(EditContext.CONTENT_VIEW);
		EditContext.getInstance(globalContext, request.getSession()).setMailing(false);
		return "";
	}

	public static String performCreatecontent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		RequestService requestService = RequestService.getInstance(request);
		MailingContext mailingContext = MailingContext.getInstance(request.getSession());
		GlobalContext globalContext = GlobalContext.getInstance(request);
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());

		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(new Date());

		String subject = requestService.getParameter("subject", "");
		String contentText = mailingContext.getCurrentContent();

		if (requestService.getParameter("previous", null) != null) {
			return performPrevious(request, response);
		}

		if (requestService.getParameter("cancel", null) != null) {
			mailingContext.setCurrentMailing(null);
			mailingContext.setPage(null);
			mailingContext.setPosition(1);
			mailingContext.setCurrentContent("");
			return performBackcreate(request, response);
		}

		MenuElement page;
		ContentService content = ContentService.createContent(ctx.getRequest());
		if (contentText.trim().length() > 0 && subject.trim().length() > 0 && !contentText.equals(i18nAccess.getText("global.content-here"))) {
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
			if (staticConfig.isMailingAsContent()) {
				if (mailingContext.getPage() == null) {
					page = MacroHelper.addPageIfNotExistWithoutMessage(ctx, content.getNavigation(ctx), "mailing", true);
					page.setVisible(false);
					String year = "" + cal.get(Calendar.YEAR);
					page = MacroHelper.addPageIfNotExistWithoutMessage(ctx, page, year, true);
					String englishMonth = MacroHelper.getDisplayName(cal, Calendar.MONTH, MacroHelper.CALENDAR_LONG, Locale.ENGLISH);
					page = MacroHelper.addPageIfNotExistWithoutMessage(ctx, page, englishMonth, true);
					page = MacroHelper.addPage(ctx, page.getName(), "mailing-" + year + "-" + englishMonth + "-", true);
					mailingContext.setPage(page);
				} else {
					ComponentBean[] contentBean = mailingContext.getPage().getContent();
					contentBean[0].setValue(contentText);
					contentBean[0].setLanguage(ctx.getLanguage());
					page = mailingContext.getPage();
					page.releaseCache();
				}
				ctx.setPath(page.getPath());
			} else {

				String pageURL = staticConfig.getDynamicContentPage() + '/' + Calendar.getInstance().get(Calendar.YEAR) + "/" + Calendar.getInstance().get(Calendar.YEAR) + "M" + (Calendar.getInstance().get(Calendar.MONTH) + 1) + "_D" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
				String url = ComponentHelper.createDynamicPage(ctx, pageURL, contentText);

				ContentContext absoluteCtx = new ContentContext(ctx);
				absoluteCtx.setAbsoluteURL(true);
				String absURL = URLHelper.createAbsoluteURL(absoluteCtx, url);
				logger.fine("mailing url = " + url);
				absURL = URLHelper.addParam(absURL, ContentOnlyServlet.TEMPLATE_PARAM_NAME, mailingContext.getCurrentTemplate());
				// absURL = URLHelper.cryptURL(absoluteCtx, absURL);
				url = URLHelper.addParam(url, ContentOnlyServlet.TEMPLATE_PARAM_NAME, mailingContext.getCurrentTemplate());
				mailingContext.setRelativeURL(url);
				logger.fine("mailing absURL = " + url);
				mailingContext.setAbsoluteURL(absURL);
			}

			PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
			persistenceService.store(ctx);
		} else {
			if (subject.trim().length() == 0) {
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				GenericMessage msg = new GenericMessage("mailing.error.nosubject", "subject", GenericMessage.ERROR);
				messageRepository.addMessage(msg);
			}
			if (contentText.trim().length() == 0 || contentText.equals(i18nAccess.getText("global.content-here"))) {
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				GenericMessage msg = new GenericMessage("mailing.error.nocontent", "content", GenericMessage.ERROR);
				messageRepository.addMessage(msg);
			}
		}

		return performPrepare(request, response);
	}

	public static String performHelp(HttpServletRequest request, HttpServletResponse response) {
		MailingContext mailingContext = MailingContext.getInstance(request.getSession());
		mailingContext.setCommandRenderer(null);
		mailingContext.setRenderer("/jsp/mailing/help.jsp");
		mailingContext.setDisplayStep(false);
		mailingContext.setView(MailingContext.HELP_VIEW);
		return null;
	}

	public static String performHistory(HttpServletRequest request, HttpServletResponse response) {
		MailingContext mailingContext = MailingContext.getInstance(request.getSession());
		mailingContext.setCommandRenderer(null);
		mailingContext.setRenderer("/jsp/mailing/history.jsp");
		mailingContext.setDisplayStep(false);
		mailingContext.setView(MailingContext.HISTORY_VIEW);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		try {
			if (globalContext.getURIAlias() != null) {
				StaticConfig staticConfig = StaticConfig.getInstance(request.getSession().getServletContext());
				SynchronisationThread synchro = (SynchronisationThread) AbstractThread.createInstance(staticConfig.getThreadFolder(), SynchronisationThread.class);
				synchro.initSynchronisationThreadForMailingHistory(staticConfig, globalContext);
				synchro.store();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String performInitcontent(HttpServletRequest request, HttpServletResponse response) throws Exception {
		MailingContext mailingContext = MailingContext.getInstance(request.getSession());
		RequestService requestService = RequestService.getInstance(request);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
		String contentText = requestService.getParameter("content", "");
		mailingContext.setCurrentContent(contentText);

		if (requestService.getParameter("previous", null) != null) {
			return performPrevious(request, response);
		}

		if (requestService.getParameter("cancel", null) != null) {
			mailingContext.setCurrentMailing(null);
			mailingContext.setPage(null);
			mailingContext.setPosition(1);
			return performBackcreate(request, response);
		}

		if (contentText.trim().length() == 0 || contentText.equals(i18nAccess.getText("global.content-here"))) {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			GenericMessage msg = new GenericMessage("mailing.error.nocontent", "content", GenericMessage.ERROR);
			messageRepository.addMessage(msg);
		} else {
			mailingContext.setPosition(3);
			mailingContext.setCommandRenderer(null);
			mailingContext.setRenderer("/jsp/mailing/mailing_command.jsp");
		}
		return null;
	}

	public static final String performNext(HttpServletRequest request, HttpServletResponse response) throws Exception {
		MailingContext mailingContext = MailingContext.getInstance(request.getSession());
		if (mailingContext.getPosition() < 5) {
			mailingContext.setPosition(mailingContext.getPosition() + 1);
			return performBackcreate(request, response);			
		}
		return null;
	}

	public static String performPrepare(HttpServletRequest request, HttpServletResponse response) throws Exception {

		RequestService requestService = RequestService.getInstance(request);

		GlobalContext globalContext = GlobalContext.getInstance(request);
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());

		ContentContext ctx = ContentContext.getContentContext(request, response);
		MessageRepository messageRepository = MessageRepository.getInstance(ctx);

		MailingContext mailingContext = MailingContext.getInstance(request.getSession());

		boolean testMail = requestService.getParameter("send-test", null) != null;

		if (requestService.getParameter("previous", null) != null) {
			return performPrevious(request, response);
		}

		if (requestService.getParameter("cancel", null) != null) {
			mailingContext.setCurrentMailing(null);
			mailingContext.setPage(null);
			mailingContext.setPosition(1);
			return performBackcreate(request, response);
		}

		boolean valid = true;

		String dateStr = requestService.getParameter("send-date", null);
		Date sendDate = null;
		if (dateStr != null) {
			try {
				sendDate = StringHelper.parseDate(dateStr);
			} catch (ParseException e) {
				GenericMessage msg = new GenericMessage("global.message.error.date", "send-date", GenericMessage.ERROR);
				messageRepository.addMessage(msg);
				valid = false;
			}
		} else {
			logger.warning("no send-date field");
		}

		String from = requestService.getParameter("from", null);
		String toTest = requestService.getParameter("from-test", null);
		if (from != null) {
			if (!PatternHelper.MAIL_PATTERN.matcher(from).matches()) {
				GenericMessage msg = new GenericMessage("mailing.error.email", "from", GenericMessage.ERROR);
				messageRepository.addMessage(msg);
				valid = false;
			}
		} else {
			logger.warning("no from field");
			valid = false;
		}

		String subject = requestService.getParameter("subject", null);

		/*
		 * if (MailingContext.getCurrentSessionMailing(request.getSession()) != null) { subject = MailingContext.getCurrentSessionMailing(request.getSession ()).getSubject(); }
		 */

		if (subject != null) {
			if (subject.trim().length() < 1) {
				GenericMessage msg = new GenericMessage("mailing.error.nosubject", "subject", GenericMessage.ERROR);
				messageRepository.addMessage(msg);
				logger.warning("no subject");
				valid = false;
			}
		} else {
			logger.warning("no subject field");
			valid = false;
		}

		String report = requestService.getParameter("report", null);
		if (report != null) {
			if (!PatternHelper.MAIL_PATTERN.matcher(report).matches()) {
				GenericMessage msg = new GenericMessage("mailing.error.email", "report", GenericMessage.ERROR);
				messageRepository.addMessage(msg);
				valid = false;
			}
		} else {
			logger.warning("no report field");
			valid = false;
		}

		if (testMail) {
			if (StringHelper.isMail(toTest)) {
				Collection<String> vracEmailsCol = new LinkedList<String>();
				vracEmailsCol.add(toTest);
				sendMailing(ctx, from, subject, vracEmailsCol, new String[0], report, new Date(0), null, true);
				GenericMessage msg = new GenericMessage(i18nAccess.getText("mailing.send-test", new String[][] { { "from", from } }), GenericMessage.INFO);
				messageRepository.setGlobalMessage(msg);
				EditActions.performSynchro(request, response); // Synchronise
																// with the DMZ
																// server
			} else {
				GenericMessage msg = new GenericMessage(i18nAccess.getText("mailing.error.email"), GenericMessage.ERROR);
				messageRepository.setGlobalMessage(msg);
			}
		} else {
			if (request.getParameter("send") == null) {
				performCancel(request, response);
			} else {
				if (valid) {
					if (ctx.getRenderMode() == ContentContext.MAILING_MODE) {
						Mailing mailing = MailingContext.getCurrentSessionMailing(request.getSession());
						int allreadyImported = 0;
						if (mailing != null) {
							allreadyImported = mailing.getReceiversSize();
						}
						String vracEmails = requestService.getParameter("vrac-emails", "");
						Collection<String> vracEmailsCol = StringHelper.searchEmail(vracEmails);
						if (vracEmailsCol.size() + allreadyImported <= 0) {
							GenericMessage msg = new GenericMessage("mailing.error.import-emails", "vrac-emails", GenericMessage.ERROR);
							messageRepository.addMessage(msg);

						} else {
							boolean test = requestService.getParameter("test", null) != null;
							String[] roles = requestService.getParameterValues("to", new String[0]);

							// if (mailing == null) {
							mailing = prepareMailing(ctx, from, subject, vracEmailsCol, roles, report, sendDate, mailingContext.getAbsoluteURL(), test);
							/*
							 * } else { mailing.addReceivers(vracEmailsCol); mailing.setFrom(new InternetAddress(from)); mailing.setSubject(subject); mailing.setRoles(roles); mailing.setNotif(new InternetAddress(report)); mailing.setSendDate(sendDate); mailing.setTest(test); }
							 */

							MailingContext.setCurrentSessionMailing(request.getSession(), mailing);

							mailingContext.setRenderer("/jsp/mailing/mailing_preview.jsp");
							mailingContext.setCommandRenderer(null);
							mailingContext.setPosition(4);
						}
					} else {
						mailingContext.setRenderer("/jsp/edit/mailing/mailing_confirmation.jsp");
						mailingContext.setPosition(3);
						mailingContext.setCommandRenderer("");
					}
				} else {
					if (messageRepository.getGlobalMessage() == null) {
						GenericMessage msg = new GenericMessage("technical error", GenericMessage.ERROR);
						messageRepository.setGlobalMessage(msg);
					}
				}
			}
		}

		return null;
	}

	public static final String performPrevious(HttpServletRequest request, HttpServletResponse response) throws Exception {
		MailingContext mailingContext = MailingContext.getInstance(request.getSession());
		if (mailingContext.getPosition() > 1) {
			mailingContext.setPosition(mailingContext.getPosition() - 1);
			return performBackcreate(request, response);
		}
		return null;
	}

	public static final String performSelectcategory(HttpServletRequest request, HttpServletResponse response) throws Exception {
		MailingContext mailingContext = MailingContext.getInstance(request.getSession());
		RequestService requestService = RequestService.getInstance(request);
		String newCat = requestService.getParameter("category", null);
		mailingContext.setCurrentCategory(newCat);
		return null;
	}

	public static String performSelectmailing(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String mailingID = request.getParameter("mailing-id");
		if (mailingID != null) {
			MailingFactory mailingFactory = MailingFactory.getInstance(request.getSession().getServletContext());
			MailingContext.getInstance(request.getSession()).setCurrentMailing(mailingFactory.getMailing(mailingID));
		}
		return null;

	}

	public static String performSend(HttpServletRequest request, HttpServletResponse response) throws Exception {

		RequestService requestService = RequestService.getInstance(request);
		Collection<String> vracEmails = StringHelper.stringToCollection(requestService.getParameter("vrac-emails", ""));
		ContentContext ctx = ContentContext.getContentContext(request, response);
		MailingContext mailingContext = MailingContext.getInstance(request.getSession());

		if (ctx.getRenderMode() == ContentContext.MAILING_MODE) {
			if (request.getParameter("cancel") != null) {
				mailingContext.setPage(null);
				mailingContext.setPosition(1);
				mailingContext.setRenderer(MailingContext.HOME_RENDERER);
				mailingContext.setCommandRenderer(MailingContext.HOME_COMMAND_RENDERER);
				mailingContext.setCurrentContent("");
				return "";
			} else if (request.getParameter("previous") != null) {
				performPrevious(request, response);
				return "";
			}
		}

		if (request.getParameter("send") == null) {
			mailingContext.setRenderer("mailing/mailing_preview.jsp");
			mailingContext.setCommandRenderer("/jsp/edit/mailing/mailing_command.jsp");
			mailingContext.setPosition(3);
		} else if (MailingContext.getCurrentSessionMailing(request.getSession()) == null) {
			String from = requestService.getParameter("from", null);
			String subject = requestService.getParameter("subject", null);
			String report = requestService.getParameter("report", null);
			boolean test = requestService.getParameter("test", null) != null;
			String sendDateStr = requestService.getParameter("send-date", "");
			Date sendDate = null;
			try {
				sendDate = StringHelper.parseDate(sendDateStr);
			} catch (Exception e) {
				// e.printStackTrace(); -> only send date null
			}
			String[] roles = requestService.getParameterValues("to", new String[0]);

			sendMailing(ctx, from, subject, vracEmails, roles, report, sendDate, mailingContext.getAbsoluteURL(), test);

			mailingContext.setPosition(4);
			mailingContext.setRenderer("/jsp/edit/mailing/mailing_end.jsp");
			mailingContext.setCommandRenderer("");

		} else {
			Mailing mailing = MailingContext.getCurrentSessionMailing(request.getSession());
			MailingContext.setCurrentSessionMailing(request.getSession(), null);
			sendMailing(ctx, mailing);
			mailingContext.setPage(null);
			mailingContext.setPosition(0);
			mailingContext.setRenderer("/jsp/mailing/conclusion.jsp");
			mailingContext.setCommandRenderer(null);

			EditActions.performSynchro(request, response); // Synchronise with
															// the DMZ server
		}
		return "";
	}

	public static String performSubscribe(HttpServletRequest request, HttpServletResponse response) throws Exception {

		RequestService requestService = RequestService.getInstance(request);

		String rolesRAW = requestService.getParameter("roles", null);
		String email = requestService.getParameter("email", null);

		if ((rolesRAW != null) && (email != null)) {
			if (PatternHelper.MAIL_PATTERN.matcher(email).matches()) {
				String[] roles = StringHelper.stringToArray(rolesRAW);

				GlobalContext globalContext = GlobalContext.getInstance(request);
				IUserFactory userFactory = UserFactory.createUserFactory(globalContext, request.getSession());

				User user = userFactory.getUser(email);
				IUserInfo userInfo;
				if (user == null) {
					userInfo = userFactory.createUserInfos();
					userInfo.setLogin(email);
					userInfo.setEmail(email);
					userFactory.addUserInfo(userInfo);
				} else {
					userInfo = user.getUserInfo();
				}
				userInfo.addRoles(roles);
				userFactory.store();
			}
		}

		return "";
	}

	public static String performTemplate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		String template = requestService.getParameter("template", null);
		if (template == null) {
			logger.warning("error in template action no template found in request.");
		} else {
			MailingContext mailingContext = MailingContext.getInstance(request.getSession());
			mailingContext.setCurrentTemplate(template);
			if (ctx.getRenderMode() == ContentContext.MAILING_MODE) {
				mailingContext.setCommandRenderer(null);
				mailingContext.setRenderer("/jsp/mailing/create_content.jsp");
			} else {
				mailingContext.setRenderer("mailing/mailing_preview.jsp");
				mailingContext.setCommandRenderer("/jsp/edit/mailing/mailing_command.jsp");
			}
			mailingContext.setPosition(2);
		}
		return "";
	}

	public static String performUnsubscribe(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);

		String rolesRAW = requestService.getParameter("roles", null);

		String dataID = requestService.getParameter(MAILING_FEEDBACK_PARAM_NAME, null);
		String data = DataToIDService.getInstance(request.getSession().getServletContext()).getData(dataID);

		if ((rolesRAW != null) && (data != null)) {
			Map<String, String> params = StringHelper.uriParamToMap(data);
			String[] roles = StringHelper.stringToArray(rolesRAW);
			String email = params.get("to");
			if (email != null) {

				GlobalContext globalContext = GlobalContext.getInstance(request);
				IUserFactory userFactory = UserFactory.createUserFactory(globalContext, request.getSession());
				User user = userFactory.getUser(email);

				// temporary before fixing by storing user login instead of constructed e-mail address
				if (user == null) {
					int index = email.indexOf('<');
					if (index > -1) {
						String emailOnly = email.substring(index + 1, email.indexOf('>'));
						user = userFactory.getUser(emailOnly);
					}
				}

				if (user != null) {
					Set<String> rolesToRemove = new HashSet<String>(Arrays.asList(roles));
					Set<String> userRoles = new HashSet<String>(Arrays.asList(user.getRoles()));

					userRoles.removeAll(rolesToRemove);

					String[] newUserRoles = new String[userRoles.size()];
					userRoles.toArray(newUserRoles);

					user.getUserInfo().setRoles(newUserRoles);
					userFactory.store();
				}
			}
		}
		return "";
	}

	public static String performUnsubscriberole(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);

		if (ctx.getRenderMode() == ContentContext.PAGE_MODE) {
			ctx.setSpecialContentRenderer("/jsp/edit/mailing_unsubscribe.jsp");
		}
		// MailingContext mCtx =
		// MailingContext.getInstance(request.getSession());
		String login = request.getParameter("login");
		if (login == null) {
			login = request.getParameter("mail");
		}

		String roleRaw = request.getParameter("roles");

		if (roleRaw != null) {
			String[] roles = StringHelper.stringToArray(roleRaw);

			GlobalContext globalContext = GlobalContext.getInstance(request);
			IUserFactory userFactory = UserFactory.createUserFactory(globalContext, request.getSession());
			User user = userFactory.getUser(login);

			Set<String> rolesToRemove = new HashSet<String>(Arrays.asList(roles));
			Set<String> userRoles = new HashSet<String>(Arrays.asList(user.getRoles()));

			userRoles.removeAll(rolesToRemove);

			String[] newUserRoles = new String[userRoles.size()];
			userRoles.toArray(newUserRoles);

			user.getUserInfo().setRoles(newUserRoles);
			userFactory.store();
		}
		return "";
	}

	public static String performVracemail(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		String emails = requestService.getParameter("emails", "");

		BufferedReader reader = new BufferedReader(new StringReader(emails));

		String[] roles = requestService.getParameterValues("emails_roles", new String[0]);

		int mailFound = 0;
		int mailAllReadyExist = 0;

		try {
			IUserFactory userFact = null;
			String line = reader.readLine();
			while (line != null) {
				String[] mailCandidate = line.split(" |\\t|,|\"|;|:|\\(|\\)|\\[|\\]|<|>");
				for (String element : mailCandidate) {
					if (PatternHelper.MAIL_PATTERN.matcher(element).matches()) {
						String newEmail = element.trim();

						GlobalContext globalContext = GlobalContext.getInstance(request);
						userFact = UserFactory.createUserFactory(globalContext, request.getSession());
						User user = userFact.getUser(newEmail);

						if (user != null) {
							mailAllReadyExist++;
							String[] oldRoles = user.getRoles();
							List<String> newRoles = new LinkedList<String>();
							newRoles.addAll(Arrays.asList(oldRoles));
							for (String role : roles) {
								newRoles.add(role);
							}
							String[] newRolesArray = new String[newRoles.size()];
							newRoles.toArray(newRolesArray);
							user.getUserInfo().setRoles(newRolesArray);
							userFact.store();

						} else {
							IUserInfo userInfo = userFact.createUserInfos();
							userInfo.setFirstName("");
							userInfo.setLastName("");
							userInfo.setEmail(newEmail);
							userInfo.setLogin(newEmail);
							userInfo.setRoles(roles);
							try {
								userFact.addUserInfo(userInfo);
							} catch (UserAllreadyExistException e) {
								mailAllReadyExist++;
							}
						}
						mailFound++;
					}
				}
				line = reader.readLine();
			}
			if (userFact != null) {
				userFact.store();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		ContentContext ctx = ContentContext.getContentContext(request, response);

		String[][] msgParam = new String[][] { { "total", "" + mailFound }, { "allready", "" + mailAllReadyExist } };
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		String txt = i18nAccess.getText("mailing.vracemail", msgParam);

		MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(txt, GenericMessage.INFO));

		return null;
	}

	private static Mailing prepareMailing(ContentContext ctx, String from, String subject, Collection<String> vracEmails, String[] roles, String reportMail, Date sendDate, String url, boolean test) throws Exception {

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		boolean valid = true;
		MessageRepository messageRepository = MessageRepository.getInstance(ctx);
		if (from != null) {
			if (!PatternHelper.MAIL_PATTERN.matcher(from).matches()) {
				GenericMessage msg = new GenericMessage("mailing.error.email", "from", GenericMessage.ERROR);
				messageRepository.addMessage(msg);
				valid = false;
			}
		} else {
			valid = false;
		}

		if (subject != null) {
			if (subject.trim().length() < 1) {
				GenericMessage msg = new GenericMessage("mailing.error.nosubject", "subject", GenericMessage.ERROR);
				messageRepository.addMessage(msg);
				valid = false;
			}
		} else {
			valid = false;
		}

		if (reportMail != null) {
			if (!PatternHelper.MAIL_PATTERN.matcher(reportMail).matches()) {
				GenericMessage msg = new GenericMessage("mailing.error.email", "report", GenericMessage.ERROR);
				messageRepository.addMessage(msg);
				valid = false;
			}
		} else {
			valid = false;
		}

		if (valid) {
			/** send date adaptation **/
			if (sendDate != null) {
				Calendar sendDateCal = GregorianCalendar.getInstance();
				sendDateCal.setTime(sendDate);
				Calendar sendTimeCal = GregorianCalendar.getInstance();
				sendTimeCal.setTime(globalContext.getMailingStartTime());
				sendDateCal.set(Calendar.HOUR, sendTimeCal.get(Calendar.HOUR));
				sendDateCal.set(Calendar.MINUTE, sendTimeCal.get(Calendar.MINUTE));
				sendDateCal.set(Calendar.SECOND, sendTimeCal.get(Calendar.SECOND));
				sendDate = sendDateCal.getTime();
			}

			String rolesToString = "[";
			String sep = "";
			for (String role : roles) {
				rolesToString = sep + rolesToString;
				sep = ",";
			}
			rolesToString = rolesToString + "]";

			logger.info("send mailing from:" + from + " roles :" + rolesToString + " subject:" + subject + " report to:" + reportMail);

			IUserFactory userFactory = UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
			List<IUserInfo> userToSend = userFactory.getUserInfoForRoles(roles);
			InternetAddress[] receivers = new InternetAddress[userToSend.size() + vracEmails.size() + 1];
			try {
				receivers[receivers.length - 1] = new InternetAddress(globalContext.getAdministratorEmail());
			} catch (Exception e) {
				e.printStackTrace();
			}
			int i = 0;
			for (IUserInfo userInfo : userToSend) {
				try {
					String email = userInfo.getEmail();
					// todo: clean better that email
					email = email.trim();
					if (email.contains("@")) {
						if ((userInfo.getFirstName() != null) && (userInfo.getLastName() != null) && ((userInfo.getLastName() + userInfo.getFirstName()).trim().length() > 0)) {
							receivers[i] = new InternetAddress(userInfo.getEmail(), userInfo.getFirstName() + ' ' + userInfo.getLastName());
						} else {
							receivers[i] = new InternetAddress(userInfo.getEmail());
						}
					}
				} catch (Throwable t) {
					logger.warning("bad email address : " + userInfo.getEmail()); 
				}
				i++;
			}
			for (String vracEmail : vracEmails) {
				try {
					vracEmail = vracEmail.trim();
					if (vracEmail.contains("@")) {
						receivers[i] = new InternetAddress(vracEmail);
					}
				} catch (Throwable t) {
					logger.warning("bad email address : " + vracEmail);
					// e.printStackTrace();
				}
				i++;
			}
			InternetAddress fromAddress = new InternetAddress(from);
			fromAddress.setPersonal(fromAddress.getPersonal(), ContentContext.CHARACTER_ENCODING); // force
			// encoding

			ContentContext pageContext = new ContentContext(ctx);
			ContentContext unsubscribeContext = new ContentContext(ctx);
			pageContext.setAbsoluteURL(true);
			if (globalContext.getDMZServerInter() != null) {
				URL dmzServer = globalContext.getDMZServerInter();
				pageContext.setHostName(dmzServer.getHost());
				pageContext.setHostPort(dmzServer.getPort());
			}
			unsubscribeContext.setAbsoluteURL(true);
			unsubscribeContext.setRenderMode(ContentContext.VIEW_MODE);
			pageContext.setRenderMode(ContentContext.PAGE_MODE);
			Map<String, String> params = new Hashtable<String, String>();
			MailingContext mailingCtx = MailingContext.getInstance(ctx.getRequest().getSession());
			if (mailingCtx.getCurrentTemplate() != null) {
				params.put("template", mailingCtx.getCurrentTemplate());
			}
			Template template = TemplateFactory.getMailingTemplates(ctx.getRequest().getSession().getServletContext()).get(mailingCtx.getCurrentTemplate());

			String mailContent = null;
			if (template != null) {
				if (template.isLinkEmail(pageContext.getLanguage())) {
					mailContent = FileUtils.readFileToString(template.getLinkEmail(pageContext.getLanguage()), ContentContext.CHARACTER_ENCODING);

					if (url == null) {
						params.put(MAILING_FEEDBACK_PARAM_NAME, "##data##");
						url = URLHelper.createURL(pageContext, params);
					} else {
						url = URLHelper.addParam(url, MAILING_FEEDBACK_PARAM_NAME, "##data##");
					}

					mailContent = StringUtils.replace(mailContent, "##url##", url);

					User user = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession()).getCurrentUser(ctx.getRequest().getSession());
					if (user != null) {
						if (user.getUserInfo().getTitle() != null) {
							mailContent = StringUtils.replace(mailContent, "##title##", user.getUserInfo().getTitle());
						} else {
							mailContent = StringUtils.replace(mailContent, "##title##", "");
						}
						mailContent = StringUtils.replace(mailContent, "##lastname##", user.getUserInfo().getLastName());
						mailContent = StringUtils.replace(mailContent, "##firstname##", user.getUserInfo().getFirstName());
					}
					mailContent = StringHelper.txt2htmlCR(mailContent);
				}
			}
			if (mailContent == null) { // insert html in mail (not only the link
				// mail)

				String pageURL = URLHelper.createURL(pageContext, params);
				System.out.println("**** url = " + pageURL);

				mailContent = NetHelper.readPage(pageURL, GlobalContext.getInstance(ctx.getRequest()).isCSSInline());

			}

			ContentService content = ContentService.createContent(ctx.getRequest());
			MenuElement currentPage = ctx.getCurrentPage();

			String unsubscribeURL = null;
			MenuElement[] children = currentPage.getChildMenuElements();
			if (children.length > 0) {
				unsubscribeURL = URLHelper.createURL(unsubscribeContext, children[0].getPath());
			}

			// MailingManager mailingManager =
			// MailingManager.getInstance(request.getSession().getServletContext());

			logger.info("send mailing [" + subject + "] to " + receivers.length + " addresses.");

			Mailing mailing = new Mailing();
			mailing.setContent(mailContent);
			mailing.setReceivers(receivers);
			mailing.setRoles(roles);
			mailing.setTest(test);
			mailing.setFrom(fromAddress);
			mailing.setNotif(new InternetAddress(reportMail));
			mailing.setSubject(subject);
			mailing.setLanguage(pageContext.getLanguage());
			mailing.setUnsubscribeURL(unsubscribeURL);
			mailing.setContextKey(globalContext.getContextKey());
			mailing.setEncoding(ContentContext.CHARACTER_ENCODING);
			mailing.setSendDate(sendDate);
			mailing.setAdminEmail(globalContext.getAdministratorEmail());
			if (template != null) {
				mailing.setTemplateId(template.getId());
			}
			return mailing;
		}
		return null;
	}

	private static boolean sendMailing(ContentContext ctx, Mailing mailing) throws Exception {

		if (mailing == null) {
			return false;
		}

		mailing.store(ctx.getRequest().getSession().getServletContext());

		// mailingManager.sendMailing(fromAddress, receivers, null,
		// subject, mailContent, true);

		return true;
	}

	private static boolean sendMailing(ContentContext ctx, String from, String subject, Collection<String> vracEmails, String[] roles, String reportMail, Date sendDate, String url, boolean test) throws Exception {
		Mailing mailing = prepareMailing(ctx, from, subject, vracEmails, roles, reportMail, sendDate, url, test);
		return sendMailing(ctx, mailing);
	}

	@Override
	public String getActionGroupName() {
		return "mailing";
	}

}
