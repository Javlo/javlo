package org.javlo.macro;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

import org.javlo.component.image.GlobalImage;
import org.javlo.component.meta.DateComponent;
import org.javlo.component.text.Description;
import org.javlo.component.text.Paragraph;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;


public class CreatePressReleaseTodayHereMacro extends AbstractMacro {

	public String getName() {
		return "create-pr-today-here";
	}

	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		MenuElement currentPage = ctx.getCurrentPage();

		if ((currentPage.getParent() == null) || (!currentPage.getParent().getName().equals("press-release"))) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String msg = i18nAccess.getText("action.add.new-news-badposition");
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
		} else {
			Calendar cal = GregorianCalendar.getInstance();
			cal.setTime(MacroHelper.getCurrentMacroDate(ctx.getRequest().getSession()));

			String year = "" + cal.get(Calendar.YEAR);
			String englishMonth = MacroHelper.getDisplayName(cal, Calendar.MONTH, MacroHelper.CALENDAR_LONG, Locale.ENGLISH);
			String englishMonthShort = MacroHelper.getDisplayName(cal, Calendar.MONTH, MacroHelper.CALENDAR_SHORT, Locale.ENGLISH);

			MenuElement yearPage = MacroHelper.addPageIfNotExist(ctx, currentPage, currentPage.getName()+"-" + year, true, false);
			MenuElement mountPage = MacroHelper.addPageIfNotExist(ctx, yearPage.getName(), currentPage.getName() + "-" + year + "-" + englishMonth, true);
			MenuElement newPage = MacroHelper.addPage(ctx, mountPage.getName(), currentPage.getName() + "-" + year + "-" + englishMonthShort + "-", true);
			newPage.setVisible(true);

			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

			Collection<String> lgs = globalContext.getLanguages();
			for (String lg : lgs) {
				String parentId = "0";
				parentId = MacroHelper.addContent(lg, newPage, parentId, DateComponent.TYPE, "");
				parentId = MacroHelper.addContent(lg, newPage, parentId, Title.TYPE, "");
				parentId = MacroHelper.addContent(lg, newPage, parentId, Description.TYPE, "");
				parentId = MacroHelper.addContent(lg, newPage, parentId, GlobalImage.TYPE, "");
				parentId = MacroHelper.addContent(lg, newPage, parentId, Paragraph.TYPE, "");
			}

			PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
			persistenceService.store(ctx);
		}

		return null;
	}
	
	@Override
	public boolean isAdmin() {
		return false;
	}

}
