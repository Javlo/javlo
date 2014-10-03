package org.javlo.module.core;


public class ModuleBean {
	
	private String name;
	private String title;
	private String url;

	public ModuleBean(Module module) {
		setName(module.getName());
		setTitle(module.getTitle());
		url = "http://10.0.1.10:8080/edit/fr/root.html?module=" + module.getName();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	

}
