package org.javlo.module.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.javlo.actions.IModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.UserInterfaceContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.VFSHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;

public class ModulesContext {

	private static class ModuleOrderComparator implements Comparator<Module> {

		@Override
		public int compare(Module m1, Module m2) {
			// TODO Auto-generated method stub
			if (m1.getOrder() == m2.getOrder()) {
				return StringHelper.neverNull(m1.getName()).compareTo(StringHelper.neverNull(m2.getName()));
			}
			return m1.getOrder() - m2.getOrder();
		}

	}

	private static Logger logger = Logger.getLogger(ModulesContext.class.getName());

	static final String MODULES_FOLDER = "/modules";

	static final String EXTERNAL_MODULES_FOLDER = "/external-modules";

	static final String EXTERNAL_MODULES_FOLDER_JAR = "/external-modules-jar";

	private static final String KEY = "modulesContext";

	private Module currentModule;
	private Module fromModule;

	Collection<Module> modules = new TreeSet<Module>(new ModuleOrderComparator());
	List<Module> allModules = new LinkedList<Module>();

	private Map<String, IModuleAction> actionClass = new HashMap<String, IModuleAction>();

	private Object siteKey;

	private ModulesContext(HttpSession session, GlobalContext globalContext) throws ModuleException {
		loadModule(session, globalContext);
	}

	private static void explodeJar(File targetFolder, File jarFile) throws IOException {
		targetFolder.mkdirs();

		FileSystemManager manager = null;
		FileObject jarFS = null;
		FileObject targetRoot = null;
		try {
			manager = VFS.getManager();
			FileObject jar = manager.resolveFile(jarFile.getAbsolutePath());
			jarFS = manager.createFileSystem(jar);
			FileObject resourcesRoot = jarFS.resolveFile("/META-INF/resources");
			targetRoot = manager.resolveFile(targetFolder.getAbsolutePath());
			if (!resourcesRoot.exists()) {
				logger.warning("addon resource folder not found : " + resourcesRoot);
			} else {
				logger.info("import addon resources from '" + resourcesRoot + "' to '" + targetFolder + "'");
				targetRoot.copyFrom(resourcesRoot, new AllFileSelector());
			}
		} finally {
			VFSHelper.closeFileSystem(jarFS);
			VFSHelper.closeFileSystem(targetRoot);
			VFSHelper.closeManager(manager);
		}
	}

