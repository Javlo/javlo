package org.javlo.service;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.javlo.context.ContentContext;
import org.javlo.data.taxonomy.TaxonomyBean;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XMLHelper;
import org.javlo.io.SecureFile;
import org.javlo.navigation.MenuElement;
import org.javlo.servlet.IVersion;
import org.javlo.servlet.zip.ZipManagement;
import org.javlo.utils.TimeTracker;

import jakarta.mail.internet.AddressException;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;

public class PersistenceThread implements Runnable {

	public static boolean ONE_INSTANCE_RUN = false;

	private AtomicInteger COUNT_THREAD = new AtomicInteger(0);

	private static Logger logger = Logger.getLogger(PersistenceThread.class.getName());

	private MenuElement menuElement;

	public Object lockReference = null;

	private Map<String, String> globalContentMap;

	private TaxonomyBean taxonomyRoot;

	private final Collection<File> folderToSave = new LinkedList<File>();

	private String dataFolder = "";

	private int mode;

	private boolean running = true;

	private String defaultLg = null;

	private String contextKey = null;
	
	private String encryptKey = null;
	
	private boolean zipStorage = false;

	private PersistenceService persistenceService;

	public static final Object LOCK = new Object();

	public void addFolderToSave(File file) {
		folderToSave.add(file);
	}

	public String getDataFolder() {
		return dataFolder;
	}

	public String getDefaultLg() {
		return defaultLg;
	}

	public TaxonomyBean getTaxonomyRoot() {
		return taxonomyRoot;
	}

	public void setTaxonomyRoot(TaxonomyBean taxonomyRoot) {
		this.taxonomyRoot = taxonomyRoot;
	}

	public Map<String, String> getGlobalContentMap() {
		return globalContentMap;
	}

	public MenuElement getMenuElement() {
		return menuElement;
	}

	public int getMode() {
		return mode;
	}

	public PersistenceService getPersistenceService() {
		return persistenceService;
	}

	public void start(boolean async) {
		if (async) {
			Thread thread = new Thread(this, this.getClass().getSimpleName());
			thread.start();
		} else {
			run();
		}
	}

