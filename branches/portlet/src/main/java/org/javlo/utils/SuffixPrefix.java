package org.javlo.utils;

public class SuffixPrefix {

	private String prefix;
	private String suffix;
	private String name;
	
	public SuffixPrefix(String inPreffix, String inSufix, String inName) {
		prefix = inPreffix;
		suffix = inSufix;
		name = inName;
	}
	
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String preffix) {
		this.prefix = preffix;
	}
	public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String sufix) {
		this.suffix = sufix;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
