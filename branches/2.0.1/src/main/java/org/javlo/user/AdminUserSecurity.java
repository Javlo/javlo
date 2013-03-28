package org.javlo.user;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AdminUserSecurity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static String GENERAL_ADMIN = "god";

	public static String FULL_CONTROL_ROLE = "admin";

	public static String CONTENT_ROLE = "content";

	public static String CONTRIBUTOR_ROLE = "contributor";

	public static String DESIGN_ROLE = "design";

	public static String MACRO_ROLE = "macro";

	public static String LIGHT_INTERFACE_ROLE = "light-interface";

	public static String VALIDATION_ROLE = "validation";

	public static String NAVIGATION_ROLE = "navigation";

	public static String USER_ROLE = "user";

	public static String ADMIN_USER_ROLE = "admin-user";

	public static String STATISTICS_ROLE = "statistics";

	public static String PUBLISHER_ROLE = "publisher";

	public static String SYNCHRO_CLIENT = "sync-client";

	public static String SYNCHRO_ADMIN = "sync-admin";

	public static String SYNCHRO_SERVER = "sync-server";

	public static String MASTER = "master";

	private final Map<String, Set<String>> rights = new HashMap<String, Set<String>>();

	private AdminUserSecurity() {

		/* CONTENT RIGHT */
		String[] contentRights = { "delete", "changeview", "changetype", "remove", "copy", "paste", "insert", "update", "blockpage", "visible", "copypagestructure", "pastepage", "updateone", "macro", "insertmsg", "selectarea", "mkdir", "persistenceopen", "cancelcopy", "savemetastaticfile", "previewedit" };
		Set<String> contentSet = new HashSet<String>(Arrays.asList(contentRights));
		rights.put(CONTENT_ROLE, contentSet);
		rights.put(CONTRIBUTOR_ROLE, contentSet);

		/* MACRO RIGHT */
		String[] macroRights = { "macro" };
		Set<String> macroSet = new HashSet<String>(Arrays.asList(macroRights));
		rights.put(MACRO_ROLE, macroSet);

		/* VALIDATION RIGHT */
		String[] validationRights = { "validationpage" };
		Set<String> validationSet = new HashSet<String>(Arrays.asList(validationRights));
		rights.put(VALIDATION_ROLE, validationSet);

		/* DESIGN RIGHT */
		String[] designRights = { "changeview", "goEditTemplate", "changeRenderer", "validate", "browse", "delete" };
		Set<String> designRightsSet = new HashSet<String>(Arrays.asList(designRights));
		rights.put(DESIGN_ROLE, designRightsSet);

	}

	private static AdminUserSecurity instance;

	public static final AdminUserSecurity getInstance() {
		if (instance == null) {
			instance = new AdminUserSecurity();
		}
		return instance;
	}

	/**
	 * return true if user have one of all right. TODO: method not tested.
	 * 
	 * @param user
	 * @param right
	 * @return
	 */
	public boolean haveRight(User user, Collection<String> rights) {
		for (String right : rights) {
			if (haveRight(user, right)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * check right (admin have all right)
	 * 
	 * @param user
	 * @param right
	 * @return
	 */
	public boolean haveRight(User user, String... inRights) {
		for (String right : inRights) {
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
		}
		return false;
	}

	/**
	 * check right (admin have all right)
	 * 
	 * @param user
	 * @param right
	 * @return
	 */
	public boolean haveAllRight(User user, String... inRights) {
		for (String right : inRights) {
			if (user != null) {
				Set<String> roles = user.getRoles();
				for (String role : roles) {
					if (role.equals(FULL_CONTROL_ROLE)) {
						return true;
					}
					Set<String> rightsRole = rights.get(role);
					if (rightsRole != null) {
						if (!rightsRole.contains(right.toLowerCase())) {
							return false;
						}
					}
				}
			}
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

	/**
	 * return true if user have no restriction
	 * 
	 * @param user
	 * @return
	 */
	public boolean isAdmin(User user) {
		if (user == null) {
			return false;
		}
		return haveRight(user, FULL_CONTROL_ROLE, ADMIN_USER_ROLE);
	}

	/**
	 * return true if user have no restriction on all website
	 * 
	 * @param user
	 * @return
	 */
	public boolean isGod(User user) {
		if (user == null) {
			return false;
		}
		return user.getRoles().contains(GENERAL_ADMIN);
	}

	public boolean isMaster(User user) {
		if (user == null) {
			return false;
		}
		return user.getRoles().contains(MASTER);
	}

}