	@Override
	public void run() {
		int timeTrackerNumber = TimeTracker.start(contextKey, PersistenceThread.class.getName());
		COUNT_THREAD.incrementAndGet();
		// synchronized (SYNCRO_LOCK) {
		File file = null;
		try {
			logger.info("before start persitence thread (#THREAD:" + COUNT_THREAD + ") - " + getContextKey());
			synchronized (lockReference) {
				logger.info("start persitence thread (#THREAD:" + COUNT_THREAD + ')');
				long startTime = System.currentTimeMillis();
				file = store(menuElement, mode, getDefaultLg());
				logger.info("end persitence thread (" + StringHelper.renderTimeInSecond(System.currentTimeMillis() - startTime) + " sec.). #file=" + StringHelper.renderSize(file.length()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				persistenceService.sendPersistenceErrorToAdministrator("Error in PersistanceThread.", file, e);
			} catch (AddressException e1) {
				e1.printStackTrace();
			}
		} finally {
			running = false;
			COUNT_THREAD.decrementAndGet();
		}
		// }
		TimeTracker.end(contextKey, PersistenceThread.class.getName(), timeTrackerNumber);
	}

	public boolean isRunning() {
		return running;
	}

	public void setDataFolder(String dataFolder) {
		this.dataFolder = dataFolder;
	}

	public void setDefaultLg(String defaultLg) {
		this.defaultLg = defaultLg;
	}

	public void setGlobalContentMap(Map<String, String> globalContentMap) {
		Map<String, String> localMap = new Hashtable<String, String>();
		localMap.putAll(globalContentMap);
		this.globalContentMap = localMap;
	}

	public void setMenuElement(MenuElement menuElement) {
		this.menuElement = menuElement;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public void setPersistenceService(PersistenceService persistenceService) {
		this.persistenceService = persistenceService;
	}

	protected File getXMLPersistenceFile(int mode) {
		return persistenceService.getXMLPersistenceFile(mode);
	}

	private File store(MenuElement menuElement, int renderMode, String defaultLg) throws Exception {
		if (menuElement == null) {
			logger.warning("no navigation found.");
			return null;
		}
		int localVersion = persistenceService.getVersion();
		if (renderMode == ContentContext.PREVIEW_MODE) {
			localVersion = persistenceService.getVersion() + 1;
			persistenceService.canRedo = false;
		}
		File file;
		if (renderMode == ContentContext.PREVIEW_MODE) {
			file = persistenceService.getXMLPersistenceFile(ContentContext.PREVIEW_MODE, localVersion);
			// file = new File(persistenceService.getDirectory() + "/content_" +
			// ContentContext.PREVIEW_MODE + '_' + localVersion + ".xml");
			OutputStream fileStream;
			if (!isZipStorage()) {
				if (!file.exists()) {
					file.getParentFile().mkdirs();
					file.createNewFile();
				}
				fileStream = new FileOutputStream(file);
			} else {
				fileStream = new ByteArrayOutputStream();
			}
			OutputStreamWriter fileWriter = new OutputStreamWriter(fileStream, ContentContext.CHARACTER_ENCODING);
			try {
				XMLHelper.storeXMLContent(fileWriter, menuElement, renderMode, localVersion, getGlobalContentMap(), getTaxonomyRoot());
				if (isZipStorage()) {
					SecureFile.createCyptedFile(file, getEncryptKey(), new ByteArrayInputStream(((ByteArrayOutputStream)fileStream).toByteArray()));
				}
			} finally {
				fileWriter.close();
				fileStream.close();
			}
			if (PersistenceService.STORE_DATA_PROPERTIES) {
				Properties prop = new Properties();
				prop.putAll(getGlobalContentMap());
				File propStorageFile = new File(persistenceService.getPersistenceFilePrefix(renderMode) + '_' + localVersion + ".properties");
				Writer wrt = new FileWriterWithEncoding(propStorageFile, ContentContext.CHARACTER_ENCODING);
				try {
					prop.store(wrt, "Javlo Version : " + IVersion.VERSION);
				} finally {
					wrt.close();
				}
			}
			persistenceService.setVersion(persistenceService.getVersion() + 1);
			persistenceService.cleanFile();
		} else {
			file = getXMLPersistenceFile(renderMode);
			// file = new File(persistenceService.getDirectory() + "/content_" +
			// renderMode + ".xml");
			if (file.exists()) {
				storeCurrentView();
				file = getXMLPersistenceFile(renderMode);
				// file = new File(persistenceService.getDirectory() +
				// "/content_" + renderMode + ".xml");
			}
			file.createNewFile();
			FileOutputStream fileStream = new FileOutputStream(file);
			OutputStreamWriter fileWriter = new OutputStreamWriter(fileStream, ContentContext.CHARACTER_ENCODING);
			try {
				XMLHelper.storeXMLContent(fileWriter, menuElement, renderMode, localVersion, getGlobalContentMap(), getTaxonomyRoot());
			} finally {
				fileWriter.close();
				fileStream.close();
			}
		}
		return file;

	}

	void storeCurrentView() throws IOException {
		File file = getXMLPersistenceFile(ContentContext.VIEW_MODE);
		// File file = new File(persistenceService.getDirectory() + "/content_"
		// + ContentContext.VIEW_MODE + ".xml");
		File zipFile = new File(persistenceService.getPersistenceFilePrefix(ContentContext.VIEW_MODE) + '.' + StringHelper.renderFileTime(new Date()) + ".xml.zip");
		OutputStream out = new FileOutputStream(zipFile);
		ZipOutputStream outZip = new ZipOutputStream(out);
		ZipManagement.zipFile(outZip, file, new File(persistenceService.getDirectory()));
		for (File fileToSave : folderToSave) {
			try {
				ZipManagement.zipFile(outZip, fileToSave, new File(getDataFolder()));
			} catch (Throwable t) {
				logger.warning(t.getMessage());
			}
		}
		outZip.close();
		out.close();
		file.delete();
	}

	public String getContextKey() {
		return contextKey;
	}

	public void setContextKey(String contextKey) {
		this.contextKey = contextKey;
	}

	public PersistenceThread(Object lockReference) {
		this.lockReference = lockReference;
	}

	private String getEncryptKey() {
		return encryptKey;
	}

	public void setEncryptKey(String encryptKey) {
		this.encryptKey = encryptKey;
	}

	private boolean isZipStorage() {
		return zipStorage;
	}

	public void setZipStorage(boolean zipStorage) {
		this.zipStorage = zipStorage;
	}

}
