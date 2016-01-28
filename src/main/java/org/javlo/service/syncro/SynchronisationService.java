package org.javlo.service.syncro;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.PartBase;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.servlet.SynchronisationServlet;
import org.javlo.utils.DebugListening;

@Deprecated
public class SynchronisationService {

	/**
	 * create a static logger.
	 */
	public static Logger logger = Logger.getLogger(SynchronisationService.class.getName());

	private static final String RESOURCES_DESCRIPTION_URI = "/synchro/";

	private URL serverURL = null;

	private String synchroCode = "";

	private File baseFolderFile = null;

	private String URI_PREFIX = "";

	private boolean deleteIntraAfterTransfert = false;

	private boolean deleteDMZItNotFoundIntra = false;

	private boolean pushOnDMZ = true;

	private boolean downloadFromDMZ = true;

	private boolean refreshDMZContent = false;
	
	private boolean fullRefreshDMZContent = false;

	private final boolean uploadPreviewFile = true;

	private String proxyHost = null;

	private int proxyPort = -1;

	private String getURL() {
		String outURL = "" + serverURL;
		outURL = URLHelper.mergePath(outURL, RESOURCES_DESCRIPTION_URI);
		outURL = URLHelper.mergePath(outURL, URI_PREFIX);
		outURL = URLHelper.addRawParam(outURL, SynchronisationServlet.SHYNCRO_CODE_PARAM_NAME, synchroCode);
		return outURL;
	}

	public boolean sendCommand(String paramName, String paramValue) {

		logger.fine("send command : " + paramName + " = " + paramValue);

		InputStream in = null;

		try {
			paramValue = URLEncoder.encode(paramValue, ContentContext.CHARACTER_ENCODING);
			URL url = new URL(URLHelper.addRawParam(getURL(), paramName, paramValue));
			URLConnection conn = url.openConnection();
			in = conn.getInputStream();
			in.read();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			ResourceHelper.closeResource(in);
		}
		return true;
	}

	public static SynchronisationService getInstance(URL serverURL, String proxyHost, int proxyPort, String synchroCode, String baseFolderFile) {
		SynchronisationService outService = new SynchronisationService();
		outService.serverURL = serverURL;
		outService.baseFolderFile = new File(baseFolderFile);
		outService.refreshDMZContent = true;
		outService.downloadFromDMZ = false;
		outService.deleteDMZItNotFoundIntra = true;
		outService.synchroCode = synchroCode;
		outService.proxyHost = proxyHost;
		outService.proxyPort = proxyPort;
		return outService;
	}

	public static SynchronisationService getLocalFolderInstance(URL serverURL, String proxyHost, int proxyPort, String synchroCode, String baseFolderFile) {
		SynchronisationService outService = new SynchronisationService();
		outService.serverURL = serverURL;
		outService.baseFolderFile = new File(baseFolderFile);
		outService.refreshDMZContent = true;
		outService.downloadFromDMZ = true;
		outService.deleteDMZItNotFoundIntra = false;
		outService.synchroCode = synchroCode;
		outService.proxyHost = proxyHost;
		outService.proxyPort = proxyPort;
		return outService;
	}

	public static SynchronisationService getInstanceForMailing(URL serverURL, String proxyHost, int proxyPort, String synchroCode, String baseFolderFile) {
		SynchronisationService outService = new SynchronisationService();
		outService.serverURL = serverURL;
		outService.baseFolderFile = new File(baseFolderFile);
		outService.URI_PREFIX = SynchronisationServlet.MAILING_PREFIX;
		outService.deleteIntraAfterTransfert = true;
		outService.synchroCode = synchroCode;
		outService.proxyHost = proxyHost;
		outService.proxyPort = proxyPort;
		outService.downloadFromDMZ = false;

		return outService;
	}

	public static SynchronisationService getInstanceForMailingHistory(URL serverURL, String proxyHost, int proxyPort, String synchroCode, String baseFolderFile) {
		SynchronisationService outService = new SynchronisationService();
		outService.serverURL = serverURL;
		outService.baseFolderFile = new File(baseFolderFile);
		outService.URI_PREFIX = SynchronisationServlet.MAILING_HISTORY_PREFIX;
		outService.synchroCode = synchroCode;
		outService.proxyHost = proxyHost;
		outService.proxyPort = proxyPort;
		outService.pushOnDMZ = false;

		return outService;
	}

