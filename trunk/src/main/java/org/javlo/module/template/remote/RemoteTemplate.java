package org.javlo.module.template.remote;

import java.util.Date;

import org.javlo.helper.StringHelper;
import org.javlo.remote.IRemoteResource;

public class RemoteTemplate implements IRemoteResource {
	
	private static final long serialVersionUID = 1L;
	
	private String downloadURL;
	private String imageURL;
	private String URL;
	private String name;
	private String description;
	private String licence;	
	private String authors;
	private String id = StringHelper.getRandomId();	
	private Date creationDate = new Date();
	private String category = null;

	
	@Override
	public String getDownloadURL() {
		return downloadURL;
	}
	public void setDownloadURL(String zipURL) {
		this.downloadURL = zipURL;
	}
	@Override
	public String getImageURL() {
		return imageURL;
	}
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}
	@Override
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Override
	public String getLicence() {
		return licence;
	}
	public void setLicence(String licence) {
		this.licence = licence;
	}
	public String getCreationDate() {
		return StringHelper.renderDate(getDate());
	}
	@Override
	public Date getDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	@Override
	public String getURL() {
		return URL;
	}
	public void setURL(String uRL) {
		URL = uRL;
	}
	@Override
	public String getAuthors() {
		return authors;
	}
	public void setAuthors(String authors) {
		this.authors = authors;
	}
	@Override
	public void setDate(Date date) {
		this.creationDate = date;
	}
	@Override
	public String getType() {	
		return "template";
	}
	@Override
	public String getId() {
		return id;
	}
	@Override
	public void setId(String id) {
		this.id = id;
	}
	@Override
	public String getCategory() {
		return category;
	}
	@Override
	public void setCategory(String category) {
		this.category = category;
	}
	@Override
	public String getDateAsString() {
		return StringHelper.renderDate(getDate());
	}	
}