	public void loadModule(HttpSession session, GlobalContext globalContext) throws ModuleException {
		IUserFactory userFactory = AdminUserFactory.createUserFactory(globalContext, session);
		File modulesFolder = new File(session.getServletContext().getRealPath(MODULES_FOLDER));
		File externalModulesFolder = new File(session.getServletContext().getRealPath(EXTERNAL_MODULES_FOLDER));

		File externalModulesFolderJar = new File(session.getServletContext().getRealPath(EXTERNAL_MODULES_FOLDER_JAR));
		externalModulesFolderJar.mkdirs(); // TODO: remove this

		List<Module> localModules = new LinkedList<Module>();

		siteKey = globalContext.getContextKey();
		if (!modulesFolder.exists()) {
			logger.severe("no modules defined.");
		} else {
			File[] allModulesFolder = modulesFolder.listFiles();

			localModules.clear();
			allModules.clear();
			
			String webappRoot = session.getServletContext().getRealPath("/");
			for (File dir : allModulesFolder) {
				if (dir.isDirectory()) {
					File configFile = new File(URLHelper.mergePath(dir.getAbsolutePath(), "config.properties"));
					if (configFile.exists()) {
						try {							
							String moduleRoot = dir.getAbsolutePath().replace(webappRoot, "/");
							Module module = new Module(configFile, new Locale(globalContext.getEditLanguage(session)), moduleRoot, globalContext.getPathPrefix());
							if (module.haveRight(session, userFactory.getCurrentUser(session)) && globalContext.getModules().contains(module.getName())) {
								localModules.add(module);
							}
							allModules.add(module);
						} catch (IOException e) {
							logger.severe(e.getMessage());
							e.printStackTrace();
						}
					} else {
						logger.severe("module, bad folder structure : no 'config.properties' in " + dir + " folder.");
					}
				} else {
					logger.warning("file found in modules folder : "+dir);
				}
			}

			if (externalModulesFolderJar != null) {
				for (File extFile : externalModulesFolderJar.listFiles()) {
					if (StringHelper.getFileExtension(extFile.getName()).toLowerCase().equals("jar")) {
						File targetFolder = new File(URLHelper.mergePath(externalModulesFolder.getAbsolutePath(), StringHelper.getFileNameWithoutExtension(extFile.getName())));
						try {
							explodeJar(targetFolder, extFile);
							File configFile = new File(targetFolder, "config.properties");
							if (configFile.exists()) {
								String moduleRoot = targetFolder.getAbsolutePath().replace(webappRoot, "/");
								ExternalModule module = new ExternalModule(configFile, new Locale(globalContext.getEditLanguage(session)), moduleRoot, globalContext.getPathPrefix());
								module.setAction(getExternalActionModule(module.getActionName(), extFile));
								if (module.haveRight(session, userFactory.getCurrentUser(session)) && globalContext.getModules().contains(module.getName())) {
									localModules.add(module);
								} else {
									logger.info("user no right for module : "+module.getName());
								}
								allModules.add(module);								
							} else {
								logger.warning("bad external module format : " + extFile);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
			if (allModules.size() == 0) {
				/*System.out.println("***** ModulesContext.loadModule : globalContext = "+globalContext.getContextKey()); //TODO: remove debug trace
				System.out.println("***** ModulesContext.loadModule : user = "+userFactory.getCurrentUser(session)); //TODO: remove debug trace
				System.out.println("***** ModulesContext.loadModule : SESSION = "+session); //TODO: remove debug trace
				System.out.println("***** ModulesContext.loadModule : allModulesFolder length = "+allModulesFolder.length); //TODO: remove debug trace*/
				throw new ModuleException("javlo need at least one module.");
			}
			
			Collections.sort(allModules, new ModuleOrderComparator());
			// load module needed
			List<Module> neededModules = new LinkedList<Module>();
			List<Module> localAllModules = new LinkedList<Module>(allModules);
			for (Module module : localModules) {
				Collection<String> neededModulesName = module.getNeeded();
				if (neededModulesName != null) {
					for (String needed : neededModulesName) {
						for (Module mod : localAllModules) {
							if (mod.getName().equals(needed)) {
								neededModules.add(mod);
							}
						}
					}
				}
			}

			for (Module module : neededModules) {
				if (!localModules.contains(module)) {
					localModules.add(module);
				}
			}
			

			if (localModules.size() == 0) { // if no module defined >>>> add
											// admin and user module for config
											// javlo.
				logger.warning("no module defined for : " + globalContext.getContextKey());
				for (Module module : allModules) {
					if (module.getName().equals("admin") || module.getName().equals("user")) {
						localModules.add(module);
					}
				}
			}

			if (localModules.size() == 0) { // if
				logger.severe("module admin or user not found, all module had selected.");
				localModules.addAll(allModules);
			}
		}

		Map<String, Module> modulesMap = new HashMap<String, Module>();
		for (Module module : localModules) {
			if (module.getParent() == null) {
				modulesMap.put(module.getName(), module);
			}
		}
		synchronized (modules) {
			modules.clear();
			for (Module module : localModules) {
				modules.add(module);
				if (module.getParent() != null) {
					Module parent = modulesMap.get(module.getParent());
					if (parent != null) {
						parent.addChild(module);
					} else {
						logger.warning("parent not found : " + module.getParent());
					}
				}
			}
		}
	}

	private IModuleAction getExternalActionModule(String actionName, File jarFile) {
		IModuleAction outAction = actionClass.get(actionName);
		if (outAction == null) {
			try {
				URLClassLoader child = new URLClassLoader(new URL[] { jarFile.toURL() }, this.getClass().getClassLoader());
				outAction = (IModuleAction) Class.forName(actionName, true, child).newInstance();
				actionClass.put(actionName, outAction);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return outAction;
	}

	public static final ModulesContext getInstance(HttpSession session, GlobalContext globalContext) throws ModuleException {
		ModulesContext outContext = (ModulesContext) globalContext.getSessionAttribute(session, KEY);
		if (outContext == null || !outContext.siteKey.equals(globalContext.getContextKey())) {
			outContext = new ModulesContext(session, globalContext);
			I18nAccess i18nAccess;
			try {
				UserInterfaceContext uic = UserInterfaceContext.getInstance(session, globalContext);
				if (uic.getCurrentModule() != null) {
					outContext.setCurrentModule(uic.getCurrentModule());
					globalContext.setSessionAttribute(session, KEY, outContext);
				}
				i18nAccess = I18nAccess.getInstance(globalContext, session);
				i18nAccess.setCurrentModule(globalContext, session, outContext.getCurrentModule());
			} catch (Exception e) {
				e.printStackTrace();
				throw new ModuleException(e.getMessage());
			}
		}
		if (!outContext.isCurrentModule()) {
			UserInterfaceContext uic = UserInterfaceContext.getInstance(session, globalContext);
			if (uic.getCurrentModule() != null) {
				outContext.setCurrentModule(uic.getCurrentModule());
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

	public boolean isCurrentModule() {
		return currentModule != null;
	}

	public void setCurrentModule(String moduleName) {
		setCurrentModule((Module) null);
		for (Module module : modules) {
			if (module.getName().equals(moduleName)) {
				this.currentModule = module;
			}
		}
	}

	private void setCurrentModule(Module currentModule) {
		if (currentModule == null) {
			this.currentModule = null;
		} else {
			setCurrentModule(currentModule.getName());
		}
	}

	/**
	 * returns the module that called the current module, null if nobody as call
	 * the module, just click on menu. as exemple : for choose templates you can
	 * call template module from admin module.
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

	public void initContext(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);

		ContentContext ctx = ContentContext.getContentContext(request, response);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		
		if (requestService.getParameter("module", null) != null) {
			UserInterfaceContext uic = UserInterfaceContext.getInstance(request.getSession(), globalContext);
			uic.setCurrentModule(requestService.getParameter("module", null));
			setCurrentModule(requestService.getParameter("module", null));
			I18nAccess i18nAccess = I18nAccess.getInstance(request);
			i18nAccess.setCurrentModule(globalContext, ctx.getRequest().getSession(), getCurrentModule());
		}
		if (requestService.getParameter("module", null) != null && requestService.getParameter("from-module", null) == null) {
			setFromModule((Module) null);
		}
		if (requestService.getParameter("from-module", null) != null) {
			Module fromModule = searchModule(requestService.getParameter("from-module", null));
			if (fromModule != null) {
				setFromModule(fromModule);
			}
		}
	}

}
