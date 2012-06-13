package org.javlo.module.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.javlo.bean.LinkToRenderer;
import org.javlo.context.GlobalContext;
import org.javlo.helper.LangHelper;
import org.javlo.helper.URLHelper;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;

public class FileModuleContext extends AbstractModuleContext {
	
	public static final String MODULE_NAME = "file";
	
	private String root;
	private String title;
	private String path = "/";
	
	public static final String KEY = "fileContext";
	
	public static final String PAGE_META = "meta";
	
	private List<LinkToRenderer> navigation = new LinkedList<LinkToRenderer>();
	
	/**
	 * use getInstance on AbstractModuleContext or smart instance in action method for instantiate.
	 */
	@Override
	public void init(){		
		loadNavigation();
	};
	
	public static FileModuleContext getInstance(HttpServletRequest request) throws FileNotFoundException, InstantiationException, IllegalAccessException, IOException, ModuleException {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		Module module = ModulesContext.getInstance(request.getSession(), globalContext).searchModule(MODULE_NAME);
		return (FileModuleContext) AbstractModuleContext.getInstance(request.getSession(), globalContext, module, FileModuleContext.class);
	}

	public void loadNavigation() {		
		navigation.clear();
		navigation.add(getHomeLink());
		navigation.add(new LinkToRenderer(i18nAccess.getText("file.navigation.meta"), PAGE_META, "/jsp/meta.jsp"));		
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

	@Override
	public List<LinkToRenderer> getNavigation() {
		return navigation;
	}

	@Override
	public LinkToRenderer getHomeLink() {	
		return new LinkToRenderer(i18nAccess.getText("file.navigation.explorer"), "explorer", "/jsp/file.jsp", LangHelper.obj(new LangHelper.MapEntry("path", URLHelper.mergePath("/",getPath()))));
	}
	
}
