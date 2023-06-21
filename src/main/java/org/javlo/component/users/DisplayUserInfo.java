package org.javlo.component.users;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.data.InfoBean;
import org.javlo.helper.StringHelper;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.user.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class DisplayUserInfo extends AbstractVisualComponent implements IAction {

	public static final String TYPE = "display-user-info";

	public static final String[] STYLES = new String[] { "admin", "visitor" };

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
	public String[] getStyleList(ContentContext ctx) {
		return STYLES;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);

		List<User> users = new LinkedList<>();

		List<String> logins = StringHelper.stringToCollection(getValue(), ",");
		for (String login : logins) {

			IUserFactory userFactory;
			if (getStyle() != null && !getStyle().contains("admin")) {
				userFactory = UserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getSession());
			} else {
				userFactory = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getSession());
			}

			User user = userFactory.getUser(login);
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
		if (users == null || users.size() == 0) {
			return "<div class=\"alert alert-warning\">user not found : "+getValue()+"</div>";
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

	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getDebugHeader(ctx));
		finalCode.append(getSpecialInputTag());
		finalCode.append("<input class=\"form-control full-width\" id=\"" + getContentName() + "\" name=\"" + getContentName() + "\" value=\""+getValue()+"\" />");
		IUserFactory userFactory;
		if (getStyle() != null && !getStyle().contains("admin")) {
			userFactory = UserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getSession());
		} else {
			userFactory = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getSession());
		}

		List<IUserInfo> users = userFactory.getUserInfoList();
		if (users.size() > 500) {
			finalCode.append("<div class=\"alert alert-warning mt-3 mb-3\">too many users for display.</div>");
		} else {
			users = users.stream().sorted(Comparator.comparing(IUserInfo::getLogin)).collect(Collectors.toList());
			finalCode.append("<div class=\"large-list mt-3\">");
			String sep="";
			for (IUserInfo ui : users) {
				String moreinfo = "";
				if (!StringHelper.isAllEmpty(ui.getFirstName(), ui.getLastName())) {
					moreinfo += " ("+ui.getFirstName()+' '+ui.getLastName()+')';
				}
				finalCode.append(sep+"<a href=\"javascript:void\" onclick=\"javascript:document.getElementById('"+getContentName()+"').value='"+ui.getLogin()+"'; return false;\">"+ui.getLogin()+moreinfo+"</a>");
				sep = " | ";
			}
			finalCode.append("</div>");
		}

		return finalCode.toString();
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
