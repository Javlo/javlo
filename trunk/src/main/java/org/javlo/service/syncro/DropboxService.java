package org.javlo.service.syncro;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

import bsh.StringUtil;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;

public class DropboxService {

	public static final class DropboxServiceException extends Exception {
		public DropboxServiceException(String msg) {
			super(msg);
		}

		public DropboxServiceException(Throwable e) {
			super(e);
		}
	}

	DbxClient client = null;
	long totalDownloadSize = 0;

	private DropboxService() {
	}

	private DbxRequestConfig getConfig(ContentContext ctx) {
		return new DbxRequestConfig("Javlo/2.0", Locale.getDefault().toString());
	}

	public static DropboxService getInstance(ContentContext ctx, String token) {
		DropboxService outService = null;
		if (ctx != null) {
			outService = (DropboxService) ctx.getRequest().getSession().getAttribute(DropboxService.class.getName());
		}
		if (outService == null) {
			outService = new DropboxService();
			outService.client = new DbxClient(outService.getConfig(ctx), token);
			if (ctx != null) {
				ctx.getRequest().getSession().setAttribute(DropboxService.class.getName(), outService);
			}
		}
		return outService;
	}

	private static Map<String, File> getAllLocalFile(File dir) {
		Map<String, File> outFileList = new HashMap<String, File>();
		for (File child : ResourceHelper.getAllFilesList(dir)) {
			if (child.isFile()) {
				try {
					child = child.getCanonicalFile();
					String prefix = StringHelper.cleanPath(dir.getAbsolutePath());
					String path = StringHelper.cleanPath(child.getAbsolutePath());
					String key = StringUtils.replaceOnce(path, prefix, "");
					outFileList.put(key, child);
				} catch (IOException e) { 
					e.printStackTrace();
				}							
			}
		}
		return outFileList;
	}

	private static void getAllDropboxFile(Map<String, DbxEntry> outFileList, DbxClient client, String dropboxMainRoot, String dropboxRoot) throws DbxException {
		DbxEntry.WithChildren listing = client.getMetadataWithChildren(dropboxRoot);
		for (DbxEntry child : listing.children) {
			if (child.isFile()) {
				outFileList.put(child.path.replaceFirst(dropboxMainRoot, ""), child);
			} else {
				getAllDropboxFile(outFileList, client, dropboxMainRoot, child.path);
			}
		}
	}

	private static Map<String, DbxEntry> getAllDropboxFile(DbxClient client, String dropboxRoot) throws DbxException {
		Map<String, DbxEntry> outFileList = new HashMap<String, DbxEntry>();
		getAllDropboxFile(outFileList, client, dropboxRoot, dropboxRoot);
		return outFileList;
	}

	public void synchronize(File localFolder, String dropboxFolder) throws DropboxServiceException {
		if (!localFolder.isDirectory()) {
			throw new DropboxServiceException("local folder : " + localFolder + " not found.");
		} else {			
			try {
				localFolder = localFolder.getCanonicalFile();
				Map<String, File> localFiles = getAllLocalFile(localFolder);
				for (Map.Entry<String, DbxEntry> childEntry : getAllDropboxFile(client, dropboxFolder).entrySet()) {
					if (localFiles.get(childEntry.getKey()) == null) {
						File targetFile = new File(URLHelper.mergePath(localFolder.getAbsolutePath(), childEntry.getKey()));
						targetFile.getParentFile().mkdirs();
						if (!targetFile.exists()) {
							FileOutputStream outputStream = new FileOutputStream(targetFile);
							try {
								DbxEntry.File downloadedFile = client.getFile(childEntry.getValue().path, null, outputStream);
								totalDownloadSize = totalDownloadSize + downloadedFile.numBytes;
							} finally {
								ResourceHelper.closeResource(outputStream);
							}
						}
					} else {
						System.out.println("file found localy : " + childEntry.getKey());
					}
				}
				return;
			} catch (Exception e) {
				throw new DropboxServiceException(e);
			}
		}
	}
	
	public long getTotalDownloadSize() {
		return totalDownloadSize;
	}
}
