package org.javlo.tracking;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import javax.servlet.ServletContext;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.javlo.helper.StringHelper;


public class TrackingProperties {

	private static final String KEY = TrackingProperties.class.getName();

	PropertiesConfiguration properties = new PropertiesConfiguration();

	static final String propertiesFile = "/WEB-INF/config/tracking.properties";

	private TrackingProperties(ServletContext application) throws ConfigurationException, IOException {
		File propFile = new File(application.getRealPath(propertiesFile));
		if (!propFile.exists()) {
			propFile.createNewFile();
		}
		properties.setFile(propFile);
		properties.load();
		properties.setAutoSave(true);
		application.setAttribute(KEY, this);
	}

	public static TrackingProperties getInstance(ServletContext application) throws ConfigurationException, IOException {
		TrackingProperties outTrkProp = (TrackingProperties) application.getAttribute(KEY);
		if (outTrkProp == null) {
			outTrkProp = new TrackingProperties(application);
		}
		return outTrkProp;
	}

	public String getPublisher() {
		return properties.getString("publisher", "");
	}

	public void setPublisher(String publisher) {
		properties.setProperty("publisher", publisher);
	}

	public Date getPublishDate() throws ParseException {
		String date = (String)properties.getProperty("publish-date");
		if (date == null) {
			return StringHelper.parseDate("1/1/1975");
		}
		return StringHelper.parseDate(date);
	}

	public void setPublishDate(Date inDate) {
		properties.setProperty("publish-date", StringHelper.renderDate(inDate));
	}
}
