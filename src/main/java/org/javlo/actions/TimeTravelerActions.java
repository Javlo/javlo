/** 
 * Created on Aug 13, 2003
 */
package org.javlo.actions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.NavigationService;
import org.javlo.service.PersistenceService;

public class TimeTravelerActions implements IAction {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(TimeTravelerActions.class.getName());

	public String getActionGroupName() {
		return "time";
	}

	public static String performSettraveltime(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		GlobalContext globalContext = GlobalContext.getInstance(request);
		ContentService content = ContentService.getInstance(request);
		Date travelTime = null;
		boolean previous = request.getParameter("previous") != null;
		boolean next = request.getParameter("next") != null;
		if (next || previous) {
			travelTime = globalContext.getTimeTravelerContext().getTravelTime();
			List<Date> dates = PersistenceService.getInstance(globalContext).getBackupDates();
			if (dates.size() > 0) {
				Integer currentIndex = null;
				if (travelTime != null) {
					long minDiff = Long.MIN_VALUE;
					for (int i = 0; i < dates.size(); i++) {
						Date backup = dates.get(i);
						long diff = backup.getTime() - travelTime.getTime();
						if (diff <= 0 && diff > minDiff) {
							minDiff = diff;
							currentIndex = i;
						}
					}
				}
				if (currentIndex == null) {
					if (previous) {
						travelTime = dates.get(0);
					}
				} else {
					currentIndex += previous ? 1 : -1;
					if (currentIndex < 0) {
						travelTime = null;
					} else if (currentIndex >= dates.size()) {
						travelTime = dates.get(dates.size() - 1);
					} else {
						travelTime = dates.get(currentIndex);
					}
				}
			}
		} else {
			String dateStr = request.getParameter("date");
			try {
				travelTime = new SimpleDateFormat("dd/MM/yy HH:mm:ss").parse(dateStr);
			} catch (Exception ex) {
				try {
					travelTime = new SimpleDateFormat("dd/MM/yy HH:mm").parse(dateStr);
				} catch (Exception ex2) {
					try {
						travelTime = new SimpleDateFormat("dd/MM/yy").parse(dateStr);
					} catch (Exception ex3) {
					}
				}
			}
		}
		globalContext.getTimeTravelerContext().setTravelTime(travelTime);
		content.releaseTimeTravelerNav(ctx);
		return null;
	}

	public static String performReplaceCurrentPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		ContentContext timeCtx = ctx.getContextWithOtherRenderMode(ContentContext.TIME_MODE);
		ContentContext editCtx = ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE);
		ContentService content = ContentService.getInstance(request);

		MenuElement timeCurrentPage = timeCtx.getCurrentPage();
		MenuElement editCurrentPage = MacroHelper.addPageIfNotExistWithoutMessage(editCtx, content.getNavigation(editCtx), timeCurrentPage, false, false);

		MacroHelper.deleteLocalContent(editCurrentPage, editCtx);

		MacroHelper.copyLocalContent(timeCurrentPage, timeCtx, editCurrentPage, editCtx);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);

		return null;
	}

	public static String performReplaceCurrentPageAndChildren(HttpServletRequest request, HttpServletResponse response) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		ContentContext timeCtx = ctx.getContextWithOtherRenderMode(ContentContext.TIME_MODE);
		ContentContext editCtx = ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE);
		ContentService content = ContentService.getInstance(request);

		NavigationService navigationService = NavigationService.getInstance(globalContext, ctx.getRequest().getSession());

		MenuElement timeCurrentPage = timeCtx.getCurrentPage();
		MenuElement editCurrentPage = content.getNavigation(editCtx);

		if (timeCurrentPage.getParent() != null) {
			editCurrentPage = editCurrentPage.searchChild(editCtx, timeCurrentPage.getPath());
		}

//		// Recreate object for other users
//		content.releaseTimeTravelerNav(timeCtx);
//		content.releasePreviewNav(editCtx);

		if (editCurrentPage != null) {
			// Switch parent (Experimental!!)
			MenuElement parent = editCurrentPage.getParent();
			timeCurrentPage.setParent(parent);
			timeCurrentPage.setId(editCurrentPage.getId());
			timeCurrentPage.setPriority(editCurrentPage.getPriority());
			editCurrentPage.setParent(null);

			parent.removeChild(editCurrentPage);
			parent.addChildMenuElement(timeCurrentPage);

		} else {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String msg = i18nAccess.getText("time.message.error.page-deleted");
			MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
		}

		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(editCtx);

		content.releaseTimeTravelerNav(timeCtx);
		content.releasePreviewNav(editCtx);

		return null;
	}

}
