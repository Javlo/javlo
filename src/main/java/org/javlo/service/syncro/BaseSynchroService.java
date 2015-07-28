package org.javlo.service.syncro;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.message.GenericMessage;
import org.javlo.service.NotificationService;
import org.javlo.service.syncro.AbstractSynchroContext.SynchroAction;
import org.javlo.service.syncro.AbstractSynchroContext.SynchroSide;
import org.javlo.service.syncro.exception.SynchroFatalException;
import org.javlo.service.syncro.exception.SynchroNonFatalException;

/**
 * Javlo implementation of the {@link AbstractSynchroService}.
 * 
 * @author bdumont
 */
public abstract class BaseSynchroService extends AbstractSynchroService<BaseSynchroContext> {

	public static Logger logger = Logger.getLogger(BaseSynchroService.class.getName());

	public static final String SERVLET_RELATIVE_PATH = "/synchro/";

	private File baseFolderFile;
	private HttpClientService httpClientService;
	private boolean manageDeletedFiles = true;
	private boolean splitBigFiles = true;
	private boolean refreshAll = false;

	public BaseSynchroService(HttpClientService httpClientService, File baseFolderFile) {
		this.httpClientService = httpClientService;
		this.baseFolderFile = baseFolderFile;
	}

	public boolean isManageDeletedFiles() {
		return manageDeletedFiles;
	}
	public void setManageDeletedFiles(boolean manageDeletedFiles) {
		this.manageDeletedFiles = manageDeletedFiles;
	}

	public boolean isSplitBigFiles() {
		return splitBigFiles;
	}
	public void setSplitBigFiles(boolean splitBigFiles) {
		this.splitBigFiles = splitBigFiles;
	}

	public File getBaseFolderFile() {
		return baseFolderFile;
	}

	@Override
	public String getDistantName() {
		return httpClientService.getServerURL();
	}

	public String buildURL(String url) {
		return URLHelper.mergePath(SERVLET_RELATIVE_PATH, url);
	}

	public File buildLocalFile(FileInfo fileInfo) {
		return buildLocalFile(fileInfo.getPath());
	}

	public File buildLocalFile(String relativePath) {
		return new File(baseFolderFile, relativePath);
	}

	public HttpClientService getHttpClientService() {
		return httpClientService;
	}

	@Override
	public BaseSynchroContext newSynchroContext() {
		return new BaseSynchroContext(this);
	}

	@Override
	protected void initializeContext(BaseSynchroContext context, Object previousState) throws SynchroFatalException {
		try {
			Map<String, FileInfo> localInfos = context.loadLocalInfo();
			if (splitBigFiles) {
				SynchroHelper.splitBigFiles(buildLocalFile(""), localInfos);
			}
		} catch (Exception ex) {
			throw new SynchroFatalException("Exception splitting big files", ex);
		}
		super.initializeContext(context, previousState);
	}

	@Override
	protected void applyActions(BaseSynchroContext context) {
		super.applyActions(context);
		deleteLocalDirectories(context);
		deleteDistantDirectories(context);
	}

	@Override
	protected void applyAction(BaseSynchroContext context, String path, SynchroAction action) throws SynchroNonFatalException {
		switch (action) {
		case COPY_TO_LOCAL: {
			FileInfo distantInfo = context.getInfo(SynchroSide.DISTANT, path);
			copyDistantToLocal(context, distantInfo);
		}
			break;
		case COPY_TO_DISTANT: {
			FileInfo localInfo = context.getInfo(SynchroSide.LOCAL, path);
			copyLocalToDistant(context, localInfo);
		}
			break;
		case DELETE_LOCAL: {
			FileInfo localInfo = context.getInfo(SynchroSide.LOCAL, path);
			if (localInfo == null) {
				// In "mark as deleted" case
				localInfo = context.getInfo(SynchroSide.DISTANT, path);
			}
			deleteLocalFile(context, localInfo);
		}
			break;
		case DELETE_DISTANT: {
			FileInfo distantInfo = context.getInfo(SynchroSide.DISTANT, path);
			deleteDistantFile(context, distantInfo);
		}
			break;
		// case MOVE_LOCAL:
		// break;
		// case MOVE_DISTANT:
		// break;
		case CONFLICT: {
			FileInfo localInfo = context.getInfo(SynchroSide.LOCAL, path);
			FileInfo distantInfo = context.getInfo(SynchroSide.DISTANT, path);
			String newPath = buildConflictPath(path);
			FileInfo movedInfo = moveLocalFile(context, localInfo, newPath);
			if (movedInfo != null) {
				// copyLocalToDistant(context, movedInfo); TODO: update to new
				// HttpClient
				copyDistantToLocal(context, distantInfo);
			}
		}
			break;
		default:
			logger.warning("Unmanaged synchro action: " + action);
			break;
		}
	}

