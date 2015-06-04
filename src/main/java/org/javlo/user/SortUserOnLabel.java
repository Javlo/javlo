package org.javlo.user;

import java.util.Comparator;

public class SortUserOnLabel implements Comparator<User> {

	public SortUserOnLabel() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(User o1, User o2) {
		return o1.getLabel().compareTo(o2.getLabel());
	}


}
