package org.javlo.macro;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.javlo.component.image.StandardImage;
import org.javlo.component.text.Description;
import org.javlo.component.text.Paragraph;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;


public class CreateDefaultPageStructure extends AbstractMacro {

	public String getName() {
		return "create-default-page-structure";
	}

	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		MenuElement currentPage = ctx.getCurrentPage();

		if (currentPage != null) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

			Calendar cal = GregorianCalendar.getInstance();
			cal.setTime(new Date());

			Collection<String> lgs = globalContext.getLanguages();
			for (String lg : lgs) {
				String parentId = "0";
				ContentContext lgCtx = new ContentContext(ctx);
				lgCtx.setLanguage(lg);
				if (currentPage.getContent(lgCtx).size(ctx) == 0) {
					//parentId = MacroHelper.addContent(lg, currentPage, parentId, DateComponent.TYPE, "");
					parentId = MacroHelper.addContent(lg, currentPage, parentId, Title.TYPE, "");
					parentId = MacroHelper.addContent(lg, currentPage, parentId, Description.TYPE, "");
					parentId = MacroHelper.addContent(lg, currentPage, parentId, StandardImage.TYPE, "");
					parentId = MacroHelper.addContent(lg, currentPage, parentId, Paragraph.TYPE, "");
				}
			}

			PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
			persistenceService.store(ctx);
		}
		return null;
	}

};