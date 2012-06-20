package org.javlo.macro;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Properties;

import org.javlo.component.links.WeekChildrenLink;
import org.javlo.component.list.FreeTextList;
import org.javlo.component.meta.DateComponent;
import org.javlo.component.meta.LocationComponent;
import org.javlo.component.title.SubTitle;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;

public class CreateWeekHereMacro extends AbstractMacro {

	public String getName() {
		return "create-week-here";
	}

	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		ContentService content = ContentService.createContent(ctx.getRequest());
		MenuElement currentPage = ctx.getCurrentPage();;

		if (currentPage.getParent() == null) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String msg = i18nAccess.getText("action.add.new-news-badposition");
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
		} else {
			Calendar cal = GregorianCalendar.getInstance();
			cal.setTime(MacroHelper.getCurrentMacroDate(ctx.getRequest().getSession()));

			String pageName = WeekChildrenLink.createWeekPageName(currentPage.getName(), cal.getTime());

			if (content.getNavigation(ctx).searchChildFromName(pageName) == null) {
				MenuElement weekPage = MacroHelper.addPageIfNotExist(ctx, currentPage, pageName, true, false);
				weekPage.setVisible(false);

				GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

				Properties weekStructure = ctx.getCurrentTemplate().getMacroProperties(globalContext, "week");
				if (weekStructure == null) {
					Collection<String> lgs = globalContext.getContentLanguages();
					for (String lg : lgs) {
						String parentId = "0";
						parentId = MacroHelper.addContent(lg, weekPage, parentId, SubTitle.TYPE, "3", "");
						parentId = MacroHelper.addContent(lg, weekPage, parentId, DateComponent.TYPE, null, "");
						parentId = MacroHelper.addContent(lg, weekPage, parentId, LocationComponent.TYPE, null, "");
						parentId = MacroHelper.addContent(lg, weekPage, parentId, FreeTextList.TYPE, null, "");
						parentId = MacroHelper.addContent(lg, weekPage, parentId, DateComponent.TYPE, null, "");
						parentId = MacroHelper.addContent(lg, weekPage, parentId, LocationComponent.TYPE, null, "");
						parentId = MacroHelper.addContent(lg, weekPage, parentId, FreeTextList.TYPE, null, "");
					}
				} else {
					createPageStructure(ctx, weekPage, weekStructure);
				}
			}

			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
			persistenceService.store(ctx);
		}

		return null;
	}
	
	public static void main(String[] args) {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(StringHelper.parseDate("02/09/2009"));
			System.out.println("*** cal 1 = "+StringHelper.renderDate(cal.getTime()));
			System.out.println("*** cal 2 = "+StringHelper.renderDate(TimeHelper.toStartWeek(cal.getTime())));

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public boolean isAdmin() {
		return false;
	}

}
