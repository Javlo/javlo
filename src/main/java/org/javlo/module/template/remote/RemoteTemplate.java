package org.javlo.module.template.remote;

import java.util.Date;

import org.javlo.helper.StringHelper;

public class RemoteTemplate implements IRemoteTemplate {
	
	private static final long serialVersionUID = 1L;
	
	private String zipURL;
	private String imageURL;
	private String URL;
	private String name;
	private String description;
	private String licence;	
	private String authors;
	private Date creationDate = new Date();
	
	public String getZipURL() {
		return zipURL;
	}
	public void setZipURL(String zipURL) {
		this.zipURL = zipURL;
	}
	public String getImageURL() {
		return imageURL;
	}
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLicence() {
		return licence;
	}
	public void setLicence(String licence) {
		this.licence = licence;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public String getURL() {
		return URL;
	}
	public void setURL(String uRL) {
		URL = uRL;
	}
	@Override
	public String getRenderedCreationDate() {
		return StringHelper.renderDate(creationDate);
	}
	public String getAuthors() {
		return authors;
	}
	public void setAuthors(String authors) {
		this.authors = authors;
	}
	
}
