package org.javlo.macro;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;

public class CreatePressReleaseStructure2007HereMacro extends AbstractMacro {

	public int getYear() {
		return 2007;
	}

	@Override
	public String getName() {
		return "create-pr-structure-here-" + getYear();
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		MenuElement currentPage = ctx.getCurrentPage();

		if ((currentPage.getParent() == null) || (!currentPage.getParent().getName().equals("press-release"))) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String msg = i18nAccess.getText("action.add.new-news-badposition");
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
		} else {

			Date startDate = StringHelper.parseDate("01/12/" + getYear());

			Calendar cal = GregorianCalendar.getInstance();
			cal.setTime(startDate);

			MenuElement yearPage = MacroHelper.addPageIfNotExist(ctx, currentPage, currentPage.getName() + "-" + getYear(), true, false);
			yearPage.setVisible(true);
			boolean lastMounth = false;
			while (!lastMounth) {
				String year = "" + cal.get(Calendar.YEAR);
				String englishMonth = MacroHelper.getDisplayName(cal, Calendar.MONTH, MacroHelper.CALENDAR_LONG, Locale.ENGLISH);

				MenuElement mounthPage = MacroHelper.addPageIfNotExist(ctx, yearPage.getName(), currentPage.getName() + "-" + year + "-" + englishMonth, false);
				Collection<String> lgs = globalContext.getLanguages();
				if (mounthPage.getContent().length == 0) {
					mounthPage.setVisible(true);
					for (String lg : lgs) {
						SimpleDateFormat mounthFormatDate = new SimpleDateFormat("MMMMMMMMMMMMMM", new Locale(lg));
						String mounthName = mounthFormatDate.format(cal.getTime());

						MacroHelper.addContent(lg, mounthPage, "0", Title.TYPE, mounthName);
					}
				}

				cal.roll(Calendar.MONTH, false);
				if (cal.get(Calendar.MONTH) == 11) {
					lastMounth = true;
				}

			}

			PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
			persistenceService.store(ctx);
		}

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

}
