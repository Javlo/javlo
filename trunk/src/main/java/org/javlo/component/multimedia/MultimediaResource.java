package org.javlo.component.multimedia;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MultimediaResource {
	private String URL;
	private String previewURL;
	private String path;
	private String description;
	private String shortDate;
	private String mediumDate;
	private String fullDate;
	private Date date;
	private String cssClass;
	private String title;
	private String relation;
	private int index;
	private String location;
	private String language;
	private String accessURL;
	private List<MultimediaResource> translation = new LinkedList<MultimediaResource>();

	public void addTranslation(MultimediaResource resource) {
		this.translation.add(resource);
	}

	public String getAccessURL() {
		return accessURL;
	}

	public String getCssClass() {
		return cssClass;
	}

	public Date getDate() {
		return date;
	}

	public String getDescription() {
		return description;
	}

	public String getFullDate() {
		return fullDate;
	}

	public int getIndex() {
		return index;
	}

	public String getLanguage() {
		return language;
	}

	public String getLocation() {
		return location;
	}

	public String getMediumDate() {
		return mediumDate;
	}

	public String getPath() {
		return path;
	}

	public String getPreviewURL() {
		return previewURL;
	}

	public String getRelation() {
		return relation;
	}

	public String getShortDate() {
		return shortDate;
	}

	public String getTitle() {
		return title;
	}

	public List<MultimediaResource> getTranslation() {
		return translation;
	}

	public String getURL() {
		return URL;
	}

	public void setAccessURL(String accessURL) {
		this.accessURL = accessURL;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setFullDate(String fullDate) {
		this.fullDate = fullDate;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setMediumDate(String mediumDate) {
		this.mediumDate = mediumDate;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setPreviewURL(String previewURL) {
		this.previewURL = previewURL;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public void setShortDate(String shortDate) {
		this.shortDate = shortDate;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

}