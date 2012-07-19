package org.javlo.module.dashboard;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.StatContext;
import org.javlo.helper.LangHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.NotificationService;
import org.javlo.service.RequestService;
import org.javlo.service.exception.ServiceException;
import org.javlo.tracking.Track;
import org.javlo.tracking.Tracker;

public class DashboardAction extends AbstractModuleAction {

	@Override
	public String getActionGroupName() {
		return "dashboard";
	}

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String msg = super.prepare(ctx, modulesContext);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		NotificationService notificationService = NotificationService.getInstance(globalContext);
		ctx.getRequest().setAttribute("notification", notificationService.getNotifications(9999));

		return msg;
	}

	public static String performReadTracker(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, GlobalContext globalContext, HttpSession session, HttpServletRequest request) throws ServiceException {
		String type = rs.getParameter("type", null);
		if (type == null) {
			return "bad request structure : need 'type' as parameter";
		}
		Tracker tracker = Tracker.getTracker(globalContext, session);
		StatContext statCtx = StatContext.getInstance(request);

		if (type.equals("languages")) {
			Map<String, Map<String, Object>> ajaxMap = new LinkedHashMap<String, Map<String, Object>>();
			Map<String, Integer> languages = tracker.getLanguage(statCtx);			
			int i = 0;
			for (String lang : languages.keySet()) {				
				ajaxMap.put("" + i, LangHelper.obj(new LangHelper.MapEntry("label", lang),new LangHelper.MapEntry("data", languages.get(lang)) ));
				i++;
			}
			ctx.setAjaxMap(ajaxMap);
		} else if (type.equals("charge")) {
			Map<Object, Object> ajaxMap = new Hashtable<Object, Object>();
			Calendar start = Calendar.getInstance();
			Calendar end = Calendar.getInstance();
			start.add(Calendar.HOUR, -1);
			Track[] trackers = tracker.getTracks(start.getTime(), end.getTime());;
			start.setTime(end.getTime()); // reset start date
			for (int i = 0; i < 10*6*60; i++) { // 10Sec * 6 = 1min * 60 = 1u
				start.add(Calendar.SECOND, -10);
				int charge = 0; 
				for (Track track : trackers) {
					Calendar trackCal = Calendar.getInstance();
					trackCal.setTimeInMillis(track.getTime());
					if (trackCal.after(start) && trackCal.before(end)) {
						charge++;
					}
				}
				ajaxMap.put(new Integer(i*10), charge);				
				end.setTime(start.getTime());
			}
			ctx.setAjaxMap(ajaxMap);
		}  else if (type.equals("week")) {
			Map<Object, Object> ajaxMap = new Hashtable<Object, Object>();
			for (int i=1; i<8;i++) {
				ajaxMap.put(new Integer(i), new Integer(0));
			}
			Calendar start = Calendar.getInstance();
			Calendar end = Calendar.getInstance();
			Calendar cal = Calendar.getInstance();
			 
			start.add(Calendar.WEEK_OF_YEAR, -1);
			start = TimeHelper.convertRemoveAfterDay(start);
			end = TimeHelper.convertRemoveAfterDay(end);
			end.add(Calendar.MILLISECOND, -1);
			
			Track[] tracks = tracker.getTracks(start.getTime(), end.getTime());
			List<String> sessionIdFound = new LinkedList<String>();

			for (int i = 1; i < tracks.length - 1; i++) {
				if (!sessionIdFound.contains(tracks[i].getSessionId())) {
					if (!NetHelper.isUserAgentRobot(tracks[i].getUserAgent())) {
						cal.setTimeInMillis(tracks[i].getTime());
						Integer key = new Integer(end.get(Calendar.DAY_OF_YEAR)) - new Integer(cal.get(Calendar.DAY_OF_YEAR));						
						Integer clicks = (Integer) ajaxMap.get(key);
						/*if (clicks == null) {
							clicks = new Integer(0);
						}*/
						clicks = new Integer(clicks.intValue() + 1);
						ajaxMap.put(key, clicks);
					}
					sessionIdFound.add(tracks[i].getSessionId());
					
				}
			}
			ctx.setAjaxMap(ajaxMap);
		} else {
			return "bad type : " + type;
		}

		return null;
	}
}
