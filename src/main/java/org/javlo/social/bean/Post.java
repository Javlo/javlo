package org.javlo.social.bean;

import java.util.Date;

import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;

public class Post {
	private long id;
	private Long mainPost;
	private String group;
	private String author;
	private String title;
	private String text;
	private String media;
	private Long parent = null;
	private Date creationDate = new Date();
	private String creationDateString;
	private Post parentPost = null;
	private int countReplies = 0;
	private boolean valid = true;
	private boolean adminValided = false;
	private String adminMessage = null;

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

	public Post getParentPost() {
		return parentPost;
	}

	public void setParentPost(Post parentPost) {
		this.parentPost = parentPost;
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

	public int getCountReplies() {
		return countReplies;
	}

	public void setCountReplies(int countReplies) {
		this.countReplies = countReplies;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getAdminMessage() {
		return adminMessage;
	}

	public void setAdminMessage(String adminMessage) {
		this.adminMessage = adminMessage;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isAdminValided() {
		return adminValided;
	}

	public void setAdminValided(boolean adminValided) {
		this.adminValided = adminValided;
	}

}
