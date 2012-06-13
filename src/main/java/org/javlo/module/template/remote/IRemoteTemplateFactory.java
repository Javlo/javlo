package org.javlo.module.template.remote;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.javlo.remote.IRemoteResource;

public interface IRemoteTemplateFactory extends Serializable {
	
	public String getName();
	
	public void refresh() throws Exception;
	
	public boolean checkConnection();
	
	public List<IRemoteResource> getTemplates();
	
	public IRemoteResource getTemplate(String name);
	
	public Date latestUpdate();
	
	public String getSponsors();

	void setTemplates(List<IRemoteResource> templates);

}
