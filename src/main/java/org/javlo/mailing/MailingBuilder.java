package org.javlo.mailing;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.servlet.http.HttpServletRequest;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.servlet.ContentOnlyServlet;
import org.javlo.user.*;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class MailingBuilder {
	
	private static Logger logger = Logger.getLogger(MailingBuilder.class.getName());

	private String currentTemplate;
	private String sender;
	private String subject;
	private String reportTo;
	private Collection<String> editorGroups = new LinkedHashSet<String>();
	private Collection<String> visitorGroups = new LinkedHashSet<String>();
	private Collection<String> excludedUsers = new HashSet<String>();
	private String recipients;
	private boolean isTestMailing;
	private Map<InternetAddress, String> allRecipients = new HashMap<InternetAddress, String>();
	private Map<String,Object> maps;

	public void setCurrentTemplate(String currentTemplate) {
		this.currentTemplate = currentTemplate;
	}

	public String getCurrentTemplate() {
		return currentTemplate;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getReportTo() {
		return reportTo;
	}

	public void setReportTo(String reportTo) {
		this.reportTo = reportTo;
	}

	public Collection<String> getEditorGroups() {
		return editorGroups;
	}

	public void setEditorGroups(Collection<String> editorGroups) {
		this.editorGroups = editorGroups;
	}

	public Collection<String> getVisitorGroups() {
		return visitorGroups;
	}

	public void setVisitorGroups(Collection<String> groups) {
		this.visitorGroups = groups;
	}

	public Collection<String> getExcludedUsers() {
		return excludedUsers;
	}

	public void setExcludedUsers(Collection<String> excludedUserLogin) {
		this.excludedUsers = excludedUserLogin;
	}

	public String getRecipients() {
		return recipients;
	}

	public void setRecipients(String recipients) {
		this.recipients = recipients;
	}

	public boolean isTestMailing() {
		return isTestMailing;
	}

	public void setTestMailing(boolean isTest) {
		this.isTestMailing = isTest;
	}

	public Map<InternetAddress, String> getAllRecipients() {
		return allRecipients;
	}

	public void setAllRecipients(Map<InternetAddress, String> allRecipients) {
		this.allRecipients = allRecipients;
	}
	
	public void setAllRecipients(Collection<InternetAddress> address) {
		if (allRecipients == null) {
			allRecipients = new HashMap<InternetAddress, String>();
		}
		for (InternetAddress add : address) {
			if (!allRecipients.containsKey(add)) {				
				allRecipients.put(add, null);
			}
		}
	}


	public boolean prepare(ContentContext ctx) {
		try {
			HttpServletRequest request = ctx.getRequest();
			GlobalContext globalContext = GlobalContext.getInstance(request);
			allRecipients.clear();
			if (editorGroups != null && !editorGroups.isEmpty()) {
				explodeGroups(allRecipients, editorGroups, excludedUsers, AdminUserFactory.createUserFactory(globalContext, request.getSession()));
			}
			if (visitorGroups != null && !visitorGroups.isEmpty()) {
				explodeGroups(allRecipients, visitorGroups, excludedUsers, UserFactory.createUserFactory(globalContext, request.getSession()));
			}
			if (recipients != null) {
				for (String fullEmail : StringHelper.searchEmail(recipients)) {
					InternetAddress email = new InternetAddress(fullEmail);
					if (!allRecipients.containsKey(email)) {
						allRecipients.put(email, null);
					}
				}
			}
			return true;
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		} catch (AddressException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static void explodeGroups(Map<InternetAddress, String> allRecipients, Collection<String> groups, Collection<String> excludedUsers, IUserFactory userFactory) throws UnsupportedEncodingException {
		for (String group : groups) {
			Collection<IUserInfo> users = userFactory.getUserInfoForRoles(new String[] { group });
			for (IUserInfo user : users) {
				if (excludedUsers != null && excludedUsers.contains(user.getLogin())) {
					continue;
				}
				if (!StringHelper.isEmpty(user.getEmail())) {
					InternetAddress email = new InternetAddress(user.getEmail(), StringHelper.neverNull(user.getFirstName()) + " " + StringHelper.neverNull(user.getLastName()));
					if (!allRecipients.containsKey(email)) {
						allRecipients.put(email, user.getLogin());
					}
				}
			}
		}
	}

	public void sendMailing(ContentContext ctx) throws Exception {
		ContentContext pageCtx = ctx.getContextWithOtherRenderMode(ContentContext.PAGE_MODE);
		pageCtx.setAbsoluteURL(true);
		Map<String, String> params = new HashMap<String, String>();
		if (currentTemplate != null) {
			params.put(ContentOnlyServlet.TEMPLATE_PARAM_NAME, currentTemplate);
			
		}
		params.put(GlobalContext.PAGE_TOKEN_PARAM, ctx.getGlobalContext().getLatestTokenForPage(pageCtx.getCurrentPage().getName()));
		String url = URLHelper.createURL(pageCtx, params);
		if (getMaps() != null) {				
			for (Map.Entry<String, Object> entry : getMaps().entrySet()) {
				url = URLHelper.addParam(url, entry.getKey(),  URLEncoder.encode(""+entry.getValue(), "UTF-8"));	
			}					
		}
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (allRecipients.size() == 0) {
			logger.warning("no recipients for mailing : "+getSubject());
		} else for (Entry<InternetAddress, String> receiver : allRecipients.entrySet()) {
			Mailing m = new Mailing();
			m.setFrom(new InternetAddress(sender));
			m.setReceivers(Collections.singleton(receiver.getKey()));
			m.setSubject(subject);
			m.setPageId(pageCtx.getCurrentPage().getId());
			m.setAdminEmail(globalContext.getAdministratorEmail());			
			m.setSmtpHost(ctx.getGlobalContext().getSMTPHost());
			m.setSmtpPort(ctx.getGlobalContext().getSMTPPort());
			m.setSmtpUser(ctx.getGlobalContext().getSMTPUser());
			m.setSmtpPassword(ctx.getGlobalContext().getSMTPPassword());
			DKIMBean bean = globalContext.getDKIMBean();
			if (bean != null) {
				m.setDkimDomain(bean.getSigningdomain());
				m.setDkimSelector(bean.getSelector());
				m.setDirectory(globalContext.getStaticConfig());
				m.storePrivateKeyFile(DKIMFactory.getDKIMPrivateKeyFile(globalContext));
			}
			if (reportTo != null) {
				m.setNotif(new InternetAddress(reportTo));
			}						
			User user = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession()).getUser(receiver.getValue());
			if (user == null) {
				user = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession()).getUserByEmail(receiver.getKey().getAddress());
			}
			if (user != null) {
				if (user.getUserInfo().getToken() == null || user.getUserInfo().getToken().trim().length() == 0) {
					user.getUserInfo().setToken(StringHelper.getRandomIdBase64());
				}
				url = URLHelper.addParam(url, IUserFactory.TOKEN_PARAM, user.getUserInfo().getToken());				
			}			
			String content = NetHelper.readPageForMailing(new URL(url));
			if (content != null) {
				m.setContent(content);
				m.setHtml(true);
				List<String> roles = new LinkedList<String>();
				if (editorGroups != null) {
					roles.addAll(editorGroups);
				}
				if (visitorGroups != null) {
					roles.addAll(visitorGroups);
				}
				m.setRoles(roles);
				m.store(ctx.getRequest().getSession().getServletContext());
			} else {
				logger.warning("content not found on url : "+url);
			}
		}
	}

	public Map<String,Object> getMaps() {
		return maps;
	}

	public void setMaps(Map<String,Object> maps) {
		this.maps = maps;
	}
}
