/*
 * Created on 11-juin-2004
 */
package org.javlo.tracking;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.ContentManager;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.StatContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.exception.ServiceException;
import org.javlo.servlet.ImageTransformServlet;
import org.javlo.user.IUserFactory;
import org.javlo.user.User;
import org.javlo.user.UserFactory;
import org.javlo.ztatic.StaticInfo;

/**
 * @author pvandermaesen the class for seralize the a track event and read the track event.
 */
public class Tracker {
	
	public static final String TRACKING_PARAM = "tracking";

	Logger logger = Logger.getLogger(Tracker.class.getName());

	PersistenceService persistenceService = null;

	//private TimeMap<String, Object> cache = new TimeMap<String, Object>(60 * 5); // 5 minutes cache

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
		//cache.clear();
	}

	public static void trace(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (!StringHelper.isTrue(request.getParameter(TRACKING_PARAM), true)) {
			return;
		}
		GlobalContext globalContext = GlobalContext.getInstance(request);
		Tracker tracker = Tracker.getTracker(globalContext, request.getSession());
		IUserFactory fact = UserFactory.createUserFactory(globalContext, request.getSession());
		User user = fact.getCurrentUser(request.getSession());
		String userName = null;
		if (user != null) {
			userName = user.getLogin();
		}
		ContentContext ctx = ContentContext.getContentContext(request, response);
		if (ctx.getRenderMode() == ContentContext.EDIT_MODE || ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
			EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
			if (editCtx.getUserPrincipal() != null) {
				userName = editCtx.getUserPrincipal().getName();
			}
		}
		RequestService requestService = RequestService.getInstance(request);
		String action = requestService.getParameter("webaction", null);

		Track track = new Track(userName, action, request.getRequestURI(), System.currentTimeMillis(), request.getHeader("Referer"), request.getHeader("User-Agent"));
		track.setIP(request.getRemoteAddr());
		track.setSessionId(request.getSession().getId());
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
	 * @return a array of language, key is language, value if a Interger ; click for the language
	 */
	public Map<String, Integer> getLanguage(StatContext statCtx) {

		Map<String, Integer> res = new HashMap<String, Integer>();

		Track[] tracks = getViewClickTracks(statCtx.getFrom(), statCtx.getTo());

		for (Track track : tracks) {
			String lg = getLanguage(track.getPath());
			Integer c = res.get(lg);
			if (c == null) {
				c = new Integer(0);
			}
			c = new Integer(c.intValue() + 1);
			res.put(lg, c);
		}
		return res;
	}

	String getLanguage(String path) {

		String[] pathDec = StringUtils.split(path, "/");
		if (pathDec.length < 2) {
			return "undifined";
		}

		String lg = "undefined";
		if (pathDec[1].length() == 2) {
			return pathDec[1];
		} else if (pathDec.length > 2 && pathDec[2].length() == 2) {
			return pathDec[2];
		}
		return lg;
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
	 * return click by a moment define by constant in Calendar object (sp. Calendar.DAY_OF_WEEK ).
	 * 
	 * @param statCtx
	 *            statistic context
	 * @param moment
	 *            a moment define by constant in Calendar object (sp. Calendar.DAY_OF_WEEK ).
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

		String key = "" + from.getTime() + " " + to.getTime();
		//String[][] content = (String[][]) cache.get(key);
		String[][] content = null;
		if (content != null) {
			return content;
		}
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

		//cache.put(key, res);

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
		 * for (int i = 0; i < allPages.length; i++) { if (allPages[i][0] != null && allPages[i][0].equals("view")) { if (StringHelper.getFileNameWithoutExtension(allPages[i][1]).endsWith(path)) { return Integer.parseInt(allPages[i][2]); } } }
		 */
		return c;
	}

	public int getPathCountAccess(int dayFromHere, String path) {
		Calendar to = TimeHelper.convertRemoveAfterMinutes(Calendar.getInstance());
		Calendar from = TimeHelper.convertRemoveAfterMinutes(Calendar.getInstance());
		from.roll(Calendar.DAY_OF_YEAR, dayFromHere * -1);
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
		//synchronized (cache) {
			String key = "resource_" + from.getTime() + " " + to.getTime();
			logger.finest("create Tracker info : " + key);
			Collection<Track> content = null;
			//Collection<Track> content = (Collection<Track>) cache.get(key);
			if (content != null) {
				return content;
			}
			collection = new LinkedList<Track>();
			Track[] tracks = getResourceTracks(from, to);
			for (Track track : tracks) {
				collection.add(track);
			}
			//cache.put(key, collection);
			//cache.clearCache();
		//}
		return collection;
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
		from.roll(Calendar.DAY_OF_YEAR, dayFromHere * -1);

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
		 * StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession()); path = URLHelper.createTransformURL(viewCtx, URLHelper.mergePath(staticConfig.getStaticFolder(), staticInfo.getResource()) , "thumb-view"); outAccess = outAccess + getCountAccessNoTemplate(from.getTime(), to.getTime(), path);
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
	 * return session open by a moment define by constant in Calendar object (sp. Calendar.DAY_OF_WEEK ).
	 * 
	 * @param statCtx
	 *            statistic context
	 * @param moment
	 *            a moment define by constant in Calendar object (sp. Calendar.DAY_OF_WEEK ).
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
	 * return the time pass on a page ( time between the click on the page and the next click in the same session )
	 * 
	 * @return a map with path as key and a array with total time in index 0 and click count in index 1
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

		String key = "getViewClickTracks_" + StringHelper.renderDate(from) + "_" + StringHelper.renderDate(to);		
		Track[] trackers = null;
		//synchronized (cache) {
			//trackers = (Track[]) cache.get(key);
			if (trackers == null) {
				trackers = persistenceService.loadTracks(from, to, true, false);
				//cache.put(key, trackers);
			}
		//}
		return trackers;
	}

	public Track[] getViewClickTracks(Date from, Date to, String path) {
		// TODO: make this method
		return getViewClickTracks(from, to);
	}

}