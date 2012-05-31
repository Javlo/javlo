package org.javlo.module.template.remote;

import java.io.Serializable;
import java.util.Date;

public interface IRemoteTemplate extends Serializable {
	
	public String getZipURL();
	
	public String getImageURL();
	
	public String getURL();
	
	public String getName();
	
	public String getAuthors();
	
	public String getDescription();
	
	public String getLicence();
	
	public Date getCreationDate();
	
	public String getRenderedCreationDate();

}
