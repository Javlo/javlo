package org.javlo.template;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.filefilter.NoPropertiesFileFilter;

public class TemplatePlugin {

	private static Logger logger = Logger.getLogger(TemplatePlugin.class.getName());

	private static final String CONFIG_FILE = "config.properties";
	
	public static final String HOME_KEY = "${plugin.home}";

	private File dir = null;

	Properties config;

	public static TemplatePlugin getInstance(File templatePluginFolder) throws IOException {
		TemplatePlugin templatePlugin = new TemplatePlugin();
		templatePlugin.dir = templatePluginFolder;
		if (!templatePlugin.dir.exists()) {
			return null;
		} else {
			templatePlugin.init();
			return templatePlugin;
		}
	}

	private File createLocalFile(String localPath) {
		return new File(URLHelper.mergePath(dir.getAbsolutePath(), localPath));
	}

	private void init() throws IOException {
		config = ResourceHelper.loadProperties(createLocalFile(CONFIG_FILE));
	}

	public String getHTMLHead(GlobalContext globalContext) throws IOException {		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		for (int i = 0; i < 10000; i++) {
			String head = config.getProperty("head."+i);
			if (head != null) {
				out.println(head);
			}
		}
		out.close();
		
		String outHead = new String(outStream.toByteArray());
		
		/** load external config **/
		Properties externalConfig = loadConfig(globalContext.getTemplatePluginConfig());		
		for (Object key : externalConfig.keySet()) {
			outHead = outHead.replace("${"+key+"}", externalConfig.getProperty(key.toString()));
		}
		
		/** load default plugin config **/		
		for (Object key : config.keySet()) {
			if (key.toString().startsWith("config.")) {
				outHead = outHead.replace("${"+key+"}", config.getProperty(key.toString()));
			}
		}
		
		return outHead;

	}

	public void importInTemplate(ContentContext ctx, File templateFolder) throws IOException {		
		if (!templateFolder.isDirectory()) {
			logger.warning("template folder not found : " + templateFolder);
			return;
		}
		File importFolder = new File(URLHelper.mergePath(templateFolder.getAbsolutePath(), getFolder()));
		if (dir.exists()) {
			logger.info("import template plugin from '" + dir + "' to '" + templateFolder + "'");
			FileUtils.copyDirectory(dir, importFolder, new NoPropertiesFileFilter());
		} else {
			logger.severe("folder not found : " + dir);
		}
	}

	public String getName() {
		return config.getProperty("name");
	}

	public String getLabel() {
		return config.getProperty("label", getName());
	}

	public String getVersion() {
		return config.getProperty("version", "?");
	}

	/**
	 * combination of name and version.
	 * @return
	 */
	public String getId() {
		return getName() + "__" + getVersion();
	}
	
	/**
	 * folder of the plugin inside the template.
	 * @return
	 */
	public String getFolder() {
		return URLHelper.mergePath(Template.PLUGIN_FOLDER, StringHelper.stringToFileName(getId()));
	}
	
	protected Properties loadConfig (String config) {
		Properties outConfig = new Properties();		
		try {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			out.println(config);
			out.println(this.config.getProperty("template.plugins.config", ""));
			out.close();			
			outConfig.load(new StringReader(new String(outStream.toByteArray())));
		} catch (IOException e) { // impossible width a string ?		
			e.printStackTrace();
		}
		return outConfig;
	}

}
