package org.javlo.service.syncro;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;


public class FileInfo implements Cloneable {
	
	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(FileInfo.class.getName());
	
	private static final String INFO_SEPARATION = "|";

	public static final FileInfo getFileInfo(String relativePath, File file, String precomputedChecksum) throws IOException {
		String path = URLHelper.cleanPath(relativePath, false);
		boolean isDirectory = file.isDirectory();
		long size = file.length();
		long modified = file.lastModified();
		String checksum = null;
		if (!file.isDirectory()) {
			if (precomputedChecksum != null) {
				checksum = precomputedChecksum;
			} else {
				checksum = ResourceHelper.computeChecksum(file);
			}
		}
		return new FileInfo(path, isDirectory, size, modified, checksum);
	}

	public static FileInfo getDeletedFileInfo(String path, long deletedDate) {
		return new FileInfo(path, false, -1, deletedDate, null);
	}

	private boolean directory;
	private String path;
	private long size;
	private long modificationDate;
	private String checksum;

	public FileInfo() {
		this(null, false, -1, -1, null);
	}

	public FileInfo(String path, boolean isDirectory, long size, long modificationDate, String checksum) {
		this.path = path;
		this.directory = isDirectory;
		this.size = size;
		this.modificationDate = modificationDate;
		this.checksum = checksum;
	}

	public FileInfo(String rawInfo) {
		try {
			String[] infoArray = StringHelper.split(rawInfo, INFO_SEPARATION);
			setDirectory(StringHelper.isTrue(infoArray[0]));
			setPath(infoArray[1]);
			setSize(Long.parseLong(infoArray[2]));
			setModificationDate(Long.parseLong(infoArray[3]));
			setChecksum(infoArray[4]);
		} catch (RuntimeException e) {
			logger.warning("Bad raw structure detected [" + rawInfo + "]");
			throw e;
		}
	}
	
	public boolean isDirectory() {
		return directory;
	}
	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	public long getModificationDate() {
		return modificationDate;
	}
	public void setModificationDate(long modificationDate) {
		this.modificationDate = modificationDate;
	}

	public String getChecksum() {
		return checksum;
	}
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public boolean isDeleted() {
		return size < 0;
	}

	public String toRaw() {
		return "" + isDirectory() + INFO_SEPARATION + getPath() + INFO_SEPARATION + getSize() + INFO_SEPARATION + getModificationDate() + INFO_SEPARATION + getChecksum();
	}

	@Override
	public FileInfo clone() {
		return new FileInfo(path, directory, size, modificationDate, checksum);
	}

	@Override
	public String toString() {
		return getPath();
	}

}
