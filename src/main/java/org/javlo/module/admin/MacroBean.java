package org.javlo.module.admin;

public class MacroBean {
	
	private String name;
	private String info;
	
	public MacroBean(String name, String info) {
		super();
		this.name = name;
		this.info = info;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	@Override
	public String toString() {	
		return name;
	}

}
