package org.javlo.module.file;

import javax.servlet.http.HttpSession;

public class FileModuleContext {
	
	private String root;
	private String title;
	private String path = "/";
	
	public static final String KEY = "fileContext";

	public static final FileModuleContext getInstance(HttpSession session) {
		FileModuleContext instance = (FileModuleContext) session.getAttribute(KEY);
		if (instance == null) {
			instance = new FileModuleContext();
			session.setAttribute(KEY, instance);
		}
		return instance;
	}
	
	public void clear() {
		root = null;
		title = null;
	}
	
	public String getRoot() {
		return root;
	}
	
	public void setRoot(String root) {
		this.root = root;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}	

}
