package org.javlo.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.io.FileUtils;
import org.apache.xml.utils.XMLChar;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentLayout;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.taxonomy.TaxonomyBean;
import org.javlo.data.taxonomy.TaxonomyService;
import org.javlo.helper.DebugHelper;
import org.javlo.helper.DebugHelper.StructureException;
import org.javlo.helper.LocalLogger;
import org.javlo.helper.NetHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.helper.URLHelper;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.exception.ServiceException;
import org.javlo.servlet.zip.ZipManagement;
import org.javlo.tracking.DayInfo;
import org.javlo.tracking.Track;
import org.javlo.utils.TimeTracker;
import org.javlo.xml.NodeXML;
import org.javlo.xml.XMLFactory;
import org.javlo.ztatic.StaticInfo;
import org.xml.sax.SAXParseException;

public class PersistenceService {

	private static final String CACHE_TRACK_FILE = "/cache_dm.properties";

	public static boolean STORE_DATA_PROPERTIES = false;

	public static final class MetaPersistenceBean {
		private int version;
		private String date;
		private String type;

		public MetaPersistenceBean(int version, String date, String type) {
			this.version = version;
			this.date = date;
			this.type = type;
		}

		public int getVersion() {
			return version;
		}

		public void setVersion(int version) {
			this.version = version;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

	}

	public static class LoadingBean {
		private int version;
		private String cmsVersion;
		private MenuElement root;
		private boolean error = false;

		public int getVersion() {
			return version;
		}

		public void setVersion(int version) {
			this.version = version;
		}

		public String getCmsVersion() {
			return cmsVersion;
		}

		public void setCmsVersion(String cmsVersion) {
			this.cmsVersion = cmsVersion;
		}

		public MenuElement getRoot() {
			return root;
		}

		public void setRoot(MenuElement root) {
			this.root = root;
		}

		public boolean isError() {
			return error;
		}

		public void setError(boolean error) {
			this.error = error;
		}
	}

	private static class BackupViewFileFilter implements FileFilter {

		public static final BackupViewFileFilter instance = new BackupViewFileFilter();

		@Override
		public boolean accept(File pathname) {
			boolean out = false;
			if (pathname.getName().startsWith("content_2") && !pathname.getName().equals("content_2.xml")) {
				out = true;
			}
			return out;
		}

	}

	private static class BackupPreviewFileFilter implements FileFilter {

		public static final BackupPreviewFileFilter instance = new BackupPreviewFileFilter();

		@Override
		public boolean accept(File pathname) {
			boolean out = false;
			if (pathname.getName().startsWith("content_3")) {
				out = true;
			}
			return out;
		}

	}

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(PersistenceService.class.getName());

	public static SimpleDateFormat PERSISTENCE_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private static String KEY = PersistenceService.class.getName();

	private static final String _DIRECTORY = "/persitence";

	private static final String stateFile = "persistence_state.properties";

	private static final String _TRACKING_DIRECTORY = "/persitence/tracking";

	public static final String GLOBAL_MAP_NAME = "global";

	public static final String STORE_FILE_PREFIX = "content_";

	public static int UNDO_DEPTH = 255;

	private PrintWriter trackWriter = null;

	private String trackWriterFileName = null;

	private Properties trackCache = null;

	private boolean loaded = false;

