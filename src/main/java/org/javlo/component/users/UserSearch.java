package org.javlo.component.users;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.ListService;
import org.javlo.service.ListService.Item;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserInfo;

public class UserSearch extends AbstractVisualComponent implements IAction {

	public static final String TYPE = "user-search";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public boolean isUnique() {
		return true;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		RequestService rs = RequestService.getInstance(ctx.getRequest());

		ListService listService = ListService.getInstance(ctx);

		List<Item> countries = listService.getList(ctx, "countries");
		List<Item> functions = listService.getList(ctx, "functions");
		List<Item> organizations = listService.getList(ctx, "organization");

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<form id=\"search-user\">");
		out.println("<input type=\"hidden\" name=\"webaction\" value=\"user-search.search\"");
		out.println("<fieldset>");
		out.println("<legend>search users</legend>");
		out.println("<div class=\"line\"><label id=\"user\">text</label>");
		out.println("<input type=\"text\" id=\"user\" name=\"text\" value=\"" + rs.getParameter("text", "") + "\" />");
		out.println("</div>");
		out.println("<div class=\"line\"><label id=\"country\">country</label>");
		if (countries == null) {
			out.println("<input type=\"text\" id=\"country\" name=\"country\" value=\"" + rs.getParameter("domain", "") + "\" />");
		} else {
			out.println("<select id=\"country\" name=\"country\">");
			out.println("<option></option>");
			for (Item item : countries) {
				String selected = "";
				if (rs.getParameter("country", "").equals(item.getKey())) {
					selected = " selected=\"selected\"";
				}
				out.println("<option value=\"" + item.getKey() + "\"" + selected + ">" + item.getValue() + "</option>");
			}
			out.println("</select>");
		}
		out.println("</div>");
		out.println("<div class=\"line\"><label id=\"domain\">domain</label>");
		if (functions == null) {
			out.println("<input type=\"text\" id=\"domain\" name=\"domain\" value=\"" + rs.getParameter("domain", "") + "\" />");
		} else {
			out.println("<select id=\"domain\" name=\"domain\">");
			out.println("<option></option>");
			for (Item item : functions) {
				String selected = "";
				if (rs.getParameter("domain", "").equals(item.getKey())) {
					selected = " selected=\"selected\"";
				}
				out.println("<option value=\"" + item.getKey() + "\"" + selected + ">" + item.getValue() + "</option>");
			}
			out.println("</select>");
		}
		out.println("</div>");

		out.println("<input type=\"submit\" value=\"search...\" />");
		out.println("</fieldset>");
		out.println("</from>");

		List<UserInfo> users = (List<UserInfo>) ctx.getRequest().getAttribute("users");
		if (users != null) {
			out.println("<div class=\"result\">");
			if (users.size() == 0) {
				out.println("<span class=\"error\">Sorry, no result</span>");
			} else {
				out.println("<table>");
				out.println("<tr><th>Photo</th><th>Firstname</th><th>Lastname</th><th>email</th><th>organization</th><th>country</th><th>domain</th><th>phone</th></tr>");
				int i = 0;
				for (UserInfo user : users) {
					i++;
					String oddEven = "odd";
					if (i % 2 == 0) {
						oddEven = "even";
					}
					String country = XHTMLHelper.renderListItem(countries, user.getCountry());
					String function = XHTMLHelper.renderMultiListItem(functions, StringHelper.stringToCollection(user.getFunction(),";"));
					String organization = XHTMLHelper.renderListItem(organizations, user.getOrganization());
					String avatar = "&nbsp;";
					if (user.getAvatarURL() != null) {
						avatar = "<img src=\""+user.getAvatarURL()+"\" alt=\""+user.getFirstName()+' '+user.getLastName()+"\" />";
					}
					
					out.println("<tr class=\"" + oddEven + "\"><td>" + avatar + "</td><td>" + user.getFirstName() + "</td><td>" + user.getLastName() + "</td><td><a href=\"mailto:" + user.getEmail() + "\">contact</a></td><td>" + organization + "</td><td>" + country + "</td><td>" + function + "</td><td>" + user.getPhone() + "</td></tr>");
				}
				out.println("</table>");
			}
			out.println("</div>");
		}

		out.close();
		return new String(outStream.toByteArray());
	}

	public static String performSearch(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		String text = rs.getParameter("text", "").trim();
		String country = rs.getParameter("country", "").trim();
		String domain = rs.getParameter("domain", "").trim();
		if (text.length() == 0 && country.length() == 0 && domain.length() == 0) {
			return null;
		}
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		AdminUserFactory userFactory = AdminUserFactory.createAdminUserFactory(globalContext, ctx.getRequest().getSession());
		List<IUserInfo> users = userFactory.getUserInfoList();
		List<IUserInfo> result = new LinkedList<IUserInfo>();
		for (IUserInfo user : users) {
			if (BeanHelper.beanToString(user).contains(text)) {
				if (country.length() == 0 || ((UserInfo) user).getCountry().equals(country)) {
					if (domain.length() == 0 || ((UserInfo) user).getFunction().equals(domain)) {
						result.add(user);
					}
				}
			}
		}
		ctx.getRequest().setAttribute("users", result);
		return null;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return IContentVisualComponent.COMPLEXITY_ADMIN;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return false;
	}

	@Override
	public String getActionGroupName() {
		return getType();
	}
}
