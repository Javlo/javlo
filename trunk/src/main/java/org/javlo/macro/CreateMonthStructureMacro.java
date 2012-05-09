package org.javlo.macro;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.StringHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;


public class CreateMonthStructureMacro extends AbstractMacro {

	public String getName() {
		return "create-month-structure-here";
	}

	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(Calendar.MONTH, 11);
		
		MenuElement yearPage = ctx.getCurrentPage();

		boolean lastMounth = false;
		while (!lastMounth) {
			Locale mouthLanguage = Locale.ENGLISH;

			Collection<String> lgs = globalContext.getContentLanguages();
			if (lgs.size() == 1) { // if only one language --> create page in this language				
				mouthLanguage = new Locale(lgs.iterator().next());
			}
			String monthName = MacroHelper.getDisplayName(cal, Calendar.MONTH, MacroHelper.CALENDAR_LONG, mouthLanguage);
			monthName = StringHelper.createFileName(monthName); // remove special char
			
			MenuElement mounthPage = MacroHelper.addPageIfNotExist(ctx, yearPage.getName(), yearPage.getName()+"-"+monthName, false);
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
		
		return null;
	}

}
