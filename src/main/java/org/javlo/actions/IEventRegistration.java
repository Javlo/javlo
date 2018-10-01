package org.javlo.actions;

import java.util.List;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.user.IUserInfo;

public interface IEventRegistration {
	
	public String getId();
	
	public List<IUserInfo> getParticipants(ContentContext ctx) throws Exception;
	
	public String getUserLink(ContentContext ctx) throws Exception;
	
	/**
	 * get information about a specific user, if exist.
	 * @param login
	 * @return
	 */
	public List<Map<String,String>> getData(ContentContext ctx, String login) throws Exception;	

}
