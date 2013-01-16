package org.javlo.macro;

import java.util.Collection;
import java.util.Map;

import org.javlo.component.image.GlobalImage;
import org.javlo.component.text.DebugNote;
import org.javlo.component.text.Description;
import org.javlo.component.text.Paragraph;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.LoremIpsumGenerator;
import org.javlo.helper.MacroHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;


public class CreateBaseStructureMacro extends AbstractMacro {

	public String getName() {
		return "create-base-site-structure-here";
	}
	
	private static void addBaseContent (ContentContext ctx, MenuElement page) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Collection<String> lgs = globalContext.getContentLanguages();
		for (String lg : lgs) {
			String parentId = MacroHelper.addContent(lg, page, "0", DebugNote.TYPE, "page created automatically - please remove me");
			parentId = MacroHelper.addContent(lg, page, parentId, Title.TYPE, LoremIpsumGenerator.getParagraph(3, false, false));
			parentId = MacroHelper.addContent(lg, page, parentId, Description.TYPE, LoremIpsumGenerator.getParagraph(55, false, true));
			parentId = MacroHelper.addContent(lg, page, parentId, GlobalImage.TYPE, "");
			parentId = MacroHelper.addContent(lg, page, parentId, Paragraph.TYPE, LoremIpsumGenerator.getParagraph(80, false, true));
		}
	}

	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		
		MenuElement currentPage = ctx.getCurrentPage();
		
		MenuElement page = MacroHelper.addPageIfNotExist(ctx, currentPage, "home", false, false);
		addBaseContent(ctx, page);
		page = MacroHelper.addPageIfNotExist(ctx, currentPage, "whoiswho", false, false);
		addBaseContent(ctx, page);
		page = MacroHelper.addPageIfNotExist(ctx, currentPage, "news", false, false);
		addBaseContent(ctx, page);
		page = MacroHelper.addPageIfNotExist(ctx, currentPage, "contact", false, false);
		addBaseContent(ctx, page);
		page = MacroHelper.addPageIfNotExist(ctx, currentPage, "partners", false, false);
		addBaseContent(ctx, page);
		page = MacroHelper.addPageIfNotExist(ctx, currentPage, "disclaimer", false, false);
		addBaseContent(ctx, page);
		page = MacroHelper.addPageIfNotExist(ctx, currentPage, "sitemap", false, false);
		addBaseContent(ctx, page);
		
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);
		
		return null;
	}

}
