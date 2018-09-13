package org.javlo.social.bean;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;

public class PostWrapper {
	
	public static List<PostWrapper> transformPostList(ContentContext ctx, String userName, Collection<Post> posts) {
		List<PostWrapper> outList = new LinkedList<PostWrapper>();
		for (Post post : posts) {
			outList.add(new PostWrapper(ctx, userName, post));
		}
		return outList;
	}
	
	public PostWrapper(ContentContext ctx, String userName, Post post) {
		this.ctx = ctx;
		this.userName = userName;
		this.post = post;
	}

	private ContentContext ctx;
	private String userName;
	private Post post;
	
	public ContentContext getCtx() {
		return ctx;
	}
	public void setCtx(ContentContext ctx) {
		this.ctx = ctx;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public Post getPost() {
		return post;
	}
	public void setPost(Post post) {
		this.post = post;
	}
	
	public String getAdminMessage() {
		return post.getAdminMessage();
	}
	
	public String getAuthor() {
		return post.getAuthor();
	}
	
	public String getAuthorsIp() {
		return post.getAuthorIp();
	}
	
	public int getCountReplies() {
		return post.getCountReplies();
	}
	
	public Date getCreationDate() {
		return post.getCreationDate();
	}
	
	public String getCreationDateString() {
		return post.getCreationDateString();
	}
	
	public String getGroup() {
		return post.getGroup();
	}
	
	public Long getId() {
		return post.getId();
	}
	
	public Long getMainPost() {
		return post.getMainPost();
	}
	
	public String getText() {
		return post.getText();
	}
	
	public String getTitle() {
		return post.getTitle();
	}
	
	public boolean isAdminValided() {
		return post.isAdminValided();
	}
	
	public boolean isUncheckedChild() {
		return post.isUncheckedChild();
	}
	
	public boolean isValid() {
		return post.isValid();
	}
	
}