	public static SynchronisationService getInstanceForTemplate(URL serverURL, String proxyHost, int proxyPort, String synchroCode, String baseFolderFile) {
		SynchronisationService outService = new SynchronisationService();
		outService.serverURL = serverURL;
		outService.baseFolderFile = new File(baseFolderFile);
		outService.URI_PREFIX = SynchronisationServlet.TEMPLATE_PREFIX;
		outService.fullRefreshDMZContent = true;
		outService.deleteDMZItNotFoundIntra = true;
		outService.synchroCode = synchroCode;
		outService.proxyHost = proxyHost;
		outService.proxyPort = proxyPort;
		outService.downloadFromDMZ = false;
		return outService;
	}

	public static SynchronisationService getInstanceForMailingTemplate(URL serverURL, String proxyHost, int proxyPort, String synchroCode, String baseFolderFile) {
		SynchronisationService outService = new SynchronisationService();
		outService.serverURL = serverURL;
		outService.baseFolderFile = new File(baseFolderFile);
		outService.fullRefreshDMZContent = true;
		outService.URI_PREFIX = SynchronisationServlet.TEMPLATE_MAILING_PREFIX;
		outService.deleteDMZItNotFoundIntra = true;
		outService.synchroCode = synchroCode;
		outService.proxyHost = proxyHost;
		outService.proxyPort = proxyPort;
		outService.downloadFromDMZ = false;
		return outService;
	}

	public static SynchronisationService getInstanceForShareFiles(URL serverURL, String proxyHost, int proxyPort, String synchroCode, String baseFolderFile) {
		SynchronisationService outService = new SynchronisationService();
		outService.serverURL = serverURL;
		outService.baseFolderFile = new File(baseFolderFile);
		outService.URI_PREFIX = SynchronisationServlet.SHARE_PREFIX;
		outService.deleteDMZItNotFoundIntra = true;
		outService.downloadFromDMZ = false;
		outService.synchroCode = synchroCode;
		outService.proxyHost = proxyHost;
		outService.proxyPort = proxyPort;
		return outService;
	}

