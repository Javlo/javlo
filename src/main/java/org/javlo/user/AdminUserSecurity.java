package org.javlo.user;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class AdminUserSecurity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static String GENERAL_ADMIN = "god";

	public static String FULL_CONTROL_ROLE = "admin";

	public static String CONTENT_ROLE = "content";

	public static String CONTRIBUTOR_ROLE = "contributor";

	public static String DESIGN_ROLE = "design";

	public static String MACRO_ROLE = "macro";

	public static String MAILING_ROLE = "mailing";

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
		
		/* USER RIGHT */
		String[] userRights = { "upload", "ajaxUserList" };
		Set<String> userRightsSet = new HashSet<String>(Arrays.asList(userRights));
		rights.put(USER_ROLE, userRightsSet);

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

	public boolean canRole(User user, String inRole) {
		if (user != null) {
			if (user != null) {
				Set<String> roles = user.getRoles();
				for (String role : roles) {
					if (role.equals(FULL_CONTROL_ROLE)) {
						return true;
					}					
				}
				return haveRole(user,inRole);
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

	public final boolean canModifyConponent(ContentContext ctx, String compId) throws Exception {
		if (ctx.getCurrentEditUser() == null) {
			return false;
		}
		ContentService content = ContentService.getInstance(ctx.getRequest());
		IContentVisualComponent comp = content.getComponent(ctx, compId);
		if (isAdmin(ctx.getCurrentEditUser()) || !ctx.getGlobalContext().isOnlyCreatorModify()) {
			return true;
		} else {
			if (ctx.getGlobalContext().isOnlyCreatorModify() && (comp != null && comp.getAuthors().equals(ctx.getCurrentEditUser().getLogin()))) {
				if (haveRole(ctx.getCurrentEditUser(), CONTENT_ROLE)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * check if the currentPage is editable by current user.
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	public static boolean canModifyPage(ContentContext ctx, MenuElement page) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		IUserFactory adminUserFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());

		if (page.isBlocked()) {
			if (!page.getBlocker().equals(adminUserFactory.getCurrentUser(ctx.getRequest().getSession()).getName())) {
				return false;
			}
		}

		AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance();
		ContentService.getInstance(globalContext);
		if (page.getEditorRoles().size() > 0) {
			if (!adminUserSecurity.haveRight(adminUserFactory.getCurrentUser(ctx.getRequest().getSession()), AdminUserSecurity.FULL_CONTROL_ROLE)) {
				if (!adminUserFactory.getCurrentUser(ctx.getRequest().getSession()).validForRoles(page.getEditorRoles())) {
					MessageRepository messageRepository = MessageRepository.getInstance(ctx);
					I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
					messageRepository.setGlobalMessageAndNotification(ctx, new GenericMessage(i18nAccess.getText("action.security.noright-onpage"), GenericMessage.ERROR));
					return false;
				}
			}
		}
		return true;
	}

}
