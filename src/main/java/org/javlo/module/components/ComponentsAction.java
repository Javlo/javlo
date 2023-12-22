package org.javlo.module.components;

import java.io.File;

import jakarta.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.javlo.actions.IModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.template.Template;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;

public class ComponentsAction implements IModuleAction {

	@Override
	public String getActionGroupName() {
		return "components";
	}

	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return true;
	}

	// protected String getPreviewCode(String css, String html) {
	// ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	// PrintStream out = new PrintStream(outStream);
	// out.print("<html>");
	// out.print("<head>");
	// out.print("<link rel=\"stylesheet\"
	// href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.1.2/css/bootstrap.min.css\"
	// integrity=\"sha384-Smlep5jCw/wG7hdkwQ/Z5nLIefveQRIY9nfy6xoR1uRYBtpZgI6339F5dgvm/e9B\"
	// crossorigin=\"anonymous\">");
	// out.print("<script src=\"https://code.jquery.com/jquery-3.3.1.slim.min.js\"
	// integrity=\"sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo\"
	// crossorigin=\"anonymous\"></script>");
	// out.print("<script
	// src=\"https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js\"
	// integrity=\"sha384-ZMP7rVo3mIykV+2+9J3UJ46jBk0WLaUAdn689aCwoqbBJiSnjAK/l8WvCWPIPm49\"
	// crossorigin=\"anonymous\"></script>");
	// out.print("<script
	// src=\"https://stackpath.bootstrapcdn.com/bootstrap/4.1.2/js/bootstrap.min.js\"
	// integrity=\"sha384-o+RDsa0aLu++PJvFqy8fFScvbHFLtbvScb8AjopnFD+iEQ7wo/CG0xlczd+2O/em\"
	// crossorigin=\"anonymous\"></script>");
	// out.print("<style>"+StringHelper.removeCR(css)+"</style>");
	// out.print("</head>");
	// out.print("<body>");
	// out.print(StringHelper.removeCR(html));
	// out.print("</body>");
	// out.print("</html>");
	// out.close();
	// return new String(outStream.toByteArray());
	// }

	public static void checkAccess(ContentContext ctx) throws SecurityException {
		AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance();
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		IUserFactory adminUserFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		if (adminUserFactory.getCurrentUser(globalContext, ctx.getRequest().getSession()) == null) {
			throw new SecurityException("no access");
		}
		ContentService.getInstance(globalContext);

		if (!adminUserSecurity.haveRight(adminUserFactory.getCurrentUser(globalContext, ctx.getRequest().getSession()), AdminUserSecurity.FULL_CONTROL_ROLE)) {
			if (!adminUserFactory.getCurrentUser(globalContext, ctx.getRequest().getSession()).validForRoles(AdminUserSecurity.DESIGN_ROLE)) {
				throw new SecurityException("no access");
			}
		}
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext moduleContext) throws Exception {
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		String comp = rs.getParameter("component");
		String cssText = null;
		String htmlText = null;
		if (comp != null) {
			for (File child : ctx.getGlobalContext().getExternComponents()) {
				if (child.getName().equals(comp)) {
					htmlText = ResourceHelper.loadStringFromFile(child);
					ctx.getRequest().setAttribute("xhtml", htmlText);
					if (!StringHelper.getFileExtension(child.getName()).equals("html")) {
						htmlText = null;
					}
					ctx.getRequest().setAttribute("xhtmlExist", true);
					File css = new File(StringHelper.getFileNameWithoutExtension(child.getAbsolutePath()) + ".scss");
					if (!css.exists()) {
						css = new File(StringHelper.getFileNameWithoutExtension(child.getAbsolutePath()) + ".css");
					}
					if (css.exists()) {
						cssText = ResourceHelper.loadStringFromFile(css);
						cssText = StringHelper.indentScss(cssText);
						ctx.getRequest().setAttribute("css", cssText);
						ctx.getRequest().setAttribute("cssExist", true);
					}
					File properties = new File(StringHelper.getFileNameWithoutExtension(child.getAbsolutePath()) + ".properties");
					if (properties.exists()) {
						ctx.getRequest().setAttribute("properties", ResourceHelper.loadStringFromFile(properties));
						ctx.getRequest().setAttribute("propertiesExist", true);
					}
				}
			}
		}
		if (htmlText != null) {
			ComponentsContext componentsContext = ComponentsContext.getInstance(ctx);
			componentsContext.setCss(cssText);
			componentsContext.setHtml(htmlText);
			ctx.getRequest().setAttribute("previousUrl", URLHelper.createStaticURL(ctx, "/modules/components/jsp/previous.jsp"));
			ctx.getRequest().setAttribute("detectedFields", Template.extractPropertiesFromHtml(htmlText));
			// ctx.getRequest().setAttribute("previousHTML", getPreviewCode(cssText,
			// htmlText));
		}

		return null;
	}

	@Override
	public String performSearch(ContentContext ctx, ModulesContext modulesContext, String query) throws Exception {
		return null;
	}

	@Override
	public Boolean haveRight(HttpSession session, User user) throws ModuleException {
		return true;
	}

	public String performAddcomponent(ContentContext ctx, ModulesContext modulesContext, RequestService rs) throws Exception {
		checkAccess(ctx);
		String comp = StringHelper.createFileName(rs.getParameter("component"));
		String ext = StringHelper.getFileExtension(comp);
		if (!(ext.equals("html") || ext.equals("jsp"))) {
			comp += ".html";
		}
		File file = new File(URLHelper.mergePath(ctx.getGlobalContext().getExternComponentFolder(), comp));
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		} else {
			return "comp: "+comp+ " already exist.";
		}
		return null;
	}
	public String performCreatefile(ContentContext ctx, ModulesContext modulesContext, RequestService rs) throws Exception {
		checkAccess(ctx);
		String comp = rs.getParameter("component");
		String type = rs.getParameter("type");
		if (!(type.equals("css") || type.equals("scss") || type.equals("properties"))) {
			return "bad file type.";
		}
		if (comp != null) {
			File file = new File(URLHelper.mergePath(ctx.getGlobalContext().getExternComponentFolder(), StringHelper.getFileNameWithoutExtension(comp)+"."+type));
			if (!file.exists()) {
				file.createNewFile();
			}
		}
		return null;
	}
	
	public String performDeletefile(ContentContext ctx, ModulesContext modulesContext, RequestService rs) throws Exception {
		checkAccess(ctx);
		String comp = rs.getParameter("component");
		String type = rs.getParameter("type");
		if (!(type.equals("css") || type.equals("scss") || type.equals("properties"))) {
			return "bad file type.";
		}
		if (comp != null) {
			File file = new File(URLHelper.mergePath(ctx.getGlobalContext().getExternComponentFolder(), StringHelper.getFileNameWithoutExtension(comp)+"."+type));
			if (file.exists()) {
				file.delete();
			}
		}
		return null;
	}

	public String performUpdate(ContentContext ctx, ModulesContext modulesContext, RequestService rs) throws Exception {
		checkAccess(ctx);
		String xhtml = rs.getParameter("html");
		String properties = rs.getParameter("properties");
		String css = rs.getParameter("css");
		String comp = rs.getParameter("component");
		if (comp != null) {
			for (File child : ctx.getGlobalContext().getExternComponents()) {
				if (child.getName().equals(comp)) {
					FileUtils.write(child, xhtml, ContentContext.CHARSET_DEFAULT);
					if (css != null) {
						File cssFile = new File(StringHelper.getFileNameWithoutExtension(child.getAbsolutePath()) + ".scss");
						if (!cssFile.exists()) {
							cssFile = new File(StringHelper.getFileNameWithoutExtension(child.getAbsolutePath()) + ".css");
						}
						if (cssFile.exists()) {
							FileUtils.write(cssFile, css, ContentContext.CHARSET_DEFAULT);
						}
						File propertiesFile = new File(StringHelper.getFileNameWithoutExtension(child.getAbsolutePath()) + ".properties");
						if (propertiesFile.exists()) {
							FileUtils.write(propertiesFile, properties, ContentContext.CHARSET_DEFAULT);
						}
					}
				}
			}
		}
		return null;
	}

}
