package org.javlo.module.dashboard;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.StatContext;
import org.javlo.helper.LangHelper;
import org.javlo.helper.LangHelper.ListBuilder;
import org.javlo.helper.LangHelper.ObjectBuilder;
import org.javlo.helper.NetHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.helper.URLHelper;
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
			ObjectBuilder ajaxMap = LangHelper.object();
			List<Entry<String, Integer>> languages = new LinkedList<Map.Entry<String, Integer>>(tracker.getLanguage(statCtx).entrySet());
			Collections.sort(languages, new Comparator<Entry<String, Integer>>() {
				@Override
				public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
					return o1.getValue().compareTo(o2.getValue());
				}
			});
			ListBuilder datas = ajaxMap.list("datas");
			ListBuilder labels = ajaxMap.list("labels");
			for (Entry<String, Integer> lang : languages) {
				labels.add(lang.getKey());
				datas.add(lang.getValue());
			}
			ctx.setAjaxMap(ajaxMap.getMap());
		} else if (type.equals("referer")) {
			ObjectBuilder ajaxMap = LangHelper.object();
			List<Entry<String, Integer>> referers = new LinkedList<Entry<String, Integer>>(tracker.getReferer(statCtx).entrySet());
			String currentHost = URLHelper.extractHost(request.getRequestURL().toString());
			Collections.sort(referers, new Comparator<Entry<String, Integer>>() {
				@Override
				public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
					return -o1.getValue().compareTo(o2.getValue());
				}
			});
			int otherCount = 0;
			ListBuilder datas = ajaxMap.list("datas");
			for (Entry<String, Integer> referer : referers) {
				if (!referer.getKey().equals(currentHost) && !referer.getKey().equals("unknown")) {
					if (datas.getList().size() < 10) {
						datas.addList()
								.add(referer.getKey())
								.add(referer.getValue());
					} else {
						otherCount += referer.getValue();
					}
				}
			}
			if (otherCount > 0) {
				datas.addList().add("other").add(otherCount);
			}
			ctx.setAjaxMap(ajaxMap.getMap());
		} else if (type.equals("charge")) {
			Map<Object, Object> ajaxMap = new Hashtable<Object, Object>();
			Calendar start = Calendar.getInstance();
			Calendar end = Calendar.getInstance();
			start.add(Calendar.HOUR, -1);
			Track[] trackers = tracker.getTracks(start.getTime(), end.getTime());;
			start.setTime(end.getTime()); // reset start date			
			
			Calendar endRange = Calendar.getInstance();
			endRange.add(Calendar.HOUR, -1);
			
			int i=0;
			while (endRange.before(start)) {
				i++;
				start.add(Calendar.SECOND, -10);
				int charge = 0; 
				for (Track track : trackers) {
					Calendar trackCal = Calendar.getInstance();
					trackCal.setTimeInMillis(track.getTime());
					if (trackCal.after(start) && trackCal.before(end)) {
						charge++;
					}
				}				
				ajaxMap.put(new Integer(i*100), charge);				
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
		} else if (type.equals("visits")) {
			ObjectBuilder ajaxMap = LangHelper.object();
			Calendar start = Calendar.getInstance();
			Calendar end = Calendar.getInstance();
			Calendar cal = Calendar.getInstance();

			start.add(Calendar.MONTH, -1);
			end.add(Calendar.MILLISECOND, -1);

			Map<String, Integer> clicksByHour = new HashMap<String, Integer>();
			SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy HH");
			cal.setTime(start.getTime());
			while (cal.before(end)) {
				clicksByHour.put(sdf.format(cal.getTime()), 0);
				cal.add(Calendar.HOUR_OF_DAY, 1);
			}

			Track[] tracks = tracker.getTracks(start.getTime(), end.getTime());
			List<String> sessionIdFound = new LinkedList<String>();

			for (int i = 1; i < tracks.length - 1; i++) {
				if (!sessionIdFound.contains(tracks[i].getSessionId())) {
					if (!NetHelper.isUserAgentRobot(tracks[i].getUserAgent())) {
						Date time = new Date(tracks[i].getTime());
						String key = sdf.format(time);
						Integer clicks = (Integer) clicksByHour.get(key);
						if (clicks == null) {
							clicks = new Integer(0);
						}
						clicks = new Integer(clicks.intValue() + 1);
						clicksByHour.put(key, clicks);
					}
					sessionIdFound.add(tracks[i].getSessionId());

				}
			}
			ListBuilder datas = ajaxMap.list("datas");
			for (Entry<String, Integer> hour : clicksByHour.entrySet()) {
				datas.addList()
						.add(hour.getKey() + ":00:00")
						.add(hour.getValue());
			}
			ctx.setAjaxMap(ajaxMap.getMap());
		} else {
			return "bad type : " + type;
		}

		return null;
	}
}
