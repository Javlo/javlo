package org.javlo.user;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;

public class Role {
	
	Properties properties = new Properties();
	String name;
	File file;

	public Role(GlobalContext globalContext, String name) throws IOException {
		this.name = name;
		file = getFile(globalContext, name);
		if (file.exists()) {
			load(globalContext, name);
		}
	}
	
	public static File getFolder(GlobalContext globalContext) {
		return new File(URLHelper.mergePath(globalContext.getDataFolder(), "roles"));
	}
	
	protected static File getFile(GlobalContext globalContext, String name) {
		File outFile = new File(URLHelper.mergePath(getFolder(globalContext).getAbsolutePath(), name+".properties"));
		return outFile;
	}		
	
	protected synchronized void store() throws IOException {
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		ResourceHelper.writePropertiesToFile(properties, file, "javlo role : "+getName());
	}
	
	protected synchronized void load(GlobalContext globalContext, String name) throws IOException {
		 properties = ResourceHelper.loadProperties(getFile(globalContext, name));		 
	}

	public String getName() {
		return name;
	}

	public String getMailingSenders() {
		return properties.getProperty("mailing.senders", "");
	}
	
	public void setMailingSenders(String senders) throws IOException {
		properties.setProperty("mailing.senders", senders);
		store();
	}
	
	public String getTemplateIncluded() {
		return properties.getProperty("template.included", "");
	}
	
	public void setTemplateIncluded(String templates) throws IOException {
		properties.setProperty("template.included", templates);
		store();
	}
	
	public String getTemplateExcluded() {
		return properties.getProperty("template.excluded", "");
	}
	
	public void setTemplateExcluded(String templates) throws IOException {
		properties.setProperty("template.excluded", templates);
		store();
	}
}
