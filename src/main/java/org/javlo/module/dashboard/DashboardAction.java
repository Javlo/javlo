package org.javlo.module.dashboard;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.component.core.DebugNote;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.StatContext;
import org.javlo.helper.LangHelper;
import org.javlo.helper.LangHelper.ListBuilder;
import org.javlo.helper.LangHelper.ObjectBuilder;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.PageBean;
import org.javlo.service.ContentService;
import org.javlo.service.NotificationService;
import org.javlo.service.RequestService;
import org.javlo.tracking.Track;
import org.javlo.tracking.Tracker;
import org.javlo.user.AdminUserSecurity;
import org.javlo.utils.MemoryBean;

public class DashboardAction extends AbstractModuleAction {
	
	public static class DebugNoteBean {
		private String message;
		private String authors;
		private String area;
		private String url;
		private String priority;
		private boolean currentUser;
		private PageBean page;
		private String date;

		public DebugNoteBean(String message, String priority, boolean currentUser, String modifDate, String authors, String area, String pageURL, PageBean page) {
			super();
			this.message = message;
			this.authors = authors;
			this.area = area;
			this.url = pageURL;
			this.page = page;
			this.priority = priority;
			this.currentUser = currentUser;
			this.setDate(modifDate);
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getAuthors() {
			return authors;
		}

		public void setAuthors(String authors) {
			this.authors = authors;
		}

		public String getArea() {
			return area;
		}

		public void setArea(String area) {
			this.area = area;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String pageURL) {
			this.url = pageURL;
		}

		public PageBean getPage() {
			return page;
		}

		public void setPage(PageBean page) {
			this.page = page;
		}

		public String getPriority() {
			return priority;
		}

		public void setPriority(String priority) {
			this.priority = priority;
		}

		public boolean isCurrentUser() {
			return currentUser;
		}

		public void setCurrentUser(boolean currentUser) {
			this.currentUser = currentUser;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

	}

	public static void main(String[] args) {
		System.out.println("***** DashboardAction.main : time = " + StringHelper.renderTime(new Date(Long.parseLong("1345208695440")))); // TODO:
																																			// remove
																																			// debug
																																			// trace
	}

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

		ModulesContext dashboardContext = ModulesContext.getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext());
		boolean report = false;
		/*for (Box box : dashboardContext.getCurrentModule().getMainBoxes()) {
			if (box.getName().equals("report")) {
				report = true;
				ctx.getRequest().setAttribute("report", ReportFactory.getReport(ctx));
			}
		}*/
		if (dashboardContext.getCurrentModule().getRenderer() != null && dashboardContext.getCurrentModule().getRenderer().contains("report")) {
			report = true;
			ctx.getRequest().setAttribute("report", ReportFactory.getReport(ctx));			
		}
		boolean pagelist = false;
		if (dashboardContext.getCurrentModule().getRenderer() != null && dashboardContext.getCurrentModule().getRenderer().contains("pagelist")) {
			pagelist = true;
			ctx.getRequest().setAttribute("report", ReportFactory.getReport(ctx));
		}
		if (dashboardContext.getCurrentModule().getBoxes().size() > 1) {
			ctx.getRequest().setAttribute("page", "main");
			ctx.getRequest().setAttribute("memory", new MemoryBean());
		} else {
			if (!report && !pagelist) {
				ctx.getRequest().setAttribute("page", "tracker");
			} else if (!pagelist) {
				ctx.getRequest().setAttribute("page", "report");
			} else {
				ctx.getRequest().setAttribute("page", "list");
			}
		}

		String trackerDate = ctx.getRequest().getParameter("date");		
		if (trackerDate != null) {
			Date date = StringHelper.smartParseDate(trackerDate);			
			ctx.getRequest().setAttribute("date", StringHelper.renderDate(date));			
			ctx.getRequest().setAttribute("tracks", Tracker.getTracker(globalContext, ctx.getRequest().getSession()).getAllTrack(date));
		} else {
			/* debug notes */
			ContentService content = ContentService.getInstance(ctx.getRequest());
			List<IContentVisualComponent> components = content.getAllContent(ctx);
			Collection<DebugNoteBean> debugNoteList = new LinkedList<DebugNoteBean>();
			for (IContentVisualComponent comp : components) {
				if (comp.getType() == DebugNote.TYPE) {
					Map<String, String> params = new HashMap<String, String>();
					params.put("module", "content");
					String url = URLHelper.createURL(ctx, comp.getPage().getPath(), params);
					debugNoteList.add(new DebugNoteBean(((DebugNote) comp).getText(), ((DebugNote) comp).getPriority(), ((DebugNote) comp).getUserList().contains(ctx.getCurrentEditUser().getLogin()), ((DebugNote) comp).getModifDate(), comp.getAuthors(), comp.getArea(), url, comp.getPage().getPageBean(ctx)));
				}
			}
			ctx.getRequest().setAttribute("debugNotes", debugNoteList);
		}
		return msg;
	}	

