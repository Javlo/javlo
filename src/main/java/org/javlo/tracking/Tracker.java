/*
 * Created on 11-juin-2004
 */
package org.javlo.tracking;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.ContentManager;
import org.javlo.context.GlobalContext;
import org.javlo.context.StatContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.exception.ServiceException;
import org.javlo.servlet.ImageTransformServlet;
import org.javlo.template.Template;
import org.javlo.utils.TimeMap;
import org.javlo.ztatic.StaticInfo;

/**
 * @author pvandermaesen the class for seralize the a track event and read the
 *         track event.
 */
public class Tracker {
	
	private static final Set<String> NOT_LANG_SET = new HashSet<String>(Arrays.asList("js", "ph", "be", "lg"));

	public static final String TRACKING_PARAM = "tracking";

	Logger logger = Logger.getLogger(Tracker.class.getName());

	PersistenceService persistenceService = null;

	// private TimeMap<String, Object> cache = new TimeMap<String, Object>(60 *
	// 5); // 5 minutes cache

	private static final String TRACKER_KEY = "tracker_key";

	public static final Tracker getTracker(GlobalContext globalContext, HttpSession session) throws ServiceException {
		Tracker tracker = (Tracker) globalContext.getAttribute(TRACKER_KEY);
		if (tracker == null) {
			tracker = new Tracker();
			tracker.persistenceService = PersistenceService.getInstance(globalContext);
			globalContext.setAttribute(TRACKER_KEY, tracker);
		}
		return tracker;
	}

	public void clearCache() {
		// cache.clear();
	}

	public static void trace(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (!StringHelper.isTrue(request.getParameter(TRACKING_PARAM), true)) {
			return;
		}
		GlobalContext globalContext = GlobalContext.getInstance(request);
		Tracker tracker = Tracker.getTracker(globalContext, request.getSession());
		// IUserFactory fact = UserFactory.createUserFactory(globalContext,
		// request.getSession());
		// User user = fact.getCurrentUser(globalContext, request.getSession());
		String userName = "" + request.getUserPrincipal();
		/*
		 * if (user != null) { userName = user.getLogin(); } ContentContext ctx =
		 * ContentContext.getContentContext(request, response); if (ctx.getRenderMode()
		 * == ContentContext.EDIT_MODE || ctx.getRenderMode() ==
		 * ContentContext.PREVIEW_MODE) { EditContext editCtx =
		 * EditContext.getInstance(globalContext, request.getSession()); if
		 * (editCtx.getUserPrincipal() != null) { userName =
		 * editCtx.getUserPrincipal().getName(); } }
		 */
		// ContentContext ctx = ContentContext.getContentContext(request, response);
		RequestService requestService = RequestService.getInstance(request);
		String action = requestService.getParameter("webaction", null);

		Track track = new Track(userName, action, request.getRequestURI(), System.currentTimeMillis(), request.getHeader("Referer"), request.getHeader("User-Agent"), request.getHeader("Range"));
		track.setIP(ContentContext.getRealRemoteIp(request, StaticConfig.getInstance(request.getSession()).isAnonymisedTracking()));
		track.setSessionId(request.getSession().getId());
		track.setInputTrackingKey(ContentContext.getInputTrackingKey(request.getSession()));
		tracker.addTrack(track);
	}

	public void addTrack(Track track) throws Exception {
		persistenceService.store(track);
	}

	/**
	 * get all session for a statCtx.
	 * 
	 * @param statCtx
	 * @return number of session
	 */
	public int getClickCount(StatContext statCtx) {
		Track[] tracks = getViewClickTracks(statCtx.getFrom(), statCtx.getTo(), statCtx.getParentPath());
		return tracks.length;
	}

	public int getCountAccessNoTemplate(Date from, Date to, String path) {
		Track[] tracjs = getTracks(from, to, null);
		int c = 0;
		for (Track track : tracjs) {
			String trackPathNoTemplate = URLHelper.removeTemplateFromResourceURL(track.getPath());
			if (trackPathNoTemplate.endsWith(URLHelper.removeTemplateFromResourceURL(path))) {
				c++;
			}
		}
		return c;
	}

