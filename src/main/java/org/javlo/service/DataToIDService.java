package org.javlo.service;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import javax.servlet.ServletContext;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.javlo.helper.StringHelper;

public class DataToIDService {

	private static final String KEY = DataToIDService.class.getName();

	private static final String TIME_SUFIX = "____TIME";

	private static final String DATA_FILE = "/WEB-INF/work/id_data/id_data.properties";

	private File dataFile;

	private PropertiesConfiguration properties = null;

	public static DataToIDService getInstance(ServletContext application) {
		DataToIDService instance = (DataToIDService) application.getAttribute(KEY);
		if (instance == null) {
			instance = new DataToIDService();
			instance.dataFile = new File(application.getRealPath(DATA_FILE));
			if (!instance.dataFile.exists()) {
				instance.dataFile.getParentFile().mkdirs();
				try {
					instance.dataFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			application.setAttribute(KEY, instance);
			instance.clearTimeData();
		}
		return instance;
	}

	public static DataToIDService getInstance(File propertiesFile) {
		DataToIDService instance = new DataToIDService();
		instance.dataFile = propertiesFile;
		if (!instance.dataFile.exists()) {
			instance.dataFile.getParentFile().mkdirs();
			try {
				instance.dataFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		instance.clearTimeData();
		return instance;
	}

	public PropertiesConfiguration getProperties() {
		if (properties == null) {
			properties = new PropertiesConfiguration();
			properties.setFile(dataFile);
			try {
				properties.load();
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}
		}
		return properties;
	}

	/**
	 * create a data with id to access it.
	 * @param data a free data
	 * @return a random id to access to data.
	 */
	public String setData(String data) {
		synchronized (DATA_FILE) {
			String id = StringHelper.getRandomId();
			getProperties().setProperty(id, data);
			saveProperties();
			return id;
		}
	}

	public void clearTimeData() {
		synchronized (DATA_FILE) {
			Iterator<String> keys = getProperties().getKeys();
			Collection<String> toBeRemoved = new LinkedList<String>();
			while (keys.hasNext()) {
				String key = keys.next();
				if (key.endsWith(TIME_SUFIX)) {
					try {
						Date endTime = StringHelper.parseTime(getProperties().getString(key));
						String dataKey = key.substring(0, key.length() - TIME_SUFIX.length());
						if (new Date().after(endTime)) {
							toBeRemoved.add(key);
							toBeRemoved.add(dataKey);
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
			for (String key : toBeRemoved) {
				getProperties().clearProperty(key);
			}
			saveProperties();
		}
	}

	private void saveProperties() {
		try {
			getProperties().save();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * set a data not for infinity time
	 * 
	 * @param data
	 *            a data
	 * @param liveTime
	 *            in ms after this time the data is obsolete
	 * @return
	 */
	public String setData(String data, long liveTime) {
		synchronized (DATA_FILE) {
			String id = StringHelper.getRandomId();
			if (liveTime > 0) {
				String timeKey = id + TIME_SUFIX;
				getProperties().setProperty(id, data);
				Date endDate = new Date((new Date()).getTime() + liveTime);
				getProperties().setProperty(id, data);
				getProperties().setProperty(timeKey, StringHelper.renderTime(endDate));
				saveProperties();
			}
			clearTimeData();
			return id;
		}
	}

	public String getData(String id) {
		if (id == null) {
			return null;
		}
		return getProperties().getString(id);
	}

	public static void main(String[] args) {
		DataToIDService service = DataToIDService.getInstance(new File("/tmp/id-data.properties"));
		// service.setData("Patrick");
		service.setData("Catherine time 4", 60 * 60);
	}

	/**
	 * remove a value from properties.
	 * 
	 * @param data
	 */
	public void clearData(String data) {
		synchronized (DATA_FILE) {
			PropertiesConfiguration p = getProperties();
			Iterator<String> keys = p.getKeys();
			Collection<String> toBeRemoved = new LinkedList<String>();
			while (keys.hasNext()) {
				String key = keys.next();
				String value = p.getString(key);
				if (value.equals(data)) {
					toBeRemoved.add(key);
				}
			}
			for (String key : toBeRemoved) {
				p.clearProperty(key);
			}
			saveProperties();
		}
	}

}
