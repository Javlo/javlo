package org.javlo.config;

public class MailingStaticConfig {

	private String mailingFolder;
	private String mailingHistoryFolder;
	
	public MailingStaticConfig(StaticConfig staticConfig) {
		super();
		this.mailingFolder = staticConfig.getMailingFolder();
		this.mailingHistoryFolder = staticConfig.getMailingHistoryFolder();
	}

	public String getMailingFolder() {
		return mailingFolder;
	}

	public void setMailingFolder(String mailingFolder) {
		this.mailingFolder = mailingFolder;
	}

	public String getMailingHistoryFolder() {
		return mailingHistoryFolder;
	}

	public void setMailingHistoryFolder(String mailingHistoryFolder) {
		this.mailingHistoryFolder = mailingHistoryFolder;
	}
	
}