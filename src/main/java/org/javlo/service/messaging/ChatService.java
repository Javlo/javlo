package org.javlo.service.messaging;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.user.IUserInfo;

public class ChatService {
	
	private Map<String, List<Room>> userRooms = new HashMap<String, List<Room>>();

	private static final String KEY = "messaging";

	private ChatService() {
	}
	
	public Room getSessionRoom(ContentContext ctx) {
		final String KEY = ChatService.class.getName();
		Room room = (Room)ctx.getRequest().getSession().getAttribute(KEY);
		if (room == null) {
			room = new Room();
			ctx.getRequest().getSession().setAttribute(KEY, room);
		}
		return room;
	}
	
	public List<Room> getRooms(IUserInfo user) {
		List<Room> rooms = userRooms.get(user);
		if (rooms == null || rooms.size() == 0) {
			rooms = new LinkedList<Room>();			
			rooms.add(new Room(user));
		}
		return rooms;
	}
	
	public static ChatService getInstance(HttpSession session) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (session == null) {						
			return new ChatService();
		} else {
			ChatService outUserDAO = (ChatService) session.getAttribute(KEY);
			if (outUserDAO == null) {
				outUserDAO = new ChatService();		
				session.setAttribute(KEY, outUserDAO);
			}			
			return outUserDAO;
		}
	}

}
