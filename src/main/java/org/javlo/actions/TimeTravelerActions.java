/** 
 * Created on Aug 13, 2003
 */
package org.javlo.actions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
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
		ContentService content = ContentService.createContent(request);
		Date travelTime = null;
		try {
			travelTime = new SimpleDateFormat("dd/MM/yy HH:mm").parse(request.getParameter("date"));
		} catch (Exception ex) {
			try {
				travelTime = new SimpleDateFormat("dd/MM/yy").parse(request.getParameter("date"));
			} catch (Exception ex2) {
			}
		}
		globalContext.getTimeTravelerContext().setTravelTime(travelTime);
		content.releaseTimeTravelerNav();
		return null;
	}

	public static String performReplacecurrentpage(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		ContentContext timeCtx = new ContentContext(ctx);
		timeCtx.setRenderMode(ContentContext.TIME_MODE);
		ContentContext editCtx = new ContentContext(ctx);
		editCtx.setRenderMode(ContentContext.EDIT_MODE);
		ContentService content = ContentService.createContent(request);

		MenuElement currentPageTime = timeCtx.getCurrentPage();
		MenuElement currentPageEdit = MacroHelper.addPageIfNotExistWithoutMessage(editCtx, content.getNavigation(editCtx), currentPageTime, false);

		MacroHelper.deleteLocalContent(currentPageEdit, editCtx);

		MacroHelper.copyLocalContent(currentPageTime, timeCtx, currentPageEdit, editCtx);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		PersistenceService persistenceService = PersistenceService.getInstance(globalContext);
		persistenceService.store(ctx);

		return null;
	}

}
