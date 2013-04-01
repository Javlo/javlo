package org.javlo.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
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
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.javlo.component.core.ComponentBean;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.DebugHelper;
import org.javlo.helper.DebugHelper.StructureException;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.helper.URLHelper;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.exception.ServiceException;
import org.javlo.servlet.zip.ZipManagement;
import org.javlo.tracking.Track;
import org.javlo.xml.NodeXML;
import org.javlo.xml.XMLFactory;
import org.javlo.ztatic.StaticInfo;
import org.xml.sax.SAXParseException;

public class PersistenceService {

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

	public static SimpleDateFormat persitenceDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private static String KEY = PersistenceService.class.getName();

	private static final String _DIRECTORY = "/persitence";

	private static final String stateFile = "persistence_state.properties";

	private static final String _TRACKING_DIRECTORY = "/persitence/tracking";

	public static final String GLOBAL_MAP_NAME = "global";

	private static int UNDO_DEPTH = 16;

	public static final PersistenceService getInstance(GlobalContext globalContext) throws ServiceException {
		PersistenceService instance = (PersistenceService) globalContext.getAttribute(getKey(globalContext));
		if (instance == null) {
			instance = new PersistenceService();
			globalContext.setAttribute(getKey(globalContext), instance);
			instance.globalContext = globalContext;
			File dir = new File(instance.getDirectory());
			dir.mkdirs();
			dir = new File(instance.getTrackingDirectory());
			dir.mkdirs();
			try {
				instance.loadVersion();
			} catch (IOException e) {
				throw new ServiceException(e.getMessage());
			}
		}
		return instance;
	}

	private static String getKey(GlobalContext globalContext) {
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

	public Integer version = -1;

	protected boolean canRedo = false;

	private GlobalContext globalContext = null;

	public boolean canRedo() {
		return versionExist(version + 1) && canRedo;
	}

	public boolean canUndo() {
		return versionExist(version - 1);
	}

	protected void cleanFile() {

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
			int workVersion = version + 1;
			File file = new File(getDirectory() + "/content_" + ContentContext.PREVIEW_MODE + '_' + workVersion + ".xml");
			while (file.exists()) {
				workVersion++;
				file.delete();
				file = new File(getDirectory() + "/content_" + ContentContext.PREVIEW_MODE + '_' + workVersion + ".xml");
			}
		}
		int workVersion = version - UNDO_DEPTH;
		File file = new File(getDirectory() + "/content_" + ContentContext.PREVIEW_MODE + '_' + workVersion + ".xml");
		while (workVersion > 0) {
			workVersion--;
			if (file.exists()) {
				file.delete();
			}
			file = new File(getDirectory() + "/content_" + ContentContext.PREVIEW_MODE + '_' + workVersion + ".xml");
		}
	}

