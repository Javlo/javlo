package org.javlo.macro.interactive;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.actions.IAction;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.image.GlobalImage;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;

public class ChangeImageFilter implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(ChangeImageFilter.class.getName());

	@Override
	public String getName() {
		return "change-image-filter";
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
	public String getRenderer() {
		return "/jsp/macros/change-image-filter.jsp";
	}

	@Override
	public String getActionGroupName() {
		return "change-image-filter";
	}

	public static String performChange(RequestService rs, ContentContext ctx, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws MalformedURLException, Exception {

		ContentContext noAreaCtx = ctx.getContextWithArea(null);
		ContentElementList compIte = ctx.getCurrentPage().getContent(noAreaCtx);

		String filter = rs.getParameter("filter", null);

		int c = 0;
		if (!StringHelper.isEmpty(filter)) {
			while (compIte.hasNext(noAreaCtx)) {
				IContentVisualComponent comp = compIte.next(noAreaCtx);
				if (comp instanceof GlobalImage && ! comp.isRepeat()) {
					c++;
					((GlobalImage)comp).setFilter(filter);
				}
			}
		}
		ctx.setClosePopup(true);
		messageRepository.setGlobalMessage(new GenericMessage(c+" images updated.", GenericMessage.INFO));

		return null;
	}

	@Override
	public String prepare(ContentContext ctx) {
		try {			
			ctx.getRequest().setAttribute("filters", ctx.getCurrentTemplate().getImageFilters());
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
}