	public static final Date parseDate(String date) throws ParseException {
		if (date == null) {
			return null;
		}
		synchronized (PERSISTENCE_DATE_FORMAT) {
			try {
				return PERSISTENCE_DATE_FORMAT.parse(date);
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public static final String renderDate(Date date) {
		synchronized (PERSISTENCE_DATE_FORMAT) {
			return PERSISTENCE_DATE_FORMAT.format(date);
		}
	}

	public static final PersistenceService getInstance(GlobalContext globalContext) throws ServiceException {
		PersistenceService instance = null;
		if (globalContext != null) {
			instance = (PersistenceService) globalContext.getAttribute(getKey(globalContext));
		}
		if (instance == null) {
			instance = new PersistenceService();
			if (globalContext != null) {
				globalContext.setAttribute(getKey(globalContext), instance);
				instance.globalContext = globalContext;
				File dir = new File(instance.getDirectory());
				dir.mkdirs();
				dir = new File(instance.getTrackingDirectory());
				dir.mkdirs();
			}
		}
		return instance;
	}

	public static String getKey(GlobalContext globalContext) {
		return KEY + globalContext.getContextKey();
	}

	private static String trackToString(Track track) {
		StringBuffer outTrack = new StringBuffer();
		outTrack.append(track.getTime());
		outTrack.append(",");
		outTrack.append(track.getPath());
		outTrack.append(",");
		outTrack.append(track.getSessionId());
		outTrack.append(",");
		outTrack.append(track.getIP());
		outTrack.append(",");
		outTrack.append(track.getUserName());
		outTrack.append(",");
		outTrack.append(track.getAction());
		outTrack.append(",");
		outTrack.append(track.getRefered());
		outTrack.append(",");
		outTrack.append(track.getUserAgent());
		return outTrack.toString();
	}

	public Integer __version = -1;

	protected boolean canRedo = false;

	private GlobalContext globalContext = null;

	private boolean askStore;

	public boolean canRedo() {
		return versionExist(getVersion() + 1) && canRedo;
	}

	public boolean canUndo() {
		return versionExist(getVersion() - 1);
	}

	public void cleanFile() {
		/** clean backup view file **/
		Map<File, Date> backups = getBackupFiles();
		Collection<String> monthFound = new LinkedList<String>();
		Calendar cal = GregorianCalendar.getInstance();
		Date currentDate = new Date();
		for (Entry<File, Date> backup : backups.entrySet()) {
			Date timeSaving = backup.getValue();
			cal.setTime(currentDate);
			cal.roll(Calendar.MONTH, false);
			if (timeSaving.getTime() < cal.getTime().getTime()) { // 30 days
				// in
				// the
				// past
				cal.setTime(timeSaving);
				String key = cal.get(Calendar.WEEK_OF_YEAR) + "_" + cal.get(Calendar.YEAR);
				if (monthFound.contains(key)) { // if file date is 30 days
					// in the past >> keep 1
					// file per month
					backup.getKey().delete();
				} else {
					monthFound.add(key);
				}
			}
		}
		if (!canRedo()) {
			int workVersion = getVersion() + 1;
			File file = new File(getPersistenceFilePrefix(ContentContext.PREVIEW_MODE) + '_' + workVersion + ".xml");
			File propFile = new File(getPersistenceFilePrefix(ContentContext.PREVIEW_MODE) + '_' + workVersion + ".properties");
			while (file.exists()) {
				workVersion++;
				file.delete();
				if (propFile.exists()) {
					propFile.delete();
				}
				file = new File(getPersistenceFilePrefix(ContentContext.PREVIEW_MODE) + '_' + workVersion + ".xml");
				propFile = new File(getPersistenceFilePrefix(ContentContext.PREVIEW_MODE) + '_' + workVersion + ".properties");
			}
		}
		int workVersion = getVersion() - UNDO_DEPTH;
		File file = new File(getPersistenceFilePrefix(ContentContext.PREVIEW_MODE) + '_' + workVersion + ".xml");
		File propFile = new File(getPersistenceFilePrefix(ContentContext.PREVIEW_MODE) + '_' + workVersion + ".properties");
		if (file.exists()) {
			file.delete();
			workVersion = 0;
		}
		if (propFile.exists()) {
			propFile.delete();
		}
		/*
		 * while (workVersion > 0) { workVersion--; if (file.exists()) { file.delete();
		 * workVersion=0; } if (propFile.exists()) { propFile.delete(); } file = new
		 * File(getPersistenceFilePrefix(ContentContext.PREVIEW_MODE) + '_' +
		 * workVersion + ".xml"); propFile = new
		 * File(getPersistenceFilePrefix(ContentContext.PREVIEW_MODE) + '_' +
		 * workVersion + ".properties"); }
		 */
		LocalLogger.stepCount("store", "workVersion");
	}

	public void correctAllFiles() {
		// synchronized (MenuElement.LOCK_ACCESS) {
		File currentPreview = new File(getPersistenceFilePrefix(ContentContext.PREVIEW_MODE) + '_' + getVersion() + ".xml");
		File currentView = new File(getPersistenceFilePrefix(ContentContext.VIEW_MODE) + ".xml");

		correctFile(currentPreview);
		correctFile(currentView);
		// }

	}

	public List<MetaPersistenceBean> getPersistences() {
		List<MetaPersistenceBean> outList = new LinkedList<PersistenceService.MetaPersistenceBean>();

		/** search published element **/
		File[] backupView = new File(getBackupDirectory()).listFiles(BackupViewFileFilter.instance);
		if (backupView != null) {
			for (File zip : backupView) {
				String timeCode = zip.getName().replaceAll(STORE_FILE_PREFIX + ContentContext.VIEW_MODE + ".", "").replaceAll(".xml", "").replaceAll(".zip", "");
				try {
					Date publishTime = StringHelper.parseSecondFileTime(timeCode);
					outList.add(new MetaPersistenceBean(0, StringHelper.renderSortableTime(publishTime), "published"));
				} catch (ParseException e) {
					logger.warning(e.getMessage());
				}
			}
		}

		/** search preview elements **/
		File[] backupPreview = new File(getDirectory()).listFiles(BackupPreviewFileFilter.instance);
		if (backupPreview != null) {
			for (File file : backupPreview) {
				if (!file.getName().endsWith(".error")) {
					String version = file.getName().replaceAll(STORE_FILE_PREFIX + ContentContext.PREVIEW_MODE + ".", "").replaceAll(".xml", "").replaceAll(".zip", "");
					int versionInteger = -1;
					try {
						versionInteger = Integer.parseInt(version);
						outList.add(new MetaPersistenceBean(versionInteger, StringHelper.renderSortableTime(new Date(file.lastModified())), "preview"));
					} catch (NumberFormatException e) {
						logger.warning("bad file name format : " + file.getName());
					}
				}
			}
		}

		return outList;
	}

	private void correctFile(File file) {

		logger.info("file correction on : " + file);

		int read;
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			StringBuffer outFile = new StringBuffer();
			read = in.read();
			boolean error = false;
			while (read >= 0) {
				if (read == 0) {
					error = true;
				} else {
					outFile.append((char) read);
				}
				Character character = new Character((char) read);
				Charset charset = Charset.forName(ContentContext.CHARACTER_ENCODING);
				ByteBuffer buf = ByteBuffer.allocate(4);
				buf.put(("" + character).getBytes());
				charset.decode(buf);
				read = in.read();
			}
			if (error) {
				logger.info("error found in : " + file + " try to write a correct version.");
				FileUtils.writeStringToFile(file, outFile.toString());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			ResourceHelper.closeResource(in);
		}

	}

	private String getBackupDirectory() {
		return URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getBackupFolder());
	}

	private Map<File, Date> getBackupFiles() {
		Map<File, Date> out = new HashMap<File, Date>();
		File[] backupView = new File(getBackupDirectory()).listFiles(BackupViewFileFilter.instance);
		if (backupView != null) {
			for (File zip : backupView) {
				String timeCode = zip.getName().replaceAll("content_" + ContentContext.VIEW_MODE + ".", "").replaceAll(".xml", "").replaceAll(".zip", "");
				try {
					Date publishTime = StringHelper.parseSecondFileTime(timeCode);
					out.put(zip, publishTime);
				} catch (ParseException e) {
					logger.warning(e.getMessage());
				}
			}
		}
		return out;
	}

	public List<Date> getBackupDates() {
		Map<File, Date> backups = getBackupFiles();
		List<Date> out = new LinkedList<Date>(backups.values());
		Collections.sort(out, Collections.reverseOrder());
		return out;
	}

	public String getDirectory() {
		return URLHelper.mergePath(globalContext.getDataFolder(), _DIRECTORY);
	}

	public String getTrackingDirectory() {
		// /*********** DEBUG *////////////
		// return "C:/Users/user/data/javlo/data-ctx/data-sexy/persitence/tracking";

		return URLHelper.mergePath(globalContext.getDataFolder(), _TRACKING_DIRECTORY);
	}

	private Reader getTrackReader(Calendar cal) throws IOException {
		int year = cal.get(Calendar.YEAR);
		int mount = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		File dir = new File(getTrackingDirectory() + '/' + year + '/' + mount);
		if (!dir.exists()) {
			return null;
		}
		File file = new File(getTrackingDirectory() + '/' + year + '/' + mount + "/tracks-" + day + ".csv");
		if (!file.exists()) {
			return null;
		}
		logger.fine("create track reader : " + file);
		Reader outReader = new FileReader(file);
		return outReader;
	}

	/**
	 * get list of track access to a resource.
	 * 
	 * @return a list of track.
	 */
	public synchronized Track[] getAllTrack(Date day) {
		Calendar from = Calendar.getInstance();
		from.setTime(day);
		from = TimeHelper.convertRemoveAfterDay(from);
		Calendar to = Calendar.getInstance();
		to.setTime(day);
		to.add(Calendar.DAY_OF_YEAR, 1);
		to = TimeHelper.convertRemoveAfterDay(to);
		Track[] tracks = loadTracks(from.getTime(), to.getTime(), true, false);
		return tracks;
	}

	public DayInfo getTrackDayInfo(Calendar cal, Map<String, Object> cache) throws IOException {
		int year = cal.get(Calendar.YEAR);
		int mount = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		File csvFile = new File(getTrackingDirectory() + '/' + year + '/' + mount + "/tracks-" + day + ".csv");
		File propFile = new File(getTrackingDirectory() + '/' + year + '/' + mount + "/tracks-" + day + ".properties");
		if (!csvFile.exists() && !propFile.exists()) {
			return null;
		}
		if (csvFile.exists() && (!propFile.exists() || csvFile.lastModified() > propFile.lastModified())) {
			DayInfo dayInfo = new DayInfo();
			for (Track track : getAllTrack(cal.getTime())) {
				if (!track.getPath().contains(".php")) {
					dayInfo.pagesCount++;
					boolean mobile = NetHelper.isMobile(track.getUserAgent());
					if (mobile) {
						dayInfo.pagesCountMobile++;
					}
					if (cache.get("session-" + track.getSessionId()) == null) {
						cache.put("session-" + track.getSessionId(), track.getPath());
						dayInfo.sessionCount++;
						if (mobile) {
							dayInfo.sessionCountMobile++;
						}
					} else if (cache.get("session2Click-" + track.getSessionId()) == null && !track.getPath().equals(cache.get("session-" + track.getSessionId()))) {
						cache.put("session2Click-" + track.getSessionId(), 1);
						dayInfo.session2ClickCount++;
						if (mobile) {
							dayInfo.session2ClickCountMobile++;
						}
					}
				}
			}
			logger.info("store dayInfo for : " + StringHelper.renderDate(cal.getTime()));
			dayInfo.store(propFile);
			return dayInfo;
		} else

		{
			return new DayInfo(propFile);
		}

	}

	public synchronized Properties getTrackCache() {
		if (trackCache == null) {
			trackCache = new Properties();
			File file = new File(getTrackingDirectory() + CACHE_TRACK_FILE);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			FileInputStream in = null;
			try {
				in = new FileInputStream(file);
				trackCache.load(in);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				ResourceHelper.closeResource(in);
			}
		}
		return trackCache;
	}

	public synchronized void clearTrackCache() {
		File file = new File(getTrackingDirectory() + CACHE_TRACK_FILE);
		if (!file.exists()) {
			file.delete();

		}
	}

	public synchronized void storeTrackCache() {
		if (trackCache == null) {
			return;
		}
		File file = new File(getTrackingDirectory() + CACHE_TRACK_FILE);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			trackCache.store(out, "");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			ResourceHelper.closeResource(out);
		}
	}

	private PrintWriter getTrackWriter(Calendar cal) throws IOException {
		int year = cal.get(Calendar.YEAR);
		int mount = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		File dir = new File(getTrackingDirectory() + '/' + year + '/' + mount);
		File file = new File(getTrackingDirectory() + '/' + year + '/' + mount + "/tracks-" + day + ".csv");

		if (file.getName().equals(trackWriterFileName) && trackWriter != null) {
			return trackWriter;
		} else {
			ResourceHelper.closeResource(trackWriter);
		}

		if (!dir.exists()) {
			dir.mkdirs();
		}

		if (!file.exists()) {
			file.createNewFile();
		}
		trackWriterFileName = file.getName();
		FileWriter fw = new FileWriter(file, true);
		BufferedWriter bw = new BufferedWriter(fw);
		trackWriter = new PrintWriter(bw);
		return trackWriter;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		ResourceHelper.closeResource(trackWriter);
	}

	public int getVersion() {
		synchronized (__version) {
			if (__version == -1) {
				File propFile = new File(getDirectory() + '/' + stateFile);
				if (propFile.exists()) {
					try {
						Properties prop = new Properties();
						InputStream in = new FileInputStream(propFile);
						prop.load(in);
						in.close();
						__version = Integer.parseInt(prop.getProperty("version", "1"));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else { // set default value
					__version = 1;
				}
			}
			return __version;
		}
	}

	public void resetVersion() {
		__version = -1;
	}

	public void insertContent(NodeXML pageXML, MenuElement elem, String defaultLg, boolean releaseID) throws StructureException {
		NodeXML contentNode = pageXML.getChild("component");
		List<ComponentBean> contentList = new LinkedList<ComponentBean>();
		while (contentNode != null) {
			String id = contentNode.getAttributeValue("id");
			if (releaseID) {
				id = StringHelper.getRandomId();
			}
			DebugHelper.checkStructure(id == null, "no id defined in a component.");
			String type = contentNode.getAttributeValue("type");
			DebugHelper.checkStructure(type == null, "no type defined in a component.");
			String inlist = contentNode.getAttributeValue("inlist", "false");
			String lg = contentNode.getAttributeValue("language", defaultLg);
			String renderer = contentNode.getAttributeValue("renderer", null);

			boolean isNolink = false;
			String strAutolink = contentNode.getAttributeValue("nolink", null);
			if (strAutolink != null) {
				isNolink = StringHelper.isTrue(strAutolink);
			}
			String authors = contentNode.getAttributeValue("authors", null);
			Set<Integer> hiddenModes = null;
			String hiddenModesStr = contentNode.getAttributeValue("hiddenModes", null);
			if (hiddenModesStr != null) {
				hiddenModes = new HashSet<Integer>();
				for (String modeStr : StringHelper.stringToCollection(hiddenModesStr, ",")) {
					hiddenModes.add(Integer.parseInt(modeStr.trim()));
				}
			}

			String style = contentNode.getAttributeValue("style");

			String content = contentNode.getContent();
			if (content == null) {
				content = "";
			}

			ComponentBean bean = new ComponentBean(type, content, lg);
			bean.setId(id);
			bean.setRepeat(StringHelper.isTrue(contentNode.getAttributeValue("repeat"), false));
			bean.setForceCachable(StringHelper.isTrue(contentNode.getAttributeValue("forceCachable"), false));
			bean.setNolink(isNolink);
			bean.setStyle(style);
			bean.setList(StringHelper.isTrue(inlist));
			bean.setHidden(StringHelper.isTrue(contentNode.getAttributeValue("hidden")));
			bean.setArea(contentNode.getAttributeValue("area", ComponentBean.DEFAULT_AREA));
			if (contentNode.getAttributeValue("colSize", null) != null) {
				bean.setColumnSize(Integer.parseInt(contentNode.getAttributeValue("colSize", null)));
			}
			bean.setBackgroundColor(contentNode.getAttributeValue("bgcol", null));
			bean.setManualCssClass(contentNode.getAttributeValue("css", null));
			bean.setTextColor(contentNode.getAttributeValue("txtcol", null));
			if (contentNode.getAttributeValue("displayCookiesStatus", null) != null) {
				bean.setCookiesDisplayStatus(Integer.parseInt(contentNode.getAttributeValue("displayCookiesStatus", null)));
			}
			String layout = contentNode.getAttributeValue("layout", null);
			if (layout != null) {
				bean.setLayout(new ComponentLayout(layout));
			}
			bean.setRenderer(renderer);
			bean.setHiddenModes(hiddenModes);
			bean.setAuthors(authors);
			try {
				bean.setModificationDate(parseDate(contentNode.getAttributeValue("modificationDate", "01/01/1970 00:00:00")));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			try {
				bean.setCreationDate(parseDate(contentNode.getAttributeValue("creationDate", "01/01/1970 00:00:00")));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			try {
				bean.setDeleteDate(StringHelper.parseTime(contentNode.getAttributeValue("delDate", null)));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			contentList.add(bean);
			contentNode = contentNode.getNext("component");
		}

		ComponentBean[] elemContent = new ComponentBean[contentList.size()];
		contentList.toArray(elemContent);
		elem.setContent(elemContent);
	}

	public MenuElement insertPage(ContentContext ctx, NodeXML pageXML, MenuElement parent, Map<MenuElement, String[]> vparentPreparation, String defaultLg, boolean checkName) throws StructureException, IOException {
		GlobalContext globalContext = ctx.getGlobalContext();
		MenuElement page = MenuElement.getInstance(ctx);

		String id = pageXML.getAttributeValue("id");
		while (parent.getRoot().searchChildFromId(id) != null) {
			id = StringHelper.getRandomId();
		}
		DebugHelper.checkStructure(id == null, "no id defined in a page node.");
		String name = pageXML.getAttributeValue("name");
		String finalPageName = null;
		try {
			finalPageName = URLDecoder.decode(name);
		} catch (Exception e1) {
			finalPageName = "error_name_" + StringHelper.getRandomId();
			name = finalPageName;
			logger.warning("error : " + e1.getMessage());
			logger.warning("parent : " + parent.getName());
			logger.warning("new name generated : " + finalPageName);
			e1.printStackTrace();
		}

		if (checkName) {
			int i = 1;
			while (parent.getRoot().searchChildFromName(finalPageName) != null && i < 10000) {
				finalPageName = name + "_" + i;
				i++;
			}
			if (i == 10000) {
				logger.severe("problem on loading page check page : " + name + "  (context:" + globalContext.getContextKey() + ")");
			}
		}

		name = finalPageName;
		DebugHelper.checkStructure(name == null, "no path defined in a page node.");
		String priority = pageXML.getAttributeValue("priority", "10");
		DebugHelper.checkStructure(priority == null, "no priority defined in a page node.");
		String visible = pageXML.getAttributeValue("visible", "false");
		String https = pageXML.getAttributeValue("https", "false");
		DebugHelper.checkStructure(visible == null, "no visible defined in a page node.");
		String roles = pageXML.getAttributeValue("userRoles", "");
		String layout = pageXML.getAttributeValue("layout", null);
		String freeData = pageXML.getAttributeValue("savedParent", null);

		// String followers = pageXML.getAttributeValue("followers", null);
		// if (followers != null) {
		// for (String follower : StringHelper.stringToCollection(followers,
		// "#")) {
		// page.addFollowers(follower);
		// }
		// }

		/* modification management */
		String creator = pageXML.getAttributeValue("creator", "");
		String latestEditor = pageXML.getAttributeValue("latestEditor", "");
		String[] editorRoles = StringHelper.stringToArray(pageXML.getAttributeValue("editor-roles", ""), "#");
		if (editorRoles != null) {
			for (String group : editorRoles) {
				if (group.trim().length() > 0) {
					page.addEditorRole(group);
				}
			}
		}

		Date creationDate = new Date();
		String creationDateStr = pageXML.getAttributeValue("creationDate");
		if (creationDateStr != null) {
			try {
				creationDate = parseDate(creationDateStr);
			} catch (ParseException e) {
				throw new StructureException(e.getMessage());
			}
		}

		Date modificationDate = new Date();
		String modificationDateStr = pageXML.getAttributeValue("modificationDate");
		if (modificationDateStr != null) {
			try {
				modificationDate = parseDate(modificationDateStr);
			} catch (ParseException e) {
				throw new StructureException(e.getMessage());
			}
		}

		Date validationDate = new Date();
		String validationDateStr = pageXML.getAttributeValue("validationDate");
		if (validationDateStr != null) {
			try {
				validationDate = parseDate(validationDateStr);
			} catch (ParseException e) {
				// no validation date
				// throw new StructureException(e.getMessage());
			}
		}

		page.setId(id);
		page.setParent(parent);
		page.setName(name);
		page.setPriority(Integer.parseInt(priority));
		page.setVisible(StringHelper.isTrue(visible));
		page.setActive(StringHelper.isTrue(pageXML.getAttributeValue("active", null), true));
		page.setHttps(StringHelper.isTrue(https));

		page.setTemplateId(layout);
		page.setModel(StringHelper.isTrue(pageXML.getAttributeValue("model", null), false));
		page.setSavedParent(freeData);

		page.setUserRoles(new HashSet<String>(StringHelper.stringToCollection(roles, ";")));

		page.setValid(StringHelper.isTrue(pageXML.getAttributeValue("valid", "true")));
		page.setNoValidation(StringHelper.isTrue(pageXML.getAttributeValue("noval", "false")));
		page.setNeedValidation(StringHelper.isTrue(pageXML.getAttributeValue("ndval", "false")));
		page.setValidater(pageXML.getAttributeValue("validater", ""));
		page.setValidationDate(validationDate);
		page.setBlocked(StringHelper.isTrue(pageXML.getAttributeValue("blocked", "false")));
		page.setBlocker(pageXML.getAttributeValue("blocker", ""));
		page.setSeoWeight(StringHelper.parseInt(pageXML.getAttributeValue("seoWeight", null), MenuElement.SEO_HEIGHT_INHERITED));

		page.setChildrenAssociation(StringHelper.isTrue(pageXML.getAttributeValue("childrenAssociation", null)));

		page.setSharedName(pageXML.getAttributeValue("sharedName", null));

		page.setIpSecurityErrorPageName(pageXML.getAttributeValue("ipsecpagename"));

		if (pageXML.getAttributeValue("taxonomy") != null) {
			page.setTaxonomy(new HashSet<String>(StringHelper.stringToCollection(pageXML.getAttributeValue("taxonomy"))));
		}

		String type = pageXML.getAttributeValue("type", null);
		if (type != null) {
			page.setType(type);
		}

		page.setCreationDate(creationDate);
		page.setCreator(creator);
		page.setModificationDate(modificationDate);
		page.setLatestEditor(latestEditor);

		try {
			page.setStartPublishDate(StringHelper.parseSortableTime(pageXML.getAttributeValue("start-publish")));
			page.setEndPublishDate(StringHelper.parseSortableTime(pageXML.getAttributeValue("end-publish")));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		page.setShortURL(pageXML.getAttributeValue("shorturl", null));

		page.setBreakRepeat(StringHelper.isTrue(pageXML.getAttributeValue("breakrepeat", "false")));

		String[] virtualParent = StringHelper.stringToArray(pageXML.getAttributeValue("vparent", ""));
		vparentPreparation.put(page, virtualParent);

		String reversedLink = pageXML.getAttributeValue("reversed-link", "");
		reversedLink = StringHelper.writeLines(StringHelper.stringToArray(reversedLink, "#"));
		page.setReversedLink(reversedLink);

		page.setLinkedURL(pageXML.getAttributeValue("linked-url"));

		insertContent(pageXML, page, defaultLg, false);

		parent.addChildMenuElement(page);

		NodeXML childPage = pageXML.getChild("page");
		while (childPage != null) {
			insertPage(ctx, childPage, page, vparentPreparation, defaultLg, checkName);
			childPage = childPage.getNext("page");
		}

		return page;
	}

	/**
	 * load data from InputStream of Reader
	 * 
	 * @param ctx
	 * @param in
	 *            can Reader of InputStream
	 * @param contentAttributeMap
	 * @param renderMode
	 * @return
	 * @throws ServiceException
	 * @throws InterruptedException
	 */
	protected LoadingBean load(ContentContext ctx, Object in, File propFile, Map<String, String> contentAttributeMap, TaxonomyBean taxonomyBean, int renderMode) throws ServiceException, InterruptedException {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		MenuElement root = MenuElement.getInstance(ctx);

		root.setValid(true);

		LoadingBean outBean = new LoadingBean();

		try {
			NodeXML firstNode;
			if (in instanceof InputStream) {
				firstNode = XMLFactory.getFirstNode((InputStream) in);
			} else {
				firstNode = XMLFactory.getFirstNode((Reader) in);
			}

			NodeXML page = firstNode.getChild("page");

			outBean.setCmsVersion(firstNode.getAttributeValue("cmsversion"));
			outBean.setVersion(Integer.parseInt(firstNode.getAttributeValue("version", "-1")));

			if (page != null) {

				String defaultLg = globalContext.getDefaultLanguages().iterator().next();
				if (!globalContext.getLanguages().contains(defaultLg)) {
					defaultLg = null;
				}

				root.setId(page.getAttributeValue("id"));
				root.setName(page.getAttributeValue("name"));
				root.setPriority(Integer.parseInt(page.getAttributeValue("priority")));
				root.setVisible(StringHelper.isTrue(page.getAttributeValue("visible", "false")));
				root.setActive(StringHelper.isTrue(page.getAttributeValue("active", "true")));
				root.setIpSecurityErrorPageName(page.getAttributeValue("ipsecpagename"));
				root.setSeoWeight(StringHelper.parseInt(page.getAttributeValue("seoWeight", null), MenuElement.SEO_HEIGHT_INHERITED));
				root.setHttps(StringHelper.isTrue(page.getAttributeValue("https", "false")));
				root.setCreator(page.getAttributeValue("creator", ""));

				try {
					root.setCreationDate(StringHelper.parseDate(page.getAttributeValue("creationDate", StringHelper.renderTime(new Date()))));
					root.setModificationDate(StringHelper.parseDate(page.getAttributeValue("modificationDate", StringHelper.renderTime(new Date()))));
				} catch (ParseException e) {
					e.printStackTrace();
				}

				root.setLatestEditor(page.getAttributeValue("latestEditor", ""));
				root.setValid(StringHelper.isTrue(page.getAttributeValue("valid", "false")));
				root.setValidater(page.getAttributeValue("validater", ""));
				root.setBlocked(StringHelper.isTrue(page.getAttributeValue("blocked", "false")));
				root.setBlocker(page.getAttributeValue("blocker", ""));
				root.setLinkedURL(page.getAttributeValue("linked-url", ""));
				root.setBreakRepeat(StringHelper.isTrue(page.getAttributeValue("breakrepeat", "false")));
				root.setShortURL(page.getAttributeValue("shorturl", null));
				root.setChildrenAssociation(StringHelper.isTrue(page.getAttributeValue("childrenAssociation", null)));
				root.setTaxonomy(StringHelper.stringToSet(page.getAttributeValue("taxonomy")));

				String[] editorRoles = StringHelper.stringToArray(page.getAttributeValue("editor-roles", ""), "#");
				if (editorRoles != null) {
					for (String roles : editorRoles) {
						if (roles.trim().length() > 0) {
							root.addEditorRole(roles);
						}
					}
				}

				String reversedLink = page.getAttributeValue("reversed-link", "");
				reversedLink = StringHelper.writeLines(StringHelper.stringToArray(reversedLink, "#"));
				root.setReversedLink(reversedLink);

				root.setTemplateId(page.getAttributeValue("layout"));
				root.setSavedParent(page.getAttributeValue("savedParent"));

				String creationDate = page.getAttributeValue("creationDate");
				if (creationDate == null) {
					root.setCreationDate(new Date());
				} else {
					root.setCreationDate(parseDate(creationDate));
				}

				String modificationDate = page.getAttributeValue("modificationDate");
				if (modificationDate == null) {
					root.setModificationDate(new Date());
				} else {
					root.setModificationDate(parseDate(modificationDate));
				}

				String validationDate = page.getAttributeValue("validationDate", "");
				if (validationDate.trim().length() == 0) {
					root.setValidationDate(null);
				} else {
					root.setValidationDate(parseDate(validationDate));
				}

				insertContent(page, root, defaultLg, false);
				page = page.getChild("page");

				Map<MenuElement, String[]> vparentPreparation = new HashMap<MenuElement, String[]>();

				while (page != null) {
					insertPage(ctx, page, root, vparentPreparation, defaultLg, false);
					page = page.getNext("page");
				}

				Collection<MenuElement> elems = vparentPreparation.keySet();
				for (MenuElement menuElement : elems) {
					String[] parentId = vparentPreparation.get(menuElement);
					for (String element : parentId) {
						menuElement.addVirtualParent(element);
					}
				}

			} else {
				root.setId("0");
				root.setName("root");
				root.setPriority(10);
				root.setVisible(true);
			}
			if (propFile != null) {
				if (!propFile.exists()) {
					NodeXML properties = firstNode.getChild("properties");
					if (properties != null && properties.getAttributeValue("name", "").equals("global")) {
						NodeXML property = properties.getChild("property");
						while (property != null) {
							contentAttributeMap.put(property.getAttributeValue("key"), property.getContent());
							property = property.getNext("property");
						}
					}
				} else {
					logger.info("load data : " + propFile);
					Reader reader = new InputStreamReader(new FileInputStream(propFile), ContentContext.CHARACTER_ENCODING);
					Properties prop = new Properties();
					try {
						prop.load(reader);
					} finally {
						ResourceHelper.closeResource(reader);
					}
					for (Map.Entry<Object, Object> entry : prop.entrySet()) {
						/*** remove static as prefix for static info */
						String key = entry.getKey().toString();
						if (key.startsWith("staticinfo-/static")) {
							key = key.replaceFirst("staticinfo-/static", "staticinfo-");
						}
						contentAttributeMap.put(key, entry.getValue().toString());
					}
				}
			}
			if (taxonomyBean != null) {
				taxonomyBean.getChildren().clear();
				taxonomyBean.getLabels().clear();
				loadTaxonomy(firstNode.getChild("taxo"), taxonomyBean);
				TaxonomyService.getInstance(ctx, renderMode).clearCache();
			}
		} catch (SAXParseException e) {
			// e.printStackTrace();
			MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage("error XML parsing (line:" + e.getLineNumber() + " col:" + e.getColumnNumber() + "): " + e.getMessage(), GenericMessage.ERROR, ""));
			root.setId("0");
			root.setName("root");
			root.setPriority(10);
			root.setVisible(true);
			outBean.setError(true);
		} catch (Exception e) {
			e.printStackTrace();
			MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage("error XML loading : " + e.getMessage(), GenericMessage.ERROR, ""));
			root.setId("0");
			root.setName("root");
			root.setPriority(10);
			root.setVisible(true);
			outBean.setError(true);
		}

		outBean.setRoot(root);
		loaded = true;

		return outBean;

	}

	private void loadTaxonomy(NodeXML node, TaxonomyBean taxonomyBean) {
		if (node != null) {
			taxonomyBean.setId(node.getAttributeValue("id"));
			String name = node.getAttributeValue("name");
			taxonomyBean.setName(name);
			taxonomyBean.setDecoration(node.getAttributeValue("deco", ""));
			for (NodeXML child : node.getChildren()) {
				if (child.getName().equals("label")) {
					taxonomyBean.updateLabel(child.getAttributeValue("lang"), child.getContent());
				} else if (child.getName().equals("taxo")) {
					TaxonomyBean bean = new TaxonomyBean();
					taxonomyBean.addChildAsLast(bean);
					loadTaxonomy(child, bean);
				}
			}
		}
	}

	public MenuElement load(ContentContext ctx, int renderMode, Map<String, String> contentAttributeMap, Date timeTravelDate) throws Exception {
		return load(ctx, renderMode, contentAttributeMap, timeTravelDate, true, null);
	}

	/**
	 * check if a preview version exist on FileSystem.
	 * 
	 * @param version
	 * @return
	 */
	public boolean isPreviewVersion(int version) {
		File file = new File(getDirectory() + "/content_" + ContentContext.PREVIEW_MODE + '_' + version + ".xml");
		return file.exists();
	}

	/**
	 * load a specific preview version.
	 * 
	 * @param ctx
	 * @param version
	 * @return
	 * @throws Exception
	 */
	public MenuElement loadPreview(ContentContext ctx, Integer version) throws Exception {
		return load(ctx, ContentContext.PREVIEW_MODE, null, null, true, version);
	}

	private int checkComponentIntegrity(ContentContext ctx, HashSet<String> componentsId, PrintStream out, ComponentBean[] comps) {
		int error = 0;
		for (ComponentBean comp : comps) {
			if (componentsId.contains(comp.getId())) {
				out.println("2 comp with same id found (type:" + comp.getType() + " id:" + comp.getId() + " context:" + ctx.getGlobalContext().getContextKey());
				error++;
			} else {
				componentsId.add(comp.getId());
			}
		}
		return error;
	}

	public int checkStructureIntegrity(ContentContext ctx, PrintStream out, MenuElement root) {
		int error = 0;
		if (ctx.getGlobalContext().getStaticConfig().isCheckContentIntegrity()) {
			out.println("");
			out.println("Test content structure : " + ctx.getGlobalContext().getContextKey() + " (mode:" + ctx.getRenderMode() + ')');
			out.println("--");
			HashSet<String> componentsId = new HashSet<String>();
			HashSet<String> pageId = new HashSet<String>();
			HashSet<String> pageName = new HashSet<String>();
			try {
				for (MenuElement page : root.getAllChildrenList()) {
					int pageContentError = checkComponentIntegrity(ctx, componentsId, out, page.getContent());
					error = error + pageContentError;
					if (pageContentError > 0) {
						out.println("   Error in content found : " + page.getPath() + " (id=" + page.getId() + " #error:" + pageContentError + ")");
					}
					if (pageId.contains(page.getId())) {
						error++;
						out.println("2 pages with same id found (path:" + page.getPath() + " id:" + page.getId() + " [content:" + pageContentError + "]");
					}
					if (pageName.contains(page.getName())) {
						error++;
						out.println("2 pages with same name found (path:" + page.getPath() + " name:" + page.getName() + " [content:" + pageContentError + "]");
					}
					pageId.add(page.getId());
					pageName.add(page.getName());
				}
			} catch (Exception e) {
				out.println("EXCEPTION : " + e.getMessage());
				e.printStackTrace();
			}
			if (error == 0) {
				out.println("no error found.");
			}
			out.println("--");
			out.println("");
		}
		return error;
	}

	public String getPersistenceFilePrefix(int mode) {
		return URLHelper.mergePath(getDirectory(), STORE_FILE_PREFIX + mode);
	}

	protected MenuElement load(ContentContext ctx, int renderMode, Map<String, String> contentAttributeMap, Date timeTravelDate, boolean correctXML, Integer previewVersion) throws Exception {
		int timeTrackerNumber = TimeTracker.start(ctx.getGlobalContext().getContextKey(), "load");
		if (contentAttributeMap == null) {
			contentAttributeMap = new HashMap(); // fake map
		}
		synchronized (ctx.getGlobalContext().getLockLoadContent()) {

			int version = getVersion();
			if (previewVersion != null) {
				version = previewVersion;
			}

			logger.info("load version : " + version + " in mode : " + renderMode + " context:" + ctx.getGlobalContext().getContextKey());

			MenuElement root;
			InputStream in = null;
			ZipInputStream zip = null;
			try {

				if (timeTravelDate != null) {
					// An other render mode than VIEW_MODE is not supported
					// with
					// a timeTravelDate.
					Map<File, Date> backups = getBackupFiles();
					long minDiff = Long.MIN_VALUE;
					Entry<File, Date> minBackup = null;
					for (Entry<File, Date> backup : backups.entrySet()) {
						long diff = backup.getValue().getTime() - timeTravelDate.getTime();
						if (diff <= 0 && diff > minDiff) {
							minDiff = diff;
							minBackup = backup;
						}
					}
					if (minBackup != null) {
						zip = new ZipInputStream(new FileInputStream(minBackup.getKey()));
						ZipEntry entry = zip.getNextEntry();
						while (entry != null) {
							if (ResourceHelper.getFile(entry.getName()).equals(STORE_FILE_PREFIX + ContentContext.VIEW_MODE + ".xml")) {
								in = zip;
								break;
							}
							entry = zip.getNextEntry();
						}
					}
				}
				File xmlFile = null;
				File propFile = null;
				if (in == null) {
					if (renderMode == ContentContext.PREVIEW_MODE) {
						xmlFile = new File(getPersistenceFilePrefix(renderMode) + '_' + version + ".xml");
						propFile = new File(getPersistenceFilePrefix(renderMode) + '_' + version + ".properties");
					} else {
						xmlFile = new File(getPersistenceFilePrefix(renderMode) + ".xml");
						propFile = new File(getPersistenceFilePrefix(renderMode) + ".properties");
					}
					if (xmlFile.exists()) {
						in = new FileInputStream(xmlFile);
					}
				}
				if (in == null) {
					root = MenuElement.getInstance(ctx);
					root.setName("root");
					root.setVisible(true);
					root.setPriority(1);
					root.setId("0");
					/*
					 * file.createNewFile(); BufferedWriter out = new BufferedWriter(new
					 * FileWriter(file)); out.write( "<content version=\"" + version +
					 * "\"><page id=\"0\" name=\"root\" priority=\"1\" visible=\"true\" userRoles=\"\" /></content>"
					 * ); out.close();
					 */
				} else {
					LoadingBean loadBean = load(ctx, in, propFile, contentAttributeMap, TaxonomyService.getInstance(ctx, renderMode).getRoot(), renderMode);
					logger.info("load : " + xmlFile);
					if (loadBean.isError() && correctXML && xmlFile != null) {
						correctCharacterEncoding(xmlFile);
						in.close();
						in = new FileInputStream(xmlFile);
						loadBean = load(ctx, in, propFile, contentAttributeMap, TaxonomyService.getInstance(ctx, renderMode).getRoot(), renderMode);
					}
					root = loadBean.getRoot();
					try {
						ConvertToCurrentVersion.convert(ctx, loadBean);
					} catch (Exception e) {
						e.printStackTrace();
						if (correctXML) {
							correctCharacterEncoding(xmlFile);
						}
						return load(ctx, renderMode, contentAttributeMap, timeTravelDate, false, null);
					}

					/** load linked content **/
					/*
					 * MenuElement[] children = root.getAllChilds(); root.updateLinkedData(ctx); for
					 * (MenuElement page : children) { page.updateLinkedData(ctx); }
					 */
				}

				checkStructureIntegrity(ctx, System.out, root);

			} finally {
				ResourceHelper.closeResource(in);
				ResourceHelper.closeResource(zip);
				TimeTracker.end(ctx.getGlobalContext().getContextKey(), "load", timeTrackerNumber);
			}

			return root;
		}
	}

	private synchronized static void correctCharacterEncoding(File file) throws FileNotFoundException, IOException {
		if (file != null) {
			logger.info("try to correct xml structure : " + file.getAbsolutePath());
			String content = ResourceHelper.loadStringFromFile(file);
			ResourceHelper.copyFile(file, new File(file.getAbsolutePath() + ".error"), true);
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), ContentContext.CHARACTER_ENCODING));
				for (char c : content.toCharArray()) {
					if (XMLChar.isValid(c)) {
						writer.write(c);
					} else {
						logger.warning("bad char found : " + c);
						writer.write('?');
					}
				}
			} finally {
				ResourceHelper.closeResource(writer);
			}
		}
	}

	/**
	 * load current version of preview content.
	 * 
	 * @return the current version
	 * @throws IOException
	 */
	/*
	 * public synchronized int loadVersion() throws IOException { File propFile =
	 * new File(getDirectory() + '/' + stateFile); if (propFile.exists()) {
	 * Properties prop = new Properties(); InputStream in = new
	 * FileInputStream(propFile); prop.load(in); in.close(); version =
	 * Integer.parseInt(prop.getProperty("version", "1")); } else { // set default
	 * value version = 1; } return version; }
	 */

	public Track[] loadTracks(Date from, Date to, boolean onlyViewClick, boolean onlyResource) {

		Collection<Track> outCol = new ArrayList<Track>();

		Calendar tmpCal = GregorianCalendar.getInstance();

		Calendar minimum = GregorianCalendar.getInstance();
		minimum.set(2008, 1, 1);
		Calendar maximum = GregorianCalendar.getInstance();
		maximum.setTime(new Date());

		Calendar calFrom = GregorianCalendar.getInstance();
		calFrom.setTime(from);

		Calendar calTo = GregorianCalendar.getInstance();
		calTo.setTime(to);

		if (calFrom.before(minimum)) {
			calFrom = minimum;
		}
		if (maximum.before(calTo)) {
			calTo = maximum;
		}

		int countTrack = 0;

		logger.fine("load track from:" + StringHelper.renderTime(from) + " to:" + StringHelper.renderTime(to) + " only view:" + onlyViewClick + " only resource:" + onlyResource);

		tmpCal.setTime(calFrom.getTime());

		int countBcl = 0;
		int countLine = 0;

		while (countBcl < 1000 && calFrom.before(calTo) || ((calFrom.get(Calendar.DAY_OF_MONTH) == calTo.get(Calendar.DAY_OF_MONTH)))) {
			countBcl++;
			try {
				Reader reader = getTrackReader(calFrom);
				if (reader != null) {
					BufferedReader bufReader = new BufferedReader(reader);
					String line = bufReader.readLine();
					Track track;
					while (line != null) {
						countLine++;
						try {
							String[] trackInfo = line.split(",");
							String userAgent = null;
							if (trackInfo.length > 7) {
								userAgent = line.substring(line.indexOf(trackInfo[7]));
							}
							if (trackInfo.length > 5) {
								if (!NetHelper.isRobot(userAgent)) {
									track = new Track();
									try {
										track.setTime(Long.parseLong(trackInfo[0]));
									} catch (NumberFormatException e) {
										track.setTime(0);
										logger.warning(e.getMessage());
									}
									track.setPath(trackInfo[1]);
									track.setSessionId(trackInfo[2]);
									track.setIP(trackInfo[3]);
									track.setUserName(trackInfo[4]);
									track.setAction(trackInfo[5]);
									if (trackInfo.length > 6) {
										track.setRefered(trackInfo[6]);
									}
									if (userAgent != null) {
										track.setUserAgent(userAgent);
									}

									if (onlyResource) {
										if (!track.getPath().startsWith("/view/") && !track.getPath().startsWith("/preview/") && !track.getPath().startsWith("/edit/") && !track.getPath().startsWith("/ajax/")) {
											track.setPath(URLHelper.removeTemplateFromResourceURL(track.getPath()));
											Calendar trackCal = Calendar.getInstance();
											trackCal.setTimeInMillis(track.getTime());
											if (calFrom.before(trackCal) && calTo.after(trackCal)) {
												outCol.add(track);
												countTrack++;
											}
										}
									} else if (!onlyViewClick) {
										Calendar trackCal = Calendar.getInstance();
										trackCal.setTimeInMillis(track.getTime());
										if (calFrom.before(trackCal) && calTo.after(trackCal)) {
											outCol.add(track);
											countTrack++;
										}
									} else {
										String ext = StringHelper.getFileExtension(track.getPath());
										if (StringHelper.isEmpty(ext) || ext.equalsIgnoreCase("htm")) {
											ext = "html"; // default rendering
										}
										if (track.getPath() != null && ext.equals("html") && !track.getPath().contains("/edit") && !track.getPath().contains("/preview") && !track.getPath().contains("/time") && !track.getPath().contains("/ajax")) {
											track.setPath(StringHelper.getFileNameWithoutExtension(track.getPath()));
											Calendar trackCal = Calendar.getInstance();
											trackCal.setTimeInMillis(track.getTime());
											if (calFrom.before(trackCal) && calTo.after(trackCal)) {
												outCol.add(track);
												countTrack++;
											}
										}
									}
								}
							}
						} catch (RuntimeException e) { // if fatal error in
							// structure -> continue
							logger.warning(e.getMessage());
						}
						line = bufReader.readLine();
					}
					releaseTrackReader(reader);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			tmpCal.add(Calendar.DAY_OF_YEAR, 1);
			tmpCal = TimeHelper.convertRemoveAfterDay(tmpCal);
			if (tmpCal.before(calFrom)) {
				tmpCal.add(Calendar.YEAR, 1);
			}
			calFrom.setTime(tmpCal.getTime());
		}

		if (countBcl > 999) {
			logger.severe("track bcl to big");
			System.out.println("*****************************************");
			System.out.println("*** TRACK BCL TO BIG");
			System.out.println("*****************************************");
		}

		logger.fine("track loaded : " + countTrack + " on " + countLine + " lines.");

		Track[] tracks = new Track[outCol.size()];
		outCol.toArray(tracks);
		return tracks;
	}

	public void publishPreviewFile(ContentContext ctx) throws IOException, ParseException {
		storeCurrentView(ctx);
		File previewFile = new File(getDirectory() + "/content_" + ContentContext.PREVIEW_MODE + '_' + getVersion() + ".xml");
		File file = new File(getDirectory() + "/content_" + ContentContext.VIEW_MODE + ".xml");
		if (file.exists()) {
			file.delete();
		}
		ResourceHelper.copyFile(previewFile, file, true);
	}

	public void redo() {
		if (canRedo()) {
			setVersion(getVersion() + 1);
			saveVersion();
		}
	}

	/**
	 * set preview version for next loading.
	 * 
	 * @param version
	 *            a content version
	 * @return true if version has changed and false if this version doens'nt exist.
	 */
	public boolean setVersion(int version) {
		if (versionExist(version)) {
			this.__version = version;
			saveVersion();
			return true;
		} else {
			return false;
		}
	}

	private void releaseTrackReader(Reader reader) throws IOException {
		reader.close();
	}

	private void releaseTrackWriter(Writer writer) throws IOException {
		writer.close();
	}

	protected void saveVersion() {
		synchronized (__version) {
			try {
				File propFile = new File(getDirectory() + '/' + stateFile);
				if (!propFile.exists()) {
					propFile.createNewFile();
				}
				Properties prop = new Properties();
				prop.setProperty("version", "" + __version);
				OutputStream out = new FileOutputStream(propFile);
				prop.store(out, "WCMS PERSISTENCE STATE");
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ask storage of data at the end of request
	 * 
	 * @param askStore
	 */
	public synchronized void setAskStore(boolean askStore) {
		this.askStore = askStore;
	}

	public synchronized boolean isAskStore() {
		return askStore;
	}

	public void store(ContentContext ctx) throws Exception {
		store(ctx, true);
	}

	public void store(ContentContext ctx, boolean async) throws Exception {
		store(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), ContentContext.PREVIEW_MODE, async);
	}

	public void store(ContentContext ctx, int renderMode) throws Exception {
		store(ctx, renderMode, true);
	}

	private AtomicInteger COUNT_STORAGE_THREAD = new AtomicInteger(0);

	public void store(ContentContext ctx, int renderMode, boolean async) throws Exception {
		setAskStore(false);
		COUNT_STORAGE_THREAD.incrementAndGet();
		try {
			logger.info("waiting storage #Thread = " + COUNT_STORAGE_THREAD.get() + " context:" + ctx.getGlobalContext().getContextKey());
			if (COUNT_STORAGE_THREAD.get() <= 2) {
				synchronized (ctx.getGlobalContext().getLockLoadContent()) {
					logger.info("store in " + renderMode + " mode.");
					PersistenceThread persThread = new PersistenceThread(globalContext.getLockLoadContent());
					ContentService content = ContentService.getInstance(globalContext);
					MenuElement menuElement = content.getNavigation(ctx);
					String defaultLg = globalContext.getDefaultLanguages().iterator().next();
					if (!globalContext.getLanguages().contains(defaultLg)) {
						defaultLg = null;
					}
					persThread.setMenuElement(menuElement);
					persThread.setMode(renderMode);
					persThread.setPersistenceService(this);
					persThread.setDefaultLg(defaultLg);
					persThread.setGlobalContentMap(content.getGlobalMap(ctx));
					persThread.setTaxonomyRoot(TaxonomyService.getInstance(ctx).getRoot());
					GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
					persThread.setContextKey(globalContext.getContextKey());
					persThread.setDataFolder(globalContext.getDataFolder());
					StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext());
					if (StaticInfo._STATIC_INFO_DIR != null) {
						String staticInfo = URLHelper.mergePath(globalContext.getDataFolder(), StaticInfo._STATIC_INFO_DIR);
						persThread.addFolderToSave(new File(staticInfo));
					}
					persThread.addFolderToSave(new File(URLHelper.mergePath(globalContext.getDataFolder(), staticConfig.getUserInfoFile())));
					persThread.start(async);
				}
			} else {
				logger.warning("at least 2 threads waiting storage -> skip storage.");
			}
		} finally {
			COUNT_STORAGE_THREAD.decrementAndGet();
		}
	}

	// public void store(InputStream in) throws Exception {
	// // synchronized (MenuElement.LOCK_ACCESS) {
	// setVersion(getVersion() + 1);
	// saveVersion();
	// File file = new File(getDirectory() + "/content_" +
	// ContentContext.PREVIEW_MODE + '_' + getVersion() + ".xml");
	// if (!file.exists()) {
	// file.createNewFile();
	// }
	// FileOutputStream out = new FileOutputStream(file);
	// int read = in.read();
	// while (read >= 0) {
	// out.write(read);
	// read = in.read();
	// }
	// out.close();
	// // }
	// }

	public void store(Track track) throws Exception {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeInMillis(track.getTime());
		PrintWriter writer = getTrackWriter(cal);
		writer.println(trackToString(track));
	}

	public void flush() {
		Calendar cal = GregorianCalendar.getInstance();
		try {
			getTrackWriter(cal).flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void storeCurrentView(ContentContext ctx) throws IOException, ParseException {
		File file = new File(getDirectory() + "/content_" + ContentContext.VIEW_MODE + ".xml");

		if (file.exists()) {

			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

			Date date = globalContext.getPublishDate();
			if (date != null) {
				File zipFile = new File(getBackupDirectory() + "/content_" + ContentContext.VIEW_MODE + "." + StringHelper.renderSecondFileTime(date) + ".xml.zip");

				zipFile.getParentFile().mkdirs();

				StaticConfig staticConfig = globalContext.getStaticConfig();

				OutputStream out = null;
				try {
					out = new FileOutputStream(zipFile);
					ZipOutputStream outZip = new ZipOutputStream(out);

					Set<String> includes = new HashSet<String>();
					includes.addAll(staticConfig.getBackupIncludePatterns());

					Set<String> excludes = new HashSet<String>();
					excludes.addAll(staticConfig.getBackupExcludePatterns());

					ZipManagement.zipDirectory(outZip, null, globalContext.getDataFolder(), ctx.getRequest(), excludes, includes);

					outZip.close();
				} finally {
					if (out != null) {
						out.close();
					}
				}

				file.delete();
			}
		}
	}

	public void storeView(InputStream in) throws Exception {
		// synchronized (MenuElement.LOCK_ACCESS) {
		File file = new File(getDirectory() + "/content_" + ContentContext.VIEW_MODE + ".xml");
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream out = new FileOutputStream(file);
		int read = in.read();
		while (read >= 0) {
			out.write(read);
			read = in.read();
		}
		out.close();
		// }
	}

	public void undo() {
		canRedo = true;
		if (canUndo()) {
			setVersion(getVersion() - 1);
			File file = new File(getDirectory() + "/content_" + ContentContext.PREVIEW_MODE + ".xml");
			if (file.exists()) {
				file.delete();
			}
		}
	}

	/*
	 * public static void main(String[] args) { File file = new File(
	 * "C:/Apache/Tomcat 6.0/webapps/dc/WEB-INF/data-ctx/ctx-121395557182868827380/persitence/content_3_2.xml"
	 * );
	 * 
	 * int read; try { InputStream in = new FileInputStream(file); StringBuffer
	 * outFile = new StringBuffer(); read = in.read(); int pos = 0; boolean error =
	 * false; while (read >= 0) { if (read == 0) { error = true; } else {
	 * outFile.append((char) read); } Character character = new Character((char)
	 * read); Charset charset = Charset.forName(ContentContext.CHARACTER_ENCODING);
	 * ByteBuffer buf = ByteBuffer.allocate(4); buf.put(("" +
	 * character).getBytes()); charset.decode(buf); pos++; read = in.read(); } if
	 * (error) { FileUtils.writeStringToFile(file, outFile.toString()); }
	 * in.close(); } catch (IOException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } System.out.println("end."); }
	 */

	private boolean versionExist(int version) {
		File file = new File(getDirectory() + "/content_" + ContentContext.PREVIEW_MODE + '_' + version + ".xml");
		return file.exists();
	}

	public void sendPersistenceErrorToAdministrator(String message, File file, Throwable e) throws AddressException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("JAVLO PERSISTENCE ERROR : " + globalContext.getContextKey());
		out.println("");
		out.println(message);
		out.println("");
		out.println("Server time : " + StringHelper.renderTime(new Date()));
		out.println("");
		out.println("Stack Trace :");
		e.printStackTrace(out);
		out.close();
		String content = new String(outStream.toByteArray());
		if (globalContext.getStaticConfig().getErrorMailReport() != null) {
			NetHelper.sendMailToAdministrator(globalContext, new InternetAddress(globalContext.getStaticConfig().getErrorMailReport()), "Javlo persistence Error on : " + globalContext.getContextKey(), content);
		} else {
			NetHelper.sendMailToAdministrator(globalContext, "Javlo persistence Error on : " + globalContext.getContextKey(), content);
		}
	}

	public boolean clean(ContentContext ctx) {
		boolean fileDeleted = false;
		for (File file : new File(getDirectory()).listFiles()) {
			if (file.getName().startsWith("content_" + ContentContext.PREVIEW_MODE)) {
				if (!file.getName().contains("" + getVersion())) {
					file.delete();
					fileDeleted = true;
				}
			}
		}
		return fileDeleted;
	}

	public static void main(String[] args) throws Exception {
		PersistenceService persistenceService = PersistenceService.getInstance(null);
		Calendar cal = Calendar.getInstance();
		Track[] tracks = persistenceService.getAllTrack(new Date());
		System.out.println("#tracks = " + tracks.length);
		for (Track track : tracks) {
			System.out.println("user agent = " + track.getUserAgent());
		}
	}

	public boolean isLoaded() {
		return loaded;
	}
}
