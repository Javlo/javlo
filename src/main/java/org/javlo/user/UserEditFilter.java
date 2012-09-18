/*
 * Created on 23-f�vr.-2004
 */
package org.javlo.user;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

import org.javlo.helper.StringHelper;

/**
 * @author pvandermaesen this class represant the filter of the user list.
 */
public class UserEditFilter implements Serializable {

	Map<String, String> fieldContain = new Hashtable<String, String>();

	public void addFieldContain(String fieldName, String contain) {
		fieldContain.put(fieldName, contain);
	}

	public String getFieldContain(String fieldName) {
		String res = fieldContain.get(fieldName);
		if (res == null) {
			res = "";
		}
		return res;
	}

	public boolean match(IUserInfo userInfos) {
		boolean filtered = false;
		if (userInfos.getLogin().toLowerCase().indexOf(getFieldContain("login").toLowerCase()) < 0) {
			filtered = true;
		} else if (userInfos.getEmail().toLowerCase().indexOf(getFieldContain("email").toLowerCase()) < 0) {
			filtered = true;
		} else if (userInfos.getFirstName().toLowerCase().indexOf(getFieldContain("firstName").toLowerCase()) < 0) {
			filtered = true;
		} else if (userInfos.getLastName().toLowerCase().indexOf(getFieldContain("lastName").toLowerCase()) < 0) {
			filtered = true;
		} else if (StringHelper.collectionToString(userInfos.getRoles(), "" + IUserInfo.ROLES_SEPARATOR).toLowerCase().indexOf(getFieldContain("roles").toLowerCase()) < 0) {
			if (!(getFieldContain("roles").equals("- no role -") && userInfos.getRoles().size() == 0)) {
				filtered = true;
			}
		}
		return !filtered;
	}

}
