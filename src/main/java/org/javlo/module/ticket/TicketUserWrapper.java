package org.javlo.module.ticket;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.javlo.context.ContentContext;

public class TicketUserWrapper implements Ticket {

	private Ticket wrapped;
	private ContentContext ctx;

	public TicketUserWrapper(Ticket ticket, ContentContext ctx) {
		this.wrapped = ticket;
		this.ctx = ctx;
	}

	public boolean isRead() {
		return wrapped.getReaders().contains(ctx.getCurrentEditUser().getLogin());
	}

	@Override
	public String getTitle() {
		return wrapped.getTitle();
	}

	@Override
	public String getMessage() {
		return wrapped.getMessage();
	}

	@Override
	public String getContext() {
		return wrapped.getContext();
	}

	@Override
	public String getUrl() {
		return wrapped.getUrl();
	}

	@Override
	public int getPriority() {
		return wrapped.getPriority();
	}

	@Override
	public String getStatus() {
		return wrapped.getStatus();
	}

	@Override
	public String getAuthors() {
		return wrapped.getAuthors();
	}

	@Override
	public String getId() {
		return wrapped.getId();
	}

	@Override
	public String getCategory() {
		return wrapped.getCategory();
	}

	@Override
	public boolean isDebugNote() {
		return wrapped.isDebugNote();
	}

	@Override
	public Date getCreationDate() {
		return wrapped.getCreationDate();
	}

	@Override
	public Date getLastUpdateDate() {
		return wrapped.getLastUpdateDate();
	}

	@Override
	public String getLatestEditor() {
		return wrapped.getLatestEditor();
	}

	@Override
	public List<Comment> getComments() {
		return wrapped.getComments();
	}

	@Override
	public String getCreationDateLabel() {
		return wrapped.getCreationDateLabel();
	}

	@Override
	public String getLastUpdateDateLabel() {
		return wrapped.getLastUpdateDateLabel();
	}

	@Override
	public Set<String> getReaders() {
		return wrapped.getReaders();
	}

	@Override
	public boolean isDeleted() {
		return wrapped.isDeleted();
	}

	@Override
	public String getShare() {
		return wrapped.getShare();
	}

	@Override
	public List<String> getUsers() {
		return wrapped.getUsers();
	}

	@Override
	public void onUpdate(String login) {
		wrapped.onUpdate(login);
	}

	@Override
	public void onRead(String login) {
		wrapped.onRead(login);
	}

}