	public void correctAllFiles() {
		// synchronized (MenuElement.LOCK_ACCESS) {
		File currentPreview = new File(getDirectory() + "/content_" + ContentContext.PREVIEW_MODE + '_' + version + ".xml");
		File currentView = new File(getDirectory() + "/content_" + ContentContext.VIEW_MODE + ".xml");

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
				String timeCode = zip.getName().replaceAll("content_" + ContentContext.VIEW_MODE + ".", "").replaceAll(".xml", "").replaceAll(".zip", "");
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
				String version = file.getName().replaceAll("content_" + ContentContext.PREVIEW_MODE + ".", "").replaceAll(".xml", "").replaceAll(".zip", "");
				int versionInteger = -1;
				try {
					versionInteger = Integer.parseInt(version);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				outList.add(new MetaPersistenceBean(versionInteger, StringHelper.renderSortableTime(new Date(file.lastModified())), "preview"));
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

	protected String getDirectory() {
		String dir = URLHelper.mergePath(globalContext.getDataFolder(), _DIRECTORY);
		return dir;
	}

	private String getTrackingDirectory() {
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

	private Writer getTrackWriter(Calendar cal) throws IOException {
		int year = cal.get(Calendar.YEAR);
		int mount = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		File dir = new File(getTrackingDirectory() + '/' + year + '/' + mount);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(getTrackingDirectory() + '/' + year + '/' + mount + "/tracks-" + day + ".csv");
		if (!file.exists()) {
			file.createNewFile();
		}
		Writer outWriter = new FileWriter(file, true);
		return outWriter;
	}

	public int getVersion() {
		return version;
	}

	public void insertContent(NodeXML pageXML, MenuElement elem, String defaultLg) throws StructureException {

		String pageId = pageXML.getAttributeValue("id", "0");
		DebugHelper.checkStructure(pageId == null, "no id defined in a page node.");

		NodeXML contentNode = pageXML.getChild("component");

		Map<String, String> parentMap = new HashMap<String, String>();

		List<ComponentBean> contentList = new LinkedList<ComponentBean>();

		while (contentNode != null) {
			String id = contentNode.getAttributeValue("id");
			DebugHelper.checkStructure(id == null, "no id defined in a component.");
			String type = contentNode.getAttributeValue("type");
			DebugHelper.checkStructure(type == null, "no type defined in a component.");
			String inlist = contentNode.getAttributeValue("inlist", "false");
			String lg = contentNode.getAttributeValue("language", defaultLg);
			String renderer = contentNode.getAttributeValue("renderer", null);
			boolean isRepeat = false;
			String strRepeat = contentNode.getAttributeValue("repeat", "false");
			if (strRepeat != null) {
				isRepeat = StringHelper.isTrue(strRepeat);
			}
			String authors = contentNode.getAttributeValue("authors", null);

			String parent = parentMap.get(lg);
			if (parent == null) {
				parent = "0";
			}

			String style = contentNode.getAttributeValue("style");

			String content = contentNode.getContent();
			if (content == null) {
				content = "";
			}

			ComponentBean bean = new ComponentBean(id, type, content, lg, isRepeat);
			bean.setStyle(style);
			bean.setList(StringHelper.isTrue(inlist));
			bean.setArea(contentNode.getAttributeValue("area", ComponentBean.DEFAULT_AREA));
			bean.setRenderer(renderer);
			bean.setAuthors(authors);
			contentList.add(bean);

			/*
			 * st.setString(1, pageId); st.setString(2, parent); st.setString(3, type); st.setString(4, content); st.setString(5, id); st.setString(6, lg); if (isRepeat) { st.setInt(7, 1); } else { st.setInt(7, 0); } st.setString(8, style);
			 */

			parentMap.put(lg, id);

			contentNode = contentNode.getNext("component");
		}

		ComponentBean[] elemContent = new ComponentBean[contentList.size()];
		contentList.toArray(elemContent);
		elem.setContent(elemContent);

	}

	public MenuElement insertPage(GlobalContext globalContext, NodeXML pageXML, MenuElement parent, Map<MenuElement, String[]> vparentPreparation, String defaultLg) throws StructureException, ConfigurationException, IOException {

		MenuElement page = MenuElement.getInstance(globalContext);

		String id = pageXML.getAttributeValue("id");
		DebugHelper.checkStructure(id == null, "no id defined in a page node.");
		String name = pageXML.getAttributeValue("name");
		DebugHelper.checkStructure(name == null, "no path defined in a page node.");
		String priority = pageXML.getAttributeValue("priority", "10");
		DebugHelper.checkStructure(priority == null, "no priority defined in a page node.");
		String visible = pageXML.getAttributeValue("visible", "false");
		String https = pageXML.getAttributeValue("https", "false");
		DebugHelper.checkStructure(visible == null, "no visible defined in a page node.");
		String roles = pageXML.getAttributeValue("userRoles", "");
		String layout = pageXML.getAttributeValue("layout", null);

		/* modification management */
		String creator = pageXML.getAttributeValue("creator", "");
		String latestEditor = pageXML.getAttributeValue("latestEditor", "");
		String[] editorRoles = StringHelper.stringToArray(pageXML.getAttributeValue("editor-roles", ""), "#");
		if (editorRoles != null) {
			for (String group : editorRoles) {
				if (group.trim().length() > 0) {
					page.addEditorRoles(group);
				}
			}
		}

		Date creationDate = new Date();
		String creationDateStr = pageXML.getAttributeValue("creationDate");
		if (creationDateStr != null) {
			try {
				creationDate = persitenceDateFormat.parse(creationDateStr);
			} catch (ParseException e) {
				throw new StructureException(e.getMessage());
			}
		}

		Date modificationDate = new Date();
		String modificationDateStr = pageXML.getAttributeValue("modificationDate");
		if (modificationDateStr != null) {
			try {
				modificationDate = persitenceDateFormat.parse(modificationDateStr);
			} catch (ParseException e) {
				throw new StructureException(e.getMessage());
			}
		}

		Date validationDate = new Date();
		String validationDateStr = pageXML.getAttributeValue("validationDate");
		if (validationDateStr != null) {
			try {
				validationDate = persitenceDateFormat.parse(validationDateStr);
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
		page.setHttps(StringHelper.isTrue(https));

		page.setTemplateName(layout);
		page.setUserRoles(new HashSet<String>(StringHelper.stringToCollection(roles, ";")));

		page.setValid(StringHelper.isTrue(pageXML.getAttributeValue("valid", "false")));
		page.setValidater(pageXML.getAttributeValue("validater", ""));
		page.setValidationDate(validationDate);
		page.setBlocked(StringHelper.isTrue(pageXML.getAttributeValue("blocked", "false")));
		page.setBlocker(pageXML.getAttributeValue("blocker", ""));

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

		insertContent(pageXML, page, defaultLg);

		parent.addChildMenuElement(page);

		NodeXML childPage = pageXML.getChild("page");
		while (childPage != null) {
			insertPage(globalContext, childPage, page, vparentPreparation, defaultLg);
			childPage = childPage.getNext("page");
		}

		return page;
	}

	private LoadingBean load(ContentContext ctx, InputStream in, Map<String, String> contentAttributeMap, int renderMode) throws ServiceException {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		MenuElement root = MenuElement.getInstance(globalContext);

		root.setValid(true);

		LoadingBean outBean = new LoadingBean();

		try {
			NodeXML firstNode = XMLFactory.getFirstNode(in);

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
				root.setHttps(StringHelper.isTrue(page.getAttributeValue("https", "false")));
				root.setCreator(page.getAttributeValue("creator", ""));
				root.setLatestEditor(page.getAttributeValue("latestEditor", ""));
				root.setValid(StringHelper.isTrue(page.getAttributeValue("valid", "false")));
				root.setValidater(page.getAttributeValue("validater", ""));
				root.setBlocked(StringHelper.isTrue(page.getAttributeValue("blocked", "false")));
				root.setBlocker(page.getAttributeValue("blocker", ""));
				root.setLinkedURL(page.getAttributeValue("linked-url", ""));
				root.setBreakRepeat(StringHelper.isTrue(page.getAttributeValue("breakrepeat", "false")));
				root.setShortURL(page.getAttributeValue("shorturl", null));

				String[] editorRoles = StringHelper.stringToArray(page.getAttributeValue("editor-roles", ""), "#");
				if (editorRoles != null) {
					for (String roles : editorRoles) {
						if (roles.trim().length() > 0) {
							root.addEditorRoles(roles);
						}
					}
				}

				String reversedLink = page.getAttributeValue("reversed-link", "");
				reversedLink = StringHelper.writeLines(StringHelper.stringToArray(reversedLink, "#"));
				root.setReversedLink(reversedLink);

				root.setTemplateName(page.getAttributeValue("layout"));

				String creationDate = page.getAttributeValue("creationDate");
				if (creationDate == null) {
					root.setCreationDate(new Date());
				} else {
					root.setCreationDate(persitenceDateFormat.parse(creationDate));
				}

				String modificationDate = page.getAttributeValue("modificationDate");
				if (modificationDate == null) {
					root.setModificationDate(new Date());
				} else {
					root.setModificationDate(persitenceDateFormat.parse(modificationDate));
				}

				String validationDate = page.getAttributeValue("validationDate", "");
				if (validationDate.trim().length() == 0) {
					root.setValidationDate(null);
				} else {
					root.setValidationDate(persitenceDateFormat.parse(validationDate));
				}

				insertContent(page, root, defaultLg);
				page = page.getChild("page");

				Map<MenuElement, String[]> vparentPreparation = new HashMap<MenuElement, String[]>();

				while (page != null) {
					insertPage(globalContext, page, root, vparentPreparation, defaultLg);
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

			NodeXML properties = firstNode.getChild("properties");
			if (properties != null && properties.getAttributeValue("name", "").equals("global")) {
				NodeXML property = properties.getChild("property");
				// Content content = Content.createContent(ctx.getRequest());
				while (property != null) {
					contentAttributeMap.put(property.getAttributeValue("key"), property.getContent());
					property = property.getNext("property");
				}
			}

		} catch (SAXParseException e) {
			e.printStackTrace();
			MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage("error XML parsing (line:" + e.getLineNumber() + " col:" + e.getColumnNumber() + "): " + e.getMessage(), GenericMessage.ERROR));
			root.setId("0");
			root.setName("root");
			root.setPriority(10);
			root.setVisible(true);
		} catch (Exception e) {
			MessageRepository.getInstance(ctx).setGlobalMessageAndNotification(ctx, new GenericMessage("error XML loading : " + e.getMessage(), GenericMessage.ERROR));
			root.setId("0");
			root.setName("root");
			root.setPriority(10);
			root.setVisible(true);
		}

		outBean.setRoot(root);

		return outBean;

	}

	public MenuElement load(ContentContext ctx, int renderMode, Map<String, String> contentAttributeMap, Date timeTravelDate) throws Exception {
		synchronized (ctx.getGlobalContext()) {
			synchronized (PersistenceThread.LOCK) {

				loadVersion();

				logger.info("load version : " + version + " in mode : " + renderMode);

				MenuElement root;
				InputStream in = null, in2 = null;
				try {

					if (timeTravelDate != null) {
						// An other render mode than VIEW_MODE is not supported with a timeTravelDate.
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
							in2 = new FileInputStream(minBackup.getKey());
							ZipInputStream zip = new ZipInputStream(in2);
							ZipEntry entry = zip.getNextEntry();
							while (entry != null) {
								if (ResourceHelper.getFile(entry.getName()).equals("content_" + ContentContext.VIEW_MODE + ".xml")) {
									in = zip;
									break;
								}
								entry = zip.getNextEntry();
							}
						}
					}
					if (in == null) {
						File file;
						if (renderMode == ContentContext.PREVIEW_MODE) {
							file = new File(getDirectory() + "/content_" + renderMode + '_' + version + ".xml");
						} else {
							file = new File(getDirectory() + "/content_" + renderMode + ".xml");
						}
						if (file.exists()) {
							in = new FileInputStream(file);
						}
					}

					if (in == null) {
						root = MenuElement.getInstance(globalContext);
						root.setName("root");
						root.setVisible(true);
						root.setPriority(1);
						root.setId("0");
						/*
						 * file.createNewFile(); BufferedWriter out = new BufferedWriter(new FileWriter(file)); out.write("<content version=\"" + version + "\"><page id=\"0\" name=\"root\" priority=\"1\" visible=\"true\" userRoles=\"\" /></content>" ); out.close();
						 */
					} else {
						LoadingBean loadBean = load(ctx, in, contentAttributeMap, renderMode);
						root = loadBean.getRoot();
						ConvertToCurrentVersion.convert(ctx, loadBean);

						/** load linked content **/
						/*
						 * MenuElement[] children = root.getAllChilds(); root.updateLinkedData(ctx); for (MenuElement page : children) { page.updateLinkedData(ctx); }
						 */
					}

				} finally {
					ResourceHelper.closeResource(in);
					ResourceHelper.closeResource(in2);
				}

				return root;
			}
		}
	}

	/**
	 * load current version of preview content.
	 * 
	 * @return the current version
	 * @throws IOException
	 */
	public int loadVersion() throws IOException {
		synchronized (version) {
			File propFile = new File(getDirectory() + '/' + stateFile);
			if (propFile.exists()) {
				Properties prop = new Properties();
				InputStream in = new FileInputStream(propFile);
				prop.load(in);
				in.close();
				version = Integer.parseInt(prop.getProperty("version", "1"));
			} else { // set default value
				version = 1;
			}
		}
		return version;
	}

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
							if (trackInfo.length > 5) {
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
								if (trackInfo.length > 7) {
									track.setUserAgent(trackInfo[7]);
								}
								if (onlyResource) {
									if (!track.getPath().startsWith("/view/") && !track.getPath().startsWith("/preview/") && !track.getPath().startsWith("/edit/") && !track.getPath().startsWith("/ajax/")) {
										if (track.getUserAgent() == null || !track.getUserAgent().toLowerCase().contains("bot")) {
											track.setPath(URLHelper.removeTemplateFromResourceURL(track.getPath()));
											Calendar trackCal = Calendar.getInstance();
											trackCal.setTimeInMillis(track.getTime());
											if (calFrom.before(trackCal) && calTo.after(trackCal)) {
												outCol.add(track);
												countTrack++;
											}
										}
									}
								} else if (!onlyViewClick) {
									if (track.getUserAgent() == null || !track.getUserAgent().toLowerCase().contains("bot")) {
										Calendar trackCal = Calendar.getInstance();
										trackCal.setTimeInMillis(track.getTime());
										if (calFrom.before(trackCal) && calTo.after(trackCal)) {
											outCol.add(track);
											countTrack++;
										}
									}
								} else {
									if ((track.getAction() == null) || (track.getAction().equals(Track.UNDEFINED_ACTION))) {
										if (track.getPath() != null && track.getPath().contains("/view")) {
											if (track.getUserAgent() == null || !track.getUserAgent().toLowerCase().contains("bot")) {
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
		File previewFile = new File(getDirectory() + "/content_" + ContentContext.PREVIEW_MODE + '_' + version + ".xml");
		File file = new File(getDirectory() + "/content_" + ContentContext.VIEW_MODE + ".xml");
		if (file.exists()) {
			file.delete();
		}
		FileUtils.copyFile(previewFile, file);
	}

	public void redo() {
		if (canRedo()) {
			version++;
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
			this.version = version;
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
		try {
			File propFile = new File(getDirectory() + '/' + stateFile);
			if (!propFile.exists()) {
				propFile.createNewFile();
			}
			Properties prop = new Properties();
			prop.setProperty("version", "" + version);
			OutputStream out = new FileOutputStream(propFile);
			prop.store(out, "WCMS PERSISTENCE STATE");
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void store(ContentContext ctx) throws Exception {
		store(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), ContentContext.PREVIEW_MODE);
	}

	public void store(ContentContext ctx, int renderMode) throws Exception {

		synchronized (ctx.getGlobalContext()) {

			logger.info("store in " + renderMode + " mode.");

			PersistenceThread persThread = new PersistenceThread();
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

			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			persThread.setDataFolder(globalContext.getDataFolder());

			StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext());
			if (StaticInfo._STATIC_INFO_DIR != null) {
				String staticInfo = URLHelper.mergePath(globalContext.getDataFolder(), StaticInfo._STATIC_INFO_DIR);
				persThread.addFolderToSave(new File(staticInfo));
			}
			persThread.addFolderToSave(new File(URLHelper.mergePath(globalContext.getDataFolder(), staticConfig.getUserInfoFile())));

			// synchronized (MenuElement.LOCK_ACCESS) {
			persThread.start();
			// }

		}
	}

	public void store(InputStream in) throws Exception {
		// synchronized (MenuElement.LOCK_ACCESS) {
		version++;
		saveVersion();
		File file = new File(getDirectory() + "/content_" + ContentContext.PREVIEW_MODE + '_' + version + ".xml");
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

	public void store(Track track) throws Exception {
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeInMillis(track.getTime());
		Writer trackWriter = getTrackWriter(cal);
		BufferedWriter writer = new BufferedWriter(trackWriter);
		writer.write(trackToString(track));
		writer.newLine();
		releaseTrackWriter(writer);
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
			version--;
			File file = new File(getDirectory() + "/content_" + ContentContext.PREVIEW_MODE + ".xml");
			if (file.exists()) {
				file.delete();
			}
			saveVersion();
		}
	}

	/*
	 * public static void main(String[] args) { File file = new File("C:/Apache/Tomcat 6.0/webapps/dc/WEB-INF/data-ctx/ctx-121395557182868827380/persitence/content_3_2.xml");
	 * 
	 * int read; try { InputStream in = new FileInputStream(file); StringBuffer outFile = new StringBuffer(); read = in.read(); int pos = 0; boolean error = false; while (read >= 0) { if (read == 0) { error = true; } else { outFile.append((char) read); } Character character = new Character((char) read); Charset charset = Charset.forName(ContentContext.CHARACTER_ENCODING); ByteBuffer buf = ByteBuffer.allocate(4); buf.put(("" + character).getBytes()); charset.decode(buf); pos++; read = in.read(); } if (error) { FileUtils.writeStringToFile(file, outFile.toString()); } in.close(); } catch (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); } System.out.println("end."); }
	 */

	private boolean versionExist(int version) {
		File file = new File(getDirectory() + "/content_" + ContentContext.PREVIEW_MODE + '_' + version + ".xml");
		return file.exists();
	}
}
