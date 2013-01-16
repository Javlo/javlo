package org.javlo.portlet;

import org.apache.pluto.container.ResourceURLProvider;

public class ResourceURLProviderImpl implements ResourceURLProvider {

	final private String basePath;
	private String fullPath;
	
	public ResourceURLProviderImpl(String basePath) {
		this.basePath = basePath;
	}
	
	public void setAbsoluteURL(String path) {
		this.fullPath = path;
	}

	public void setFullPath(String path) {
		this.fullPath = basePath + path;
	}

	public String toString() {
		return fullPath;
	}
}