	protected String buildConflictPath(String path) {
		String ext = FilenameUtils.getExtension(path);
		if (!ext.isEmpty()) {
			ext = "." + ext;
		}
		return path + ".conflict." + StringHelper.createFileName(getLocalName()) + "." + StringHelper.renderFileTime(new Date()) + ext;
	}

	public boolean sendCommand(String paramName, String paramValue) {

		logger.fine("send command : " + paramName + " = " + paramValue);

		try {
			paramValue = URLEncoder.encode(paramValue, ContentContext.CHARACTER_ENCODING);
			String relativeURL = buildURL("");
			relativeURL = URLHelper.addParam(relativeURL, paramName, paramValue);
			return httpClientService.callURL(relativeURL).getStatusCode() == HttpStatus.SC_OK;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean copyLocalToDistant(BaseSynchroContext context, FileInfo localInfo) throws SynchroNonFatalException {
		return copyLocalToDistant(context, localInfo, 3);
	}
	protected boolean copyLocalToDistant(BaseSynchroContext context, FileInfo localInfo, int retry) throws SynchroNonFatalException {

		if (localInfo.isDirectory()) {
			// logger.fine("push directory " + localInfo);
			// File localFile = buildLocalFile(localInfo);
			// context.updateOutState(localInfo.getPath(), localFile,
			// localInfo);
			return false;
		}

		logger.fine("push file " + localInfo);

		synchronized (httpClientService.lock) {
			File localFile = buildLocalFile(localInfo);

			String relativeURL = buildURL(SynchroHelper.encodeURLPath(localInfo.getPath()));
			relativeURL = URLHelper.addParam(relativeURL, "checksum", "" + localInfo.getChecksum());

			HttpResponse resp = null;
			try {
				HttpUriRequest request;
				if (splitBigFiles && SynchroHelper.isBigFile(localInfo.getSize())) {
					//Just send "mergeBigFile" command
					relativeURL = URLHelper.addParam(relativeURL, "mergeBigFile", "true");
					String finalURL = httpClientService.encodeURL(relativeURL);
					request = new HttpGet(finalURL);
				} else {
					//Post file content
					String finalURL = httpClientService.encodeURL(relativeURL);
					HttpPost filePost = new HttpPost(finalURL);
					MultipartEntity multipart = new MultipartEntity();
					multipart.addPart(localFile.getName(), new FileBody(localFile));
					filePost.setEntity(multipart);
					// client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
					request = filePost;
				}
				resp = httpClientService.execute(request);
				if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					context.updateOutState(localInfo.getPath(), localFile, localInfo);
					return true;
				} else if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
					if (retry > 0) {
						context.report.println("Upload failed : " + localInfo.getPath() + "(" + retry + " retry left)");
						return copyLocalToDistant(context, localInfo, retry - 1);
					}
					throw new SynchroFatalException("Upload failed: " + localInfo.getPath());
				} else if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_PRECONDITION_FAILED) {
					throw new SynchroNonFatalException("Rebuild of splitted not done.");
				} else {
					String msg = "Upload '" + localInfo.getPath() + "' failed, response=" + resp.getStatusLine().getReasonPhrase();
					logger.warning(msg);
					context.getReportWriter().println(msg);
					context.onError();
					return false;
				}
			} catch (SynchroNonFatalException ex) {
				throw ex;
			} catch (Exception ex) {
				String msg = "Upload '" + localInfo.getPath() + "' failed, message=" + ex.getMessage();
				logger.log(Level.WARNING, msg, ex);
				context.getReportWriter().println(msg);
				context.onError(ex);
				return false;
			} finally {
				httpClientService.safeConsume(resp);
			}
		}
	}

	protected boolean copyDistantToLocal(BaseSynchroContext context, FileInfo distantInfo) throws SynchroNonFatalException {

		if (distantInfo.isDirectory()) {
			// logger.fine("create local directory " + distantInfo);
			// File localFile = buildLocalFile(distantInfo);
			// if (localFile.exists() || localFile.mkdir()) {
			// context.updateOutState(distantInfo.getPath(), localFile,
			// distantInfo);
			// }
			// return true;
			return false;
		}

		logger.fine("download file " + distantInfo);

		if (splitBigFiles && SynchroHelper.isBigFile(distantInfo.getSize())) {

			try {
				SynchroHelper.rebuildSplitted(buildLocalFile("").getAbsolutePath(), distantInfo.getPath(), distantInfo.getChecksum());
				context.updateOutState(distantInfo.getPath(), buildLocalFile(distantInfo), distantInfo);
				return true;
			} catch (IOException ex) {
				throw new SynchroNonFatalException("Exception rebuilding splitted file: " + distantInfo.getPath(), ex);
			}

		} else {

			synchronized (httpClientService.lock) {
				File localFile = buildLocalFile(distantInfo);

				String relativeURL = buildURL(SynchroHelper.encodeURLPath(distantInfo.getPath()));
				String finalURL = httpClientService.encodeURL(relativeURL);

				InputStream in = null;
				HttpResponse resp = null;
				try {
					HttpGet req = new HttpGet(finalURL);
					resp = httpClientService.execute(req);

					HttpEntity entity = resp.getEntity();
					if (entity != null) {
						in = entity.getContent();
						if (!localFile.getParentFile().exists()) {
							localFile.getParentFile().mkdirs();
						}
						ResourceHelper.writeStreamToFile(in, localFile);
					}
					String localChecksum = ResourceHelper.computeChecksum(localFile);
					if (!ResourceHelper.checksumEquals(localChecksum, distantInfo.getChecksum())) {
						String msg = "Download error : '" + distantInfo.getPath() + "' not same checksum [size source:" + distantInfo.getSize() + " size target:" + localFile.length() + "]";
						logger.warning(msg);
						context.getReportWriter().println(msg);
						localFile.delete();
						return false;
					}
					context.updateOutState(distantInfo.getPath(), localFile, distantInfo);
					return true;
				} catch (Exception ex) {
					// ex.printStackTrace();
					logger.warning("error with : " + distantInfo.getPath() + " message : " + ex.getMessage());
					context.getReportWriter().println("Download '" + distantInfo.getPath() + "' failed, message=" + ex.getMessage());
					localFile.delete();
					return false;
				} finally {
					httpClientService.safeConsume(resp);
					ResourceHelper.safeClose(in);
				}
			}
		}
	}

	protected boolean deleteLocalFile(BaseSynchroContext context, FileInfo localInfo) {

		if (localInfo.isDirectory()) {
			// Mark for post process deletion
			context.getLocalDirectoryToDelete().add(localInfo);
			return false;
		}

		logger.fine("delete local file " + localInfo);

		File localFile = buildLocalFile(localInfo);
		localFile.delete();
		context.updateOutState(localInfo.getPath(), localFile, localInfo);
		return true;
	}

	protected void deleteLocalDirectories(BaseSynchroContext context) {
		Collections.sort(context.getLocalDirectoryToDelete(), new FileInfoPathComparator());
		Collections.reverse(context.getLocalDirectoryToDelete()); // To delete
																	// childs
																	// before
																	// parents
		for (FileInfo localInfo : context.getLocalDirectoryToDelete()) {
			File directory = buildLocalFile(localInfo);
			if (directory.isDirectory()) {
				logger.fine("delete local directory " + localInfo);
				boolean deleted = !directory.exists();
				if (!deleted) {
					deleted = directory.delete();
				}
				if (deleted) {
					context.updateOutState(localInfo.getPath(), directory, localInfo);
				}
			}
		}
	}

	protected boolean deleteDistantFile(BaseSynchroContext context, FileInfo distantInfo) {

		if (distantInfo.isDirectory()) { // no syncro directory
			// Mark for post process deletion
			context.getDistantDirectoryToDelete().add(distantInfo);
			return false;
		}

		logger.fine("delete distant file " + distantInfo);

		File localFile = buildLocalFile(distantInfo);

		String relativeURL = buildURL(SynchroHelper.encodeURLPath(distantInfo.getPath()));
		relativeURL = URLHelper.addParam(relativeURL, "delete", "true");

		try {
			StatusLine status = httpClientService.callURL(relativeURL);
			if (status.getStatusCode() == HttpStatus.SC_OK) {
				context.updateOutState(distantInfo.getPath(), localFile, distantInfo);
				return true;
			} else {
				String msg = "Delete distant '" + distantInfo.getPath() + "' failed, response=" + status.getReasonPhrase();
				logger.warning(msg);
				context.getReportWriter().println(msg);
				context.onError();
				return false;
			}
		} catch (Exception ex) {
			// ex.printStackTrace();
			logger.warning("error with : " + distantInfo.getPath() + " message : " + ex.getMessage());
			context.getReportWriter().println("Download '" + distantInfo.getPath() + "' failed, message=" + ex.getMessage());
			return false;
		}
	}

	protected void deleteDistantDirectories(BaseSynchroContext context) {
		// Collections.sort(context.getDistantDirectoryToDelete(), new
		// FileInfoPathComparator());
		// Collections.reverse(context.getDistantDirectoryToDelete()); //To
		// delete childs before parents
		// for (FileInfo distantInfo : context.getDistantDirectoryToDelete()) {
		// File directory = buildLocalFile(distantInfo);
		// context.updateOutState(distantInfo.getPath(), directory,
		// distantInfo);
		// }
	}

	protected FileInfo moveLocalFile(BaseSynchroContext context, FileInfo localInfo, String newPath) {

		logger.fine("move local file '" + localInfo + "' to '" + newPath + "'");

		File localFile = buildLocalFile(localInfo);
		File renamedFile = buildLocalFile(newPath);
		localFile.renameTo(renamedFile);
		context.updateOutState(localInfo.getPath(), null, null);
		return context.updateOutState(newPath, renamedFile, localInfo);
	}

	public void pushContext(String context) {

		// System.out.println("******** context **********");
		// System.out.println(context);
		// System.out.println("***************************");

		sendCommand("context", context);
	}

	public void sendRefresh() {
		sendCommand("refresh", "true");
	}

	@Override
	protected void onActionsApplied(BaseSynchroContext context) {
		super.onActionsApplied(context);
		if (context.isChangeOccured()) {
			if (isRefreshAll()) {				
				sendCommand("refresh-all", "true");
			} else {				
				sendCommand("refresh", "true");
			}
		}
	}

	@Override
	protected void onNonFatalException(BaseSynchroContext context, SynchroNonFatalException ex, String currentFilePath) {
		logger.log(Level.WARNING, "Synchro - Non fatal exception on file '" + currentFilePath + "' : " + ex.getMessage(), ex);
		PrintWriter out = context.getReportWriter();
		out.println("Non fatal exception on file '" + currentFilePath + "' :");
		ex.printStackTrace(out);
		super.onNonFatalException(context, ex, currentFilePath);
	}

	@Override
	protected void onFatalException(BaseSynchroContext context, SynchroFatalException ex) {
		logger.log(Level.SEVERE, "Synchro - Fatal exception: " + ex.getMessage(), ex);
		PrintWriter out = context.getReportWriter();
		out.println("Fatal exception in synchronisation: ");
		ex.printStackTrace(out);
		super.onFatalException(context, ex);
	}

	@Override
	protected void onUncaughtException(BaseSynchroContext context, Throwable ex) {
		logger.log(Level.SEVERE, "Synchro - Uncaught exception: " + ex.getMessage(), ex);
		PrintWriter out = context.getReportWriter();
		out.println("Uncaught exception in synchronisation :");
		ex.printStackTrace(out);
		super.onUncaughtException(context, ex);
	}

	public boolean isRefreshAll() {
		return refreshAll;
	}

	public void setRefreshAll(boolean refreshAll) {
		this.refreshAll = refreshAll;
	}

	public class FileInfoPathComparator implements Comparator<FileInfo> {
		@Override
		public int compare(FileInfo o1, FileInfo o2) {
			return o1.getPath().compareTo(o2.getPath());
		}
	}

}
