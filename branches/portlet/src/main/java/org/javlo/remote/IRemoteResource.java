package org.javlo.remote;

import java.io.Serializable;
import java.util.Date;

public interface IRemoteResource extends Serializable {
	
	public static final String TYPE_TEMPLATE = "template";
	public static final String TYPE_COMPONENT = "component";
	public static final String TYPE_MODULE = "module";
	
	public String getDownloadURL();
	
	public String getImageURL();
	
	public String getURL();
	
	public String getName();
	
	public String getAuthors();
	
	public String getDescription();
	
	public String getLicence();
	
	public String getType();
	
	public String getCategory();
	
	public String getDateAsString();
	
	public String getVersion();
	
	public Date getDate();
	
	public String getId();
	
	public void setDownloadURL(String url);
	
	public void setImageURL(String url);
	
	public void setURL(String url);
	
	public void setName(String name);
	
	public void setAuthors(String authors);
	
	public void setDescription(String description);
	
	public void setLicence(String licence);
	
	public void setDate(Date date);
	
	public void setId(String id);
	
	public void setCategory(String category);
	
	public void setVersion(String version);

}
