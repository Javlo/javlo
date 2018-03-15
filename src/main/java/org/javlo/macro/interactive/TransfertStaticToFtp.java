package org.javlo.macro.interactive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.mailing.DKIMBean;
import org.javlo.mailing.MailService;
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
	private MailService mailService;
	private String email;
	private String siteTitle;
	private String sender;
	private DKIMBean dkimBean;
	private String siteURL;
	
	private int error = 0;
	
	
	public TransfertStaticToFtp(ContentContext ctx, File folder, URL url, String host, int port, String username, String password, String path, MailService mailService, String email) {
		super();
		this.folder = folder;
		this.url = url;
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.path = path;
		this.mailService = mailService;
		this.email = email;	
		this.siteTitle = ctx.getGlobalContext().getGlobalTitle();
		this.sender = ctx.getGlobalContext().getAdministratorEmail();
		this.dkimBean = ctx.getGlobalContext().getDKIMBean();
		this.siteURL = URLHelper.createURL(ctx.getContextForAbsoluteURL(), "/");
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
	public static boolean uploadSingleFile(FTPClient ftpClient, String localFilePath, String remoteFilePath) throws IOException {
	    File localFile = new File(localFilePath);
	 
	    InputStream inputStream = new FileInputStream(localFile);
	    try {
	        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
	        ftpClient.deleteFile(remoteFilePath);
	        return ftpClient.storeFile(remoteFilePath, inputStream);
	    } catch (Exception e ) {
	    	e.printStackTrace();
	    	return false;
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
	public int uploadDirectory(FTPClient ftpClient, String remoteDirPath, File localDir, String remoteParentDir) throws IOException {
	 
	   logger.info("LISTING directory: " + localDir);
	   
	    int c=0;
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
	                	c++;
	                	logger.info("UPLOADED a file to: "+ remoteFilePath);
	                } else {
	                	error++;
	                	logger.warning("COULD NOT upload the file: "+ localFilePath);
	                }
	            } else {
	                // create directory on the server
	            	boolean created; 
	            	try {
	            		created = ftpClient.makeDirectory(remoteFilePath);
	            	} catch (Exception e) {
	            		e.printStackTrace();
	            		created = false;
	            	}
	                if (created) {
	                	logger.info("CREATED the directory: " + remoteFilePath);
	                } else {
	                	error++;
	                	logger.warning("COULD NOT create the directory: " + remoteFilePath);
	                }
 	                // upload the sub directory
	                String parent = remoteParentDir + "/" + item.getName();
	                if (remoteParentDir.equals("")) {
	                    parent = item.getName();
	                }
	                c  = c + uploadDirectory(ftpClient, remoteDirPath, new File(item.getAbsolutePath()),parent);
	            }
	        }
	    }
	    return c;
	}

	@Override
	public void run() {
		Date start = new Date();
		
		FTPClient ftp = null;
		try {
			FileUtils.deleteDirectory(folder);
			folder.mkdirs();
			Status status = new Status();
			Html2Directory.download(url, folder, status, 0);
			ftp = new FTPClient();		
			ftp.connect(host, port);
			
			int c = 0;
			if (!ftp.isConnected()) {
				logger.severe("could not connect to : " + host + ":" + port);
			} else {
				if (!StringHelper.isEmpty(username)) {
					if (!ftp.login(username, password)) {
						logger.severe("could not log with username:" + username);
					} else if (!ftp.changeWorkingDirectory(path)) {
						logger.severe("path not found : " + path);
					} else {
						c = uploadDirectory(ftp, "/", folder, path);						
					}
				}
			}
			
			if (mailService != null && StringHelper.isMail(email) && StringHelper.isMail(sender)) {
				Map<String, String> data = new LinkedHashMap<String, String>();			
				data.put("ftp host", ""+ftp.getRemoteAddress());
				data.put("ftp path", path);
				data.put("# uploaded files", ""+c);
				data.put("# error", ""+error);
				data.put("start time", StringHelper.renderTime(start));
				data.put("end time", StringHelper.renderTime(new Date()));
				String adminMailContent = XHTMLHelper.createAdminMail("Ftp file uploaded.", null, data, siteURL, "back on source site", null);
				try {
					mailService.sendMail( null, new InternetAddress(sender),  new InternetAddress(email),null,null,siteTitle+" : static site generated.", adminMailContent, true, null, dkimBean);
				} catch (AddressException e) {
					e.printStackTrace();
				} catch (MessagingException e) {
					e.printStackTrace();
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
