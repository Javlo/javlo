package org.javlo.social.bean;

import java.util.Date;

import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;

public class Post {
	private long id;
	private Long mainPost;
	private String author;
	private String text;
	private String media;
	private Long parent = null;
	private Date creationDate = new Date();
	private String creationDateString;

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getText() {
		return text;
	}
	public void setText(String message) {
		this.text = message;
	}
	public String getMedia() {
		return media;
	}
	public void setMedia(String media) {
		this.media = media;
	}
	public Long getParent() {
		return parent;
	}
	public void setParent(Long parent) {
		this.parent = parent;
	}
	public Date getCreationDate() {
		if (creationDate == null) {
			return TimeHelper.NO_DATE;
		} else {
			return creationDate;
		}
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
		creationDateString = StringHelper.renderTime(getCreationDate());
	}
	
	public String getCreationDateString() {
		return creationDateString;
	}
	public void setCreationDateString(String creationDateString) {
		this.creationDateString = creationDateString;
	}
	public Long getMainPost() {
		return mainPost;
	}
	public void setMainPost(Long mainPost) {
		this.mainPost = mainPost;
	}
}
