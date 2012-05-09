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
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService;


public class CreatePressReleaseStructure2007Macro extends AbstractMacro {
	
	public int getYear() {
		return 2007;
	}

	public String getName() {
		return "create-press-release-structure-"+getYear();
	}

	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		
		Date startDate = StringHelper.parseDate("01/12/"+getYear());
		
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(startDate);
		
		MenuElement yearPage = MacroHelper.addPageIfNotExist(ctx, "press-release", "pr-"+getYear(), true);
		yearPage.setVisible(true);
		boolean lastMounth = false;
		while (!lastMounth) {
			String year = ""+cal.get(Calendar.YEAR);
			String englishMonth = MacroHelper.getDisplayName(cal, Calendar.MONTH, MacroHelper.CALENDAR_LONG, Locale.ENGLISH);
			
			MenuElement mounthPage = MacroHelper.addPageIfNotExist(ctx, yearPage.getName(), "pr-"+year+"-"+englishMonth, false);
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
			
			//MenuElement newPage = MacroHelper.addPage(ctx, mounthPage.getName(), "pr-"+year+"-"+englishMonthShort+"-");

		}
		
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);
		
		return null;
	}

}
