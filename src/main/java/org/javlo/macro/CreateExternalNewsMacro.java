package org.javlo.macro;

import java.util.Map;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentContext;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.StringHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;

public class CreateExternalNewsMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "create-external-news";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		ComponentContext compCtx = ComponentContext.getInstance(ctx.getRequest());
		ContentService content = ContentService.getInstance(ctx.getRequest());

		MenuElement newPage = MacroHelper.addPage(ctx, "news", "external-news-", true);		

		ComponentBean title = new ComponentBean(StringHelper.getRandomId(), "title", null, ctx.getLanguage(), false, ctx.getCurrentEditUser());
		newPage.addContent(null, title);
		compCtx.addNewComponent(content.getComponent(ctx, title.getId()));

		ComponentBean text = new ComponentBean(StringHelper.getRandomId(), "paragraph", null, ctx.getLanguage(), false, ctx.getCurrentEditUser());
		newPage.addContent(title.getId(), text);
		compCtx.addNewComponent(content.getComponent(ctx, text.getId()));

		ComponentBean elnk = new ComponentBean(StringHelper.getRandomId(), "external-link", null, ctx.getLanguage(), false, ctx.getCurrentEditUser());
		newPage.addContent(text.getId(), elnk);
		compCtx.addNewComponent(content.getComponent(ctx, elnk.getId()));

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.setAskStore(true);

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
	
	@Override
	public boolean isAdd() {
		return true;
	}
	
	

}
