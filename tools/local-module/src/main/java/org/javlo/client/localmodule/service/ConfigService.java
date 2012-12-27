package org.javlo.client.localmodule.service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.javlo.client.localmodule.model.ServerConfig;
import org.javlo.helper.StringHelper;

public class ConfigService {

	private static final Logger logger = Logger.getLogger(ConfigService.class.getName());

	private static final String FILE_NAME = "${user.home}${file.separator}.javlo-localmodule.properties";
	public static final String DEFAULT_FOLDER = "${user.home}${file.separator}javlo-localmodule";

	private static ConfigService instance;
	public static ConfigService getInstance() {
		synchronized (ConfigService.class) {
			if (instance == null) {
				instance = new ConfigService();
			}
			return instance;
		}
	}

	PropertiesConfiguration properties = new PropertiesConfiguration();

	public final Object lock = new Object();

	private ConfigService() {
	}

	public void init() throws IOException, ConfigurationException {
		synchronized (lock) {
			File file = new File(StringHelper.expandSystemProperties(FILE_NAME));
			logger.info("load local-module config : " + file);
			if (!file.exists()) {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			}
			properties.setDelimiterParsingDisabled(true);
			properties.setFile(file);
			properties.load();
		}
	}

	public void reload() throws ConfigurationException {
		synchronized (lock) {
			properties.clear();
			properties.load();
		}
	}

	public void save() throws ConfigurationException {
		synchronized (lock) {
//			String saved = null;
//			if (!isStorePassword()) {
//				saved = getPassword();
//				setPassword(null);
//			}
			properties.save();
//			if (saved != null) {
//				setPassword(saved);
//			}
		}
	}

//	public String getComputerName() {
//		synchronized (lock) {
//			String out = properties.getString("local.computerName", null);
//			if (out == null) {
//				try {
//					out = InetAddress.getLocalHost().getHostName();
//				} catch (UnknownHostException ex) {
//					out = new SimpleDateFormat("'Computer'D-sSSS").format(new Date());
//				}
//			}
//			return out;
//		}
//	}
//	public void setComputerName(String computerName) {
//		synchronized (lock) {
//			properties.setProperty("local.computerName", computerName);
//		}
//	}

//	public File getLocalFolderFile() {
//		File out = new File(getLocalFolder());
//		out.mkdirs();
//		return out;
//	}
//	public String getLocalFolder() {
//		synchronized (lock) {
//			return properties.getString("local.folder");
//		}
//	}
//	public void setLocalFolder(String localFolder) {
//		synchronized (lock) {
//			properties.setProperty("local.folder", localFolder);
//		}
//	}

	public ServerConfig[] getServers() {
		List<ServerConfig> servers = new LinkedList<ServerConfig>();
		int i = 0;
		while (true) {
			String base = "server." + i + ".";
			String serverURL = properties.getString(base + "url");
			if (serverURL == null) {
				break;
			}
			ServerConfig server = new ServerConfig();
			server.setServerURL(serverURL);
			servers.add(server);
			i++;
		}
		return servers.toArray(new ServerConfig[servers.size()]);
	}

	public void setServers(ServerConfig[] servers) {
		for (Iterator<String> iterator = properties.getKeys("server"); iterator.hasNext();) {
			properties.clearProperty(iterator.next());
		}
		int i = 0;
		for (ServerConfig server : servers) {
			String base = "server." + i + ".";
			properties.setProperty(base + "url", server.getServerURL());
			i++;
		}
	}

//	public String getServerURL() {
//		synchronized (lock) {
//			return properties.getString("server.url");
//		}
//	}
//	public void setServerURL(String serverURL) {
//		synchronized (lock) {
//			properties.setProperty("server.url", serverURL);
//		}
//	}
//
//	public String getUsername() {
//		synchronized (lock) {
//			String out = properties.getString("server.username");
//			if (out == null) {
//				out = System.getProperty("user.name");
//			}
//			return out;
//		}
//	}
//	public void setUsername(String username) {
//		synchronized (lock) {
//			properties.setProperty("server.username", username);
//		}
//	}
//
//	public boolean isStorePassword() {
//		synchronized (lock) {
//			return properties.getBoolean("server.store-password", false);
//		}
//	}
//	public void setStorePassword(boolean password) {
//		synchronized (lock) {
//			properties.setProperty("server.store-password", password);
//		}
//	}
//
//	public String getPassword() {
//		synchronized (lock) {
//			return properties.getString("server.password");
//		}
//	}
//	public void setPassword(String password) {
//		synchronized (lock) {
//			properties.setProperty("server.password", password);
//		}
//	}

	public String getProxyHost() {
		synchronized (lock) {
			String out = properties.getString("http.proxyHost");
			if (out == null) {
				out = System.getProperty("http.proxyHost");
			}
			return out;
		}
	}
	public void setProxyHost(String proxyHost) {
		synchronized (lock) {
			properties.setProperty("http.proxyHost", proxyHost);
		}
	}

	public Integer getProxyPort() {
		synchronized (lock) {
			Integer out = properties.getInteger("http.proxyPort", null);
			if (out == null) {
				try {
					out = Integer.parseInt(System.getProperty("http.proxyPort"));
				} catch (Exception e) {
				}
			}
			return out;
		}
	}
	public void setProxyPort(Integer proxyPort) {
		synchronized (lock) {
			properties.setProperty("http.proxyPort", proxyPort);
		}
	}

	public String getProxyUsername() {
		synchronized (lock) {
			String out = properties.getString("http.proxyUserName");
			if (out == null) {
				out = System.getProperty("http.proxyUserName");
			}
			return out;
		}
	}
	public void setProxyUsername(String proxyUsername) {
		synchronized (lock) {
			properties.setProperty("http.proxyUserName", proxyUsername);
		}
	}

	public String getProxyPassword() {
		synchronized (lock) {
			String out = properties.getString("http.proxyPassword");
			if (out == null) {
				out = System.getProperty("http.proxyPassword");
			}
			return out;
		}
	}
	public void setProxyPassword(String proxyPassword) {
		synchronized (lock) {
			properties.setProperty("http.proxyPassword", proxyPassword);
		}
	}

	public boolean isValid() {
		synchronized (lock) {
			return true //
			&& getServers().length > 0 //
//					&& getComputerName() != null //
//					&& checkLocalFolder(getLocalFolder()) //
//					&& checkServerURL(getServerURL()) //
//					&& getUsername() != null //
//					&& getPassword() != null //
			;
		}
	}

	public static boolean checkLocalFolder(String localFolder) {
		if (localFolder == null)
			return false;
		File lf = new File(localFolder);
		try {
			lf.mkdir();
			return lf.exists() && lf.isDirectory();
		} catch (Exception ex) {
			return false;
		}
	}

	public static boolean checkServerURL(String serverURL) {
		if (serverURL == null) {
			return false;
		}
		try {
			new URL(serverURL);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

}
