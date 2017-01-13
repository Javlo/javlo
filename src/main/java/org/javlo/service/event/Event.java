package org.javlo.service.event;

import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.javlo.actions.IEventRegistration;
import org.javlo.component.image.IImageTitle;
import org.javlo.context.ContentContext;
import org.javlo.user.IUserInfo;

public class Event implements Serializable {

	public static final Event NO_EVENT = new Event(null, null, null, null, null, "no event", null, null);

	private static final long serialVersionUID = 1L;

	private String id;
	private URL url;
	private Date start;
	private Date end;
	private String summary;
	private String location;
	private String category;
	private String status;
	private String description;
	private IImageTitle image;
	private int sequence;
	private String user;
	private String participantsFileURL = null;
	private IEventRegistration eventRegistration;
	private String pageName;

	public Event(ContentContext ctx, String pageName, String id, Date start, Date end, String summary, String description, IImageTitle image) {
		this.id = id;
		this.start = start;
		this.end = end;
		this.summary = summary;
		this.description = description;
		this.image = image;
		this.pageName = pageName;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public String getProdID() {
		return "-//ImmanenceSPRL//NONSGML Javlo//EN";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public List<IUserInfo> getParticipants(ContentContext ctx) throws Exception {
		IEventRegistration comp = getEventRegistration();
		if (comp == null) {
			return Collections.EMPTY_LIST;
		}
		participantsFileURL = comp.getUserLink(ctx);
		return comp.getParticipants(ctx);
	}

	public IImageTitle getImage() {
		return image;
	}

	public void setImage(IImageTitle image) {
		this.image = image;
	}

	public String getParticipantsFileURL() {
		return participantsFileURL;
	}

	public void setParticipantsFileURL(String participantsFileURL) {
		this.participantsFileURL = participantsFileURL;
	}

	public IEventRegistration getEventRegistration() {
		return eventRegistration;
	}

	public void setEventRegistration(IEventRegistration eventRegistration) {
		this.eventRegistration = eventRegistration;
	}

	public String getPageName() {
		return pageName;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

}
