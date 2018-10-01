package org.javlo.social.bean;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;

public class Post {
	private long id;
	private Long mainPost;
	private String group;
	private String author;
	private String latestContributor;
	private String title;
	private String text;
	private String media;
	private Long parent = null;
	private Date creationDate = new Date();
	private String creationDateString;
	private Date latestUpdate = new Date();
	private String latestUpdateString;
	private Post parentPost = null;
	private int countReplies = 0;
	private boolean uncheckedChild = false;
	private boolean valid = true;
	private boolean adminValided = false;
	private String adminMessage = null;
	private String authorIp = null;
	private Collection<String> contributors = new LinkedList<String>();

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

	public String getAuthorIp() {
		return authorIp;
	}

	public void setAuthorIp(String authorIp) {
		this.authorIp = authorIp;
	}

	public boolean isUncheckedChild() {
		return uncheckedChild;
	}

	public void setUncheckedChild(boolean uncheckedChild) {
		this.uncheckedChild = uncheckedChild;
	}

	public Collection<String> getContributors() {
		return contributors;
	}

	public void setContributors(Collection<String> contributors) {
		this.contributors = contributors;
	}

	public Date getLatestUpdate() {
		if (latestUpdate == null) {
			return creationDate;
		} else {
			return latestUpdate;
		}
	}

	public void setLatestUpdate(Date latestUpdate) {
		this.latestUpdate = latestUpdate;
		latestUpdateString = StringHelper.renderTime(latestUpdate);
	}

	public String getLatestUpdateString() {
		if (latestUpdateString == null) {
			latestUpdateString = StringHelper.renderTime(getLatestUpdate());
		}
		return latestUpdateString;
	}

	public void setLatestUpdateString(String latestUpdateString) {
		this.latestUpdateString = latestUpdateString;
	}

	public String getLatestContributor() {
		return latestContributor;
	}

	public void setLatestContributor(String latestContributor) {
		this.latestContributor = latestContributor;
	}

}
