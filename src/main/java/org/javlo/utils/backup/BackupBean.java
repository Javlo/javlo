package org.javlo.utils.backup;

import java.io.File;
import java.util.Date;

public class BackupBean {
	
	private File folder;
	private long delta;
	private Date latestBackup = new Date(0);
	private int backupCount;
	
	public BackupBean(File folder, long delta, int backupCount) {
		super();
		this.folder = folder;
		this.delta = delta;
		this.setBackupCount(backupCount);
	}
	public File getFolder() {
		return folder;
	}
	public void setFolder(File folder) {
		this.folder = folder;
	}
	public long getDelta() {
		return delta;
	}
	public void setDelta(long delta) {
		this.delta = delta;
	}
	public Date getLatestBackup() {
		return latestBackup;
	}
	public void setLatestBackup(Date latestBackup) {
		this.latestBackup = latestBackup;
	}
	public int getBackupCount() {
		return backupCount;
	}
	public void setBackupCount(int backupCount) {
		this.backupCount = backupCount;
	}

}
