package org.javlo.macro;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.javlo.component.links.MonthChildrenLink;
import org.javlo.component.title.PageTitle;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.NavigationHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;

public class CreateMonthHereMacro extends AbstractMacro {

	public String getName() {
		return "create-month-here";
	}

	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		ContentService content = ContentService.createContent(ctx.getRequest());
		MenuElement currentPage = ctx.getCurrentPage();

		if (currentPage.getParent() == null) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String msg = i18nAccess.getText("action.add.new-news-badposition");
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
		} else {
			Calendar cal = GregorianCalendar.getInstance();
			cal.setTime(MacroHelper.getCurrentMacroDate(ctx.getRequest().getSession()));

			String pageName = MonthChildrenLink.createMonthPageName(currentPage.getName(), cal.getTime());

			if (content.getNavigation(ctx).searchChildFromName(pageName) == null) {
				MenuElement monthPage = MacroHelper.addPageIfNotExist(ctx, currentPage, pageName, true, false);
				
				List<MenuElement> children = currentPage.getChildMenuElementsList();
				for (MenuElement child : children) {
					String[] step = child.getName().split("_");
					if (step.length>=3) {
						SimpleDateFormat simpleDate = new SimpleDateFormat("MMMM-yyyy", Locale.ENGLISH);
						String year = step[step.length-2];
						String month = step[step.length-1];
						
						Date date = simpleDate.parse(month+'-'+year);
						Calendar pageCal = GregorianCalendar.getInstance();
						pageCal.setTime(date);
						
						if (pageCal.after(cal)) {
							monthPage.setPriority(child.getPriority()+2);
						}
					}
				}
				NavigationHelper.changeStepPriority(children, 10);
				
				monthPage.setVisible(true);

				GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

				Properties monthStructure = ctx.getCurrentTemplate().getMacroProperties(globalContext, "month");
				if (monthStructure == null) {
					Collection<String> lgs = globalContext.getLanguages();
					for (String lg : lgs) {
						String parentId = "0";
						
						SimpleDateFormat simpleDate = new SimpleDateFormat("MMMM", new Locale(lg));
						String currentMonth = simpleDate.format(cal.getTime());
						ContentContext localContext = new ContentContext(ctx);
						localContext.setLanguage(lg);
						localContext.setContentLanguage(lg);
						localContext.setRequestContentLanguage(lg);
						
						String title = currentMonth+' '+cal.get(Calendar.YEAR);
						parentId = MacroHelper.addContent(lg, monthPage, parentId, PageTitle.TYPE, null, currentPage.getTitle(localContext)+" - "+title);
						parentId = MacroHelper.addContent(lg, monthPage, parentId, Title.TYPE, null, title);
					}
				} else {
					createPageStructure(ctx, monthPage, monthStructure);
				}
				
				
			}

			/*GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
			persistenceService.store(ctx);*/
			
		}

		return null;
	}
	
	@Override
	public boolean isAdmin() {
		return false;
	}

}
