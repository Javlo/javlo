package org.javlo.module.core;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.javlo.context.GlobalContext;
import org.javlo.context.UserInterfaceContext;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;

public class ModulesContext {

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

	private static Logger logger = Logger.getLogger(ModulesContext.class.getName());

	static final String MODULES_FOLDER = "/modules";

	private static final String KEY = "modulesContext";

	private Module currentModule;
	private Module fromModule;

	Collection<Module> modules = new TreeSet<Module>(new ModuleOrderComparator());
	Collection<Module> allModules = new TreeSet<Module>(new ModuleOrderComparator());

	private Object siteKey;

	private ModulesContext(HttpSession session, GlobalContext globalContext) throws ModuleException {
		loadModule(session, globalContext);
	}

	public void loadModule(HttpSession session, GlobalContext globalContext) throws ModuleException {
		IUserFactory userFactory = AdminUserFactory.createUserFactory(globalContext, session);
		File modulesFolder = new File(session.getServletContext().getRealPath(MODULES_FOLDER));
		siteKey = globalContext.getContextKey();
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
							Module module = new Module(configFile, new Locale(globalContext.getEditLanguage()), moduleRoot);

							if (module.haveRight(session, userFactory.getCurrentUser(session)) && globalContext.getModules().contains(module.getName())) {
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
			
			if (allModules.size() == 0) {
				throw new ModuleException("javlo need at least one module.");
			}
			
			if (modules.size() == 0) { // if no module defined >>>> add admin and user module for config javlo.
				logger.warning("no module defined for : "+globalContext.getContextKey());
				for (Module module : allModules) {
					if (module.getName().equals("admin") || module.getName().equals("user")) {
						modules.add(module);
					}
				}
			}
			
			if (modules.size() == 0) { // if 
				logger.severe("module admin or user not found, all module had selected.");
				modules.addAll(allModules);
			}
		}
	}

	public static final ModulesContext getInstance(HttpSession session, GlobalContext globalContext) throws ModuleException {
		ModulesContext outContext = (ModulesContext) session.getAttribute(KEY);
		if (outContext == null || !outContext.siteKey.equals(globalContext.getContextKey())) {
			outContext = new ModulesContext(session, globalContext);
			session.setAttribute(KEY, outContext);
			I18nAccess i18nAccess;
			try {
				UserInterfaceContext uic = UserInterfaceContext.getInstance(session, globalContext);				
				if (uic.getCurrentModule() != null) {
					outContext.setCurrentModule(uic.getCurrentModule());
				}
				i18nAccess = I18nAccess.getInstance(globalContext, session);
				i18nAccess.setCurrentModule(globalContext, outContext.getCurrentModule());
			} catch (Exception e) {				
				e.printStackTrace();
				throw new ModuleException(e.getMessage());
			}			
		}
		return outContext;
	}

	public Collection<Module> getModules() {
		return modules;
	}

	public Collection<Module> getAllModules() {
		return allModules;
	}

	public Module getCurrentModule() throws Exception {
		if (currentModule == null) {
			if (modules.size() > 0) {
				currentModule = modules.iterator().next();
			} else {
				throw new Exception("no modules defined.");
			}
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
	 * returns the module that called the current module, null if nobody as call the module, just click on menu. as exemple : for choose templates you can call template module from admin module.
	 * 
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
