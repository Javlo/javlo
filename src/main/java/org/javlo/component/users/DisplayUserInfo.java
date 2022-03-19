package org.javlo.component.users;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.data.InfoBean;
import org.javlo.helper.StringHelper;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.user.UserInfo;

public class DisplayUserInfo extends AbstractVisualComponent implements IAction {

	public static final String TYPE = "display-user-info";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isDefaultValue(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean isDisplayable(ContentContext ctx) throws Exception {
		return true;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);

		List<User> users = new LinkedList<>();

		List<String> logins = StringHelper.stringToCollection(getValue(), ",");
		for (String login : logins) {
			User user = UserFactory.createUserFactory(ctx.getRequest()).getUser(login);
			if (user != null) {
				users.add(user);
			}
		}

		if (users.size() == 0) {
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage("user(s) not found : " + getValue(), GenericMessage.ERROR));
		} else {
			ctx.getRequest().setAttribute("usersToDisplay", users);
		}
	}

	protected boolean getColumnableDefaultValue() {
		return true;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		List<User> users = (List<User>) ctx.getRequest().getAttribute("usersToDisplay");
		if (users == null) {
			prepareView(ctx);
			users = (List<User>) ctx.getRequest().getAttribute("usersToDisplay");
		}
		if (users.size() == 0) {
			return "user(s) not found : " + getValue();
		}
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		for (User user : users) {
			if (user != null) {
				out.println("<div class=\"row user-item mb-3\"><div class=\"col\"><ul class=\"list-group\">");
				if (!StringHelper.isEmpty(user.getUserInfo().getFirstName()) || !StringHelper.isEmpty(user.getUserInfo().getLastName())) {
					out.println("<li class=\"list-group-item\">" + user.getUserInfo().getFirstName() + ' ' + user.getUserInfo().getLastName() + "</li>");
				}
				if (!StringHelper.isEmpty(user.getUserInfo().getEmail())) {
					out.println("<li class=\"list-group-item\"><a href=\"mailto:" + user.getUserInfo().getEmail() + "\">" + user.getUserInfo().getEmail() + "</a></li>");
				}
				if (user.getUserInfo() instanceof UserInfo) {
					UserInfo userInfo = (UserInfo) user.getUserInfo();
					if (!StringHelper.isEmpty(userInfo.getMobile())) {
						out.println("<li class=\"list-group-item\"><a href=\"tel:" + userInfo.getMobile() + "\">" + userInfo.getMobile() + "</a></li>");
					}
				}
				out.println("</ul></div>");

				out.println("<div class=\"col-3\">");
				InfoBean info = InfoBean.getCurrentInfoBean(ctx);
				out.println("<img src=\"" + info.getAvatarURL().get(user.getUserInfo()) + "\" class=\"img-thumbnail\" alt=\"" + user.getUserInfo().getFirstName() + "\">");
				out.println("</div>");

				out.println("</div>");
			}
		}
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return IContentVisualComponent.COMPLEXITY_STANDARD;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

	@Override
	public String getActionGroupName() {
		return getType();
	}
}