	/**
	 * return a count of access by language
	 * 
	 * @return a array of language, key is language, value if a Interger ; click for
	 *         the language
	 */
	public Map<String, Integer> getLanguage(StatContext statCtx) {
		Map<String, Integer> res = new HashMap<String, Integer>();
		Track[] tracks = getViewClickTracks(statCtx.getFrom(), statCtx.getTo());
		for (Track track : tracks) {
			String lg = getLanguage(track, tracks);
			Integer c = res.get(lg);
			if (c == null) {
				c = new Integer(0);
			}
			c = new Integer(c.intValue() + 1);
			res.put(lg, c);
		}
		return res;
	}

	public static String getLanguage(String path) {
		String[] pathDec = StringUtils.splitByWholeSeparator(path, "/");
		if (pathDec.length < 1) {
			return "?";
		}
		String lg = "?";
		if (pathDec[0].length() == 2) {
			return pathDec[0];
		} else if (pathDec.length > 1 && pathDec[1].length() == 2) {
			return pathDec[1];
		} else if (pathDec.length > 2 && pathDec[2].length() == 2) {
			return pathDec[2];
		}
		return lg;
	}
	
	public static String getLanguageDEBUG(String path) {
		String[] pathDec = StringUtils.splitByWholeSeparator(path, "/");
		System.out.println(">>>>>>>>> Tracker.getLanguageDEBUG : pathDec.length = "+pathDec.length); //TODO: remove debug trace
		if (pathDec.length < 1) {
			System.out.println(">>>>>>>>> Tracker.getLanguageDEBUG : < 1"); //TODO: remove debug trace
			return "?";
		}
		String lg = "?";
		System.out.println(">>>>>>>>> Tracker.getLanguageDEBUG : pathDec[0] = "+pathDec[0]); //TODO: remove debug trace
		if (pathDec[0].length() == 2 && !NOT_LANG_SET.contains(pathDec[0])) {
			return pathDec[0];
		} else if (pathDec.length > 1 && pathDec[1].length() == 2 && !NOT_LANG_SET.contains(pathDec[1])) {
			return pathDec[1];
		} else if (pathDec.length > 2 && pathDec[2].length() == 2 && !NOT_LANG_SET.contains(pathDec[2])) {
			return pathDec[2];
		}
		return lg;
	}

	public static String getLanguage(Track inTrack, Track[] tracks) {
		for (Track track : tracks) {			
			if (track.getSessionId().equals(inTrack.getSessionId())) {
				String lg = getLanguage(track.getPath());
				if (lg.length() == 2 && !lg.equals("wp") && !lg.equals("js") && !lg.equals("wp") && !StringHelper.isDigit(lg)) {
					return lg;
				}
			}
		}
		return "?";
	}

	/**
	 * return a count of access by mode 0 : view 1 : edit 2 : preview
	 * 
	 * @return a array of count.
	 */
	public int[] getModes(StatContext statCtx) {
		String[][] pagesTracking = getPagesTracking(statCtx);
		int[] res = { 0, 0, 0 };
		for (String[] element : pagesTracking) {
			int c = Integer.parseInt(element[2]);
			if (element[0].equals("view")) {
				res[0] += c;
			} else if (element[0].equals("edit")) {
				res[1] += c;
			} else if (element[0].equals("preview")) {
				res[2] += c;
			}
		}
		return res;
	}

	/* structured method for tracking event access */

	/**
	 * return click by day
	 * 
	 * @param statCtx
	 *            statistic context
	 * @return a map day (Calendar) is the key count of click is the value
	 * @throws DAOException
	 */
	public Map<Integer, Integer> getPagesByDay(StatContext statCtx) {
		return getPagesByMoment(statCtx, Calendar.DAY_OF_WEEK);
	}

	/**
	 * return click by day
	 * 
	 * @param statCtx
	 *            statistic context
	 * @return a map hours (Calendar) is the key count of click is the value
	 * @throws DAOException
	 */
	public Map<Integer, Integer> getPagesByHour(StatContext statCtx) {
		return getPagesByMoment(statCtx, Calendar.HOUR_OF_DAY);
	}

