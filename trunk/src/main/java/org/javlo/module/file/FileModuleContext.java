package org.javlo.module.file;

import java.util.LinkedList;
import java.util.List;

import org.javlo.bean.LinkToRenderer;
import org.javlo.module.core.AbstractModuleContext;

public class FileModuleContext extends AbstractModuleContext {
	
	private String root;
	private String title;
	private String path = "/";
	
	private LinkToRenderer homeLink;
	
	public static final String KEY = "fileContext";
	
	public static final String PAGE_META = "meta";
	
	private List<LinkToRenderer> navigation = new LinkedList<LinkToRenderer>();
	
	/**
	 * use getInstance on AbstractModuleContext or smart instance in action method for instantiate.
	 */
	@Override
	public void init(){
		homeLink = new LinkToRenderer(i18nAccess.getText("file.navigation.explorer"), "explorer", "/jsp/file.jsp");
		navigation.add(homeLink);
		navigation.add(new LinkToRenderer(i18nAccess.getText("file.navigation.meta"), PAGE_META, "/jsp/meta.jsp"));		
	};

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

	@Override
	public List<LinkToRenderer> getNavigation() {		
		return navigation;
	}

	@Override
	public LinkToRenderer getHomeLink() {
		return homeLink;
	}
	
}
