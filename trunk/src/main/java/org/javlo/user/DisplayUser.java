package org.javlo.user;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;

/**
 * this class is user for display user list.
 * 
 * @author pvanderm
 * 
 */
public class DisplayUser {

	private String id;

	private String login;

	private String email;

	private String firstName;

	private String lastName;

	private String modificationDate;

	private String[] roles;

	private ContentContext ctx;

	public DisplayUser(ContentContext inCtx) {
		ctx = inCtx;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getId() {
		return id;
	}

	public String getLastName() {
		return lastName;
	}

	public String getLinkToEdit() {
		StringWriter outStr = new StringWriter();

		PrintWriter out = new PrintWriter(outStr);

		String strId = "";
		if (getId() != null) {
			strId = StringHelper.stringWithoutSpecialChar(getId());
		}

		out.println("<form name=\"editoneuser" + strId + "\" method=\"post\">");
		out.println("<input type=\"hidden\" name=\"_login\" value=\"" + getId() + "\"/>");
		out.println("<a href=\"javascript:document.editoneuser" + strId + ".submit();\">" + getId() + "</a>");
		out.println("</form>");

		out.flush();
		return outStr.toString();
	}

	public String getLogin() {
		return login;
	}

	public String getModificationDate() {
		return modificationDate;
	}

	public String getRoles() {

		StringWriter outStr = new StringWriter();

		PrintWriter out = new PrintWriter(outStr);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		boolean admin = EditContext.getInstance(globalContext, ctx.getRequest().getSession()).getCurrentView() == EditContext.ADMIN_USER_VIEW;

		IUserFactory userFactory;
		if (admin) {
			userFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		} else {
			userFactory = UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		}
		String[] userRoles = userFactory.getAllRoles(globalContext, ctx.getRequest().getSession());
		out.println("<form name=\"roles" + getLogin() + "\" method=\"post\">");
		out.println("<div style=\"float: left\">");
		out.println("<input type=\"hidden\" name=\"webaction\" value=\"changeuserroles\"/>");
		out.println("<input type=\"hidden\" name=\"login\" value=\"" + getLogin() + "\"/>");
		out.println(XHTMLHelper.getInputMultiSelect("roles", userRoles, roles));
		out.println("</div>");
		out.println("<div style=\"text-align: right;\">");
		String imageDown = URLHelper.createStaticURL(ctx, "/images/trash.png");
		String imageUp = URLHelper.createStaticURL(ctx, "/images/trash_on.png");
		String js = "document.forms['roles" + getLogin() + "'].webaction.value='deleteuser';document.forms['roles" + getLogin() + "'].submit();";
		out.println(XHTMLHelper.getImageLink("delete", imageDown, imageUp, "#", js));
		out.println("<input style=\"margin-top: 15px;\" type=\"submit\" name=\"ok\" value=\"ok\"/>");
		out.println("</div>");
		out.println("</form>");

		return outStr.toString();
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public void setModificationDate(String modificationDate) {
		this.modificationDate = modificationDate;
	}

	public void setRoles(String[] roles) {
		this.roles = roles;
	}

}
