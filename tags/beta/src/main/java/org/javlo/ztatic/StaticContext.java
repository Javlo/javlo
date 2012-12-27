package org.javlo.ztatic;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;

public class StaticContext implements Serializable {

	private static final String KEY = StaticContext.class.getName();

	public static final int THUMBNAILS_SIZE = 100;

	private List<File> copyFiles = new LinkedList<File>();

	private List<File> cutFiles = new LinkedList<File>();

	private File currentPath = null;

	HttpSession session = null;

	private StaticContext(HttpSession inSession, int mode) {
		inSession.setAttribute(KEY + mode, this);
		session = inSession;
	}

	public static final StaticContext getInstance(HttpSession inSession) {
		return getInstance(inSession, ContentContext.EDIT_MODE);
	}

	public static final StaticContext getInstance(HttpSession inSession, int mode) {
		StaticContext stCtx = (StaticContext) inSession.getAttribute(KEY + mode);
		if (stCtx == null) {
			stCtx = new StaticContext(inSession, mode);
		}
		return stCtx;
	}

	public List<File> getCopyFiles() {
		return copyFiles;
	}

	public void setCopyFiles(List<File> copyFiles) {
		this.copyFiles = copyFiles;
	}

	public List<File> getCutFiles() {
		return cutFiles;
	}

	public void setCutFiles(List<File> cutFiles) {
		this.cutFiles = cutFiles;
	}

	public File getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(String currentPath) {
		this.currentPath = new File(ResourceHelper.getLinuxPath(currentPath));
	}
}