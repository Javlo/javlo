package org.javlo.macro;

import java.util.Map;
import java.util.logging.Logger;

import org.javlo.actions.DataAction;
import org.javlo.actions.IAction;
import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.importation.ImportConfigBean;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.MessageRepository;
import org.javlo.module.macro.MacroModuleContext;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.user.User;

public class SmartImport implements IInteractiveMacro, IAction {
	
	private static Logger logger = Logger.getLogger(SmartImport.class.getName());

	@Override
	public String getName() {
		return "smart-import";
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
		return "macro-smart-import";
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/smart_import/home.jsp";
	}

	@Override
	public String prepare(ContentContext ctx) {
		

		return null;
	}

	public static String performUpload(RequestService rs, ContentContext ctx, GlobalContext gc, ContentService cs, User user, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		
		String url = rs.getParameter("url", null).trim();
		if (StringHelper.isURL(url)) {
			
		}

		ImportConfigBean config = new ImportConfigBean(ctx);
		if (rs.getParameter("image", "").equals("gallery")) {
			config.setImagesAsGallery(true);
		} else if (rs.getParameter("image", "").equals("image")) {
			config.setImagesAsImages(true);
		}
		config.setArea(rs.getParameter("area", ComponentBean.DEFAULT_AREA));
		if (rs.getParameter("after", null) != null) {			
			config.setBeforeContent(false);
		}
		
		DataAction.uploadContent(rs, ctx, gc, cs, user, messageRepository, i18nAccess, config);
		
		MacroModuleContext.getInstance(ctx.getRequest()).setActiveMacro(null);
		if (ctx.isEditPreview()) {
			ctx.setClosePopup(true);			
		}
		
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
}
