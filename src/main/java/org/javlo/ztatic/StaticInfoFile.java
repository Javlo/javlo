package org.javlo.ztatic;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.utils.TimeMap;

public class StaticInfoFile {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(StaticInfo.class.getName());

	protected Map<String, Object> cache = new TimeMap<String, Object>(60 * (int) Math.round(((Math.random() + 1) * 60))); // cache between 1u and 2u, all cache can not be updated at the same time

	public static class StaticSort implements Comparator<String> {

		ContentContext ctx;
		String baseFolder;
		boolean ascending = true;

		public StaticSort(ContentContext inCtx, String inBaseFolder, boolean inAscending) {
			ctx = inCtx;
			baseFolder = StringHelper.neverEmpty(inBaseFolder, "");
			ascending = inAscending;
		}

		@Override
		public int compare(String uri1, String uri2) {
			try {
				String fullURI1 = URLHelper.mergePath(baseFolder, uri1);
				String fullURI2 = URLHelper.mergePath(baseFolder, uri2);

				StaticInfo uri1Info = StaticInfo.getInstance(ctx, fullURI1);
				StaticInfo uri2Info = StaticInfo.getInstance(ctx, fullURI2);

				int changeOrder = 1;
				if (!ascending) {
					changeOrder = -1;
				}

				if (uri1Info.getDate(ctx) == null && uri2Info.getDate(ctx) == null) {
					return uri1.compareTo(uri2) * changeOrder;
				} else if (uri1Info.getDate(ctx) == null) {
					return -1 * changeOrder;
				} else if (uri2Info.getDate(ctx) == null) {
					return 1 * changeOrder;
				} else {
					return uri1Info.getDate(ctx).compareTo(uri2Info.getDate(ctx)) * changeOrder;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;
		}

	}

	public static class StaticFileSort implements Comparator<File> {

		ContentContext ctx;
		boolean ascending = true;

		public StaticFileSort(ContentContext inCtx, boolean inAscending) {
			ctx = inCtx;
			ascending = inAscending;
		}

		@Override
		public int compare(File file1, File file2) {
			try {
				StaticInfo uri1Info = StaticInfo.getInstance(ctx, file1);
				StaticInfo uri2Info = StaticInfo.getInstance(ctx, file2);

				int changeOrder = 1;
				if (!ascending) {
					changeOrder = -1;
				}

				if (uri1Info.getDate(ctx) == null && uri2Info.getDate(ctx) == null) {
					return file1.compareTo(file2) * changeOrder;
				} else if (uri1Info.getDate(ctx) == null) {
					return -1 * changeOrder;
				} else if (uri2Info.getDate(ctx) == null) {
					return 1 * changeOrder;
				} else {
					return uri1Info.getDate(ctx).compareTo(uri2Info.getDate(ctx)) * changeOrder;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;
		}

	}

	public static class StaticFileSortByAccess implements Comparator<File> {

		ContentContext ctx;
		boolean ascending = true;

		private final Map<File, Integer> fileAccess = new HashMap<File, Integer>();

		public StaticFileSortByAccess(ContentContext inCtx, boolean inAscending) {
			ctx = inCtx;
			ascending = inAscending;
		}

		@Override
		public int compare(File file1, File file2) {
			try {
				int changeOrder = 1;
				if (!ascending) {
					changeOrder = -1;
				}

				Integer access1 = fileAccess.get(file1);
				if (access1 != null) {
					StaticInfo uri1Info = StaticInfo.getInstance(ctx, file1);
					access1 = uri1Info.getAccessFromSomeDays(ctx);
					fileAccess.put(file1, access1);
				}

				Integer access2 = fileAccess.get(file2);
				if (access2 != null) {
					StaticInfo uri2Info = StaticInfo.getInstance(ctx, file2);
					access2 = uri2Info.getAccessFromSomeDays(ctx);
					fileAccess.put(file2, access2);
				}

				if (access1.intValue() == access2.intValue()) {
					return changeOrder;
				} else {
					return (access1 - access2) * changeOrder;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;
		}

	}

	public static class StaticFileSortByPopularity implements Comparator<File> {

		ContentContext ctx;
		boolean ascending = true;

		public StaticFileSortByPopularity(ContentContext inCtx, boolean inAscending) {
			ctx = inCtx;
			ascending = inAscending;
		}

		@Override
		public int compare(File file1, File file2) {
			try {
				StaticInfo uri1Info = StaticInfo.getInstance(ctx, file1);
				StaticInfo uri2Info = StaticInfo.getInstance(ctx, file2);

				int changeOrder = 1;
				if (!ascending) {
					changeOrder = -1;
				}

				if (uri1Info.getAccessFromSomeDays(ctx) == uri2Info.getAccessFromSomeDays(ctx)) {
					if (uri1Info.getDate(ctx) == null && uri2Info.getDate(ctx) == null) {
						return file1.compareTo(file2) * changeOrder;
					} else if (uri1Info.getDate(ctx) == null) {
						return -1 * changeOrder;
					} else if (uri2Info.getDate(ctx) == null) {
						return 1 * changeOrder;
					} else {
						return uri1Info.getDate(ctx).compareTo(uri2Info.getDate(ctx)) * changeOrder;
					}
				} else {
					return (uri1Info.getAccessFromSomeDays(ctx) - uri2Info.getAccessFromSomeDays(ctx)) * changeOrder;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;
		}

	}

	public static final String _STATIC_INFO_DIR = "_static_info";

	private static final String KEY = StaticInfo.class.getName();

	public static final int ACCES_DAYS = 7;

	private String dataFolder = null;

	private String staticURL = null;

	private MenuElement linkedPage = null;

	private Date linkedDate;

	private String linkedTitle;

	private String linkedDescription;

	private String linkedLocation;

	private long size = -1;

	private Collection<MenuElement> containers = null;

	PropertiesConfiguration properties;

	private File file;

	private StaticInfoFile() {
		properties = new PropertiesConfiguration();
		properties.setAutoSave(false);
	}

	protected static File getPropertiesFileNameFromResourceFile(ContentContext ctx, File file) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext());
		String fullURL = file.getPath();
		String fullStaticFolder = URLHelper.mergePath(globalContext.getDataFolder(), staticConfig.getStaticFolder());
		String relURL = fullURL.replace(fullStaticFolder, "");
		relURL = relURL.replace('\\', '/').replaceAll("//", "/").trim();

		String dataFolder = globalContext.getDataFolder();

		String path = URLHelper.mergePath(dataFolder, _STATIC_INFO_DIR);
		String realPathProperties = URLHelper.mergePath(path, relURL) + ".properties";
		return new File(realPathProperties);
	}

	public static StaticInfoFile getInstance(ContentContext ctx, File file) throws Exception {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		String fullURL = file.getPath();

		String fullStaticFolder = URLHelper.mergePath(globalContext.getDataFolder(), staticConfig.getStaticFolder());

		String relURL = fullURL.replace(fullStaticFolder, "");

		return getInstance(ctx, relURL);
	}

	public static StaticInfoFile getInstance(ContentContext ctx, String inStaticURL) throws Exception {

		inStaticURL = inStaticURL.replace('\\', '/').replaceAll("//", "/").trim();

		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		if (inStaticURL.startsWith(staticConfig.getStaticFolder())) {
			inStaticURL = inStaticURL.substring(staticConfig.getStaticFolder().length());
		}
		if (!inStaticURL.startsWith("/")) {
			inStaticURL = '/' + inStaticURL;
		}
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		// ServletContextWeakReference scwr = ServletContextWeakReference.getInstance(ctx.getRequest().getSession().getServletContext());

		ServletContext application = ctx.getRequest().getSession().getServletContext();

		int renderMode = 0;

		String key = KEY + inStaticURL + "_" + renderMode + "_" + globalContext.getContextKey();
		StaticInfoFile staticInfo = (StaticInfoFile) globalContext.getAttribute(key);
		if (staticInfo == null) {
			staticInfo = new StaticInfoFile();
			ctx.getRequest().getSession().setAttribute(key, staticInfo); // when
			// session
			// lost
			// ->
			// weak
			// ref
			// lost
			staticInfo.dataFolder = globalContext.getDataFolder();

			staticInfo.loadProperties(application, inStaticURL);

			/* load real file */
			String realPath = URLHelper.mergePath(globalContext.getDataFolder(), staticConfig.getStaticFolder());
			realPath = URLHelper.mergePath(realPath, inStaticURL);

			File file = new File(realPath);
			staticInfo.setFile(file);
			staticInfo.size = file.length();

			if (staticInfo.getLinkedPage() == null && staticInfo.getLinkedPageId() != null) {
				ContentService content = ContentService.getInstance(ctx.getRequest());
				MenuElement linkedPage = content.getNavigation(ctx).searchChildFromId(staticInfo.getLinkedPageId());
				if (linkedPage != null) {
					staticInfo.setLinkedPage(linkedPage);
					/*
					 * staticInfo.linkedDate = linkedPage.getContentDate(ctx); staticInfo.linkedTitle = linkedPage.getTitle(ctx); staticInfo.linkedDescription = linkedPage.getDescription(ctx); staticInfo.setLinkedLocation(linkedPage.getLocation(ctx));
					 */
				} /*
				 * else { try to decoment this, but problem when download in view mode in imageTransformServlet the content loader is not preview so to new page can not be found. staticInfo.setLinkedPageId(null); }
				 */

			}

			globalContext.setAttribute(key, staticInfo);
		}

		if (staticInfo.getResource() == null) {
			staticInfo.setResource(inStaticURL);
		}

		staticInfo.staticURL = inStaticURL;

		if (staticInfo.getDate(ctx) == null && staticInfo.isEmptyDate()) {
			Date lastModifiedDate = new Date(staticInfo.getFile().lastModified());
			staticInfo.setDate(lastModifiedDate);
		}

		return staticInfo;
	}

	private void loadProperties(ServletContext application, String inStaticURL) throws IOException, ConfigurationException {

		String path = URLHelper.mergePath(dataFolder, _STATIC_INFO_DIR);

		String realPathProperties = URLHelper.mergePath(path, inStaticURL) + ".properties";
		File file = new File(realPathProperties);

		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}

		synchronized (properties) {
			properties.setFile(file);
			properties.load();
		}
	}

	public String getFullDescription(ContentContext ctx) {
		return getFullDescription(ctx, true);
	}

	public String getFullDescription(ContentContext ctx, boolean showTitle) {

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Iterator<String> defaultLg = globalContext.getDefaultLanguages().iterator();

		ContentContext pertientContext = new ContentContext(ctx);

		while (!isPertinent(pertientContext) && defaultLg.hasNext()) {
			pertientContext.setRequestContentLanguage(defaultLg.next());
		}

		if (!isPertinent(pertientContext)) {
			pertientContext = ctx;
		}

		String title = getTitle(pertientContext);
		if (title != null && title.trim().length() > 0) {
			if ((title.trim().charAt(title.trim().length() - 1) == '.')) {
				title = title.substring(0, title.length() - 1);
			}
			out.print(title);
		}

		/*
		 * if (getDescription() != null && getDescription().trim().length() > 0) { String description = getDescription().trim(); if (description.charAt(description.length() - 1) == '.') { description = description.substring(0, description.length() - 1); } if (getTitle() != null && getTitle().trim().length() > 0) { out.print(". "); } out.print(description); }
		 */

		String sufix = "";

		if (getLocation(pertientContext) != null && getLocation(pertientContext).trim().length() > 0) {
			sufix = " (" + getLocation(pertientContext) + " ";
		}

		if (getDate(ctx) != null && !isEmptyDate()) {
			if (getLocation(pertientContext) == null || getLocation(pertientContext).trim().length() == 0) {
				sufix = sufix + " (";
			}
			sufix = sufix + StringHelper.renderDate(getDate(ctx));
		}

		if (sufix.length() > 0) {
			sufix = sufix + ")";
		}

		out.print(sufix);

		if ((getDescription(pertientContext) != null && getDescription(pertientContext).trim().length() > 0) || (getTitle(pertientContext) != null && getTitle(pertientContext).trim().length() > 0)) {
			out.print('.');
		}

		out.close();
		return writer.toString();
	}

	public String getManualDescription(ContentContext ctx) {
		return properties.getString("description-" + ctx.getRequestContentLanguage(), "");
	}

	public String getDescription(ContentContext ctx) {
		if (getManualDescription(ctx).length() > 0) {
			return getManualDescription(ctx);
		} else {
			return getLinkedDescription(ctx);
		}
	}

	protected void save() {
		try {
			synchronized (properties) {
				properties.save();
			}
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	public void setDescription(ContentContext ctx, String description) {
		properties.setProperty("description-" + ctx.getRequestContentLanguage(), description);
		save();
	}

	public String getManualLocation(ContentContext ctx) {
		return properties.getString("location-" + ctx.getRequestContentLanguage(), "");
	}

	public String getLocation(ContentContext ctx) {
		if (getManualLocation(ctx).length() > 0) {
			return getManualLocation(ctx);
		} else {
			return getLinkedLocation(ctx);
		}
	}

	public void setLocation(ContentContext ctx, String location) {
		properties.setProperty("location-" + ctx.getRequestContentLanguage(), location);
		save();
	}

	public String getTitle(ContentContext ctx) {
		if (getManualTitle(ctx).length() > 0) {
			return getManualTitle(ctx);
		} else {
			return getLinkedTitle(ctx);
		}
	}

	public String getManualTitle(ContentContext ctx) {
		return properties.getString("title-" + ctx.getRequestContentLanguage(), "");
	}

	public void setTitle(ContentContext ctx, String title) {
		properties.setProperty("title-" + ctx.getRequestContentLanguage(), title);
		save();
	}

	public Date getManualDate() {
		String dateStr = properties.getString("date", null);

		if (dateStr != null) {
			try {
				return StringHelper.parseTime(dateStr);
			} catch (ParseException e) {
				// logger.warning(e.getMessage());
			}
		}

		return null;
	}

	public Date getDate(ContentContext ctx) {
		if (getManualDate() != null) {
			return getManualDate();
		} else {
			return getLinkedDate(ctx);
		}
	}

	public void setDate(Date date) {
		properties.setProperty("date", StringHelper.renderTime(date));
		save();
	}

	public String getResource() {
		return properties.getString("resource");
	}

	public void setResource(String resource) {
		properties.setProperty("resource", resource);
		save();
	}

	public void setEmptyDate() {
		properties.setProperty("date", "");
		save();
	}

	public boolean isEmptyDate() {
		return properties.getProperty("date") == null;
	}

	public long getSize() {
		return size;
	}

	public String getLinkedPageId() {
		return properties.getString("linked-page-id");
	}

	public void setLinkedPageId(String pageId) {
		properties.setProperty("linked-page-id", pageId);
		setLinkedPage(null);
		save();
	}

	public String getStaticURL() {
		return staticURL;
	}

	public Collection<MenuElement> getContainers() {
		return containers;
	}

	public void setContainers(Collection<MenuElement> containers) {
		this.containers = containers;
	}

	public int getFocusZoneX() {
		return properties.getInt("focus-zone-x", 500);
	}

	public int getFocusZoneY() {
		return properties.getInt("focus-zone-y", 300);
	}

	public boolean isShared() {
		return properties.getBoolean("shared", true);
	}

	public void setShared(boolean shared) {
		properties.setProperty("shared", shared);
		save();
	}

	public void setFocusZoneX(int focusZoneX) {
		properties.setProperty("focus-zone-x", focusZoneX);
		save();
	}

	public void setFocusZoneY(int focusZoneY) {
		properties.setProperty("focus-zone-y", focusZoneY);
		save();
	}

	public void setLinkedPage(MenuElement linkedPage) {
		this.linkedPage = linkedPage;
	}

	public MenuElement getLinkedPage() {
		return linkedPage;
	}

	public Date getLinkedDate(ContentContext ctx) {
		if (getLinkedPage() != null) {
			if (linkedDate == null) {
				try {
					linkedDate = getLinkedPage().getContentDate(ctx);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return linkedDate;
		} else {
			return null;
		}
	}

	public void setLinkedDate(Date linkedDate) {
		this.linkedDate = linkedDate;
	}

	public String getLinkedTitle(ContentContext ctx) {
		if (getLinkedPage() != null) {
			if (linkedTitle == null) {
				try {
					linkedTitle = getLinkedPage().getTitle(ctx);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return linkedTitle;
		} else {
			return "";
		}
	}

	public void setLinkedTitle(String linkedTitle) {
		this.linkedTitle = linkedTitle;
	}

	public String getLinkedDescription(ContentContext ctx) {
		if (getLinkedPage() != null) {
			if (linkedDescription == null) {
				try {
					linkedDescription = getLinkedPage().getDescription(ctx);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return linkedDescription;
		} else {
			return "";
		}
	}

	public void setLinkedDescription(String linkedDescription) {
		this.linkedDescription = linkedDescription;
	}

	public void setLinkedLocation(String linkedLocation) {
		this.linkedLocation = linkedLocation;
	}

	public String getLinkedLocation(ContentContext ctx) {
		if (getLinkedPage() != null) {
			if (linkedLocation == null) {
				try {
					linkedLocation = getLinkedPage().getLocation(ctx);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return linkedLocation;
		} else {
			return "";
		}
	}

	public boolean isPertinent(ContentContext ctx) {
		boolean outPertinent = getManualTitle(ctx).length() > 0 || getManualDescription(ctx).length() > 0 || getManualLocation(ctx).length() > 0;

		return outPertinent;

	}

	public boolean delete() {
		return this.properties.getFile().delete();
	}

	public void setFile(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public long getCRC32() {
		try {
			return FileUtils.checksumCRC32(file);
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public void renameFile(ContentContext ctx, File newFile) {
		synchronized (properties) {
			File currentPropFile = properties.getFile();
			File newPropFile = getPropertiesFileNameFromResourceFile(ctx, newFile);
			currentPropFile.renameTo(newPropFile);
		}
	}

	/*
	 * public int getAccessFromSomeDays(ContentContext ctx) throws Exception { final String KEY = "mount-access"; Integer outCount = (Integer) cache.get(KEY); if (outCount == null) { StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession()); outCount = Tracker.getTracker(ctx.getRequest().getSession()).getRessourceCountAccess(ctx, staticConfig.getLastAccessStatic(), this); cache.put(KEY, outCount); } return outCount; }
	 */

	public int getAccessFromSomeDays(ContentContext ctx) throws Exception {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		Calendar cal = Calendar.getInstance();
		int countDay = 0;
		int outAccess = 0;
		while (countDay < staticConfig.getLastAccessStatic()) {
			outAccess = outAccess + getAccess(cal.getTime());
			cal.roll(Calendar.DAY_OF_YEAR, false);
			countDay++;
		}
		return outAccess;
	}

	public static void main(String[] args) {
		Calendar cal = Calendar.getInstance();
		for (int i = 0; i < 10; i++) {
			cal.roll(Calendar.DATE, false);
			// System.out.println("*** cal day = "+cal.get(Calendar.DAY_OF_MONTH));
			String key = getAccessKey(cal.getTime());
			System.out.println("** key = " + key);
		}
	}

	private static String getAccessKey(Date date) {
		return "access-" + StringHelper.renderDate(date, "yyyy-MM-dd");
	}

	public int getAccess(Date date) {
		String key = getAccessKey(date);
		if (this.properties.getProperty(key) == null) {
			return 0;
		}
		return this.properties.getInt(key);
	}

	public void addAccess() {
		String key = getAccessKey(new Date());
		int todayAccess = 0;
		if (this.properties.getProperty(key) != null) {
			todayAccess = this.properties.getInt(key);
		}
		todayAccess++;
		this.properties.setProperty(key, todayAccess);
		save();
	}

}
