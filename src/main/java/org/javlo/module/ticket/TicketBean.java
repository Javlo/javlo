package org.javlo.module.ticket;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.javlo.helper.BeanHelper;
import org.javlo.helper.StringHelper;

public class TicketBean implements Serializable, Ticket {

	private static final long serialVersionUID = 1L;

	public static final String SHARE_SITE = "site";
	public static final String CATEGORY_DEBUG_NOTE = "debug-note";
	public static final String STATUS_NEW = "new";

	public final int PRIORITY_NONE = 0;
	public final int PRIORITY_LOW = 1;
	public final int PRIORITY_MIDDLE = 2;
	public final int PRIORITY_HIGH = 3;

	private String id = StringHelper.getRandomId();
	private String title;
	private String share;
	private String message;
	private String context;
	private String url;
	private String authors;
	private String latestEditor;
	private String category;
	private Set<String> readers = new HashSet<String>();
	private boolean deleted = false;
	private Date creationDate = new Date();
	private Date lastUpdateDate = new Date();
	private int priority = 1;
	private String status = "new";
	private List<Comment> comments = new LinkedList<Comment>();
	private List<String> users = new LinkedList<String>();

	public TicketBean() {
	}

	public TicketBean(Ticket ticket) {
		try {
			BeanHelper.copy(ticket, this);
		} catch (Exception e) {
			throw new RuntimeException("Exception when copying Ticket", e);
		}
	}

	@Override
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	@Override
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String getAuthors() {
		return authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
		if (latestEditor == null) {
			latestEditor = authors;
		}
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public boolean isDebugNote() {
		return CATEGORY_DEBUG_NOTE.equals(this.category);
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	@Override
	public String getLatestEditor() {
		return latestEditor;
	}

	public void setLatestEditor(String latestEditor) {
		this.latestEditor = latestEditor;
	}

	@Override
	public List<Comment> getComments() {
		return comments;
	}

	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public void addComments(Comment comment) {
		this.comments.add(comment);
	}

	@Override
	public String getCreationDateLabel() {
		return StringHelper.renderSortableTime(getCreationDate());
	}

	@Override
	public String getLastUpdateDateLabel() {
		return StringHelper.renderSortableTime(getLastUpdateDate());
	}

	@Override
	public Set<String> getReaders() {
		return readers;
	}

	public void setReaders(Set<String> readers) {
		this.readers = readers;
	}

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public String getShare() {
		return share;
	}

	public void setShare(String share) {
		this.share = share;
	}

	@Override
	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	@Override
	public void onUpdate(String login) {
		readers.clear();
		onRead(login);
	}

	@Override
	public void onRead(String login) {
		readers.add(login);
	}

}
