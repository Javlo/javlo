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
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.MessageRepository;
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
		Set<String> types = new HashSet<String>();
		ContentElementList list;
		try {
			list = ctx.getCurrentPage().getContent(ctx);
			while (list.hasNext(ctx)) {
				String type = list.next(ctx).getType();
				if (!types.contains(type)) {
					types.add(type);
				}
			}
			ctx.getRequest().setAttribute("types", types);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return null;
	}

	public static String performDelete(RequestService rs, ContentService content, EditContext editCtx, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException, Exception {
		Set<String> types = new HashSet<String>(Arrays.asList(rs.getParameterValues("types", new String[0])));
		List<String> deleteId = new LinkedList<String>();
		ContentElementList list = ctx.getCurrentPage().getContent(ctx);
		while (list.hasNext(ctx)) {
			IContentVisualComponent comp = list.next(ctx);			
			if (types.contains(comp.getType())) {
				deleteId.add(comp.getId());
			}
		}
		for (String id : deleteId) {
			ctx.getCurrentPage().removeContent(ctx, id);
		}
		ctx.getCurrentPage().releaseCache();
		ctx.setClosePopup(true);
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
}
