package org.javlo.user;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

public class AdminUserSecurity implements Serializable {

	private static final long serialVersionUID = 1L;

	private static String SESSION_KEY = AdminUserSecurity.class.getName();

	public static String ALL_USER_ROLE = "all-user";

	public static String GENERAL_ADMIN = "god";

	public static String FULL_CONTROL_ROLE = "admin";

	public static String CONTENT_ROLE = "content";

	public static String MACRO_ROLE = "macro";

	public static String LIGHT_INTERAFACE_ROLE = "light-interface";

	public static String VALIDATION_ROLE = "validation";

	public static String REMOVE_STATIC_ROLE = "remove-file";

	public static String NAVIGATION_ROLE = "navigation";

	public static String ADD_NAVIGATION_ROLE = "add-navigation";

	public static String USER_ROLE = "user";

	public static String ADMIN_USER_ROLE = "admin-user";

	public static String STATISTICS_ROLE = "statistics";

	public static String PUBLISHER_ROLE = "publisher";

	public static String SYNCHRO_CLIENT = "sync-client";

	public static String SYNCHRO_ADMIN = "sync-admin";

	public static String SYNCHRO_SERVER = "sync-server";

	public static String MAILING_ROLE = "mailing";

	public static String WEBDESGIN_ROLE = "webdesign";

	public static String SPECIAL_RIGHT_USER = "user";

	public static String SPECIAL_RIGHT_ADMIN_USER = "admin-user";

	public static String SPECIAL_RIGHT_STATISTICS = "stat";

	public static String SPECIAL_RIGHT_NAVIGATION = "navigation";

	public static String SPECIAL_RIGHT_ADD_NAVIGATION = "add-navigation";

	public static String SPECIAL_RIGHT_PUBLISHER = "publisher";

	private Map<String, Set<String>> rights = new HashMap<String, Set<String>>();

	private AdminUserSecurity() {

		String[] allUserRightArray = {"adminlogin"};
		Set<String> allUserSet = new HashSet<String>(Arrays.asList(allUserRightArray));
		rights.put(ALL_USER_ROLE, allUserSet);

		/* CONTENT RIGHT */
		String[] contentRights = { "persistanceopen", "changeview", "changetype", "remove", "copy", "paste", "insert", "update", "blockpage", "visible",
				"copypagestructure", "pastepage", "updateone", "macro", "insertmsg", "selectarea", "mkdir", "persistenceopen", "cancelcopy", "savemetastaticfile",
				"previewedit"};
		Set<String> contentSet = new HashSet<String>(Arrays.asList(contentRights));
		rights.put(CONTENT_ROLE, contentSet);

		/* MACRO RIGHT */
		String[] macroRights = { "macro", "macroopen" };
		Set<String> macroSet = new HashSet<String>(Arrays.asList(macroRights));
		rights.put(MACRO_ROLE, macroSet);

		/* VALIDATION RIGHT */
		String[] validationRights = { "validationpage" };
		Set<String> validationSet = new HashSet<String>(Arrays.asList(validationRights));
		rights.put(VALIDATION_ROLE, validationSet);

		/* REMOVE STATIC RIGHT */
		String[] removeStaticRights = {"rmdir", "deletestaticfile"};
		Set<String> removeStatSet = new HashSet<String>(Arrays.asList(removeStaticRights));
		rights.put(REMOVE_STATIC_ROLE, removeStatSet);

		/* NAVIGATION RIGHT */
		String[] navigationRights = { SPECIAL_RIGHT_NAVIGATION, "itemopen", "changeview", "itemclose", "repeat", "unrepeat", "changename", "visible","movepreview",
				"priority", "userroles", "menuopen", "menuclose", "removenav", "link", "addnav", "addnavfirst", "moveup", "movedown", "movetoparent", "movetochild" };
		Set<String> navigationSet = new HashSet<String>(Arrays.asList(navigationRights));
		rights.put(NAVIGATION_ROLE, navigationSet);

		/* ADD NAVIGATION RIGHT */
		String[] addNavigationRights = { SPECIAL_RIGHT_ADD_NAVIGATION, "itemopen", "changeview", "itemclose", "userroles", "menuopen", "menuclose", "link",
				"addnav", "insertpage" };
		Set<String> addNavigationSet = new HashSet<String>(Arrays.asList(addNavigationRights));
		rights.put(ADD_NAVIGATION_ROLE, addNavigationSet);

		/* USER RIGHT */
		String[] userRights = { SPECIAL_RIGHT_USER, "adduser", "changeview", "uploadusers", "userfilter", "changeuserroles", "deleteuser" };
		Set<String> userSet = new HashSet<String>(Arrays.asList(userRights));
		rights.put(USER_ROLE, userSet);

		/* ADMIN USER RIGHT */
		String[] adminUserRights = { SPECIAL_RIGHT_ADMIN_USER, "adduser", "changeview", "uploadusers", "userfilter", "changeuserroles", "deleteuser" };
		Set<String> adminUser = new HashSet<String>(Arrays.asList(adminUserRights));
		rights.put(ADMIN_USER_ROLE, adminUser);

		/* STAT USER RIGHT */
		String[] statRights = { SPECIAL_RIGHT_STATISTICS, "changeview", "statselect" };
		Set<String> statSet = new HashSet<String>(Arrays.asList(statRights));
		rights.put(STATISTICS_ROLE, statSet);

		/* PUBLISH USER RIGHT */
		String[] publishRights = { SPECIAL_RIGHT_PUBLISHER, "publish", "changeview" };
		Set<String> publishSet = new HashSet<String>(Arrays.asList(publishRights));
		rights.put(PUBLISHER_ROLE, publishSet);
	}

	public static final AdminUserSecurity getInstance(ServletContext application) {
		AdminUserSecurity ctx = (AdminUserSecurity) application.getAttribute(SESSION_KEY);
		if (ctx == null) {
			ctx = new AdminUserSecurity();
			application.setAttribute(SESSION_KEY, ctx);
		}
		return ctx;
	}

	/**
	 * check right (admin have all right)
	 *
	 * @param user
	 * @param right
	 * @return
	 */
	public boolean haveRight(User user, String right) {
		if (user != null) {
			Set<String> roles = user.getRoles();
			for (String role : roles) {
				if (role.equals(FULL_CONTROL_ROLE)) {
					return true;
				}
				Set<String> rightsRole = rights.get(role);
				if (rightsRole != null) {
					if (rightsRole.contains(right.toLowerCase())) {
						return true;
					}
				}
			}
		}
		if (rights.get(ALL_USER_ROLE).contains(right.toLowerCase())) {
			return true;
		}
		return false;
	}

	public boolean haveRole(User user, String inRole) {
		if (user != null) {
			Set<String> roles = user.getRoles();
			for (String role : roles) {
				if (role.equalsIgnoreCase(inRole)) {
					return true;
				}
			}
		}
		return false;
	}
}
