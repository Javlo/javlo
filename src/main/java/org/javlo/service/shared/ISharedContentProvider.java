package org.javlo.service.shared;

import java.net.URL;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

import org.javlo.context.ContentContext;

public interface ISharedContentProvider {
	
	public static final String TYPE_DEFAULT = "default";
	
	public static final String TYPE_TEXT = "text";
	
	public static final String TYPE_IMAGE = "image";
	
	public static final String TYPE_MULTIMEDIA = "text";
	
	public String getName();
	
	public void setName(String name);
	
	public String getLabel(Locale locale);
	
	/**
	 * get the url of the provider.  Can be null and can be only for information about provider (not webservice).
	 * @return
	 */
	public URL getURL();
	
	public Collection<SharedContent> getContent(ContentContext ctx);
	
	public Collection<SharedContent> searchContent(ContentContext ctx, String query);
	
	public Collection<SharedContent> getContent(ContentContext ctx, Collection<String> categories);
	
	public Map<String,String> getCategories(ContentContext ctx);
	
	/**
	 * return true if search is possible.
	 * @return
	 */
	public boolean isSearch();
	
	/**
	 * return true if provider have no content.
	 * @return
	 */
	public boolean isEmpty(ContentContext ctx);
	
	/**
	 * refresh the content list and the list of categories
	 */
	public void refresh(ContentContext ctx);
	
	public String getType();
	
	public int getCategoriesSize(ContentContext ctx);
	
	public int getContentSize(ContentContext ctx);

}
