package org.javlo.service.shared;

import java.net.URL;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public interface ISharedContentProvider {
	
	public String getName();
	
	public String getLabel(Locale locale);
	
	/**
	 * get the url of the provider.  Can be null and can be only for information about provider (not webservice).
	 * @return
	 */
	public URL getURL();
	
	public Collection<SharedContent> getContent();
	
	public Collection<SharedContent> searchContent(String query);
	
	public Collection<SharedContent> getContent(Collection<String> categories);
	
	public Map<String,String> getCategories();
	
	/**
	 * return true if provider have no content.
	 * @return
	 */
	public boolean isEmpty();
	
	/**
	 * refresh the content list and the list of categories
	 */
	public void refresh();

}
