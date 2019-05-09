package org.javlo.context;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.javlo.helper.NetHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;

public class GlobalContextCreationBean {
	
	private static Set<String> DEFAULT_ROLES = new HashSet<String>(Arrays.asList(new String[] {"content", "light-interface", "contributor", "navigation"}));

	private static final String KEY = "globalContextCreation";

	private String contextKey;
	private String referenceContext;
	private String title;
	private String email;
	private String password;

	public static GlobalContextCreationBean getInstance(HttpSession session) {
		GlobalContextCreationBean outBean = (GlobalContextCreationBean) session.getAttribute(KEY);
		if (outBean == null) {
			outBean = new GlobalContextCreationBean();
			session.setAttribute(KEY, outBean);
		}
		return outBean;
	}

	public String getContextKey() {
		return contextKey;
	}

	public void setContextKey(String contextKey) {
		this.contextKey = contextKey;
	}

	public String getReferenceContext() {
		return referenceContext;
	}

	public void setReferenceContext(String referenceContext) {
		this.referenceContext = referenceContext;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public GlobalContext create(ContentContext ctx) throws Exception {
		GlobalContext newContext = GlobalContext.getInstance(ctx.getRequest().getSession(), getContextKey());
		newContext.setAdministrator(getEmail());
		newContext.setGlobalTitle(getTitle());
		newContext.initDataFile();
		
		
		/** copy default content **/
		GlobalContext defaultContext = GlobalContext.getInstance(ctx.getRequest().getSession(), getReferenceContext());
		FileUtils.copyDirectory(new File(defaultContext.getDataFolder()), new File(newContext.getDataFolder()));
		
		IUserFactory userFactory = AdminUserFactory.createUserFactory(newContext, ctx.getRequest().getSession());
		IUserInfo newUser = userFactory.createUserInfos();
		newUser.setLogin(getEmail());
		newUser.setPassword(getPassword());
		newUser.addRoles(DEFAULT_ROLES);
		newUser.setSite(ctx.getGlobalContext().getContextKey());
		userFactory.addUserInfo(newUser);	
		userFactory.store();
		
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
		String subject = i18nAccess.getViewText("create-context.msg.email.subject")+getTitle();
		String content = i18nAccess.getViewText("create-context.msg.email.msg")+email;
		
		ContentContext newCtx = new ContentContext(ctx);
		newCtx.setForceGlobalContext(newContext);
		newCtx.setRenderMode(ContentContext.PREVIEW_MODE);
		newCtx.setAbsoluteURL(true);
			
		
		String newURL = URLHelper.createStaticURL(newCtx, "/");
		newURL = URLHelper.mergePath(newURL, getContextKey(), "/preview/");
		
		String mail = XHTMLHelper.createAdminMail(getTitle(), content, null, newURL, i18nAccess.getViewText("global.open"), null);
		try {
			NetHelper.sendMail(ctx.getGlobalContext(), new InternetAddress(ctx.getGlobalContext().getAdministratorEmail()), new InternetAddress(email), null, null, subject, mail, null, true);
		} catch (AddressException e) {
			e.printStackTrace();
		}
		MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(i18nAccess.getViewText("create-context.msg.done"), GenericMessage.SUCCESS));
		return newContext;
	}
}