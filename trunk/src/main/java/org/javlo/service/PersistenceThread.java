package org.javlo.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipOutputStream;

import javax.mail.internet.AddressException;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XMLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.servlet.zip.ZipManagement;

public class PersistenceThread extends Thread {
	
	private int COUNT_THREAD = 0;

	private static Logger logger = Logger.getLogger(PersistenceThread.class.getName());

	private MenuElement menuElement;

	private Map<String, String> globalContentMap;

	private final Collection<File> folderToSave = new LinkedList<File>();

	private String dataFolder = "";

	private int mode;

	private boolean running = true;

	private String defaultLg = null;

	private PersistenceService persistenceService;

	public void addFolderToSave(File file) {
		folderToSave.add(file);
	}

	public String getDataFolder() {
		return dataFolder;
	}

	public String getDefaultLg() {
		return defaultLg;
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

	@Override
	public void run() {		
		COUNT_THREAD++;
		File file = null;
		try {
			logger.info("before start persitence thread (#THREAD:"+COUNT_THREAD+')');
			synchronized (menuElement.getLock()) {
				logger.info("start persitence thread (#THREAD:"+COUNT_THREAD+')');
				long startTime = System.currentTimeMillis();
				logger.info("store persitence thread");		
				file = store(menuElement, mode, getDefaultLg());
				logger.info("end persitence thread ("+StringHelper.renderTimeInSecond(System.currentTimeMillis()-startTime)+" sec.).");
			}			
		} catch (Exception e) {
			e.printStackTrace();
			try {
				persistenceService.sendPersistenceErrorToAdministrator("Error in PersistanceThread.", file, e);
			} catch (AddressException e1) {
				logger.warning(e1.getMessage());
			}
		} finally {
			synchronized (this) {
				persistenceService.resetThread();
				running = false;
				COUNT_THREAD--;
				notify();
			}
		}
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

	private File store(MenuElement menuElement, int renderMode, String defaultLg) throws Exception {

		if (menuElement == null) {
			return null;
		}
		// don't save empty site
		if ((menuElement.getAllChildren().length > 0) || (menuElement.getContent().length > 0)) {

			int localVersion = persistenceService.getVersion();

			if (renderMode == ContentContext.PREVIEW_MODE) {
				localVersion = persistenceService.getVersion() + 1;
				persistenceService.canRedo = false;

			}
			File file;
			if (renderMode == ContentContext.PREVIEW_MODE) {
				file = new File(persistenceService.getDirectory() + "/content_" + ContentContext.PREVIEW_MODE + '_' + localVersion + ".xml");
				if (!file.exists()) {
					file.createNewFile();
				}

				FileOutputStream fileStream = new FileOutputStream(file);
				OutputStreamWriter fileWriter = new OutputStreamWriter(fileStream, ContentContext.CHARACTER_ENCODING);
				try {
					XMLHelper.storeXMLContent(fileWriter, menuElement, renderMode, localVersion, defaultLg, getGlobalContentMap());
				} finally {
					fileWriter.close();
					fileStream.close();
				}
				persistenceService.setVersion(persistenceService.getVersion()+1);				
				persistenceService.cleanFile();
			} else {
				file = new File(persistenceService.getDirectory() + "/content_" + renderMode + ".xml");
				if (file.exists()) {
					storeCurrentView();
					file = new File(persistenceService.getDirectory() + "/content_" + renderMode + ".xml");
				}
				file.createNewFile();
				FileOutputStream fileStream = new FileOutputStream(file);
				OutputStreamWriter fileWriter = new OutputStreamWriter(fileStream, ContentContext.CHARACTER_ENCODING);
				try {
					XMLHelper.storeXMLContent(fileWriter, menuElement, renderMode, localVersion, defaultLg, getGlobalContentMap());
				} finally {
					fileWriter.close();
					fileStream.close();
				}
			}
			return file;
		}
		return null;
	}

	void storeCurrentView() throws IOException {
		File file = new File(persistenceService.getDirectory() + "/content_" + ContentContext.VIEW_MODE + ".xml");

		File zipFile = new File(persistenceService.getDirectory() + "/content_" + ContentContext.VIEW_MODE + "." + StringHelper.renderFileTime(new Date()) + ".xml.zip");

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

}