	/**
	 * return click by a moment define by constant in Calendar object (sp.
	 * Calendar.DAY_OF_WEEK ).
	 * 
	 * @param statCtx
	 *            statistic context
	 * @param moment
	 *            a moment define by constant in Calendar object (sp.
	 *            Calendar.DAY_OF_WEEK ).
	 * @return a map day (Calendar) is the key count of click is the value
	 * @throws DAOException
	 */
	private Map<Integer, Integer> getPagesByMoment(StatContext statCtx, int moment) {

		Track[] tracks = getViewClickTracks(statCtx.getFrom(), statCtx.getTo(), statCtx.getParentPath());
		Map<Integer, Integer> res = new HashMap<Integer, Integer>();
		GregorianCalendar cal = new GregorianCalendar();
		for (int i = 1; i < tracks.length - 1; i++) {
			cal.setTimeInMillis(tracks[i].getTime());
			Integer key = new Integer(cal.get(moment));
			Integer clicks = res.get(key);
			if (clicks == null) {
				clicks = new Integer(0);
			}
			clicks = new Integer(clicks.intValue() + 1);
			res.put(key, clicks);
		}
		return res;
	}

	/**
	 * count access per pages
	 * 
	 * @return a array. 0:view, edit, preview 1:path 2:count
	 * @throws DAOException
	 */
	private synchronized String[][] getPagesTracking(Date from, Date to, String parentPath) {
		Track[] tracks = getTracks(from, to);
		Map<String, Integer> pages = new TreeMap<String, Integer>();
		for (int i = 0; i < tracks.length; i++) {
			if (tracks[i].getAction() == null || !tracks[i].getAction().equals(ImageTransformServlet.VIEW_PICTURE_ACTION)) {
				if ((tracks[i].getPath().contains("/view/") && (tracks[i].getAction() == null || tracks[i].getAction().equals(Track.UNDEFINED_ACTION))) || (!tracks[i].getPath().contains("/view/"))) {
					Integer c = pages.get(tracks[i].getPath());
					if (c == null) {
						c = new Integer(0);
					}
					pages.put(tracks[i].getPath(), new Integer(c.intValue() + 1));
				}
			}
		}
		Collection<String[]> collection = new LinkedList<String[]>();
		for (String path : pages.keySet()) {
			boolean acceptedPath = true;
			if ((parentPath != null) && (!path.contains(parentPath))) {
				acceptedPath = false;
			}
			if (acceptedPath) {
				if (path.indexOf("jsession") < 0) {
					String[] line = new String[3];

					line[2] = "" + pages.get(path);

					if (path.indexOf("/edit") > -1) {
						path = path.replaceFirst(".*/edit", "");
						line[0] = "edit";
					} else if (path.indexOf("/view") > -1) {
						path = path.replaceFirst(".*/view", "");
						line[0] = "view";
					} else if (path.indexOf("/preview") > -1) {
						path = path.replaceFirst(".*/preview", "");
						line[0] = "preview";
					}
					if (path.length() == 0) {
						path = "/";
					}

					line[1] = path;
					collection.add(line);
				}
			}
		}
		String[][] res = new String[collection.size()][];
		collection.toArray(res);

		// cache.put(key, res);

		return res;
	}

	public String[][] getPagesTracking(StatContext statCtx) {
		return getPagesTracking(statCtx.getFrom(), statCtx.getTo(), statCtx.getParentPath());
	}

	public int getPathCountAccess(Date from, Date to, String path) {
		// String[][] allPages = getPagesTracking(from, to, null);
		Track[] tracks = getViewClickTracks(from, to);
		int c = 0;
		for (Track track : tracks) {
			if (track.getPath().endsWith(path)) {
				c++;
			}
		}
		/*
		 * for (int i = 0; i < allPages.length; i++) { if (allPages[i][0] != null &&
		 * allPages[i][0].equals("view")) { if
		 * (StringHelper.getFileNameWithoutExtension(allPages[i][1]).endsWith( path)) {
		 * return Integer.parseInt(allPages[i][2]); } } }
		 */
		return c;
	}

