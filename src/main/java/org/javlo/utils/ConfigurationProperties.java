package org.javlo.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.Properties;

import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;

public class ConfigurationProperties {

	protected Properties prop;
	private File file;

	public ConfigurationProperties() {
		this.prop = new Properties();
	}

	public ConfigurationProperties(Properties prop) {
		this.prop = prop;
	}

	public void load(File file) throws IOException {
		this.file = file;
		FileReader reader = new FileReader(file);
		try {
			prop.load(reader);
		} finally {
			ResourceHelper.safeClose(reader);
		}
	}

	public File getFile() {
		return file;
	}

	public Iterator getKeys() {
		return prop.keySet().iterator();
	}

	public void clear() {
		prop.clear();
	}

	public Properties getProperties() {
		return prop;
	}

	public int getInt(String key, int i) {
		String val = prop.getProperty(key);
		if (val == null) {
			return i;
		} else {
			return Integer.parseInt(val.trim());
		}
	}

	public String getString(String key, String defaultValue) {
		String val = prop.getProperty(key);
		if (val == null) {
			return defaultValue;
		} else {
			return val.trim();
		}
	}

	public String getString(String key) {
		return prop.getProperty(key);
	}

	public void setProperty(String key, int value) {
		prop.setProperty(key, "" + value);
	}

	public void setProperty(String key, String value) {
		if (key != null && value != null) {
			prop.setProperty(key, value);
		}
	}

	public String getProperty(String key) {
		return prop.getProperty(key);
	}

	public long getLong(String key, long i) {
		String val = prop.getProperty(key);
		if (val == null) {
			return i;
		} else {
			return Long.parseLong(val);
		}
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		String val = prop.getProperty(key);
		if (val == null) {
			return defaultValue;
		} else {
			return StringHelper.isTrue(val);
		}
	}

	public void save(OutputStream outputStream) throws IOException {
		prop.store(outputStream, "");
	}

	public void setProperty(String key, boolean adminManagement) {
		prop.setProperty(key, "" + adminManagement);
	}

	public void clearProperty(String key) {
		prop.remove(key);
	}

	public void setProperty(String key, Object value) {
		prop.setProperty(key, "" + value);
	}

	public void addProperty(String key, Object object) {
		prop.setProperty(key, "" + object);
	}

	public boolean containsKey(String key) {
		return prop.containsKey(key);
	}

	public boolean isEmpty() {
		return prop.isEmpty();
	}

	public void setFile(File dataFile) throws IOException {
		load(dataFile);
	}

	public void save() throws IOException {
		if (file != null) {
			OutputStream out = new FileOutputStream(file);
			try {
				prop.store(out, "");
			} finally {
				ResourceHelper.closeResource(out);
			}
		}
	}

	public void load(InputStream in) throws IOException {
		prop.load(in);
	}

	public void setEncoding(String cHARACTER_ENCODING) {
		// TODO: check how implement that
	}

	public void load(Reader reader) throws IOException {
		prop.load(reader);
	}

	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	public void load() throws IOException {
		load(file);
	}

	public Double getDouble(String key, double defaultValue) {
		String val = prop.getProperty(key);
		if (val == null) {
			return defaultValue;
		} else {
			return Double.parseDouble(val);
		}
	}

	public Float getFloat(String key, float defaultValue) {
		String val = prop.getProperty(key);
		if (val == null) {
			return defaultValue;
		} else {
			return Float.parseFloat(val);
		}
	}

	public Integer getInteger(String key, Integer defaultValue) {
		String val = prop.getProperty(key);
		if (val == null) {
			return defaultValue;
		} else {
			return Integer.parseInt(val);
		}
	}

	public Integer getInteger(String key) {
		return getInteger(key, null);
	}

}
