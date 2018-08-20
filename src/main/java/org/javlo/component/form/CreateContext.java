package org.javlo.component.form;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextCreationBean;
import org.javlo.helper.SecurityHelper;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;

public class CreateContext extends AbstractVisualComponent implements IAction {
	
	
	
	public static final String TYPE  = "create-context";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getActionGroupName() {
		return "createContext";
	}
	
	public static String performCreate(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String name = rs.getParameter("name", null);
		String url = rs.getParameter("url", null);
		String contextName = StringHelper.createFileName(url);
		if (GlobalContext.isExist(ctx.getRequest(), contextName)) {
			return i18nAccess.getViewText("create-context.msg.error.exist");
		}
		if (name == null || name.length() < 3) {			
			return i18nAccess.getViewText("create-context.msg.error.name-size-small");
		}
		if (url == null || url.length() < 2) {			
			return i18nAccess.getViewText("create-context.msg.error.url-size-small");
		}
		if (url.length() > 32) {
			return i18nAccess.getViewText("create-context.msg.error.url-size");
		}	
		String email = rs.getParameter("email", null);
		if (!StringHelper.isMail(email)) {
			return i18nAccess.getViewText("create-context.msg.error.email");
		}
		String pwd = rs.getParameter("pwd", "");
		String pwd2 = rs.getParameter("pwd2", "");
		if (pwd.length() < 5) {
			return i18nAccess.getViewText("create-context.msg.error.pwd-size");
		}
		if (!pwd.equals(pwd2)) {
			return i18nAccess.getViewText("create-context.msg.error.pwd-same");
		}
		
		GlobalContextCreationBean createBean = GlobalContextCreationBean.getInstance(ctx.getRequest().getSession());
		createBean.setTitle(name);
		createBean.setContextKey(contextName);
		createBean.setEmail(email);
		createBean.setPassword(SecurityHelper.passwordEncrypt.encrypt(pwd));
		ctx.setPath(ctx.getCurrentPage().getNextBrother().getPath());
		return null;
	}
	
	@Override
	public boolean isContentCachableByQuery(ContentContext ctx) {
		return false;
	}

}

