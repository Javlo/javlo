package org.javlo.service.messaging;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.javlo.helper.StringHelper;
import org.javlo.user.IUserInfo;
import org.javlo.user.PublicUserInfo;
import org.javlo.utils.TimeMap;

public class Room {
	
	TimeMap<String, Message> messages = new TimeMap<String, Message>(60*60*24);
	
	private List<PublicUserInfo> member = new LinkedList<PublicUserInfo>();
	
	private PublicUserInfo owner = null;
	
	private Date date = new Date();
	
	public Room(IUserInfo owner) {
		this.owner = new PublicUserInfo(owner);
	}
	
	/**
	 * create a public room
	 */
	public Room() {
	}
	
	
	public List<PublicUserInfo> getMember() {
		return member;
	}
	
	public void addMember(PublicUserInfo user) {
		member.add(user);
	}
	
	public void removeMember(String login) {
		for (Iterator iterator = member.iterator(); iterator.hasNext();) {
			IUserInfo user = (IUserInfo) iterator.next();
			if (user.getLogin().equals(login)) {
				iterator.remove();
				return;
			}			
		}
	}
	
	public Collection<Message> getMessages() {
		List<Message> outMessages = new LinkedList<Message>(messages.values());
		Collections.sort(outMessages, new SortMessageOnDate());
		return outMessages;
	}
	
	public void addMessages(String userName, String body) {
		Message msg = new Message();
		msg.setUserName(userName);
		msg.setBody(body);
		messages.put(msg.getId(), msg);
	}

	public PublicUserInfo getOwner() {
		return owner;
	}

	public String getName() {
		if (!StringHelper.isAllEmpty(owner.getFirstName(), owner.getLastName())) {
			return owner.getFirstName()+' '+owner.getLastName();
		} else {
			return owner.getLogin();
		}
	}
	
	public String getDescription() {
		return "#member : "+member.size();
	}
	
	public String getCreationTime() {
		return StringHelper.renderTime(date);
	}
}