	private boolean pushFile(FileInfo fileInfo, PrintWriter errorReport) throws IOException {

		if (!pushOnDMZ) {
			return true;
		}

		if (!uploadPreviewFile && ResourceHelper.isPreviewFile(fileInfo.getPath())) {
			return true;
		}

		logger.fine("push file " + fileInfo);

		if (fileInfo.isDirectory()) { // no syncro directory
			return true;
		}

		File targetFile = new File(URLHelper.mergePath(baseFolderFile.getAbsolutePath(), fileInfo.getPath()));

		String targetURL = URLHelper.mergePath(getURL(), URLHelper.path2URL(fileInfo.getPath()));

		PostMethod filePost = null;
		boolean filedeleted = false;
		try {
			String finalURL = URLHelper.addRawParam(targetURL, "checksum", "" + fileInfo.getChecksum());
			filePost = new PostMethod(finalURL);
			PartBase[] parts = { new FilePart(targetFile.getName(), targetFile) };
			filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
			HttpClient client = new HttpClient();
			if (proxyPort > 0) {
				client.getHostConfiguration().setProxy(proxyHost, proxyPort);
			}
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			int status = client.executeMethod(filePost);
			if (status == HttpStatus.SC_OK) {
				if (deleteIntraAfterTransfert) {
					filedeleted = ResourceHelper.deleteFileAndParentDir(targetFile);
				}
				return true;
			} else {
				logger.warning("Upload '" + fileInfo.getPath() + "' failed, response=" + HttpStatus.getStatusText(status));
				errorReport.println("Upload '" + fileInfo.getPath() + "' failed, response=" + HttpStatus.getStatusText(status));
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.warning("error with : " + fileInfo.getPath() + " message : " + ex.getMessage());
			errorReport.println("Upload '" + fileInfo.getPath() + "' failed, message=" + ex.getMessage());
			return false;
		} finally {
			if (filePost != null) {
				filePost.releaseConnection();
			}
			if (!filedeleted && deleteIntraAfterTransfert) { // if file not
																// local deleted
																// -> delete on
																// the
				// file on DMZ.
				deleteFile(fileInfo, errorReport);
			}
		}
	}

	private boolean downloadFile(FileInfo fileInfo, File localFile, PrintWriter errorReport) throws IOException {

		if (!downloadFromDMZ) {
			return true;
		}

		logger.fine("download file " + fileInfo);

		if (fileInfo.isDirectory()) { // no syncro directory
			return true;
		}

		String targetURL = URLHelper.mergePath(getURL(), URLHelper.path2URL(fileInfo.getPath()));

		URL url = new URL(targetURL);
		InputStream in = null;
		try {
			URLConnection conn = url.openConnection();
			in = conn.getInputStream();
			if (!localFile.getParentFile().exists()) {
				localFile.getParentFile().mkdirs();
			}
			ResourceHelper.writeStreamToFile(in, localFile);
			String localChecksum = ResourceHelper.computeChecksum(localFile);
			if (!ResourceHelper.checksumEquals(localChecksum, fileInfo.getChecksum())) {
				logger.warning("Download error : '" + fileInfo.getPath() + "' not same checksum [size source:" + fileInfo.getSize() + " size target:" + localFile.length() + "]");
				errorReport.println("Download error : '" + fileInfo.getPath() + "' not same checksum");
				localFile.delete();
				return false;
			}
		} catch (Exception ex) {
			// ex.printStackTrace();
			logger.warning("error with : " + fileInfo.getPath() + " message : " + ex.getMessage());
			errorReport.println("Download '" + fileInfo.getPath() + "' failed, message=" + ex.getMessage());
			localFile.delete();
			return false;
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return true;
	}

	private boolean deleteFile(FileInfo fileInfo, PrintWriter errorReport) throws IOException {

		logger.fine("delete file in DMZ : " + fileInfo);

		if (fileInfo.isDirectory()) { // no syncro directory
			return true;
		}

		String targetURL = URLHelper.mergePath(getURL(), URLHelper.path2URL(fileInfo.getPath()));

		URL url = new URL(URLHelper.addRawParam(targetURL, "delete", "true"));
		InputStream in = null;
		try {
			URLConnection conn = url.openConnection();
			conn.connect();
			in = conn.getInputStream();
			in.read();
			return true;
		} catch (Exception ex) {
			// ex.printStackTrace();
			logger.warning("error with : " + fileInfo.getPath() + " message : " + ex.getMessage());
			errorReport.println("Download '" + fileInfo.getPath() + "' failed, message=" + ex.getMessage());
			return false;
		} finally {
			ResourceHelper.closeResource(in);
		}
	}

	public void pushContext(String context) {

		// System.out.println("******** context **********");
		// System.out.println(context);
		// System.out.println("***************************");

		sendCommand("context", context);
	}

	public void syncroResource() throws IOException {
		syncroResource(null);
	}

	public String syncroResource(String previousResult) throws IOException {
		
		if (serverURL == null) {
			throw new NullPointerException("serverURL is null");
		}

		logger.fine("start synchronisation with : " + serverURL + "   (URI_PREFIX=" + URI_PREFIX + ").");

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		boolean noError = true;
		boolean needRefresh = false;
		try {

			out.println("Error on syncronisation.");
			out.println("time : " + StringHelper.renderTime(new Date()));
			out.println("serverURL : " + serverURL);
			out.println("");

			// Load structure from the previous sync
			Properties savedProp = null;
			if (previousResult != null) {
				savedProp = new Properties();
				savedProp.load(new StringReader(previousResult));
			}

			// Load structure from the distant folder (DMZ)
			URL workURL = new URL(URLHelper.mergePath(getURL(), SynchronisationServlet.FILE_INFO));
			previousResult = ResourceHelper.downloadResourceAsString(workURL);
			Properties dmzProp = new Properties();
			dmzProp.load(new StringReader(previousResult));

			// Load structure from the locale folder
			Properties intraProp = new Properties();
			String intraPropertyContent = FileStructureFactory.getInstance(baseFolderFile).fileTreeToProperties();
			ByteArrayInputStream in = new ByteArrayInputStream(intraPropertyContent.getBytes());
			intraProp.load(in);
			ResourceHelper.closeResource(in);

			Enumeration<Object> keys;
			
			// Browsing intra structure
			keys = intraProp.keys();
			while (keys.hasMoreElements()) {
				String intraKey = (String) keys.nextElement();
				String intraFileInfoStr = intraProp.getProperty(intraKey);
				FileInfo intraFileInfo = new FileInfo(intraFileInfoStr);
				FileInfo dmzFileInfo = null;
				if (dmzProp.get(intraKey) == null) {
					// Intra = Exist + DMZ = Unknown > push
					if (!pushFile(intraFileInfo, out)) {
						noError = false;
					}
				} else {
					dmzFileInfo = new FileInfo(dmzProp.getProperty(intraKey));
					if (dmzFileInfo.isDeleted()) {
						File realFile = new File(baseFolderFile, intraFileInfo.getPath());
						if (savedProp == null) {
							// Intra = Exist, DMZ = Deleted, Saved = Unknown > delete
							realFile.delete();
						} else {
							String savedFileInfoStr = savedProp.getProperty(intraKey);
							if (savedFileInfoStr == null) {
								// Intra = Exist, DMZ = Deleted, Saved = Unknown > delete
								realFile.delete();
							} else {
								FileInfo savedFileInfo = new FileInfo(savedProp.getProperty(intraKey));
								if (savedFileInfo.isDeleted()) {
									// Intra = Exist, DMZ = Deleted, Saved = Deleted > push
									if (!pushFile(intraFileInfo, out)) {
										noError = false;
									}
									needRefresh = true;
								} else {
									// Intra = Exist, DMZ = Deleted, Saved = Exist > delete
									realFile.delete();
								}
							}
						}
					} else {
						// Intra = Exist, DMZ = Exist ...
						if (!ResourceHelper.checksumEquals(dmzFileInfo.getChecksum(), intraFileInfo.getChecksum())) {
							needRefresh = true;
							// ... Intra or DMZ = Modified ...
							FileInfo savedFileInfo = null;
							if (savedProp != null) {
								savedFileInfo = new FileInfo(savedProp.getProperty(intraKey));
							}
							if (/* downloadFromDMZ || */(savedFileInfo != null && ResourceHelper.checksumEquals(savedFileInfo.getChecksum(), intraFileInfo.getChecksum()))) {
								// ... Saved = Intra > DMZ = Newer > download
								File realFile = new File(baseFolderFile, intraFileInfo.getPath());
								if (!downloadFile(dmzFileInfo, realFile, out)) {
									noError = false;
								}
							} else if (/* pushOnDMZ || */(savedFileInfo != null && ResourceHelper.checksumEquals(savedFileInfo.getChecksum(), dmzFileInfo.getChecksum()))) {
								// ... Saved = DMZ > Intra = Newer > push
								if (!pushFile(intraFileInfo, out)) {
									noError = false;
								}
							} else {
								// ... Both modified > CONFLICT
								if ((intraFileInfo.getModificationDate() > dmzFileInfo.getModificationDate())) {
									// ... Intra = Newer > push
									if (!pushFile(intraFileInfo, out)) {
										noError = false;
									}
								} else {
									// ... DMZ = Newer > download
									File realFile = new File(baseFolderFile, intraFileInfo.getPath());
									if (!downloadFile(dmzFileInfo, realFile, out)) {
										noError = false;
									}
								}
							}
						}
					}
				}
			}

			// Browsing DMZ structure
			keys = dmzProp.keys();
			while (keys.hasMoreElements()) {
				String dmzKey = (String) keys.nextElement();
				String dmzFileInfoStr = dmzProp.getProperty(dmzKey);
				FileInfo dmzFileInfo = new FileInfo(dmzFileInfoStr);
				if (intraProp.get(dmzKey) == null) {
					if (!dmzFileInfo.isDeleted()) {
						// DMZ = Exist, Intra = Unknown > download (or delete)
						File realFile = new File(baseFolderFile, dmzFileInfo.getPath());
						if (deleteDMZItNotFoundIntra) {
							deleteFile(dmzFileInfo, out);
						} else if (!downloadFile(dmzFileInfo, realFile, out)) {
							noError = false;
						}
					}
				} else {
					// Intra = Exist, DMZ = Exist > The process is already done in the first loop.
				}
			}			
		} finally {			
			if (needRefresh && refreshDMZContent) {
				sendCommand("refresh", "true");
			}
			if (needRefresh && fullRefreshDMZContent) {
				sendCommand("refresh-all", "true");
			}
			out.close();
			if (!noError) {
				DebugListening.getInstance().sendError(writer.toString());
			}

		}

		logger.fine("end synchronisation with : " + serverURL);

		return previousResult;
	}
}