	public int getPathCountAccess(int dayFromHere, String path) {
		Calendar to = TimeHelper.convertRemoveAfterMinutes(Calendar.getInstance());
		Calendar from = TimeHelper.convertRemoveAfterMinutes(Calendar.getInstance());
		from.add(Calendar.DAY_OF_YEAR, dayFromHere * -1);
		return getPathCountAccess(from.getTime(), to.getTime(), path);
	}

	/**
	 * get the referer count by name
	 */
	public Map<String, Integer> getReferer(StatContext statCtx) {

		Track[] tracks = getViewClickTracks(statCtx.getFrom(), statCtx.getTo());
		Map<String, Integer> referers = new TreeMap<String, Integer>();
		for (Track track : tracks) {
			String referer = track.getRefered();
			if (referer.equals("null")) {
				referer = "";
			}
			referer = URLHelper.extractHost(referer);
			if (referer.trim().isEmpty()) {
				referer = "unknown";
			}
			Integer c = referers.get(referer);
			if (c == null) {
				c = new Integer(0);
			}
			referers.put(referer, new Integer(c.intValue() + 1));
		}
		return referers;
	}

	public int getResourcePathCountAccess(Date from, Date to, String path) {
		Collection<Track> tracks = getResourceTracking(from, to, null);
		int c = 0;
		path = URLHelper.removeTemplateFromResourceURL(path);
		for (Track track : tracks) {
			if (track.getPath().equals(path)) {
				c++;
			}
		}
		return c;
	}

	/**
	 * get list of track access to a resource.
	 * 
	 * @return a list of track.
	 */
	private synchronized Collection<Track> getResourceTracking(Date from, Date to, String parentPath) {
		Collection<Track> collection;
		// synchronized (cache) {
		String key = "resource_" + from.getTime() + " " + to.getTime();
		logger.finest("create Tracker info : " + key);
		collection = new LinkedList<Track>();
		Track[] tracks = getResourceTracks(from, to);
		for (Track track : tracks) {
			collection.add(track);
		}
		// cache.put(key, collection);
		// cache.clearCache();
		// }
		return collection;
	}

	/**
	 * get list of track access to a resource.
	 * 
	 * @return a list of track.
	 */
	public synchronized Track[] getAllTrack(Date day) {
		return persistenceService.getAllTrack(day);
	}

	public Track[] getResourceTracks(Date from, Date to) {
		return persistenceService.loadTracks(from, to, false, true);
	}

	public int getResourceCountAccess(ContentContext ctx, int dayFromHere, StaticInfo staticInfo) throws Exception {
		ContentContext viewCtx = new ContentContext(ctx);
		viewCtx.setRenderMode(ContentContext.VIEW_MODE);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		Calendar to = Calendar.getInstance();
		Calendar from = Calendar.getInstance();
		from.add(Calendar.DAY_OF_YEAR, dayFromHere * -1);

		to = TimeHelper.convertRemoveAfterHour(to);
		from = TimeHelper.convertRemoveAfterHour(from);

		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		String path = URLHelper.createResourceURL(ctx, URLHelper.mergePath(staticConfig.getStaticFolder(), staticInfo.getResource(ctx)));
		int outAccess = getResourcePathCountAccess(from.getTime(), to.getTime(), path);

		Collection<String> filters = globalContext.getImageViewFilter();
		for (String filter : filters) {
			path = URLHelper.createTransformURL(viewCtx, URLHelper.mergePath(staticConfig.getStaticFolder(), staticInfo.getResource(ctx)), filter);
			outAccess = outAccess + getResourcePathCountAccess(from.getTime(), to.getTime(), path);
		}
		/*
		 * StaticConfig staticConfig =
		 * StaticConfig.getInstance(ctx.getRequest().getSession()); path =
		 * URLHelper.createTransformURL(viewCtx,
		 * URLHelper.mergePath(staticConfig.getStaticFolder(), staticInfo.getResource())
		 * , "thumb-view"); outAccess = outAccess +
		 * getCountAccessNoTemplate(from.getTime(), to.getTime(), path);
		 */
		return outAccess;
	}

