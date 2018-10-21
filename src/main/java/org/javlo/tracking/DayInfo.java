package org.javlo.tracking;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.javlo.helper.ResourceHelper;
import org.javlo.utils.StructuredProperties;

public class DayInfo {
	
	public int sessionCount = 0;
	public int session2ClickCount = 0;
	public int session2ClickCountMobile = 0;
	public int sessionCountMobile = 0;
	public int pagesCount = 0;
	public int pagesCountMobile = 0;
	
	public DayInfo() {
	}
	
	public DayInfo(File file) throws IOException {
		Properties prop = ResourceHelper.loadProperties(file);
		sessionCount = Integer.parseInt(prop.getProperty("session.count", "0"));
		pagesCount = Integer.parseInt(prop.getProperty("pages.count", "0"));
		pagesCountMobile = Integer.parseInt(prop.getProperty("session.mobile.count"));
		session2ClickCount = Integer.parseInt(prop.getProperty("session.2clicks.count"));
		session2ClickCountMobile = Integer.parseInt(prop.getProperty("session.2clicks.mobile.count"));
	}
	
	public void store(File file) throws IOException {
		Properties prop = new StructuredProperties();
		prop.setProperty("session.count", ""+sessionCount);
		prop.setProperty("pages.count", ""+pagesCount);
		prop.setProperty("session.mobile.count", ""+pagesCountMobile);
		prop.setProperty("session.2clicks.count", ""+session2ClickCount);
		prop.setProperty("session.2clicks.mobile.count", ""+session2ClickCountMobile);
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
	
}
