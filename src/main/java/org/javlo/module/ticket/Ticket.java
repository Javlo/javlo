package org.javlo.module.ticket;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface Ticket {
	
	public static final String STATUS_NEW = "new";	
	public static final String STATUS_WORKING = "working";	
	public static final String STATUS_ONHOLD = "on hold";
	public static final String STATUS_REJECTED = "rejected";
	public static final String STATUS_DONE = "done";
	public static final String STATUS_ARCHIVED = "archived";
	
	public static final List<String> STATUS = Arrays.asList(new String[] {STATUS_NEW,STATUS_WORKING,STATUS_ONHOLD,STATUS_REJECTED,STATUS_DONE,STATUS_ARCHIVED });
	
	String getTitle();

//	void setTitle(String title);

	String getMessage();	
	
	public String getHtmlMessage();

//	void setMessage(String message);

	String getContext();

//	void setContext(String context);

	String getUrl();

//	void setUrl(String url);

	int getPriority();

//	void setPriority(int priority);

	String getStatus();

//	void setStatus(String status);

	String getAuthors();

//	void setAuthors(String authors);

	String getId();

//	void setId(String id);

	String getCategory();

//	void setCategory(String category);

	boolean isDebugNote();

	Date getCreationDate();

//	void setCreationDate(Date creationDate);

	Date getLastUpdateDate();

//	void setLastUpdateDate(Date lastUpdateDate);

	String getLatestEditor();

//	void setLatestEditor(String latestEditor);

	List<Comment> getComments();

//	void setComments(List<Comment> comments);

//	void addComments(Comment comment);

	String getCreationDateLabel();

	String getLastUpdateDateLabel();

	Set<String> getReaders();

//	void setReaders(Set<String> readers);

	boolean isDeleted();

//	void setDeleted(boolean deleted);

	String getShare();

//	void setShare(String share);

	List<String> getUsers();
	
	String getScreenshot();

//	void setUsers(List<String> users);

	void onUpdate(String login);

	void onRead(String login);

}