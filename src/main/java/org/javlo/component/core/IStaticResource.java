package org.javlo.component.core;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.javlo.context.ContentContext;

/**
 * represent a static resource with meta data.
 * 
 * @author Patrick Vandermaesen
 * 
 */
public interface IStaticResource {

	public String getURL(ContentContext ctx);

	public String getCssClass(ContentContext ctx);

	public Date getDate(ContentContext ctx);

	public String getDescription(ContentContext ctx);

	public String getLocation(ContentContext ctx);

	public String getTitle(ContentContext ctx);

	public String getPreviewURL(ContentContext ctx, String fitler);

	public String getLanguage(ContentContext ctx);

	public List<String> getTags(ContentContext ctx);

	public File getFile(ContentContext ctx);

	public boolean isShared(ContentContext ctx);
	
	/**
	 * return true if resource is local to the page, false if resource is global to the site.
	 * exemple : imported image is local to the page.
	 * @param ctx
	 * @return
	 */
	public boolean isLocal(ContentContext ctx);
	

}