	/**
	 * get all session for a statCtx.
	 * 
	 * @param statCtx
	 * @return number of session
	 */
	public int getSession2ClickCount(StatContext statCtx) {
		Track[] tracks = getViewClickTracks(statCtx.getFrom(), statCtx.getTo(), statCtx.getParentPath());
		int res = 0;
		Set<String> sessionIdFound = new HashSet<String>();
		Set<String> sessionIdSecondClickFound = new HashSet<String>();
		for (int i = 1; i < tracks.length - 1; i++) {
			if (!sessionIdFound.contains(tracks[i].getSessionId())) {
				sessionIdFound.add(tracks[i].getSessionId());
			} else {
				if (!sessionIdSecondClickFound.contains(tracks[i].getSessionId())) {
					sessionIdSecondClickFound.add(tracks[i].getSessionId());
					res++;
				}
			}
		}
		return res;
	}

	/**
	 * return click by day
	 * 
	 * @param statCtx
	 *            statistic context
	 * @return a map day (Calendar) is the key count of click is the value
	 * @throws DAOException
	 */
	public Map<Integer, Integer> getSessionByDay(StatContext statCtx) {
		return getSessionByMoment(statCtx, Calendar.DAY_OF_WEEK);
	}

	/**
	 * return session by month
	 * 
	 * @param statCtx
	 *            statistic context
	 * @return a map month (Calendar) is the key count of click is the value
	 * @throws DAOException
	 */
	public Map<Integer, Integer> getSessionByMonth(StatContext statCtx) {
		return getSessionByMoment(statCtx, Calendar.MONTH);
	}

	/*
	 * public Map<Integer, Integer> _getSession2ClickByMonth(StatContext statCtx,
	 * GlobalContext globalContext) { return getSession2ClickByMoment(statCtx,
	 * Calendar.MONTH); }
	 */

	public Map<Integer, Integer[]> getSession2ClickByMonth(StatContext statCtx, GlobalContext globalContext) {
		Properties cache = null;
		if (globalContext != null) {
			try {
				cache = PersistenceService.getInstance(globalContext).getTrackCache();
			} catch (ServiceException e) {
				e.printStackTrace();
			}
		}
		final String CACHE_KEY_PREFIX = "sess2clk_";
		Map<Integer, Integer[]> outStat = new HashMap<Integer, Integer[]>();
		for (int i = 0; i < 12; i++) {
			outStat.put(i, new Integer[] { 0, 0 });
		}
		Calendar from = Calendar.getInstance();
		from.setTime(statCtx.getFrom());
		from = TimeHelper.convertRemoveAfterMonth(from);
		Calendar to = Calendar.getInstance();
		to.setTime(statCtx.getTo());
		to = TimeHelper.convertRemoveAfterMonth(to);
		Calendar now = Calendar.getInstance();
		while (from.before(to)) {
			Calendar localTo = Calendar.getInstance();
			localTo.setTime(from.getTime());
			localTo.add(Calendar.MONTH, 1);
			String key = CACHE_KEY_PREFIX + from.get(Calendar.YEAR) + '-' + from.get(Calendar.MONTH);
			String val0 = null;
			String val1 = null;
			if (cache != null) {
				val0 = cache.getProperty(key + "-0");
				val1 = cache.getProperty(key + "-1");
			}
			if (val0 != null || val1 != null) {
				outStat.put(from.get(Calendar.MONTH), new Integer[] { Integer.parseInt(StringHelper.neverNull(val0, "0")), Integer.parseInt(StringHelper.neverNull(val1, "0")) });
			} else {
				statCtx.setFrom(from.getTime());
				statCtx.setTo(localTo.getTime());
				Map<Integer, Integer[]> data = getSession2ClickByMonth(statCtx, globalContext);
				if (data.size() == 1) {
					Integer[] click = data.entrySet().iterator().next().getValue();
					if (cache != null && localTo.before(now)) {
						cache.setProperty(key + "-0", "" + click[0]);
						cache.setProperty(key + "-1", "" + click[1]);
					}
					outStat.put(from.get(Calendar.MONTH), click);
					try {
						PersistenceService.getInstance(globalContext).storeTrackCache();
					} catch (ServiceException e) {
						e.printStackTrace();
					}
				}
				if (data.size() > 1) {
					logger.warning("bad size returned : " + data.size());
				}
			}
			from = localTo;
		}
		return outStat;
	}

