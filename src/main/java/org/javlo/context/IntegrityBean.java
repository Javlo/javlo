package org.javlo.context;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Properties;

public class IntegrityBean {
	
	private Properties data;

	public IntegrityBean(String rawValue) {
		data = new Properties();
		Reader reader = new StringReader(rawValue);
		try {
			data.load(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getDescriptionMinSize() {
		return Integer.parseInt(data.getProperty("description.min-size", "100"));
	}
	
	public int getDescriptionMaxSize() {
		return Integer.parseInt(data.getProperty("description.max-size", "300"));
	}
	
	public int getTitleMaxSize() {
		return Integer.parseInt(data.getProperty("title.max-size", "60"));
	}
	
	public int getContentMinSize() {
		return Integer.parseInt(data.getProperty("content.min-size", "300"));
	}
	
}
