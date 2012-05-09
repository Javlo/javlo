package org.javlo.utils;

public class SufixPreffix {

	private String preffix;
	private String sufix;
	private String name;
	
	public SufixPreffix(String inPreffix, String inSufix, String inName) {
		preffix = inPreffix;
		sufix = inSufix;
		name = inName;
	}
	
	public String getPreffix() {
		return preffix;
	}
	public void setPreffix(String preffix) {
		this.preffix = preffix;
	}
	public String getSufix() {
		return sufix;
	}
	public void setSufix(String sufix) {
		this.sufix = sufix;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
