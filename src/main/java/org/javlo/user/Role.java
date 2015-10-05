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
	Role parent = null;

	public Role(GlobalContext globalContext, String name) throws IOException {
		this.name = name;
		file = getFile(globalContext, name);
		if (file.exists()) {
			load(globalContext, name);
		}
		parent = getParent(globalContext);
	}
	
	public Role getParent(GlobalContext globalContext) throws IOException {
		String parent = getParent();
		if (parent != null && parent.length() > 0) {
			return new Role(globalContext, getParent());
		} else {
			return null;
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
		String senders = getLocalMailingSenders();
		if (senders.length() == 0 && parent != null) {
			return parent.getMailingSenders();
		} else {
			return senders;
		}
	}
	
	public String getLocalMailingSenders() {		
		return properties.getProperty("mailing.senders", "");
	}
	
	public void setMailingSenders(String senders) throws IOException {
		properties.setProperty("mailing.senders", senders);
		store();
	}
	
	public String getTemplateIncluded() {
		String included = getLocalTemplateIncluded();
		if (included.length() == 0 && parent != null) {
			return parent.getTemplateIncluded();
		} else {
			return included;
		}
	}
	
	public String getLocalTemplateIncluded() {
		return properties.getProperty("template.included", "");
	}
	
	public void setTemplateIncluded(String templates) throws IOException {
		properties.setProperty("template.included", templates);
		store();
	}
	
	public String getTemplateExcluded() {
		String excluded = getLocalTemplateExcluded();
		if (excluded.length() == 0 && parent != null) {
			return parent.getTemplateExcluded();
		} else {
			return excluded;
		}
	}
	
	public String getLocalTemplateExcluded() {
		return properties.getProperty("template.excluded", "");
	}
	
	public void setTemplateExcluded(String templates) throws IOException {
		properties.setProperty("template.excluded", templates);
		store();
	}
	
	public String getParent() {
		return properties.getProperty("parent", "");
	}
	
	public Role getParentRole() {
		return parent;
	}
	
	public void setParent(String parent) throws IOException {
		properties.setProperty("parent", parent);
		store();
	}
}
