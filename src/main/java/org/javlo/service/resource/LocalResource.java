package org.javlo.service.resource;

import java.io.File;

import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;

public class LocalResource extends Resource {

	private File file = null;

	public LocalResource(ContentContext ctx, File file) {
		setUri(URLHelper.createLocalURI(ctx, file));
		setName(file.getName());
		setId("" + file.hashCode());
		setFile(file);
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

}
