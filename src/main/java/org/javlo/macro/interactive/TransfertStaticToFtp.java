package org.javlo.macro.interactive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.javlo.helper.StringHelper;
import org.javlo.utils.downloader.Html2Directory;
import org.javlo.utils.downloader.Html2Directory.Status;

public class TransfertStaticToFtp extends Thread {	
	
	private static Logger logger = Logger.getLogger(TransfertStaticToFtp.class.getName());
	
	
	public boolean running = true;

	private File folder;
	private URL url;
	private String host;
	private int port;
	private String username;
	private String password;
	private String path;
	
	
	public TransfertStaticToFtp(File folder, URL url, String host, int port, String username, String password, String path) {
		super();
		this.folder = folder;
		this.url = url;
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.path = path;
	}
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
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
	                System.out.println("About to upload the file: " + localFilePath);
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
			ftp = new FTPClient();		
			ftp.connect(host, port);
			if (!ftp.isConnected()) {
				logger.severe("could not connect to : " + host + ":" + port);
			} else {
				if (!StringHelper.isEmpty(username)) {
					if (!ftp.login(username, password)) {
						logger.severe("could not log with username:" + username);
					} else if (!ftp.changeWorkingDirectory(path)) {
						logger.severe("path not found : " + path);
					} else {
						uploadDirectory(ftp, "/", folder, path);						
					}
				}
			}
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
