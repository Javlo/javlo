package org.javlo.module;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.javlo.context.GlobalContext;
import org.javlo.helper.URLHelper;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;

public class ModuleContext {
	
	private static class ModuleOrderComparator implements Comparator<Module> {

		@Override
		public int compare(Module m1, Module m2) {
			// TODO Auto-generated method stub
			if (m1.getOrder() == m2.getOrder()) {
				return 1;
			}
			return m1.getOrder() - m2.getOrder();
		}
		
	}

	private static Logger logger = Logger.getLogger(ModuleContext.class.getName());

	static final String MODULES_FOLDER = "/modules";

	private static final String KEY = "module";

	private Module currentModule;
	private Module fromModule;	
	
	Collection<Module> modules = new TreeSet<Module>(new ModuleOrderComparator());
	Collection<Module> allModules = new TreeSet<Module>(new ModuleOrderComparator());
	
	private ModuleContext(HttpSession session, GlobalContext globalContext ) {
		loadModule(session, globalContext);
	}

	public void loadModule(HttpSession session, GlobalContext globalContext ) {
		IUserFactory userFactory = AdminUserFactory.createUserFactory(globalContext, session);
		File modulesFolder = new File(session.getServletContext().getRealPath(MODULES_FOLDER));
		if (!modulesFolder.exists()) {
			logger.severe("no modules defined.");
		} else {
			File[] allModulesFolder = modulesFolder.listFiles();
			
			modules.clear();
			allModules.clear();
			
			for (File dir : allModulesFolder) {
				if (dir.isDirectory()) {
					File configFile = new File(URLHelper.mergePath(dir.getAbsolutePath(), "config.properties"));
					if (configFile.exists()) {
						try {
							String webappRoot = session.getServletContext().getRealPath("/");
							String moduleRoot = dir.getAbsolutePath().replace(webappRoot, "/");
							Module module = new Module(configFile,new Locale(globalContext.getEditLanguage()),moduleRoot);
							if (module.haveRight(userFactory.getCurrentUser(session)) && globalContext.getModules().contains(module.getName())) {								
								modules.add(module);
							}
							allModules.add(module);
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
			outContext = new ModuleContext(session,globalContext);
			session.setAttribute(KEY, outContext);
		}
		return outContext;
	}
	
	public Collection<Module> getModules() {
		return modules;
	}
	
	public Collection<Module> getAllModules() {
		return allModules;
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
