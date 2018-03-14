package org.javlo.macro.interactive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;
import java.util.zip.ZipException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.javlo.utils.downloader.Html2Directory;
import org.javlo.utils.downloader.Html2Directory.Status;
import org.zeroturnaround.zip.ZipUtil;

public class TransfertStaticToZip extends Thread {	
	
	private static Logger logger = Logger.getLogger(TransfertStaticToZip.class.getName());
	
	
	public boolean running = true;

	private File folder;
	private URL url;
	private String path;
	private File zipFile = null;
	
	
	public TransfertStaticToZip(File folder, URL url, File zipFile, String path) throws ZipException, IOException {
		super();
		this.folder = folder;
		this.url = url;
		this.zipFile = zipFile;		
		this.path = path;
	}
	public File getFolder() {
		return folder;
	}
	public void setFolder(File folder) {
		this.folder = folder;
	}
	public URL getUrl() {
		return url;
	}
	public void setUrl(URL url) {
		this.url = url;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	/**
	 * Upload a single file to the FTP server.
	 *
	 * @param ftpClient
	 *            an instance of org.apache.commons.net.ftp.FTPClient class.
	 * @param localFilePath
	 *            Path of the file on local computer
	 * @param remoteFilePath
	 *            Path of the file on remote the server
	 * @return true if the file was uploaded successfully, false otherwise
	 * @throws IOException
	 *             if any network or IO error occurred.
	 */
	public static boolean uploadSingleFile(FTPClient ftpClient,
	        String localFilePath, String remoteFilePath) throws IOException {
	    File localFile = new File(localFilePath);
	 
	    InputStream inputStream = new FileInputStream(localFile);
	    try {
	        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
	        return ftpClient.storeFile(remoteFilePath, inputStream);
	    } finally {
	        inputStream.close();
	    }
	}
	
	/**
	 * Upload a whole directory (including its nested sub directories and files)
	 * to a FTP server.
	 *
	 * @param ftpClient
	 *            an instance of org.apache.commons.net.ftp.FTPClient class.
	 * @param remoteDirPath
	 *            Path of the destination directory on the server.
	 * @param localParentDir
	 *            Path of the local directory being uploaded.
	 * @param remoteParentDir
	 *            Path of the parent directory of the current directory on the
	 *            server (used by recursive calls).
	 * @throws IOException
	 *             if any network or IO error occurred.
	 */
	public static void uploadDirectory(FTPClient ftpClient,
	        String remoteDirPath, File localDir, String remoteParentDir)
	        throws IOException {
	 
	   logger.info("LISTING directory: " + localDir);
	 
	    File[] subFiles = localDir.listFiles();
	    if (subFiles != null && subFiles.length > 0) {
	        for (File item : subFiles) {
	            String remoteFilePath = remoteDirPath + "/" + remoteParentDir + "/" + item.getName();
	            if (remoteParentDir.equals("")) {
	                remoteFilePath = remoteDirPath + "/" + item.getName();
	            }
	            if (item.isFile()) {
	                // upload the file
	                String localFilePath = item.getAbsolutePath();	                
	                boolean uploaded = uploadSingleFile(ftpClient,  localFilePath, remoteFilePath);
	                if (uploaded) {
	                	logger.info("UPLOADED a file to: "+ remoteFilePath);
	                } else {
	                	logger.warning("COULD NOT upload the file: "+ localFilePath);
	                }
	            } else {
	                // create directory on the server
	                boolean created = ftpClient.makeDirectory(remoteFilePath);
	                if (created) {
	                	logger.info("CREATED the directory: " + remoteFilePath);
	                } else {
	                	logger.warning("COULD NOT create the directory: " + remoteFilePath);
	                }
 	                // upload the sub directory
	                String parent = remoteParentDir + "/" + item.getName();
	                if (remoteParentDir.equals("")) {
	                    parent = item.getName();
	                }
	                uploadDirectory(ftpClient, remoteDirPath, new File(item.getAbsolutePath()),parent);
	            }
	        }
	    }
	}

	@Override
	public void run() {
		FTPClient ftp = null;
		try {
			FileUtils.deleteDirectory(folder);
			folder.mkdirs();
			Status status = new Status();
			Html2Directory.download(url, folder, status, 0);
			ZipUtil.pack(folder, zipFile);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ftp != null) {
				try {
					ftp.disconnect();
				} catch (IOException e) {				
					e.printStackTrace();
				}
			}
			running = false;
		}
	}
}
