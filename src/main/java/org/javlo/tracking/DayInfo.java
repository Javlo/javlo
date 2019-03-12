package org.javlo.tracking;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.utils.StructuredProperties;

public class DayInfo {
	
	public static final int CURRENT_VERSION = 2;
	
	public int sessionCount = 0;
	public int session2ClickCount = 0;
	public int session2ClickCountMobile = 0;
	public int sessionCountMobile = 0;
	public int pagesCount = 0;
	public int pagesCountMobile = 0;
	public int publishCount = 0;
	public int saveCount = 0;
	
	public int version = 1;
	
	public Date date;
	
	public DayInfo(Date date) {
		this.date = date;
	}
	
	public DayInfo(File file) throws IOException {
		Properties prop = ResourceHelper.loadProperties(file);
		sessionCount = Integer.parseInt(prop.getProperty("session.count", "0"));
		pagesCount = Integer.parseInt(prop.getProperty("pages.count", "0"));
		pagesCountMobile = Integer.parseInt(prop.getProperty("session.mobile.count"));
		session2ClickCount = Integer.parseInt(prop.getProperty("session.2clicks.count"));
		session2ClickCountMobile = Integer.parseInt(prop.getProperty("session.2clicks.mobile.count"));
		publishCount = Integer.parseInt(prop.getProperty("action.publish.count", "0"));
		saveCount = Integer.parseInt(prop.getProperty("action.save.count", "0"));
		version = Integer.parseInt(prop.getProperty("version", "1"));
		try {
			date = StringHelper.parseSortableDate(prop.getProperty("date", "2000-01-01"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public void store(File file) throws IOException {
		Properties prop = new StructuredProperties();
		prop.setProperty("session.count", ""+sessionCount);
		prop.setProperty("pages.count", ""+pagesCount);
		prop.setProperty("session.mobile.count", ""+pagesCountMobile);
		prop.setProperty("session.2clicks.count", ""+session2ClickCount);
		prop.setProperty("session.2clicks.mobile.count", ""+session2ClickCountMobile);
		prop.setProperty("action.publish.count", ""+publishCount);
		prop.setProperty("action.save.count", ""+saveCount);
		prop.setProperty("version", ""+CURRENT_VERSION);
		prop.setProperty("date", ""+StringHelper.renderSortableDate(date));
		ResourceHelper.writePropertiesToFile(prop, file, "day info");
	}

	public int getSessionCount() {
		return sessionCount;
	}

	public void setSessionCount(int sessionCount) {
		this.sessionCount = sessionCount;
	}

	public int getPagesCount() {
		return pagesCount;
	}

	public void setPagesCount(int pagesCount) {
		this.pagesCount = pagesCount;
	}

	public int getSession2ClickCount() {
		return session2ClickCount;
	}

	public void setSession2ClickCount(int session2ClickCount) {
		this.session2ClickCount = session2ClickCount;
	}

	public int getSession2ClickCountMobile() {
		return session2ClickCountMobile;
	}

	public void setSession2ClickCountMobile(int session2ClickCountMobile) {
		this.session2ClickCountMobile = session2ClickCountMobile;
	}

	public int getSessionCountMobile() {
		return sessionCountMobile;
	}

	public void setSessionCountMobile(int sessionCountMobile) {
		this.sessionCountMobile = sessionCountMobile;
	}

	public int getPagesCountMobile() {
		return pagesCountMobile;
	}

	public void setPagesCountMobile(int pagesCountMobile) {
		this.pagesCountMobile = pagesCountMobile;
	}
	
	public String getDate() {
		return StringHelper.renderSortableDate(date);
	}
	
}
