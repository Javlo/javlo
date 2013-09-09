/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;

/**
 * @author pvandermaesen
 */
public class UserInfoLink extends AbstractVisualComponent {

	public static final String TYPE = "user-info-link";

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}

	private String getId(String userName) {
		return '_' + StringHelper.createFileName(userName) + '_' + getId();
	}

	protected List<String> getUserIds() {
		return StringHelper.stringToCollection(getValue());
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		List<String> selectedUser = getUserIds();

		IUserFactory userFact = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		List<IUserInfo> users = userFact.getUserInfoList();
		List<IUserInfo> finalUsers = new LinkedList<IUserInfo>();
		for (IUserInfo user : users) {
			String userName = user.getLogin();
			out.println("<span class=\"line-inline\">");
			String checked = "";
			String inputName = getId(userName);
			if (selectedUser.contains(user.getLogin())) {
				finalUsers.add(user);
			}
			out.println("<input type=\"checkbox\" name=\"" + inputName + "\" id=\"" + getId(userName) + "\"" + checked + " />");
			out.println("<label for=\"" + getId(userName) + "\">" + userName + "</label>");
			out.println("</span>");
		}

		ctx.getRequest().setAttribute("users", finalUsers);

		out.close();

	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		List<String> selectedUser = getUserIds();

		IUserFactory userFact = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		List<IUserInfo> users = userFact.getUserInfoList();
		for (IUserInfo user : users) {
			String userName = user.getLogin();
			out.println("<span class=\"line-inline\">");
			String checked = "";
			String inputName = getId(userName);
			if (selectedUser.contains(user.getLogin())) {
				checked = " checked=\"checked\"";
			}
			out.println("<input type=\"checkbox\" name=\"" + inputName + "\" id=\"" + getId(userName) + "\"" + checked + " />");
			out.println("<label for=\"" + getId(userName) + "\">" + userName + "</label>");
			out.println("</span>");
		}

		out.close();
		return writer.toString();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		IUserFactory userFact = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		List<IUserInfo> users = userFact.getUserInfoList();
		List<String> selectedUserName = new LinkedList<String>();
		for (IUserInfo userInfo : users) {
			String userParamName = getId(userInfo.getLogin());
			if (requestService.getParameter(userParamName, null) != null) {
				selectedUserName.add(userInfo.getLogin());
			}
		}

		String value = StringHelper.collectionToString(selectedUserName);
		if (!value.equals(getValue())) {
			setValue(value);
			setModify();
		}

	}

	@Override
	public String getHexColor() {
		return DEFAULT_COLOR;
	}

	@Override
	public boolean isUnique() {
		return true;
	}

}
