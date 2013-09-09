package org.javlo.template;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.javlo.config.StaticConfig;
import org.javlo.filter.DirectoryFilter;

public class TemplatePluginFactory {

	private static Logger logger = Logger.getLogger(TemplatePluginFactory.class.getName());

	private static final String PLUGIN_LOCAL_FOLDER = "/WEB-INF/template-plugin";

	private File dir = null;
	private File localDir = null;

	private static final String KEY = TemplatePluginFactory.class.getName();

	public static TemplatePluginFactory getInstance(ServletContext application) {
		TemplatePluginFactory fact = (TemplatePluginFactory) application.getAttribute(KEY);
		if (fact == null) {
			fact = new TemplatePluginFactory();
			fact.dir = new File(StaticConfig.getInstance(application).getTemplatePluginFolder());
			fact.localDir = new File(application.getRealPath(PLUGIN_LOCAL_FOLDER));
			if (!fact.dir.isDirectory()) {
				logger.warning("template plugin folder not found : " + fact.dir);
			}
		}
		return fact;
	}

	public List<TemplatePlugin> getAllTemplatePlugin() throws IOException {
		if (!dir.isDirectory() && !localDir.isDirectory()) {
			return Collections.EMPTY_LIST;
		}
		List<TemplatePlugin> allTemplatePlungin = new LinkedList<TemplatePlugin>();
		if (dir.isDirectory()) {
			File[] templatePluginFolders = dir.listFiles(new DirectoryFilter());
			for (File folder : templatePluginFolders) {
				TemplatePlugin tp = TemplatePlugin.getInstance(folder);
				allTemplatePlungin.add(tp);
			}
		}
		if (localDir.isDirectory()) {
			File[] templatePluginFolders = localDir.listFiles(new DirectoryFilter());
			for (File folder : templatePluginFolders) {
				TemplatePlugin tp = TemplatePlugin.getInstance(folder);
				allTemplatePlungin.add(tp);
			}
		}
		return allTemplatePlungin;
	}

	public List<TemplatePlugin> getAllTemplatePlugin(Collection<String> ids) throws IOException {
		if (!dir.isDirectory() && !localDir.isDirectory()) {
			return Collections.EMPTY_LIST;
		}
		List<TemplatePlugin> allTemplatePlungin = new LinkedList<TemplatePlugin>();
		for (String id : ids) {
			TemplatePlugin tp = getTemplatePlugin(id);
			allTemplatePlungin.add(tp);
		}
		return allTemplatePlungin;
	}

	public TemplatePlugin getTemplatePlugin(String name, String version) throws IOException {
		List<TemplatePlugin> plugins = getAllTemplatePlugin();
		for (TemplatePlugin templatePlugin : plugins) {
			if (templatePlugin.getName().equals(name) && templatePlugin.getVersion().equals(version)) {
				return templatePlugin;
			}
		}
		return null;
	}

	public TemplatePlugin getTemplatePlugin(String id) throws IOException {
		List<TemplatePlugin> plugins = getAllTemplatePlugin();
		for (TemplatePlugin templatePlugin : plugins) {
			if (templatePlugin.getId().equals(id)) {
				return templatePlugin;
			}
		}
		return null;
	}

}
