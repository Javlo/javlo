package org.javlo.macro;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.javlo.actions.IAction;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

public class DeleteComponent implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(DeleteComponent.class.getName());

	private static final String NAME = "delete-component";

	@Override
	public String getName() {
		return NAME;
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
		return "macro-delete-component";
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/delete-component.jsp";
	}

	@Override
	public String prepare(ContentContext ctx) {
		try {			
			ctx.getRequest().setAttribute("components", ComponentFactory.getComponents(ctx.getGlobalContext()));
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return null;
	}

	public static String performDelete(RequestService rs, ContentService content, EditContext editCtx, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException, Exception {
		Set<String> types = new HashSet<String>(Arrays.asList(rs.getParameterValues("types", new String[0])));		
		List<MenuElement> pages = new LinkedList<MenuElement>();
		String contentContains = rs.getParameter("content", "").trim();
		if (StringHelper.isTrue(rs.getParameter("allpages", null))) {
			ContentService pageContent = ContentService.getInstance(ctx.getRequest());
			for (MenuElement page : pageContent.getNavigation(ctx).getAllChildren()) {
				pages.add(page);
			}
		} else {
			pages.add(ctx.getCurrentPage());
		}
		int countDelete = 0;
		for (MenuElement page : pages) {
			List<String> deleteId = new LinkedList<String>();
			ContentElementList list = page.getContent(ctx);
			while (list.hasNext(ctx)) {
				IContentVisualComponent comp = list.next(ctx);			
				if (types.contains(comp.getType())) {
					if (comp.getValue(ctx).contains(contentContains) || contentContains.length() == 0) {
						deleteId.add(comp.getId());
					}
				}
			}
			for (String id : deleteId) {
				page.removeContent(ctx, id);
				countDelete++;
			}
		}		
		ctx.getCurrentPage().releaseCache();
		ctx.setClosePopup(true);
		MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(countDelete+" components deleted", GenericMessage.INFO));
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
}
