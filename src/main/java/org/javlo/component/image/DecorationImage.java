/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.image;

import java.util.Collection;
import java.util.Collections;

import org.javlo.component.core.ComponentBean;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;
import org.javlo.service.resource.Resource;

/**
 * @author pvandermaesen
 */
public class DecorationImage extends GlobalImage {

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {
		super.init(bean, newContext);
	}

	@Override
	public String getType() {
		return "decoration-image";
	}

	@Override
	protected boolean canUpload(ContentContext ctx) {
		return false;
	}

	@Override
	public String getFileDirectory(ContentContext ctx) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		String imageFolder = URLHelper.mergePath(staticConfig.getShareDataFolder(), staticConfig.getShareImageFolder());
		return imageFolder;
	}

	@Override
	public String getResourceURL(ContentContext ctx, String fileLink) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		String imageFolder = URLHelper.mergePath(staticConfig.getShareDataFolderKey(), staticConfig.getShareImageFolder());
		return URLHelper.mergePath(imageFolder, URLHelper.mergePath(getDirSelected(), fileLink));
	}

	@Override
	protected boolean isLinkToStatic() {
		return false;
	}

	@Override
	/**
	 * no importation of shared image.
	 */
	public Collection<Resource> getAllResources(ContentContext ctx) {
		return Collections.EMPTY_LIST;
	}

}
