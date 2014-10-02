package org.javlo.module.core;

public class ModuleBean {
	
	private String name;
	private String title;

	public ModuleBean(Module module) {
		setName(module.getName());
		setTitle(module.getTitle());
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
	
	

}
