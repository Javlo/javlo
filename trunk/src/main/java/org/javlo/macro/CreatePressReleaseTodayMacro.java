package org.javlo.macro;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.javlo.component.image.GlobalImage;
import org.javlo.component.meta.DateComponent;
import org.javlo.component.text.Description;
import org.javlo.component.text.Paragraph;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;

public class CreatePressReleaseTodayMacro extends AbstractMacro {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(CreatePressReleaseTodayMacro.class.getName());

	@Override
	public String getName() {
		return "create-press-release-today";
	}

	public String getPageStructureName() {
		return "press-release";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(MacroHelper.getCurrentMacroDate(ctx.getRequest().getSession()));

		String year = "" + cal.get(Calendar.YEAR);
		String englishMonth = MacroHelper.getDisplayName(cal, Calendar.MONTH, MacroHelper.CALENDAR_LONG, Locale.ENGLISH);
		String englishMonthShort = MacroHelper.getDisplayName(cal, Calendar.MONTH, MacroHelper.CALENDAR_SHORT, Locale.ENGLISH);

		MenuElement yearPage = MacroHelper.addPageIfNotExist(ctx, "press-release", "pr-" + year, true);
		MenuElement mountPage = MacroHelper.addPageIfNotExist(ctx, yearPage.getName(), "pr-" + year + "-" + englishMonth, true);
		MenuElement newPage = MacroHelper.addPage(ctx, mountPage.getName(), "pr-" + year + "-" + englishMonthShort + "-", true);
		newPage.setVisible(true);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		Collection<String> lgs = globalContext.getLanguages();
		Properties pressReleaseStructure = ctx.getCurrentTemplate().getMacroProperties(globalContext, getPageStructureName());
		if (pressReleaseStructure == null) {
			for (String lg : lgs) {
				logger.info("no '" + getPageStructureName() + "' page structure found in template : " + ctx.getCurrentTemplate().getName());
				String parentId = "0";
				parentId = MacroHelper.addContent(lg, newPage, parentId, DateComponent.TYPE, "");
				parentId = MacroHelper.addContent(lg, newPage, parentId, Title.TYPE, "");
				parentId = MacroHelper.addContent(lg, newPage, parentId, Description.TYPE, "");
				parentId = MacroHelper.addContent(lg, newPage, parentId, GlobalImage.TYPE, "");
				parentId = MacroHelper.addContent(lg, newPage, parentId, Paragraph.TYPE, "");
			}
		} else {
			MacroHelper.createPageStructure(ctx, newPage, pressReleaseStructure, false);
		}

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);

		return null;
	}

	@Override
	public boolean isAdmin() {
		return false;
	}

}
