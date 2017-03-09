package org.javlo.macro.interactive;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.javlo.actions.IAction;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.fields.Field;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.MessageRepository;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

public class DeleteDynamicComponent implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(DeleteDynamicComponent.class.getName());

	private static final String NAME = "delete-dynamic-component";

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
		return "macro-delete-dynamic-component";
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/delete-dynamic-component.jsp";
	}

	@Override
	public String prepare(ContentContext ctx) {
		return null;
	}

	public static String performDelete(RequestService rs, ContentService content, EditContext editCtx, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException, Exception {
		String filter = rs.getParameter("filter", "").trim();
		String confirm = rs.getParameter("confirm", null);
		if (filter.length() == 0) {
			return "no filter defined.";
		} else {
			int deleteField = 0;
			int realDeleteField = 0;
			int foundField = 0;
			Properties prop = new Properties();
			prop.load(new StringReader(filter));
			for (DynamicComponent comp : ComponentFactory.getAllDynamicComponents(ctx)) {
				foundField++;
				for (Field field : comp.getFields(ctx)) {
					if (prop.containsKey(field.getName())) {
						if (field != null && field.getValue() != null && !field.getValue().contains(prop.getProperty(field.getName()))) {
							if (confirm == null) {
								deleteField++;
							} else {
								realDeleteField++;
								comp.getPage().removeContent(ctx, comp.getId());								
							}
						}
					}
				}
			}
			ctx.getRequest().setAttribute("deleteField", deleteField);
			ctx.getRequest().setAttribute("foundField", foundField);
			ctx.getRequest().setAttribute("resultField", foundField - deleteField);
			if (confirm != null) {
				return realDeleteField + " field deleted";
			} else {
				return null;
			}
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
