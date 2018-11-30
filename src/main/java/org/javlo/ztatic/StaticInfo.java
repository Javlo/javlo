package org.javlo.ztatic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.naming.ConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.javlo.cache.ICache;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.rest.IRestItem;
import org.javlo.helper.ExifHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.image.ImageHelper;
import org.javlo.image.ImageSize;
import org.javlo.service.ContentService;
import org.javlo.service.PersistenceService;
import org.javlo.service.exception.ServiceException;
import org.javlo.service.location.LocationService;
import org.javlo.user.User;
import org.javlo.xml.NodeXML;
import org.owasp.encoder.Encode;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class StaticInfo implements IRestItem {

	private static final String FOCUS_ZONE_Y = "focus-zone-y";

	private static final String FOCUS_ZONE_X = "focus-zone-x";

	public static final StaticInfo EMPTY_INSTANCE = getFakeInstance();

	public static final String _STATIC_INFO_DIR = null;

	public static final int DEFAULT_FOCUS_X = 500;

	public static final int DEFAULT_FOCUS_Y = 500;

	private boolean isDescription = true;

	private ReferenceBean refBean = null;

	public static class ReferenceBean {
		private String reference;
		private String language;

		public ReferenceBean(String reference, String language) {
			super();
			this.reference = reference;
			this.language = language;
		}

		public String getReference() {
			return reference;
		}

		public String getLanguage() {
			return language;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			} else if (!(obj instanceof ReferenceBean)) {
				return false;
			} else {
				ReferenceBean refObj = (ReferenceBean) obj;
				return StringHelper.compare(reference, refObj.reference) && StringHelper.compare(language, refObj.language);

			}
		}

		@Override
		public String toString() {
			return reference + " - " + language;
		}
	}

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(StaticInfo.class.getName());

	public static final class Position {
		private double longitude = 0;
		private double latitude = 0;

		public Position(double longitude, double latiture) {
			super();
			this.longitude = longitude;
			this.latitude = latiture;
		}

		public double getLongitude() {
			return longitude;
		}

		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}

		public double getLatitude() {
			return latitude;
		}

		public void setLatitude(double latiture) {
			this.latitude = latiture;
		}

		@Override
		public String toString() {
			return getLatitude() + "," + getLongitude();
		}

	}

	public ContentContext getContextWithContent(ContentContext ctx) {
		String content = (getTitle(ctx) + getDescription(ctx) + getCopyright(ctx)).trim();
		if (content.length() > 0) {
			return ctx;
		} else {
			ContentContext lgCtx = new ContentContext(ctx);
			for (String lg : ctx.getGlobalContext().getDefaultLanguages()) {
				lgCtx.setAllLanguage(lg);
				content = (getTitle(lgCtx) + getDescription(lgCtx) + getCopyright(lgCtx)).trim();
				if (content.length() > 0) {
					return lgCtx;
				}
			}
		}
		return ctx;
	}

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
					if (uri1Info.getDate(ctx).equals(uri2Info.getDate(ctx))) {
						return 0;
					} else {
						Calendar cal1 = Calendar.getInstance();
						cal1.setTime(uri1Info.getDate(ctx));
						Calendar cal2 = Calendar.getInstance();
						cal2.setTime(uri2Info.getDate(ctx));
						if (cal1.compareTo(cal2) > 0) {
							return -1;
						} else {
							return 1;
						}
					}
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
				if (access1 == null) {
					StaticInfo uri1Info = StaticInfo.getInstance(ctx, file1);
					access1 = uri1Info.getAccessFromSomeDays(ctx);
					fileAccess.put(file1, access1);
				}

				Integer access2 = fileAccess.get(file2);
				if (access2 == null) {
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

	public static class StaticInfoSortByDate implements Comparator<StaticInfo> {

		ContentContext ctx;
		boolean ascending = true;

		public StaticInfoSortByDate(ContentContext inCtx, boolean inAscending) {
			ctx = inCtx;
			ascending = inAscending;
		}

		@Override
		public int compare(StaticInfo uri1Info, StaticInfo uri2Info) {
			try {
				int changeOrder = 1;
				if (!ascending) {
					changeOrder = -1;
				}

				if (uri1Info.getDate(ctx) == null && uri2Info.getDate(ctx) == null) {
					return 1;
				} else if (uri1Info.getDate(ctx) == null) {
					return -1 * changeOrder;
				} else if (uri2Info.getDate(ctx) == null) {
					return 1 * changeOrder;
				} else {
					if (uri1Info.getDate(ctx).equals(uri2Info.getDate(ctx))) {
						return 1;
					} else {
						return uri1Info.getDate(ctx).compareTo(uri2Info.getDate(ctx)) * changeOrder;
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;
		}

	}

	public static class StaticInfoSortByCreationDate implements Comparator<StaticInfo> {
		ContentContext ctx;
		boolean ascending = true;

		public StaticInfoSortByCreationDate(ContentContext inCtx, boolean inAscending) {
			ctx = inCtx;
			ascending = inAscending;
		}

		@Override
		public int compare(StaticInfo uri1Info, StaticInfo uri2Info) {
			try {
				int changeOrder = 1;
				if (!ascending) {
					changeOrder = -1;
				}
				return uri1Info.getCreationDate(ctx).compareTo(uri2Info.getCreationDate(ctx)) * changeOrder;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;
		}
	}

	private static final String KEY = "staticinfo-";

	public static final int ACCES_DAYS = 7;

	private String staticURL = null;

	private Date linkedDate;

	private Date fileDate = null;

	private String linkedTitle;

	private String linkedDescription;

	private String linkedLocation;

	private List<String> tags = null;

	private Set<String> taxonomy = null;

	private List<String> readRoles = null;

	private long size = -1;

	private File file;

	private Long crc32 = null;

	private Date date;

	private String id = null;

	private boolean staticFolder = true;

	private ImageSize imageSize = null;

	private static final ImageSize NO_IMAGE_SIZE = new ImageSize(0, 0);

	/**
	 * false if date come from last modified of the file.
	 */
	private boolean dateFromData = true;

	private int accessFromSomeDays = -1;

	private boolean searchFace = false;

	// private Metadata imageMetadata = null;

	/**
	 * instance of static info sur shared file
	 * 
	 * @param ctx
	 * @param inStaticURL
	 * @return
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public static StaticInfo getShareInstance(ContentContext ctx, String inStaticURL) throws Exception {
		return getInstance(ctx, inStaticURL);
	}

	private String getKey(ContentContext ctx, String key) {
		return getKey(ctx, staticURL, key);
	}

	private String getKey(ContentContext ctx, String inStaticURL, String key) {
		// if (key.contains("-")) {
		// if (ctx != null) {
		// ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		// String previousKey = key;
		// key = key.replace("-", "_");
		// content.setAttribute(ctx, key, content.getAttribute(ctx, previousKey));
		// content.removeAttribute(ctx, previousKey);
		// }
		// key = key.replace("-", "_");
		// }
		return KEY + inStaticURL + '-' + key;
	}

	public static String getStaticUrlFromKey(Object key) {
		if (!("" + key).contains(".")) {
			return null;
		}
		String url = key.toString().substring(KEY.length());
		String suffix = url.substring(url.lastIndexOf("."));
		suffix = suffix.substring(suffix.indexOf("-"));
		url = url.substring(0, url.lastIndexOf(suffix));
		return url;
	}

	public static boolean isStaticInfoKey(Object key) {
		return ("" + key).startsWith(KEY);
	}

	public static boolean isDefaultStaticKeyValue(Object key, String value) {
		if (isStaticInfoKey(key)) {
			if (key.toString().endsWith(FOCUS_ZONE_X)) {
				return value.equals("" + DEFAULT_FOCUS_X);
			}
			if (key.toString().endsWith(FOCUS_ZONE_Y)) {
				return value.equals("" + DEFAULT_FOCUS_Y);
			}
		}
		return false;
	}

	public static StaticInfo getInstance(ContentContext ctx, File file) throws Exception {
		String relURL = ResourceHelper.getRelativeStaticURL(ctx, file);
		StaticInfo staticInfo = getInstance(ctx, relURL);
		staticInfo.setStaticFolder(true);
		return staticInfo;
	}

	private static StaticInfo getFakeInstance() {
		StaticInfo outInstance = new StaticInfo();
		outInstance.setFile(new File(""));
		return outInstance;
	}

	public static StaticInfo getInstance(ContentContext ctx, String inStaticURL) throws Exception {
		inStaticURL = inStaticURL.replace('\\', '/').replaceAll("//", "/").trim();
		if (!inStaticURL.startsWith("/")) {
			inStaticURL = '/' + inStaticURL;
		}
		while (inStaticURL.startsWith("/static")) {
			inStaticURL = inStaticURL.replaceFirst("/static", "");
		}

		GlobalContext globalContext = ctx.getGlobalContext();

		// StaticInfo outStaticInfo = (StaticInfo) request.getAttribute(inStaticURL);

		final String KEY = "staticInfo-" + inStaticURL;

		StaticInfo outStaticInfo = (StaticInfo) globalContext.getTimeAttribute(KEY);

		if (outStaticInfo == null) {
			StaticInfo staticInfo = new StaticInfo();
			staticInfo.isDescription = ctx.getGlobalContext().getStaticConfig().isStaticInfoDescription();
			// init creation data
			staticInfo.getCreationDate(ctx);
			staticInfo.staticURL = inStaticURL;

			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
			if (inStaticURL.startsWith(staticConfig.getStaticFolder())) {
				inStaticURL = inStaticURL.substring(staticConfig.getStaticFolder().length());
			}
			String realPath = URLHelper.mergePath(globalContext.getDataFolder(), staticConfig.getStaticFolder());
			realPath = URLHelper.mergePath(realPath, inStaticURL);

			File file = new File(realPath);
			staticInfo.setFile(file);
			staticInfo.size = file.length();

			if (!staticInfo.isInitialised(ctx)) {
				staticInfo.init(ctx);
			}

			outStaticInfo = staticInfo;

			// request.setAttribute(inStaticURL, outStaticInfo);
			globalContext.setTimeAttribute(KEY, outStaticInfo);
		}

		return outStaticInfo;
	}

	private void init(ContentContext ctx) throws IOException, SAXException, TikaException {
		setInitialised(ctx);
		if (getFile() == null || !getFile().exists()) {
			return;
		}
		if (StringHelper.isSound(getFile().getName())) {
			InputStream input = new FileInputStream(getFile());
			Metadata metadata = new Metadata();
			try {
				ContentHandler handler = new DefaultHandler();
				Parser parser = new Mp3Parser();
				ParseContext parseCtx = new ParseContext();
				parser.parse(input, handler, metadata, parseCtx);
			} finally {
				ResourceHelper.closeResource(input);
			}
			// List all metadata
			String[] metadataNames = metadata.names();
			setTitle(ctx, metadata.get("title"));
			setAuthors(ctx, StringHelper.mergeString(" - ", metadata.get("xmpDM:artist"), metadata.get("xmpDM:composer")));
			setDescription(ctx, StringHelper.mergeString(" - ", metadata.get("xmpDM:genre"), metadata.get("xmpDM:album")));
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
		 * if (getDescription() != null && getDescription().trim().length() > 0) {
		 * String description = getDescription().trim(); if
		 * (description.charAt(description.length() - 1) == '.') { description =
		 * description.substring(0, description.length() - 1); } if (getTitle() != null
		 * && getTitle().trim().length() > 0) { out.print(". "); }
		 * out.print(description); }
		 */

		String sufix = "";

		if (getLocation(pertientContext) != null && getLocation(pertientContext).trim().length() > 0) {
			sufix = " (" + getLocation(pertientContext) + " ";
		}

		if (getDate(ctx) != null && !isEmptyDate(ctx)) {
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
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		return content.getAttribute(ctx, getKey(ctx, "description-" + ctx.getRequestContentLanguage()), "");
	}

	public String getReference(ContentContext ctx) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		return content.getAttribute(ctx, getKey(ctx, "ref-" + ctx.getRequestContentLanguage()), "");
	}

	public void setReference(ContentContext ctx, String ref) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		if (!StringHelper.isEmpty(ref)) {
			if (!getReference(ctx).equals(ref)) {
				content.setAttribute(ctx, getKey(ctx, "ref-" + ctx.getRequestContentLanguage()), ref);
				refBean = null;
			}
		} else {
			content.removeAttribute(ctx, getKey(ctx, "ref-" + ctx.getRequestContentLanguage()));
			refBean = null;
		}
	}

	public String getLanguage(ContentContext ctx) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		return content.getAttribute(ctx, getKey(ctx, "lg-" + ctx.getRequestContentLanguage()), "");
	}

	public ReferenceBean getReferenceBean(ContentContext ctx) {
		if (refBean == null) {
			if (!StringHelper.isEmpty(getReference(ctx)) || !StringHelper.isEmpty(getReference(ctx))) {
				refBean = new ReferenceBean(getReference(ctx), getLanguage(ctx));
			} else {
				return null;
			}
		}
		return refBean;
	}

	public void setLanguage(ContentContext ctx, String lg) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		if (!StringHelper.isEmpty(lg)) {
			if (!lg.equals(getLanguage(ctx))) {
				content.setAttribute(ctx, getKey(ctx, "lg-" + ctx.getRequestContentLanguage()), lg);
				refBean = null;
			}
		} else {
			content.removeAttribute(ctx, getKey(ctx, "lg-" + ctx.getRequestContentLanguage()));
			refBean = null;
		}
	}

	public boolean isResized(ContentContext ctx) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		return StringHelper.isTrue(content.getAttribute(ctx, getKey(ctx, "resized"), null));
	}

	public void setResized(ContentContext ctx, boolean resized) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		content.setAttribute(ctx, getKey(ctx, "resized"), "" + resized);
	}

	public String getDescription(ContentContext ctx) {
		if (!isDescription) {
			return "";
		}
		if (getManualDescription(ctx).length() > 0) {
			return getManualDescription(ctx);
		} else {
			return "";
		}
	}

	public void save(ContentContext ctx) throws ServiceException, Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		PersistenceService.getInstance(globalContext).setAskStore(true);
	}

	public void setDescription(ContentContext ctx, String description) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		if (!StringHelper.isEmpty(description)) {
			content.setAttribute(ctx, getKey(ctx, "description-" + ctx.getRequestContentLanguage()), description);
		} else {
			content.removeAttribute(ctx, getKey(ctx, "description-" + ctx.getRequestContentLanguage()));
		}
	}

	public String getManualLocation(ContentContext ctx) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		return content.getAttribute(ctx, getKey(ctx, "location-" + ctx.getRequestContentLanguage()), "");
		// return properties.getString("location-" +
		// ctx.getRequestContentLanguage(), "");
	}

	public String getLocation(ContentContext ctx) {
		if (getManualLocation(ctx).length() > 0) {
			return getManualLocation(ctx);
		} else {
			return "";
		}
	}

	public Position getPosition(ContentContext ctx) {
		try {
			return ExifHelper.readPosition(getFile());
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}

		/*
		 * System.out.println("***** StaticInfo.getPosition : name = "+file. getName
		 * ()); //TODO: remove debug trace
		 * 
		 * Metadata md = getImageMetadata(); if (md != null &&
		 * md.getDirectoriesOfType(GpsDirectory.class) != null) { Iterator<GpsDirectory>
		 * directories = md.getDirectoriesOfType(GpsDirectory.class).iterator(); if
		 * (directories.hasNext()) { GpsDirectory gpsDirectory = (GpsDirectory)
		 * directories.next(); GeoLocation geoLocation = gpsDirectory.getGeoLocation();
		 * if (geoLocation != null) {
		 * System.out.println("***** StaticInfo.getPosition : OK"); //TODO: remove debug
		 * trace return new Position(geoLocation.getLatitude(),
		 * geoLocation.getLongitude()); } else {
		 * System.out.println("***** StaticInfo.getPosition : no geo"); //TODO: remove
		 * debug trace } } System.out.println("***** StaticInfo.getPosition : NO NEXT");
		 * //TODO: remove debug trace } else {
		 * System.out.println("***** StaticInfo.getPosition : md="+md); //TODO: remove
		 * debug trace System.out.println("***** StaticInfo.getPosition : NULL");
		 * //TODO: remove debug trace }
		 */
		return null;
	}

	public String getGeoLocation(ContentContext ctx) throws IOException {
		Position pos = getPosition(ctx);
		if (pos != null) {
			return LocationService.getLocation(pos.getLatitude(), pos.getLongitude(), ctx.getLanguage()).getFullLocality();
		} else {
			return null;
		}
	}

	public void setLocation(ContentContext ctx, String location) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		if (StringHelper.isEmpty(location)) {
			content.removeAttribute(ctx, getKey(ctx, "location-" + ctx.getRequestContentLanguage()));
		} else {
			content.setAttribute(ctx, getKey(ctx, "location-" + ctx.getRequestContentLanguage()), location);
		}
	}

	public String getTitle(ContentContext ctx) {
		if (getManualTitle(ctx).length() > 0) {
			return getManualTitle(ctx);
		}
		return "";
	}

	public String getManualTitle(ContentContext ctx) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		String key = getKey(ctx, "title-" + ctx.getRequestContentLanguage());
		String title = content.getAttribute(ctx, key, "");
		if (!isDescription && StringHelper.isEmpty(title)) {
			String description = getManualDescription(ctx);
			if (!StringHelper.isEmpty(description)) {
				setDescription(ctx, "");
				setTitle(ctx, description);
				return description;
			}
		}
		return title;
	}

	public String getId(ContentContext ctx) {
		if (id == null) {
			ContentService content = ContentService.getInstance(ctx.getGlobalContext());
			String key = getKey(ctx, "id-" + ctx.getRequestContentLanguage());
			id = content.getAttribute(ctx, key, "");
			if (id.trim().length() == 0) {
				id = StringHelper.getRandomId();
				content.setAttribute(ctx, key, id);
			}
		}
		return id;
	}

	public void setInitialised(ContentContext ctx) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		content.setAttribute(ctx, getKey(ctx, "init-" + ctx.getRequestContentLanguage()), "1");
	}

	public boolean isInitialised(ContentContext ctx) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		return StringHelper.isTrue(content.getAttribute(ctx, getKey(ctx, "init-" + ctx.getRequestContentLanguage())));
	}

	public void setTitle(ContentContext ctx, String title) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		if (StringHelper.isEmpty(title)) {
			content.removeAttribute(ctx, getKey(ctx, "title-" + ctx.getRequestContentLanguage()));
		} else {
			content.setAttribute(ctx, getKey(ctx, "title-" + ctx.getRequestContentLanguage()), title);
		}
	}

	public Date getManualDate(ContentContext ctx) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		String dateStr = content.getAttribute(ctx, getKey(ctx, "date"), null);
		if (dateStr != null) {
			try {
				return StringHelper.parseTime(dateStr);
			} catch (ParseException e) {
			}
		}
		return null;
	}

	public Date getCreationDate(ContentContext ctx) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		String dateStr = content.getAttribute(ctx, getKey(ctx, "creation-date"), null);
		if (dateStr == null) {
			dateStr = StringHelper.renderTime(new Date());
			content.setAttribute(ctx, getKey(ctx, "creation-date"), dateStr);
			try {
				PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
			} catch (ServiceException e) {
				e.printStackTrace();
				return null;
			}
		}
		try {
			return StringHelper.parseTime(dateStr);
		} catch (ParseException e) {
		}
		return null;
	}

	public String getAuthors(ContentContext ctx) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		return content.getAttribute(ctx, getKey(ctx, "authors"), "");
	}

	public void setAuthors(ContentContext ctx, String authors) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		if (StringHelper.isEmpty(authors)) {
			content.removeAttribute(ctx, getKey(ctx, "authors"));
		} else {
			content.setAttribute(ctx, getKey(ctx, "authors"), authors);
		}
	}

	public Date getDate(ContentContext ctx) {
		if (date == null) {
			if (getManualDate(ctx) != null) {
				date = getManualDate(ctx);
			} else {
				if (getExifDate() != null) {
					date = getExifDate();
				} else {
					dateFromData = false;
					date = getFileDate(ctx);
				}
			}
		}
		return date;
	}

	public Date getFileDate(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ICache fileInfoCache = globalContext.getEternalCache("file-info");
		Date outDate = (Date) fileInfoCache.get(getKey(ctx, "file-date"));
		if (outDate == null) {
			outDate = getFileDate();
			fileInfoCache.put(getKey(ctx, "file-date"), outDate);
		}
		return outDate;
	}

	public void setDate(ContentContext ctx, Date date) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		if (date == null) {
			content.removeAttribute(ctx, getKey(ctx, "date"));
		} else {
			content.setAttribute(ctx, getKey(ctx, "date"), StringHelper.renderTime(date));
		}
	}

	public String getResource(ContentContext ctx) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		return content.getAttribute(ctx, getKey(ctx, "resource"), "");
	}

	public void setResource(ContentContext ctx, String resource) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		content.setAttribute(ctx, getKey(ctx, "resource"), resource);
	}

	public void setEmptyDate(ContentContext ctx) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		content.setAttribute(ctx, getKey(ctx, "date"), "");
	}

	public boolean isEmptyDate(ContentContext ctx) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		return content.getAttribute(ctx, getKey(ctx, "date"), null) == null;
	}

	public long getSize() {
		return size;
	}

	// public String getLinkedPageId(ContentContext ctx) {
	// ContentService content = ContentService.getInstance(ctx.getGlobalContext());
	// return content.getAttribute(ctx, getKey(ctx, "linked-page-id"));
	// }
	//
	// public void setLinkedPageId(ContentContext ctx, String pageId) {
	// if (pageId != null) {
	// ContentService content = ContentService.getInstance(ctx.getGlobalContext());
	// content.setAttribute(ctx, getKey(ctx, "linked-page-id"), pageId);
	// }
	// }

	public String getStaticURL() {
		return staticURL;
	}

	public String getFolder() {
		return StringUtils.chomp(ResourceHelper.getPath(getStaticURL()), "/");
	}

	/**
	 * return the focus point of a image. Return always the focus point of edit mode
	 * (problem with image cache).
	 * 
	 * @param ctx
	 * @return
	 */
	public int getFocusZoneX(ContentContext ctx) {

		ContentService content = ContentService.getInstance(ctx.getGlobalContext());

		ContentContext editCtx = ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE);
		if (!content.isNavigationLoaded(editCtx)) {
			editCtx = ctx;
		}
		if (!searchFace && content.getAttribute(editCtx, getKey(ctx, FOCUS_ZONE_X), null) == null) {
			searchFace = true;
			if (StringHelper.isImage(getFile().getName())) {
				try {
					if (getFile().exists()) {
						try {
							if (ctx.getGlobalContext().getStaticConfig().isAutoFocus() && ctx.isAsPreviewMode()) {
								logger.info("search point on interest on START : " + getFile() + " [" + ctx.getGlobalContext().getContextKey() + "]");
								InitInterest.setPointOfInterestWidthThread(ctx, getFile(), getKey(ctx, FOCUS_ZONE_X), getKey(ctx, FOCUS_ZONE_Y));
							}
						} catch (Throwable t) {
							logger.warning(t.getMessage());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		String kzx = content.getAttribute(editCtx, getKey(ctx, FOCUS_ZONE_X), "" + DEFAULT_FOCUS_X);
		return Integer.parseInt(kzx);
	}

	/**
	 * return the focus point of a image. Return always the focus point of edit mode
	 * (problem with image cache).
	 * 
	 * @param ctx
	 * @return
	 */
	public int getFocusZoneY(ContentContext ctx) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		ContentContext editCtx = ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE);
		if (!content.isNavigationLoaded(editCtx)) {
			editCtx = ctx;
		}
		if (content.getAttribute(editCtx, getKey(ctx, FOCUS_ZONE_X), null) == null) {
			getFocusZoneX(editCtx); // generate default value
		}
		String kzy = content.getAttribute(editCtx, getKey(ctx, FOCUS_ZONE_Y), "" + DEFAULT_FOCUS_Y);
		return Integer.parseInt(kzy);
	}

	public boolean isShared(ContentContext ctx) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		return StringHelper.isTrue(content.getAttribute(ctx, getKey(ctx, "shared"), "true"));
	}

	public void setShared(ContentContext ctx, boolean shared) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		if (shared) {
			content.removeAttribute(ctx, getKey(ctx, "shared"));
		} else {
			content.setAttribute(ctx, getKey(ctx, "shared"), "" + shared);
		}
	}

	/**
	 * change the focus point on a image.
	 * 
	 * @param ctx
	 * @param focusZoneX
	 */
	public void setFocusZoneX(ContentContext ctx, int focusZoneX) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		content.setAttribute(ctx, getKey(ctx, FOCUS_ZONE_X), "" + focusZoneX);
	}

	/**
	 * change the focus point on a image.
	 * 
	 * @param ctx
	 * @param focusZoneY
	 */
	public void setFocusZoneY(ContentContext ctx, int focusZoneY) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		if (focusZoneY == DEFAULT_FOCUS_Y) {
			content.removeAttribute(ctx, getKey(ctx, FOCUS_ZONE_Y));
		} else {
			content.setAttribute(ctx, getKey(ctx, FOCUS_ZONE_Y), "" + focusZoneY);
		}
	}

	private Date getFileDate() {
		if (fileDate == null) {
			fileDate = new Date(getFile().lastModified());
		}
		return fileDate;
	}

	public void setLinkedDate(Date linkedDate) {
		this.linkedDate = linkedDate;
	}

	public void setLinkedTitle(String linkedTitle) {
		this.linkedTitle = linkedTitle;
	}

	public void setLinkedDescription(String linkedDescription) {
		this.linkedDescription = linkedDescription;
	}

	public void setLinkedLocation(String linkedLocation) {
		this.linkedLocation = linkedLocation;
	}

	public boolean isPertinent(ContentContext ctx) {
		boolean outPertinent = getManualTitle(ctx).length() > 0 || getManualDescription(ctx).length() > 0 || getManualLocation(ctx).length() > 0;
		return outPertinent;

	}

	public boolean delete(ContentContext ctx) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		content.deleteKeys(getKey(ctx, ""));
		return true;
	}

	public void setFile(File file) {
		crc32 = null;
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public long getCRC32() {
		if (crc32 == null) {
			try {
				if (file.isFile()) {
					crc32 = FileUtils.checksumCRC32(file);
				} else {
					crc32 = 0L;
				}
			} catch (IOException e) {
				e.printStackTrace();
				crc32 = 0L;
			}
		}
		return crc32;
	}

	public void renameFile(ContentContext ctx, File newFile) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		StaticInfo newStaticInfo = StaticInfo.getInstance(ctx, newFile);
		content.renameKeys(getKey(ctx, ""), newStaticInfo.getKey(ctx, ""));
	}

	public void deleteFile(ContentContext ctx) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		content.deleteKeys(getKey(ctx, ""));
	}

	public void duplicateFile(ContentContext ctx, File newFile) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		StaticInfo newStaticInfo = StaticInfo.getInstance(ctx, newFile);
		content.duplicateKeys(getKey(ctx, ""), newStaticInfo.getKey(ctx, ""));
	}

	public int getAccessFromSomeDays(ContentContext ctx) throws Exception {
		if (accessFromSomeDays < 0) {
			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
			Calendar cal = Calendar.getInstance();
			int countDay = 0;
			int outAccess = 0;
			while (countDay < staticConfig.getLastAccessStatic()) {
				outAccess = outAccess + getAccess(ctx, cal.getTime());
				cal.roll(Calendar.DAY_OF_YEAR, false);
				countDay++;
			}
			accessFromSomeDays = outAccess;
		}
		return accessFromSomeDays;
	}

	private static String getAccessKey(Date date) {
		return "access-" + StringHelper.renderDate(date, GlobalContext.ACCESS_DATE_FORMAT);
	}

	public int getAccess(ContentContext ctx, Date date) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String key = getKey(ctx, getAccessKey(date));
		if (globalContext.getData(key) == null) {
			return 0;
		}
		return Integer.parseInt(globalContext.getData(key));
	}

	public void addAccess(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (globalContext.getSpecialConfig().isTrackingAccess()) {
			String key = getKey(ctx, getAccessKey(new Date()));
			int todayAccess = 0;
			if (globalContext.getData(key) != null) {
				todayAccess = Integer.parseInt(globalContext.getData(key));
			}
			todayAccess++;
			globalContext.setData(key, "" + todayAccess);
		}
	}

	private void storeTags(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String key = getKey(ctx, "tags");
		String rawTags = StringHelper.collectionToString(tags);
		globalContext.setData(key, rawTags);
	}

	public List<String> getTags(ContentContext ctx) {
		if (tags == null) {
			String key = getKey(ctx, "tags");
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			String rawTags = globalContext.getData(key);
			if (rawTags == null) {
				tags = Collections.EMPTY_LIST;
			} else {
				tags = StringHelper.stringToCollection(rawTags);
			}
		}
		return tags;
	}

	public Set<String> getTaxonomy(ContentContext ctx) {
		if (taxonomy == null) {
			String key = getKey(ctx, "taxonomy");
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			String rawTaxonomy = globalContext.getData(key);
			if (rawTaxonomy == null) {
				taxonomy = Collections.EMPTY_SET;
			} else {
				taxonomy = new HashSet(StringHelper.stringToCollection(rawTaxonomy));
			}
		}
		return taxonomy;
	}

	private void storeTaxonomy(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String key = getKey(ctx, "taxonomy");
		String taxonomyTags = StringHelper.collectionToString(taxonomy);
		globalContext.setData(key, taxonomyTags);
	}

	public void setTaxonomy(ContentContext ctx, Set<String> taxonomy) {
		this.taxonomy = taxonomy;
		storeTaxonomy(ctx);
	}

	public void addTag(ContentContext ctx, String tag) {
		if (tags == null || tags == Collections.EMPTY_LIST) {
			tags = new LinkedList<String>();
		}
		if (!tags.contains(tag)) {
			tags.add(tag);
			storeTags(ctx);
		}
	}

	public void removeTag(ContentContext ctx, String tag) {
		if (tags == null || tags == Collections.EMPTY_LIST) {
			tags = new LinkedList<String>();
		}
		if (tags.contains(tag)) {
			tags.remove(tag);
			storeTags(ctx);
		}
	}

	private void storeReadRoles(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String key = getKey(ctx, "read-roles");
		String rawRoles = StringHelper.collectionToString(readRoles);
		globalContext.setData(key, rawRoles);
	}

	public List<String> getReadRoles(ContentContext ctx) {
		if (readRoles == null) {
			String key = getKey(ctx, "read-roles");
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			String rawReadRole = globalContext.getData(key);
			if (rawReadRole == null) {
				readRoles = Collections.EMPTY_LIST;
			} else {
				readRoles = StringHelper.stringToCollection(rawReadRole);
			}
		}
		return readRoles;
	}

	public void addReadRole(ContentContext ctx, String role) {
		if (readRoles == null || readRoles == Collections.EMPTY_LIST) {
			readRoles = new LinkedList<String>();
		}
		if (!readRoles.contains(role)) {
			readRoles.add(role);
			storeReadRoles(ctx);
		}
	}

	public void removeReadRole(ContentContext ctx, String role) {
		readRoles = getReadRoles(ctx);
		if (readRoles.contains(role)) {
			readRoles.remove(role);
			storeReadRoles(ctx);
		}
	}

	public StaticInfo getDirecotry(ContentContext ctx) throws Exception {
		if (getFile().exists()) {
			return StaticInfo.getInstance(ctx, getFile().getParentFile());
		} else {
			return null;
		}
	}

	public String getVersionHash(ContentContext ctx) {
		return StringHelper.asBase64(getFocusZoneX(ctx) * getFocusZoneY(ctx)) + StringHelper.asBase64(getCRC32());
	}

	public Date getExifDate() {
		try {
			return ExifHelper.readDate(getFile());
		} catch (Exception e) {
			logger.warning(e.getMessage());
		}

		/*
		 * Metadata md = getImageMetadata(); if (md != null) { // obtain the Exif
		 * directory ExifSubIFDDirectory directory =
		 * md.getFirstDirectoryOfType(ExifSubIFDDirectory.class); // query the tag's
		 * value if (directory != null) { return
		 * directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL); } }
		 */
		return null;
	}

	public String getCopyright(ContentContext ctx) {
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		return content.getAttribute(ctx, getKey(ctx, "copyright"), "");
	}

	public void setCopyright(ContentContext ctx, String copyright) {
		if (copyright != null) {
			ContentService content = ContentService.getInstance(ctx.getGlobalContext());
			content.setAttribute(ctx, getKey(ctx, "copyright"), copyright);
		}
	}

	public String getFullHTMLTitle(ContentContext ctx) throws Exception {
		String title = getTitle(ctx);
		StaticInfo folder = StaticInfo.getInstance(ctx, file.getParentFile());
		if (title == null || title.trim().length() > 0) {
			title = folder.getTitle(ctx);
		}
		String location = getLocation(ctx);
		if (location == null || location.trim().length() > 0) {
			location = folder.getLocation(ctx);
		}
		String date = null;
		try {
			if (dateFromData) {
				date = StringHelper.renderShortDate(ctx, getDate(ctx));
			} else if (folder.dateFromData) {
				date = StringHelper.renderShortDate(ctx, folder.getDate(ctx));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		String sep = "";

		if (title != null && title.trim().length() > 0) {
			title = "<span class=\"title\">" + title + "</span>";
			sep = " - ";
		}

		String description = getDescription(ctx);
		if (description != null && description.trim().length() > 0) {
			description = "<span class=\"description\">" + description + "</span>";
			sep = " - ";
		}

		String authors = getAuthors(ctx);
		if (authors != null && authors.trim().length() > 0) {
			authors = "<span class=\"authors\">" + authors + "</span>";
			sep = " - ";
		}

		if (location != null && location.trim().length() > 0) {
			title = title + "<span class=\"location\">" + sep + location + "</span>";
			sep = " - ";
		}
		if (date != null) {
			title = title + "<span class=\"date\">" + sep + date + "</span>";
		}
		return title;
	}

	public String getFullTitle(ContentContext ctx) throws Exception {
		String title = getTitle(ctx);
		StaticInfo folder = StaticInfo.getInstance(ctx, file.getParentFile());
		if (title == null || title.trim().length() == 0) {
			title = folder.getTitle(ctx);
		}
		String location = getLocation(ctx);
		if (location == null || location.trim().length() == 0) {
			location = folder.getLocation(ctx);
		}
		String date = null;
		try {
			date = StringHelper.renderShortDate(ctx, getManualDate(ctx));
		} catch (Exception e) {
			e.printStackTrace();
		}

		title = StringHelper.mergeString(" - ", title, location, getDescription(ctx), getAuthors(ctx), date);

		if (StringHelper.isEmpty(title)) {
			if (getFile() != null) {
				title = getFile().getName();
			}
		}

		return title;
	}

	public Boolean canRead(ContentContext ctx, User user, String accessToken) {
		if (accessToken != null && accessToken.equals(getAccessToken(ctx))) {
			return true;
		}
		List<String> roles = getReadRoles(ctx);
		if (roles == null || roles.size() == 0) {
			return true;
		}
		if (user == null) {
			return false;
		} else {
			return !Collections.disjoint(roles, user.getUserInfo().getRoles());
		}
	}

	public void setAccessToken(ContentContext ctx, String token) {
		ctx.getGlobalContext().setTimeAttribute(getKey(ctx, "accesstoken"), token, 120);
	}

	public String getAccessToken(ContentContext ctx) {
		String key = getKey(ctx, "accesstoken");
		String token = (String) ctx.getGlobalContext().getTimeAttribute(key);
		if (token == null) {
			token = StringHelper.getRandomString(32, StringHelper.ALPHANUM);
			setAccessToken(ctx, token);
		}
		return token;
	}

	public String getURL(ContentContext ctx) throws IOException {
		return URLHelper.createResourceURL(ctx, getFile());
	}

	public boolean isStaticFolder() {
		return staticFolder;
	}

	public void setStaticFolder(boolean staticFolder) {
		this.staticFolder = staticFolder;
	}

	public void toXML(ContentContext ctx, Writer inWrt) {
		PrintWriter out = new PrintWriter(inWrt);
		String uri = file.getAbsolutePath();
		uri = uri.replace(ctx.getGlobalContext().getDataFolder(), "");
		out.println("<resource uri=\"" + uri + "\">");
		if (!StringHelper.isEmpty(getTitle(ctx))) {
			out.println("<title>" + Encode.forXml(getTitle(ctx)) + "</title>");
		}
		if (!StringHelper.isEmpty(getDescription(ctx))) {
			out.println("<description>" + Encode.forXml(getDescription(ctx)) + "</description>");
		}
		if (!StringHelper.isEmpty(getManualDate(ctx))) {
			out.println("<date>" + StringHelper.renderSortableTime(getManualDate(ctx)) + "</date>");
		}
		if (!StringHelper.isEmpty(getLocation(ctx))) {
			out.println("<location>" + Encode.forXml(getLocation(ctx)) + "</location>");
		}
		if (!StringHelper.isEmpty(getCopyright(ctx))) {
			out.println("<copyright>" + Encode.forXml(getCopyright(ctx)) + "</copyright>");
		}
		if (getReadRoles(ctx).size() > 0) {
			out.println("<roles>" + StringHelper.collectionToString(getReadRoles(ctx), ",") + "</roles>");
		}
		if (getFocusZoneX(ctx) != DEFAULT_FOCUS_X) {
			out.println("<focusx>" + getFocusZoneX(ctx) + "</focusx>");
		}
		if (getFocusZoneY(ctx) != DEFAULT_FOCUS_Y) {
			out.println("<focusy>" + getFocusZoneY(ctx) + "</focusy>");
		}
		if (getTags(ctx).size() > 0) {
			out.println("<tags>" + StringHelper.collectionToString(getTags(ctx), ",") + "</tags>");
		}
		if (!isShared(ctx)) {
			out.println("<shared>false</shared>");
		}
		out.println("</resource>");
		out.flush();
	}

	public void fromXML(ContentContext ctx, NodeXML node) {
		for (NodeXML child : node.getAllChildren()) {
			if (child.getName().equals("title")) {
				setTitle(ctx, child.getContent());
			}
			if (child.getName().equals("description")) {
				setDescription(ctx, child.getContent());
			}
			if (child.getName().equals("location")) {
				setLocation(ctx, child.getContent());
			}
			if (child.getName().equals("copyright")) {
				setCopyright(ctx, child.getContent());
			}
			if (child.getName().equals("focusx")) {
				setFocusZoneX(ctx, Integer.parseInt(child.getContent()));
			}
			if (child.getName().equals("focusy")) {
				setFocusZoneY(ctx, Integer.parseInt(child.getContent()));
			}
			if (child.getName().equals("date")) {
				try {
					setDate(ctx, StringHelper.parseSortableTime(child.getContent()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			if (child.getName().equals("shared")) {
				setShared(ctx, StringHelper.isTrue(child.getContent()));
			}
			if (child.getName().equals("roles")) {
				try {
					for (String role : StringHelper.stringToCollection(child.getContent())) {
						addReadRole(ctx, role);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (child.getName().equals("tags")) {
				try {
					for (String tag : StringHelper.stringToCollection(child.getContent())) {
						addTag(ctx, tag);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public StaticInfo getParent(ContentContext ctx) throws Exception {
		return StaticInfo.getInstance(ctx, file.getParentFile());
	}

	public void resetImageSize(ContentContext ctx) {
		String key = getKey(ctx, "imageSize-" + ctx.getRequestContentLanguage());
		ContentService content = ContentService.getInstance(ctx.getGlobalContext());
		content.removeAttribute(ctx, key);
		imageSize = null;
	}

	public ImageSize getImageSize(ContentContext ctx) {
		if (imageSize == NO_IMAGE_SIZE) {
			return null;
		}
		if (imageSize != null) {
			return imageSize;
		} else {
			try {
				String key = getKey(ctx, "imageSize-" + ctx.getRequestContentLanguage());
				ContentService content = ContentService.getInstance(ctx.getGlobalContext());
				String imageSizeRAW = content.getAttribute(ctx, key, null);
				if (imageSizeRAW != null) {
					ImageSize loadedImageSize = new ImageSize();
					if (loadedImageSize.loadFromString(imageSizeRAW)) {
						imageSize = loadedImageSize;
						return imageSize;
					}
				}
				imageSize = ImageHelper.getImageSize(file);
				content.setAttribute(ctx, key, imageSize.storeToString());
			} catch (Throwable e) {
				logger.fine(e.getMessage());
			}
		}
		if (imageSize == null) {
			imageSize = NO_IMAGE_SIZE;
			return null;
		} else {
			return imageSize;
		}
	}

	@Override
	public Map<String, Object> getContentAsMap(ContentContext ctx) throws Exception {
		if (getFile() == null || !getFile().exists() || !canRead(ctx, ctx.getCurrentUser(), null) || !isShared(ctx)) {
			return Collections.EMPTY_MAP;
		}
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("title", getTitle(ctx));
		data.put("fullTitle", getFullTitle(ctx));
		data.put("fullHtmlTitle", getFullHTMLTitle(ctx));
		data.put("name", getFile().getName());
		data.put("description", getDescription(ctx));
		String authors = getAuthors(ctx);
		if (!StringHelper.isEmpty(authors)) {
			data.put("authors", authors);
		}
		Collection<String> tags = getTags(ctx);
		if (tags.size() > 0) {
			data.put("tags", tags);
		}
		data.put("date", getDate(ctx));
		String location = getLocation(ctx);
		if (!StringHelper.isEmpty(location)) {
			data.put("location", location);
		}
		Collection<String> taxonomy = getTaxonomy(ctx);
		if (taxonomy.size() > 0) {
			data.put("taxonomy", getTaxonomy(ctx));
		}
		String copyright = getCopyright(ctx);
		if (!StringHelper.isEmpty(copyright)) {
			data.put("copyright", copyright);
		}
		data.put("language", getLanguage(ctx));
		data.put("url", getURL(ctx));

		if (getFile().isDirectory()) {
			List<Map<String, Object>> childrenArray = new LinkedList<Map<String, Object>>();
			for (File child : getFile().listFiles()) {
				Map<String, Object> childData = StaticInfo.getInstance(ctx, child).getContentAsMap(ctx);
				if (childData.size() > 0) {
					childrenArray.add(childData);
				}
			}
			data.put("children", childrenArray);
		}

		return data;
	}

}
