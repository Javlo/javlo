package org.javlo.macro.interactive;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.actions.IAction;
import org.javlo.component.image.IImageTitle;
import org.javlo.component.title.Heading;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.RequestService;
import org.javlo.service.social.SocialService;
import org.owasp.encoder.Encode;

public class SocialShare implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(SocialShare.class.getName());

	@Override
	public String getName() {
		return "social-share";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		return null;
	}

	@Override
	public boolean isAdmin() {
		return false;
	}

	@Override
	public String getActionGroupName() {
		return "macro-create-children";
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/socialShare.jsp";
	}

	@Override
	public String prepare(ContentContext ctx) {		
		try {
			ctx.getRequest().setAttribute("facebookID", SocialService.getInstance(ctx).getFacebook().getClientId());
			ctx.getRequest().setAttribute("title", Encode.forUri(ctx.getCurrentPage().getTitle(ctx)).replaceAll("'", "\'"));
			ContentContext absCtx = new ContentContext(ctx);
			absCtx.setRenderMode(ContentContext.VIEW_MODE);
			absCtx.setAbsoluteURL(true);
			ctx.getRequest().setAttribute("url", URLHelper.createURL(absCtx));
			IImageTitle image = ctx.getCurrentPage().getImage(absCtx);
			if (image != null) {
				ctx.getRequest().setAttribute("img", image.getResourceURL(absCtx));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String performCreate(RequestService rs, EditContext editCtx, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		int error = 0;
		String childrenNames = rs.getParameter("children",null);
		if (childrenNames == null) {
			return "request structure error : 'children' parameter needed.";
		} else {
			MenuElement currentPage = ctx.getCurrentPage();
			BufferedReader reader = new BufferedReader(new StringReader(childrenNames));
			String line = reader.readLine();
			while (line != null) {
				line = line.trim();
				if (line.length() > 0) {
					String pageName = StringHelper.removeRepeatedChar(StringHelper.createFileName(line),'-');
					if (line.contains("=")) {
						pageName = StringHelper.split(line, "=")[0];
						line = StringHelper.split(line, "=")[1];
					}
					MenuElement newPage = MacroHelper.addPage(ctx, currentPage, pageName, false, false);
					if (newPage == null) {
						error++;
					} else {
						MacroHelper.addContent(ctx.getRequestContentLanguage(), newPage, "0", Heading.TYPE, "depth=1\ntext="+line, ctx.getCurrentEditUser());
					}
				}
				line = reader.readLine();
			}
		}

		if (error == 0) {
			return null;
		} else {
			return error + " errors found.";
		}
	}

	@Override
	public boolean isPreview() {
		return true;
	}
	
	@Override
	public boolean isAdd() {
		return false;
	}
	
	@Override
	public boolean isInterative() {	
		return true;
	}
}
