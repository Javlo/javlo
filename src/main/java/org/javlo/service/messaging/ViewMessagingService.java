package org.javlo.service.messaging;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.javlo.user.IUserInfo;

public class ViewMessagingService {
	
	private Map<String, List<Room>> userRooms = new HashMap<String, List<Room>>();

	private static final String KEY = "messaging";

	private ViewMessagingService() {
	}
	
	public List<Room> getRooms(IUserInfo user) {
		List<Room> rooms = userRooms.get(user);
		if (rooms == null || rooms.size() == 0) {
			rooms = new LinkedList<Room>();			
			rooms.add(new Room(user));
		}
		return rooms;
	}
	
	public static ViewMessagingService getInstance(HttpSession session) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (session == null) {						
			return new ViewMessagingService();
		} else {
			ViewMessagingService outUserDAO = (ViewMessagingService) session.getAttribute(KEY);
			if (outUserDAO == null) {
				outUserDAO = new ViewMessagingService();		
				session.setAttribute(KEY, outUserDAO);
			}			
			return outUserDAO;
		}
	}

}
