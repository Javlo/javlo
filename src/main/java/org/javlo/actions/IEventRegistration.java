package org.javlo.actions;

import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.user.IUserInfo;

public interface IEventRegistration {
	
	public String getId();
	
	public List<IUserInfo> getParticipants(ContentContext ctx) throws Exception;
	
	public String getUserLink(ContentContext ctx) throws Exception;

}
