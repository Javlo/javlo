package org.javlo.social;

public class SocialStat {
	
	private int totalAuthors;
	private int totalMessage;
	private int totalPost;
	
	public int getTotalAuthors() {
		return totalAuthors;
	}
	public void setTotalAuthors(int totalAuthors) {
		this.totalAuthors = totalAuthors;
	}
	public int getTotalMessage() {
		return totalMessage;
	}
	public void setTotalMessage(int totalMessage) {
		this.totalMessage = totalMessage;
	}
	public int getTotalPost() {
		return totalPost;
	}
	public void setTotalPost(int totalPost) {
		this.totalPost = totalPost;
	}
	public int getAverageReply() {
		if (totalMessage == 0) {
			if (totalPost == 0) {
				return 0;
			} else {
				return -1;
			}
		}
		return totalPost/totalMessage;
	}
}