	/**
	 * return session open by a moment define by constant in Calendar object (sp.
	 * Calendar.DAY_OF_WEEK ).
	 * 
	 * @param statCtx
	 *            statistic context
	 * @param moment
	 *            a moment define by constant in Calendar object (sp.
	 *            Calendar.DAY_OF_WEEK ).
	 * @return a map day (Calendar) is the key count of click is the value
	 * @throws DAOException
	 */
	private Map<Integer, Integer> getSessionByMoment(StatContext statCtx, int moment) {
		Track[] tracks = getViewClickTracks(statCtx.getFrom(), statCtx.getTo(), statCtx.getParentPath());
		Map<Integer, Integer> res = new HashMap<Integer, Integer>();
		GregorianCalendar cal = new GregorianCalendar();
		Set<String> sessionIdFound = new HashSet<String>();
		for (int i = 1; i < tracks.length - 1; i++) {
			if (!sessionIdFound.contains(tracks[i].getSessionId())) {
				cal.setTimeInMillis(tracks[i].getTime());
				Integer key = new Integer(cal.get(moment));
				Integer clicks = res.get(key);
				if (clicks == null) {
					clicks = new Integer(0);
				}
				clicks = new Integer(clicks.intValue() + 1);
				res.put(key, clicks);
				sessionIdFound.add(tracks[i].getSessionId());
			}
		}
		return res;
	}

	private static boolean isView(String url) {
		if (url.contains("/preview")) {
			return false;
		} else if (url.contains("/edit")) {
			return false;
		} else if (url.contains("/" + Template.DEFAULT_TEMPLATE_NAME + "/")) {
			return false;
		} else if (url.contains("/img/")) {
			return false;
		} else if (url.contains("/transform/")) {
			return false;
		} else {
			return true;
		}
	}

	public Map<Integer, Integer[]> getSession2ClickByMonth(StatContext statCtx) throws IOException {
		Calendar from = TimeHelper.convertRemoveAfterDay(TimeHelper.getCalendar(statCtx.getFrom()));
		Calendar to = TimeHelper.convertRemoveAfterDay(TimeHelper.getCalendar(statCtx.getTo()));
		Map<Integer, Integer[]> data = new HashMap<Integer, Integer[]>();
		for (int i = 0; i < 12; i++) {
			data.put(i, new Integer[] { 0, 0 });
		}
		Map<String, Object> cache = new HashMap<String, Object>();
		while (from.getTimeInMillis() <= to.getTimeInMillis()) {
			DayInfo dayInfo = persistenceService.getTrackDayInfo(from, cache);
			if (dayInfo != null) {
				data.get(from.get(Calendar.MONTH))[0] += dayInfo.getSession2ClickCount() - dayInfo.getSession2ClickCountMobile();
				data.get(from.get(Calendar.MONTH))[1] += dayInfo.getSession2ClickCountMobile();
			}
			from.add(Calendar.DAY_OF_MONTH, 1);
		}
		return data;
	}

	public int getLastMountPathReading(String path) throws IOException {
		Calendar cal = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal2.add(Calendar.MONTH, -1);
		AtomicInteger pageTotalVisit = new AtomicInteger(0);
		StatContext statCtx = new StatContext(cal2.getTime(), cal.getTime());
		List<DayInfo> dayInfoList = getDayInfos(statCtx);

		for (DayInfo d : dayInfoList) {
			if (d.visitPath.get(path) != null) {
				pageTotalVisit.addAndGet(d.visitPath.get(path).intValue());
			}
		}

		// dayInfoList.parallelStream()
		// .filter(d -> d.visitPath.get(path) != null)
		// .forEach(d -> pageTotalVisit.addAndGet(d.visitPath.get(path).intValue()));
		return pageTotalVisit.get();
	}

