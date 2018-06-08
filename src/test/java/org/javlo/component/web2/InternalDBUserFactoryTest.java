package org.javlo.component.web2;

import org.javlo.user.InternalDBUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserInfo;
import org.javlo.user.exception.UserAllreadyExistException;

import junit.framework.TestCase;

public class InternalDBUserFactoryTest extends TestCase {
	
	private static final String AUTHOR_1 = "test_case_1";
	private static final String AUTHOR_2 = "test_case_2";
	private static final String AUTHOR_3 = "test_case_3";
	private static final int USERS_SIZE = 5;

	
	public void testCreateUser() {
		InternalDBUserFactory userFactory = InternalDBUserFactory.getTestInstance();
		UserInfo latestUserInfo = null;
		for (int i=1; i<=USERS_SIZE; i++) {
			UserInfo ui = new UserInfo();
			ui.setLogin("user-"+i);
			ui.setEmail("user-"+i+"@test.org");
			latestUserInfo = ui;
			try {
				userFactory.addUserInfo(ui);
			} catch (UserAllreadyExistException e) {
			}
		}
		assertEquals(userFactory.getUserInfoList().size(), USERS_SIZE);
		latestUserInfo.setEmail("test@test.org");
		userFactory.updateUserInfo(latestUserInfo);
		IUserInfo newUserInfo = userFactory.getUser(latestUserInfo.getLogin()).getUserInfo();
		assertEquals(userFactory.getUserInfoList().size(), USERS_SIZE);
		assertEquals(newUserInfo.getEmail(), "test@test.org");
		userFactory.deleteUser("user-1");
		assertEquals(userFactory.getUserInfoList().size(), USERS_SIZE-1);
	}
}
