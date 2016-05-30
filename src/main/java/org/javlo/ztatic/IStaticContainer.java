package org.javlo.ztatic;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.javlo.bean.Link;
import org.javlo.context.ContentContext;
import org.javlo.service.resource.Resource;

public interface IStaticContainer {

	/**
	 * check if the class contains the static resources define by a URI
	 * 
	 * @param uri
	 *            a uri to a static resources
	 * @return true if the class conatins this static resources.
	 */
	public boolean contains(ContentContext ctx, String uri);

	/**
	 * return all resources found in the component.
	 * 
	 * @return
	 */
	public Collection<Resource> getAllResources(ContentContext ctx);

	/**
	 * rename a resource
	 * 
	 * @param oldName
	 * @param newName
	 * @return true if rename, false else
	 */
	public boolean renameResource(ContentContext ctx, File oldName, File newName);

	/**
	 * create link to resources.
	 * 
	 * @return
	 */
	public Collection<Link> getAllResourcesLinks(ContentContext ctx);

	/**
	 * return indice of popularity
	 * 
	 * @param ctx
	 * @return
	 */
	public int getPopularity(ContentContext ctx);
	
	/**
	 * set folder with resources
	 * @param dir
	 */
	public void setDirSelected(String dir);
	
	/**
	 * get folder with ressources
	 * @return
	 */	
	public String getDirSelected();
	
	public List<File> getFiles(ContentContext ctx);

}
