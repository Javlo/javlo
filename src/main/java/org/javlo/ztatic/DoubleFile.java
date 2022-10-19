package org.javlo.ztatic;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class DoubleFile {
	
	private File file1;
	private File file2;
	private String url1;
	private String url2;
	
	public DoubleFile(ContentContext ctx, File file1, File file2) throws IOException {
		super();
		this.setFile1(file1);
		this.setFile2(file2);
		this.url1 = URLHelper.createFileURL(ctx, file1);
		this.url2 = URLHelper.createFileURL(ctx, file2);
	}

	public File getFile1() {
		return file1;
	}

	public void setFile1(File file1) {
		this.file1 = file1;
	}


	public File getFile2() {
		return file2;
	}

	public void setFile2(File file2) {
		this.file2 = file2;
	}
	
	public long lastModified() {
		if (file1.lastModified()>file2.lastModified()) {
			return file1.lastModified();
		} else {
			return file2.lastModified();
		}
	}
	
	public String getModificationTime() {
		return StringHelper.renderTime(new Date(lastModified()));
	}

	public String getUrl1() {
		return url1;
	}

	public void setUrl1(String url1) {
		this.url1 = url1;
	}

	public String getUrl2() {
		return url2;
	}

	public void setUrl2(String url2) {
		this.url2 = url2;
	}
	
	public boolean delete() {
		boolean del1 = file1.delete();
		boolean del2 = file2.delete();
		return del1 && del2;
	}

}