	public static String performReadTracker(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess, GlobalContext globalContext, HttpSession session, HttpServletRequest request) throws Exception {
		String type = rs.getParameter("type", null);
		if (type == null) {
			return "bad request structure : need 'type' as parameter";
		}
		Tracker tracker = Tracker.getTracker(globalContext, session);
		StatContext statCtx = StatContext.getInstance(request);
		// 30 days is default period
		Calendar now = Calendar.getInstance();
		statCtx.setTo(new Date(now.getTime().getTime()));
		now.add(Calendar.DAY_OF_YEAR, -30);
		statCtx.setFrom(new Date(now.getTime().getTime()));
		
		if (type.equals("year")) {
			//Map<String, Integer> ajaxMap = new LinkedHashMap<String, Integer>();
			String year = rs.getParameter("y", ""+now.get(Calendar.YEAR));
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, Integer.parseInt(year));			
			cal.set(Calendar.MONTH, 11);
			cal.set(Calendar.DAY_OF_MONTH, 31);
			cal.set(Calendar.HOUR, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			statCtx.setTo(cal.getTime());
			cal.set(Calendar.YEAR, Integer.parseInt(year));
			cal.set(Calendar.MONTH, 0);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			statCtx.setFrom(cal.getTime());
			Map<Integer, Integer> map = tracker.getSession2ClickByMonth(statCtx, globalContext);
			ObjectBuilder ajaxMap = LangHelper.object();
			ListBuilder datas = ajaxMap.list("datas");
			for (Map.Entry<Integer, Integer> input : map.entrySet()) {
				cal.set(Calendar.MONTH, input.getKey());
				String month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, new Locale(ctx.getGlobalContext().getEditLanguage(session)));
				datas.addList().add(month).add(input.getValue());				
			}
			ctx.setAjaxMap(ajaxMap.getMap());
			
		} else if (type.equals("languages")) {
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
						datas.addList().add(referer.getKey()).add(referer.getValue());
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
			Track[] trackers = tracker.getTracks(start.getTime(), end.getTime());

			start.setTime(end.getTime()); // reset start date

			Calendar endRange = Calendar.getInstance();
			endRange.add(Calendar.HOUR, -1);

			int i = 0;
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
				ajaxMap.put(new Integer(i * 100), charge);
				end.setTime(start.getTime());
			}
			ctx.setAjaxMap(ajaxMap);
		} else if (type.equals("week")) {
			Map<Object, Object> ajaxMap = new Hashtable<Object, Object>();
			for (int i = 1; i < 8; i++) {
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
						/*
						 * if (clicks == null) { clicks = new Integer(0); }
						 */
						clicks = new Integer(clicks.intValue() + 1);
						ajaxMap.put(key, clicks);
					}
					sessionIdFound.add(tracks[i].getSessionId());

				}
			}
			ctx.setAjaxMap(ajaxMap);
		} else if (type.equals("visits")) {
			String rangeStr = request.getParameter("range");
			int range = Calendar.WEEK_OF_MONTH;
			if (rangeStr == null || rangeStr.equals("WEEK")) {
				range = Calendar.WEEK_OF_MONTH;
			} else if (rangeStr.equals("MONTH")) {
				range = Calendar.MONTH;
			} else if (rangeStr.equals("YEAR")) {
				range = Calendar.YEAR;
			}
			ObjectBuilder ajaxMap = LangHelper.object();
			Calendar start = Calendar.getInstance();
			Calendar end = Calendar.getInstance();
			Calendar cal = Calendar.getInstance();

			start.add(range, -1);
			end.add(Calendar.MILLISECOND, -1);

			Map<String, Integer> clicksByHour = new HashMap<String, Integer>();
			SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy HH");
			cal.setTime(start.getTime());
			while (cal.before(end)) {
				clicksByHour.put(sdf.format(cal.getTime()), 0);
				cal.add(Calendar.HOUR_OF_DAY, 1);
			}

			Track[] tracks = tracker.getViewClickTracks(start.getTime(), end.getTime());
			List<String> sessionIdFound = new LinkedList<String>();

			for (int i = 1; i < tracks.length - 1; i++) {
				if (!sessionIdFound.contains(tracks[i].getSessionId())) {
					if (!NetHelper.isUserAgentRobot(tracks[i].getUserAgent())) {
						Date time = new Date(tracks[i].getTime());
						String key = sdf.format(time);
						Integer clicks = clicksByHour.get(key);
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
				datas.addList().add(hour.getKey() + ":00:00").add(hour.getValue());
			}

			ctx.setAjaxMap(ajaxMap.getMap());
		} else {
			return "bad type : " + type;
		}

		return null;
	}

	public static String performMainPage(RequestService rs, ContentContext ctx, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		ModulesContext dashboardContext = ModulesContext.getInstance(session, ctx.getGlobalContext());
		dashboardContext.getCurrentModule().restoreAll();
		return null;
	}

	public static String performTrackerPage(RequestService rs, ContentContext ctx, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		ModulesContext dashboardContext = ModulesContext.getInstance(session, ctx.getGlobalContext());
		dashboardContext.getCurrentModule().setRenderer(null);
		dashboardContext.getCurrentModule().clearAllBoxes();
		dashboardContext.getCurrentModule().addMainBox("tracker", "tracker", "/jsp/tracker.jsp", false);
		return null;

	}
	
	public static String performReportPage(RequestService rs, ContentContext ctx, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		ModulesContext dashboardContext = ModulesContext.getInstance(session, ctx.getGlobalContext());
		dashboardContext.getCurrentModule().clearAllBoxes();
		dashboardContext.getCurrentModule().setRenderer("/jsp/report.jsp");
		//dashboardContext.getCurrentModule().addMainBox("report", "report", "/jsp/report.jsp", false);
		return null;
	}
	
	public static String performListpage(RequestService rs, ContentContext ctx, HttpSession session, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		ModulesContext dashboardContext = ModulesContext.getInstance(session, ctx.getGlobalContext());
		dashboardContext.getCurrentModule().clearAllBoxes();
		dashboardContext.getCurrentModule().setRenderer("/jsp/pagelist.jsp");
		//dashboardContext.getCurrentModule().addMainBox("report", "report", "/jsp/report.jsp", false);
		return null;
	}
	
	public static String performGarbage (ContentContext ctx) throws Exception {
		if (!AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser())) {
			return "ERROR: you have not suffisant right to do this action.";
		} else {
			System.gc();
			return null;
		}
	}
	
	
}
