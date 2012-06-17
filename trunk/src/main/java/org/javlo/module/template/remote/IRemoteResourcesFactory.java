package org.javlo.module.template.remote;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.javlo.remote.IRemoteResource;

public interface IRemoteResourcesFactory extends Serializable {
	
	public String getName();
	
	public void refresh() throws Exception;
	
	public boolean checkConnection();
	
	public List<IRemoteResource> getResources();
	
	public IRemoteResource getResource(String name);
	
	public Date latestUpdate();
	
	public String getSponsors();

	void setResources(List<IRemoteResource> resources);

}
