package org.javlo.module.ticket;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.javlo.helper.StringHelper;

public class TicketBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public final int PRIORITY_LOW = 1;
	public final int PRIORITY_MIDDLE = 2;
	public final int PRIORITY_HIGH = 3;

	private String id = StringHelper.getRandomId();
	private String title;
	private String message;
	private String context;
	private String url;
	private String authors;
	private String latestEditor;
	private String category;
	private boolean read = false;
	private boolean deleted = false;
	private Date creationDate = new Date();
	private Date lastUpdateDate = new Date();
	private int priority = 1;
	private String status = "new";
	private List<Comment> comments = new LinkedList<Comment>();

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAuthors() {
		return authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
		if (latestEditor == null) {
			latestEditor = authors;
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public String getLatestEditor() {
		return latestEditor;
	}

	public void setLatestEditor(String latestEditor) {
		this.latestEditor = latestEditor;
	}

	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public void addComments(Comment comment) {
		this.comments.add(comment);
	}

	public String getCreationDateLabel() {
		return StringHelper.renderSortableTime(getCreationDate());
	}

	public String getLastUpdateDateLabel() {
		return StringHelper.renderSortableTime(getLastUpdateDate());
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
}
