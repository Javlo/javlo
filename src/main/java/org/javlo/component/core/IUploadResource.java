package org.javlo.component.core;

import org.javlo.context.ContentContext;

public interface IUploadResource {
	public String performUpload(ContentContext ctx) throws Exception;
	public boolean isUploadOnDrop();
}
