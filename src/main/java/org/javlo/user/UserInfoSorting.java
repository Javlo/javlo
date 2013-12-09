package org.javlo.user;

import java.util.Comparator;

public class UserInfoSorting implements Comparator<IUserInfo> {

	public static final int LOGIN = 1;
	public static final int EMAIL = 2;
	public static final int FIRSTNAME = 3;
	public static final int LASTNAME = 4;
	public static final int CREATION_DATE = 5;

	private int sortField = LOGIN;
	private int ascending = 1;

	public UserInfoSorting(int sortField, boolean ascending) {
		this.sortField = sortField;
		if (!ascending) {
			this.ascending = -1;
		}
	}

	@Override
	public int compare(IUserInfo o1, IUserInfo o2) {
		switch (sortField) {
		case LOGIN:
			return ascending * o1.getLogin().toLowerCase().compareTo(o2.getLogin().toLowerCase());
		case EMAIL:
			return ascending * o1.getEmail().toLowerCase().compareTo(o2.getEmail().toLowerCase());
		case FIRSTNAME:
			return ascending * o1.getFirstName().toLowerCase().compareTo(o2.getFirstName().toLowerCase());
		case LASTNAME:
			return ascending * o1.getLastName().toLowerCase().compareTo(o2.getLastName().toLowerCase());
		case CREATION_DATE:
			return ascending * o1.getCreationDate().compareTo(o2.getCreationDate());
		default:
			break;
		}
		return 0;
	}

}
