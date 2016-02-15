package org.javlo.module.template;

public class PageTemplateRef {
	
	private String label;
	private String context;
	private String url;
	private String usage;
	
	public PageTemplateRef(String page, String context, String url, String ref) {
		super();
		this.label = page;
		this.context = context;
		this.url = url;
		this.usage = ref;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String page) {
		this.label = page;
	}
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUsage() {
		return usage;
	}
	public void setUsage(String ref) {
		this.usage = ref;
	}
}