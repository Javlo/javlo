package org.javlo.client.localmodule.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.javlo.client.localmodule.model.AppConfig;
import org.javlo.client.localmodule.model.ServerConfig;
import org.javlo.helper.ResourceHelper;
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

	private AppConfig bean;

	private ConfigService() {
	}

	public AppConfig getBean() {
		return bean;
	}

	public synchronized void init() throws IOException {
		reload();
	}

	private File getFile() throws IOException {
		File file = new File(StringHelper.expandSystemProperties(FILE_NAME));
		logger.info("load local-module config : " + file);
		if (!file.exists()) {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			file.createNewFile();
		}
		return file;
	}

	public synchronized void reload() throws IOException {
		Properties properties = new Properties();
		InputStream in = null;
		try {
			in = new FileInputStream(getFile());
			properties.load(in);
			bean = loadBean(properties);
		} finally {
			ResourceHelper.safeClose(in);
		}
	}

	public synchronized void save() throws IOException {
		OutputStream out = null;
		try {
			out = new FileOutputStream(getFile());
			Properties p = storeBean(bean);
			p.store(out, "");
		} finally {
			ResourceHelper.safeClose(out);
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

	private static AppConfig loadBean(Properties properties) {
		AppConfig bean = new AppConfig();
		bean.setProxyHost(StringHelper.trimAndNullify(properties.getProperty("http.proxyHost", System.getProperty("http.proxyHost"))));
		bean.setProxyPort(safeParseInt(StringHelper.trimAndNullify(properties.getProperty("http.proxyPort", System.getProperty("http.proxyPort")))));
		bean.setProxyUsername(StringHelper.trimAndNullify(properties.getProperty("http.proxyUserName")));
		bean.setProxyPassword(StringHelper.trimAndNullify(properties.getProperty("http.proxyPassword")));

		List<ServerConfig> servers = new LinkedList<ServerConfig>();
		int i = 0;
		while (true) {
			String base = "server." + i + ".";
			String serverURL = properties.getProperty(base + "url");
			if (serverURL == null) {
				break;
			}
			ServerConfig server = new ServerConfig();
			server.setServerURL(serverURL);
			servers.add(server);
			i++;
		}
		bean.setServers(servers.toArray(new ServerConfig[servers.size()]));
		return bean;
	}

	private static Integer safeParseInt(String str) {
		Integer out = null;
		if (str != null) {
			try {
				out = Integer.parseInt(str);
			} catch (Exception e) {
			}
		}
		return out;
	}

	private static Properties storeBean(AppConfig bean) {
		Properties out = new Properties();
		ServerConfig[] servers = bean.getServers();
		for (int i = 0; i < servers.length; i++) {
			ServerConfig server = servers[i];
			String base = "server." + i + ".";
			out.setProperty(base + "url", server.getServerURL());
		}
		out.setProperty("http.proxyHost", StringHelper.neverNull(bean.getProxyHost()));
		out.setProperty("http.proxyPort", StringHelper.neverNull(bean.getProxyPort()));
		out.setProperty("http.proxyUserName", StringHelper.neverNull(bean.getProxyUsername()));
		out.setProperty("http.proxyPassword", StringHelper.neverNull(bean.getProxyPassword()));
		return out;
	}

}
