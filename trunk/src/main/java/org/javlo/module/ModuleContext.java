package org.javlo.module;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.javlo.context.GlobalContext;
import org.javlo.helper.URLHelper;

public class ModuleContext {

	private static Logger logger = Logger.getLogger(ModuleContext.class.getName());

	static final String MODULES_FOLDER = "/modules";

	private static final String KEY = "module";

	private Module currentModule;
	private Module fromModule;	
	
	List<Module> modules = new LinkedList<Module>();
	
	private ModuleContext(ServletContext application,GlobalContext globalContext) {
		loadModule(application, globalContext);
	}

	public void loadModule(ServletContext application,GlobalContext globalContext) {
		File modulesFolder = new File(application.getRealPath(MODULES_FOLDER));
		if (!modulesFolder.exists()) {
			logger.severe("no modules defined.");
		} else {
			File[] allModulesFolder = modulesFolder.listFiles();
			for (File dir : allModulesFolder) {
				if (dir.isDirectory()) {
					File configFile = new File(URLHelper.mergePath(dir.getAbsolutePath(), "config.properties"));
					if (configFile.exists()) {
						try {
							String webappRoot = application.getRealPath("/");
							String moduleRoot = dir.getAbsolutePath().replace(webappRoot, "/");							
							modules.add(new Module(configFile,new Locale(globalContext.getEditLanguage()),moduleRoot));
						} catch (IOException e) {
							logger.severe(e.getMessage());
							e.printStackTrace();
						}
					} else {
						logger.warning("module, bad folder structure : no 'config.properties' in " + dir + " folder.");
					}
				}
			}
		}
	}

	public static final ModuleContext getInstance(GlobalContext globalContext, HttpSession session) {		
		ModuleContext outContext = (ModuleContext) session.getAttribute(KEY);
		if (outContext == null) {
			outContext = new ModuleContext(session.getServletContext(),globalContext);
			session.setAttribute(KEY, outContext);
		}
		return outContext;
	}
	
	public Collection<Module> getAllModules() {
		return modules;
	}

	public Module getCurrentModule() {
		if (currentModule == null) {
			currentModule = modules.iterator().next();
		}
		return currentModule;
	}
	
	public void setCurrentModule(String moduleName) {
		for (Module module : modules) {
			if (module.getName().equals(moduleName)) {
				setCurrentModule(module);
			}
		}
	}

	private void setCurrentModule(Module currentModule) {
		this.currentModule = currentModule;		
	}

	/**
	 * returns the module that called the current module, null if nobody as call the module, just click on menu.
	 * as exemple : for choose templates you can call template module from admin module.
	 * @return
	 */
	public Module getFromModule() {
		return fromModule;
	}

	public void setFromModule(Module fromModule) {
		this.fromModule = fromModule;
	}
	
	public Module searchModule(String name) {
		for (Module module : modules) {
			if (module.getName().equals(name)) {
				return module;
			}
		}
		return null;
	}
	
	public void setFromModule(String moduleName) {
		setFromModule(searchModule(moduleName));
	}

}
