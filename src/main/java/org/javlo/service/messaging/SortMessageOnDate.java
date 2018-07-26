package org.javlo.service.messaging;

import java.util.Comparator;

public class SortMessageOnDate implements Comparator<Message> {

	@Override
	public int compare(Message o1, Message o2) {
		return o1.getDate().compareTo(o2.getDate());
	}

}
