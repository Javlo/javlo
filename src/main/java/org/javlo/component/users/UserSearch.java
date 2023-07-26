package org.javlo.component.users;

import com.google.gson.Gson;
import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.*;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.IListItem;
import org.javlo.service.ListService;
import org.javlo.service.RequestService;
import org.javlo.user.*;
import org.javlo.ztatic.StaticInfo;

import java.io.*;
import java.util.*;

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
		String searchUrl = URLHelper.createActionURL(ctx.setAjax(true), TYPE + ".ajaxsearch", "/");
		ctx.getRequest().setAttribute("searchUrl", searchUrl);
		ctx.getRequest().setAttribute("showAll", StringHelper.isTrue(getValue()));

		IUserFactory userFactory = UserFactory.createUserFactory(ctx.getRequest());
		List<IUserInfo> users = userFactory.getUserInfoList();

		List<String> compagnies = new LinkedList<>();
		List<String> countries = new LinkedList<>();
		for (IUserInfo ui : users) {
			if (!StringHelper.isEmpty(ui.getOrganization())) {
				if (!compagnies.contains(ui.getOrganization())) {
					compagnies.add(ui.getOrganization());
				}
			}
			if (!StringHelper.isEmpty(ui.getCountry())) {
				if (!countries.contains(ui.getCountry())) {
					countries.add(ui.getCountry());
				}
			}
		}
		Collections.sort(compagnies);
		Collections.sort(countries);
		ctx.getRequest().setAttribute("compagnies", compagnies);
		ctx.getRequest().setAttribute("countries", countries);
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		RequestService rs = RequestService.getInstance(ctx.getRequest());

		ListService listService = ListService.getInstance(ctx);

		List<IListItem> countries = listService.getList(ctx, "countries");
		List<IListItem> functions = listService.getList(ctx, "functions");
		List<IListItem> organizations = listService.getList(ctx, "organization");

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		out.println("<h2>user search</h2>");

		if (!ctx.getDevice().getCode().equals("pdf")) {
			out.println("<form id=\"search-user\">");
			out.println("<input type=\"hidden\" name=\"webaction\" value=\"user-search.search\" />");
			out.println("<fieldset>");
			out.println("<legend>search users</legend><div class=\"row\"><div class=\"col-sm-6\">");
			out.println("<div class=\"form-group\"><label id=\"user\">text</label>");
			out.println("<input class=\"form-control\" type=\"text\" id=\"user\" name=\"text\" value=\"" + rs.getParameter("text", "") + "\" />");
			out.println("</div>");
			out.println("<div class=\"form-group country\"><label id=\"country\">country</label>");
			if (countries == null) {
				out.println("<input class=\"form-control\" type=\"text\" id=\"country\" name=\"country\" value=\"" + rs.getParameter("domain", "") + "\" />");
			} else {
				out.println("<select class=\"form-control\" id=\"country\" name=\"country\">");
				out.println("<option></option>");
				for (IListItem item : countries) {
					String selected = "";
					if (rs.getParameter("country", "").equals(item.getKey())) {
						selected = " selected=\"selected\"";
					}
					out.println("<option value=\"" + item.getKey() + "\"" + selected + ">" + item.getValue() + "</option>");
				}
				out.println("</select>");
			}
			out.println("</div></div><div class=\"col-sm-6\">");
			out.println("<div class=\"line\"><label id=\"domain\">domain</label>");
			if (functions == null) {
				out.println("<input type=\"text\" id=\"domain\" name=\"domain\" value=\"" + rs.getParameter("domain", "") + "\" />");
			} else {
				out.println("<select class=\"form-control\" id=\"domain\" name=\"domain\">");
				out.println("<option></option>");
				for (IListItem item : functions) {
					String selected = "";
					if (rs.getParameter("domain", "").equals(item.getKey())) {
						selected = " selected=\"selected\"";
					}
					out.println("<option value=\"" + item.getKey() + "\"" + selected + ">" + item.getValue() + "</option>");
				}
				out.println("</select>");
			}
			out.println("</div>");

			GlobalContext globalContext = ctx.getGlobalContext();
			if (globalContext.getAdminUserRoles() != null && globalContext.getAdminUserRoles().size() > 0) {
				out.println("<div class=\"form-group roles\"><label id=\"role\">group</label>");
				out.println("<select class=\"form-control\" id=\"role\" name=\"role\">");
				out.println("<option></option>");
				for (String role : globalContext.getAdminUserRoles()) {
					String selected = "";
					if (rs.getParameter("role", "").equals(role)) {
						selected = " selected=\"selected\"";
					}
					out.println("<option value=\"" + role + "\"" + selected + ">" + role + "</option>");
				}
				out.println("</select></div>");
			}

			out.println("</div><div class=\"col-12\"><input class=\"pull-right btn btn-primary\" type=\"submit\" value=\"search...\" /></div></div>");
			out.println("</fieldset>");
			out.println("</from>");
		}

		List<UserInfo> users = (List<UserInfo>) ctx.getRequest().getAttribute("users");
		if (users != null) {
			out.println("<div class=\"result\">");
			if (users.size() == 0) {
				out.println("<span class=\"error\">Sorry, no result</span>");
			} else {
				StringBuffer emails = new StringBuffer();
				String sep = "";
				for (UserInfo user : users) {
					emails.append(sep);
					emails.append(user.getEmail());
					sep = ";";
				}
				out.print(renderList(ctx, users, emails.toString(), countries, functions, organizations));
			}

			out.println("</div>");
		}

		out.close();
		return new String(outStream.toByteArray());
	}

	private static String renderTable(ContentContext ctx, Collection<UserInfo> users, String emails, List<IListItem> countries, List<IListItem> functions, List<IListItem> organizations) throws FileNotFoundException, IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<table>");
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		out.println("<tr><th>Photo</th><th>Name</th><th>email (<a href=\"mailto:" + emails + "\">all</a>)</th><th>" + i18nAccess.getAllText("field.organization", "organization") + "</th><th>country</th><th>" + i18nAccess.getAllText("field.domain", "domain") + "</th><th>phone</th><th>Info</th></tr>");
		int i = 0;
		for (UserInfo user : users) {
			i++;
			String oddEven = "odd";
			if (i % 2 == 0) {
				oddEven = "even";
			}
			String country = XHTMLHelper.renderListItem(countries, user.getCountry());
			String function = XHTMLHelper.renderMultiListItem(functions, StringHelper.stringToCollection(user.getFunction(), ";"));
			String organization = XHTMLHelper.renderListItem(organizations, user.getOrganization());
			String avatar = "&nbsp;";
			String avatarURL = URLHelper.createAvatarUrl(ctx, user);
			if (avatarURL != null) {
				avatar = "<img src=\"" + avatarURL + "\" alt=\"" + user.getFirstName() + ' ' + user.getLastName() + "\" />";
			}

			if (user.getUrl() != null && user.getUrl().trim().length() > 0) {
				organization = "<a href=\"" + user.getUrl() + "\">" + organization + "</a>";
			}
			StringBuffer info = new StringBuffer();
			if (user.getExperience() != null && user.getExperience().trim().length() > 0) {
				info.append("<h3>Experience</h3>");
				info.append(user.getExperience());
			}
			if (user.getRecommendation() != null && user.getRecommendation().trim().length() > 0) {
				info.append("<h3>Recommendation</h3>");
				info.append(user.getRecommendation());
			}
			if (user.getInfo() != null && user.getInfo().trim().length() > 0) {
				info.append("<h3>More info</h3>");
				info.append(user.getInfo());
			}
			if (user.getSpecialFunction() != null && user.getSpecialFunction().trim().length() > 0) {
				info.append("<h3>Function</h3>");
				info.append(user.getSpecialFunction());
			}

			out.println("<tr class=\"" + oddEven + "\"><td>" + avatar + "</td><td>" + user.getFirstName() + ' ' + user.getLastName() + "</td><td><a href=\"mailto:" + user.getEmail() + "\">" + user.getEmail() + "</a></td><td>" + organization + "</td><td>" + country + "</td><td>" + function + "</td><td>" + user.getPhone() + "</td><td>" + info + "</td></tr>");
		}
		out.println("</table>");
		out.close();
		return new String(outStream.toByteArray());
	}

	private static String renderList(ContentContext ctx, Collection<UserInfo> users, String emails, List<IListItem> countries, List<IListItem> functions, List<IListItem> organizations) throws Exception {

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"user-list\">");
		int i = 0;

		if (ctx.getCurrentTemplate().isPDFRenderer()) {
			ContentContext pdfCtx = new ContentContext(ctx);
			pdfCtx.setFormat("pdf");
			String url = URLHelper.createURL(pdfCtx);
			for (Object key : ctx.getRequest().getParameterMap().keySet()) {
				if (!key.equals("__check_context")) {
					url = URLHelper.addParam(url, key.toString(), ctx.getRequest().getParameter(key.toString()));
				}
			}
			out.println("<a class=\"btn btn-default btn-exportpdf pull-right\" href=\"" + url + "\">Download PDF</a>");
		}
		out.println("<a class=\"btn btn-default btn-sendmail\" href=\"mailto:" + emails + "\">Send email to all list.</a>");

		for (UserInfo user : users) {
			if (!StringHelper.isEmpty(user.getFirstName())) {
				i++;
				String oddEven = "odd";
				if (i % 2 == 0) {
					oddEven = "even";
				}

				String country = XHTMLHelper.renderListItem(countries, user.getCountry());
				String function = XHTMLHelper.renderMultiListItem(functions, StringHelper.stringToCollection(user.getFunction(), ";"));
				String organization = XHTMLHelper.renderListItem(organizations, user.getOrganization());
				if (user.getUrl() != null && user.getUrl().trim().length() > 0) {
					String url = user.getUrl();
					if (!StringHelper.isURL(url)) {
						url = "http://" + url;
					}
					organization = "<a target=\"_blank\" href=\"" + url + "\">" + organization + "</a>";
				}

				out.println("<div class=\"card user-item " + oddEven + "\"><div class=\"card-body\">");
				out.println("<div class=\"card-title\">" + user.getFirstName() + ' ' + user.getLastName() + "</div>");
				out.println("<div class=\"card-text\"><div class=\"row\">");
				out.println("<div class=\"col-sm-2\">");
				String avatarURL = URLHelper.createAvatarUrl(ctx, user);
				String avatar = "&nbsp;";
				if (avatarURL != null) {
					avatar = "<img width=\"80\" src=\"" + avatarURL + "\" alt=\"" + user.getFirstName() + ' ' + user.getLastName() + "\" />";
				}
				out.println(avatar);
				out.println("</div><div class=\"col-sm-5\">");

				if (user.getPhone().trim().length() > 0) {
					out.println("<div class=\"data\"><span class=\"label\">Phone</span>");
					out.println(user.getPhone() + "</div>");
				}
				if (user.getMobile().trim().length() > 0) {
					out.println("<div class=\"data\"><span class=\"label\">Mobile</span>");
					out.println(user.getMobile() + "</div>");
				}

				if (user.getEmail().trim().length() > 0) {
					out.println("<div class=\"data\"><span class=\"label\">E-Mail</span>");
					out.println("<a href=\"mailto:" + user.getEmail() + "\">" + user.getEmail() + "</a></div>");
				}
				if (country.trim().length() > 0) {
					out.println("<div class=\"data\"><span class=\"label\">Country</span>");
					out.println(country + "</div>");
				}
				out.println("</div><div class=\"col-sm-5\">");
				if (organization.trim().length() > 0) {
					out.println("<div class=\"data\"><span class=\"label\">Organization</span>");
					out.println(organization + "</div>");
				}
				out.println("<div class=\"data\"><span class=\"label\">Function</span>");
				out.println(user.getSpecialFunction() + "</div>");
				out.println("</div>");
				out.println("</div><hr />");
				out.println("<div class=\"row\">");
				out.println("<div class=\"col-sm-3\">");
				out.println("<h3>Experience</h3>");
				out.println("<p>" + user.getExperience() + "</p>");
				out.println("</div><div class=\"col-sm-4\">");
				out.println("<h3>Recommendation</h3>");
				out.println("<p>" + user.getRecommendation() + "</p>");
				out.println("</div><div class=\"col-sm-4\">");
				out.println("<h3>Area of specialisation</h3>");
				out.println("<p>" + function + "</p>");
				out.println("</div>");
				out.println("</div>");

				File userFolder = new File(ctx.getGlobalContext().getUserFolder(user));
				if (userFolder.isDirectory() && userFolder.listFiles().length > 0) {
					out.println("<hr /><h3>Attachments</h3><ul class=\"files\">");
					for (File file : userFolder.listFiles()) {
						try {
							StaticInfo info = StaticInfo.getInstance(ctx, file);
							out.println("<li><a target=\"_blank\" href=\"" + info.getURL(ctx) + "\">" + info.getFile().getName() + "</a></li>");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					out.println("</ul>");
				}

				if (user.getInfo().trim().length() > 0) {
					out.println("<hr /><h3>More info</h3>");
					out.println("<p>" + user.getInfo() + "</p>");
				}

				out.println("</div></div></div>");
			}
		}
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	public static String performSearch(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		String text = rs.getParameter("text", "").trim();
		String country = rs.getParameter("country", "").trim();
		String domain = rs.getParameter("domain", "").trim();
		String role = rs.getParameter("role", "").trim();
		if (text.length() == 0 && country.length() == 0 && domain.length() == 0 && role.length() == 0) {
			return null;
		}
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		AdminUserFactory userFactory = AdminUserFactory.createAdminUserFactory(globalContext, ctx.getRequest().getSession());
		List<IUserInfo> users = userFactory.getUserInfoList();
		List<IUserInfo> result = new LinkedList<IUserInfo>();
		for (IUserInfo user : users) {
			if (BeanHelper.beanToString(user).contains(text)) {
				if (country.length() == 0 || ((UserInfo) user).getCountry().equals(country)) {
					if (domain.length() == 0 || ((UserInfo) user).getFunction().contains(domain)) {
						if (role.length() == 0 || ((UserInfo) user).getRoles().contains(role)) {
							result.add(user);
						}
					}
				}
			}
		}
		ctx.getRequest().setAttribute("users", result);
		return null;
	}

	public synchronized static String performAjaxsearch(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String text = rs.getParameter("text", "").trim().toLowerCase();
		String country = rs.getParameter("country", "").trim();
		String domain = rs.getParameter("domain", "").trim();
		String organization = rs.getParameter("organization", "").trim();
		String role = rs.getParameter("role", "").trim();
		IUserFactory userFactory = UserFactory.createUserFactory(ctx.getRequest());
		List<IUserInfo> users = userFactory.getUserInfoList();
		List<UserInfoWrapper> result = new LinkedList<>();
		for (IUserInfo user : users) {
			if ((BeanHelper.beanToString(user) + ' ' + user.getLastName()+' '+user.getFirstName()).toLowerCase().contains(text) || StringHelper.isEmpty(text)) {
				if (country.length() == 0 || ((UserInfo) user).getCountry().equals(country)) {
					if (domain.length() == 0 || ((UserInfo) user).getFunction().contains(domain)) {
						if (role.length() == 0 || ((UserInfo) user).getRoles().contains(role)) {
							if (organization.length() == 0 || ((UserInfo) user).getOrganization().equals(organization)) {
								result.add(new UserInfoWrapper(ctx, user));
							}
						}
					}
				}
			}
		}
		Collections.sort(result, new Comparator<UserInfoWrapper>() {
			@Override
			public int compare(UserInfoWrapper o1, UserInfoWrapper o2) {
				return o1.getUserInfo().getFirstName().compareTo(o2.getUserInfo().getFirstName());
			}
		});
		
		String json = new Gson().toJson(result);
		ResourceHelper.writeStringToStream(json, ctx.getResponse().getOutputStream(), ContentContext.CHARACTER_ENCODING);
		ctx.setStopRendering(true);
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
	public String getActionGroupName() {
		return getType();
	}
}

