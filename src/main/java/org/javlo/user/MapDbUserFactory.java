package org.javlo.user;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.javlo.context.GlobalContext;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.JavaHelper;
import org.javlo.helper.StringHelper;
import org.javlo.service.mapdb.MapDBService;
import org.javlo.user.exception.UserAllreadyExistException;

public class MapDbUserFactory extends UserFactory {

	private Map<char[], String> internalMap;
	public static final char[] FIELDS_KEY = "__FIELDS".toCharArray();
	private static final String SEP = ",";

	@Override
	public void init(GlobalContext globalContext, HttpSession newSession) {
		super.init(globalContext, newSession);
		internalMap = MapDBService.getInstance(globalContext).getDb(this.getClass().getName());
	}

	@Override
	public User getUser(String login) {
		if (internalMap.size() > 1) {
			String[] fields = StringHelper.stringToArray(internalMap.get(FIELDS_KEY), SEP);
			if (fields == null) {
				logger.severe("fields not found.");
			} else {
				String val = internalMap.get(login.toCharArray());
				if (val != null) {
					IUserInfo newUserInfo = createUserInfos();
					Map<String, String> values = JavaHelper.createMap(fields, StringHelper.stringToArray(val, SEP));
					try {
						BeanHelper.copy(values, newUserInfo, false);
						return new User(newUserInfo);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	@Override
	public List<IUserInfo> getUserInfoList() {
		List<IUserInfo> userInfoList = new LinkedList<IUserInfo>();
		if (internalMap.size() > 1) {
			String fieldLabels = internalMap.get(FIELDS_KEY);
			String[] fields = StringHelper.stringToArray(fieldLabels, SEP);
			if (fields == null) {
				logger.severe("fields not found.");
			} else {
				for (Map.Entry<char[], String> entry : internalMap.entrySet()) {
					if (!entry.getValue().equals(fieldLabels)) {
						IUserInfo newUserInfo = createUserInfos();
						Map<String, String> values = JavaHelper.createMap(fields, StringHelper.stringToArray(entry.getValue(), SEP));
						try {
							BeanHelper.copy(values, newUserInfo, false);
							userInfoList.add(newUserInfo);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return userInfoList;
	}

	public void addUserInfo(IUserInfo userInfo) throws UserAllreadyExistException {
		userInfo.setModificationDate(new Date());
		if (getUser(userInfo.getLogin()) != null) {
			throw new UserAllreadyExistException(userInfo.getLogin() + " allready exist.");
		}
		try {
			updateUserInfo(userInfo);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javlo.user.IUserFactory#updateUserInfo(org.javlo.user.UserInfos)
	 */
	@Override
	public void updateUserInfo(IUserInfo userInfo) throws IOException {		
		internalMap.put(FIELDS_KEY, StringHelper.arrayToString(createUserInfos().getAllLabels(), SEP));
		String[] values= userInfo.getAllValues();		
		for (int i = 0; i < values.length; i++) {
			if (values[i]==null) {
				values[i]="";
			}
		}
		internalMap.put(userInfo.getLogin().toCharArray(), StringHelper.arrayToString(values, SEP));
	}

}