	public int getLastYearPathReading(String path) throws IOException {
		Calendar cal = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal2.add(Calendar.YEAR, -1);
		AtomicInteger pageTotalVisit = new AtomicInteger(0);
		StatContext statCtx = new StatContext(cal2.getTime(), cal.getTime());
		List<DayInfo> dayInfoList = getDayInfos(statCtx);

		for (DayInfo d : dayInfoList) {
			if (d.visitPath.get(path) != null) {
				pageTotalVisit.addAndGet(d.visitPath.get(path).intValue());
			}
		}

		// dayInfoList.parallelStream()
		// .filter(d -> d.visitPath.get(path) != null)
		// .forEach(d -> pageTotalVisit.addAndGet(d.visitPath.get(path).intValue()));
		return pageTotalVisit.get();
	}

	public int getLastDayPathReading(String path) throws IOException {
		Calendar cal = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal2.add(Calendar.DAY_OF_YEAR, -1);
		AtomicInteger pageTotalVisit = new AtomicInteger(0);
		try {
			StatContext statCtx = new StatContext(cal2.getTime(), cal.getTime());
			List<DayInfo> dayInfoList = getDayInfos(statCtx);

			for (DayInfo d : dayInfoList) {
				if (d.visitPath.get(path) != null) {
					pageTotalVisit.addAndGet(d.visitPath.get(path).intValue());
				}
			}

			// dayInfoList.parallelStream()
			// .filter(d -> d.visitPath.get(path) != null)
			// .forEach(d -> pageTotalVisit.addAndGet(d.visitPath.get(path).intValue()));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return pageTotalVisit.get();
	}

	private static int testLastMountPageReading(String path) throws IOException {
		Calendar cal = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal2.add(Calendar.MONTH, -1);
		AtomicInteger pageTotalVisit = new AtomicInteger(0);
		StatContext statCtx = new StatContext(cal2.getTime(), cal.getTime());
		List<DayInfo> dayInfoList = getDayInfos(statCtx, null, "C:\\Users\\user\\data\\javlo\\data-ctx\\data-sexy\\persitence\\tracking");

		for (DayInfo d : dayInfoList) {
			if (d.visitPath.get(path) != null) {
				pageTotalVisit.addAndGet(d.visitPath.get(path).intValue());
			}
		}

		// dayInfoList.parallelStream()
		// .filter(d -> d.visitPath.get(path) != null)
		// .forEach(d -> pageTotalVisit.addAndGet(d.visitPath.get(path).intValue()));
		return pageTotalVisit.get();
	}

	public List<DayInfo> getDayInfos(StatContext statCtx) throws IOException {
		return getDayInfos(statCtx, persistenceService.dayInfoCache, persistenceService.getTrackingDirectory());
	}
	
	public static List<DayInfo> getDayInfos(StatContext statCtx, TimeMap<Long, DayInfo> dayInfoCache, String trackingDir) throws IOException {
		Calendar from = TimeHelper.convertRemoveAfterDay(TimeHelper.getCalendar(statCtx.getFrom()));
		Calendar to = TimeHelper.convertRemoveAfterDay(TimeHelper.getCalendar(statCtx.getTo()));
		List<DayInfo> data = new LinkedList<>();
		while (from.getTimeInMillis() <= to.getTimeInMillis()) {
			DayInfo dayInfo = PersistenceService.getTrackDayInfo(from, null, dayInfoCache, trackingDir);
			if (dayInfo != null) {
				data.add(dayInfo);
			}
			from.add(Calendar.DAY_OF_MONTH, 1);
		}
		return data;
	}

	/**
	 * return session open by a moment define by constant in Calendar object (sp.
	 * Calendar.DAY_OF_WEEK ). 2
	 * 
	 * @param statCtx
	 *            statistic context
	 * @param moment
	 *            a moment define by constant in Calendar object (sp.
	 *            Calendar.DAY_OF_WEEK ).
	 * @return a map day (Calendar) is the key count of click is the value
	 * @throws DAOException
	 */
	private Map<Integer, Integer[]> getSession2ClickByMoment(StatContext statCtx, int moment) {
		/*
		 * get all tracks because a "real" session is a session with get html AND
		 * ressources
		 */
		Track[] tracks = getClickTracks(statCtx.getFrom(), statCtx.getTo());
		Map<Integer, Integer[]> res = new HashMap<Integer, Integer[]>();
		GregorianCalendar cal = new GregorianCalendar();
		Set<String> sessionIdFound = new HashSet<String>();
		Set<String> secondSessionIdFound = new HashSet<String>();
		Set<String> viewSessionFound = new HashSet<String>();
		for (int i = 0; i < tracks.length - 1; i++) {
			Track track = tracks[i];
			if (isView(track.getPath())) {
				viewSessionFound.add(track.getSessionId());
			}
			if (!sessionIdFound.contains(track.getSessionId())) {
				sessionIdFound.add(track.getSessionId());
			} else if (!secondSessionIdFound.contains(track.getSessionId()) && viewSessionFound.contains(track.getSessionId())) {
				cal.setTimeInMillis(track.getTime());
				Integer key = new Integer(cal.get(moment));
				Integer[] clicks = res.get(key);
				if (clicks == null) {
					clicks = new Integer[] { 0, 0 };
				}
				if (!NetHelper.isMobile(track.getUserAgent())) {
					clicks[0] = new Integer(clicks[0].intValue() + 1);
				} else {
					clicks[1] = new Integer(clicks[1].intValue() + 1);
				}
				res.put(key, clicks);
				secondSessionIdFound.add(tracks[i].getSessionId());
			}
		}
		return res;
	}

	/**
	 * get all session for a statCtx.
	 * 
	 * @param statCtx
	 * @return number of session
	 */
	public int getSessionCount(StatContext statCtx) {
		Track[] tracks = getViewClickTracks(statCtx.getFrom(), statCtx.getTo(), statCtx.getParentPath());
		int res = 0;
		Set<String> sessionIdFound = new HashSet<String>();
		for (int i = 1; i < tracks.length - 1; i++) {
			if (!sessionIdFound.contains(tracks[i].getSessionId())) {
				res++;
				sessionIdFound.add(tracks[i].getSessionId());
			}
		}
		return res;
	}

	/**
	 * return the time pass on a page ( time between the click on the page and the
	 * next click in the same session )
	 * 
	 * @return a map with path as key and a array with total time in index 0 and
	 *         click count in index 1
	 * @throws DAOException
	 */
	public Map<String, double[]> getTimeTracking(StatContext statCtx) {
		Track[] tracks = getViewClickTracks(statCtx.getFrom(), statCtx.getTo());
		Arrays.sort(tracks);
		Map<String, double[]> res = new HashMap<String, double[]>();
		for (int i = 1; i < tracks.length - 1; i++) {
			if (tracks[i - 1].getSessionId().equals(tracks[i].getSessionId())) {
				String path = tracks[i - 1].getPath();
				String[] splitPath = ContentManager.splitPath(path);
				if (splitPath.length > 1) {
					if (ContentManager.isView(path)) {
						double[] clicks = res.get(path);
						if (clicks == null) {
							clicks = new double[] { 0, 0 };
							res.put(path, clicks);
						}
						clicks[0] += (tracks[i].getTime() - tracks[i - 1].getTime());
						clicks[1]++;
					}
				}
			}
		}
		return res;
	}

	public Track[] getTracks(Date from, Date to) {
		return persistenceService.loadTracks(from, to, false, false);
	}

	public Track[] getTracks(Date from, Date to, String path) {
		// TODO: make this method
		return getTracks(from, to);
	}

	public Track[] getViewClickTracks(Date from, Date to) {
		Track[] trackers = persistenceService.loadTracks(from, to, true, false);
		return trackers;
	}

	public Track[] getClickTracks(Date from, Date to) {
		Track[] trackers = persistenceService.loadTracks(from, to, false, false);
		return trackers;
	}

	public Track[] getViewClickTracks(Date from, Date to, String path) {
		// TODO: make this method
		return getViewClickTracks(from, to);
	}

	public static void main(String[] args) throws IOException {
		System.out.println(">>>>>>>>> Tracker.main : lang = " + getLanguage("/fr/qui-est-le-bdf/atingo.html")); // TODO: remove debug trace
	}

}