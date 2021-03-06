package org.javlo.component.social;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.actions.IAction;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.RequestService;
import org.javlo.social.SocialFilter;
import org.javlo.social.SocialLocalService;
import org.javlo.social.bean.Post;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;
import org.javlo.utils.JSONMap;

public class Wall extends AbstractPropertiesComponent implements IAction {
	
	private static final int PAGE_SIZE = 30;

	public static final String TYPE = "wall";

	private static final String UNVALIDED_NOT_VISIBLE = "unvalided_not_visible";
	
	private static final String POST_DELETABLE = "post_deletable";

	private static final List<String> FIELDS = new LinkedList<String>(Arrays.asList(new String[] { "name", "title", "roles", "labelAddReply", "labelCreateOn", "labelLatestMsg","labelReplyAt", "labelToBottom", "labelToTop", "labelWaitValidation", UNVALIDED_NOT_VISIBLE+"#checkbox", POST_DELETABLE+"#checkbox" }));

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getActionGroupName() {
		return TYPE;
	}

	@Override
	public String getFontAwesome() {
		return "comments-o";
	}
	
	private String getWallName(ContentContext ctx) throws Exception {
		String wallName = getFieldValue("name");
		if (StringHelper.isEmpty(wallName)) {
			wallName=ctx.getCurrentPage().getName();
		}
		return wallName;
	}
	
	private boolean isNeedCheck() {
		return StringHelper.isTrue(getFieldValue(UNVALIDED_NOT_VISIBLE));
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		SocialFilter.getInstance(ctx.getRequest().getSession());
		User currentUser = ctx.getCurrentUser();
		if (currentUser == null) {
			ctx.getRequest().setAttribute("access", false);
		} else {
			SocialLocalService socialService = SocialLocalService.getInstance(ctx.getGlobalContext());
			if (AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser())) {
				ctx.getRequest().setAttribute("stat30", socialService.getSocialStat(LocalDate.now().minusDays(30)));
				ctx.getRequest().setAttribute("stat365", socialService.getSocialStat(LocalDate.now().minusDays(365)));
			}
			ctx.getRequest().setAttribute("pageSize", PAGE_SIZE);
			long countResult = socialService.getPostListSize(SocialFilter.getInstance(ctx.getRequest().getSession()), ctx.getCurrentUserId(), getWallName(ctx), AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentUser()), isNeedCheck());
			float pageCountFloat = ((float)countResult/(float)PAGE_SIZE);
			int pageCount = Math.round(pageCountFloat);
			if (pageCountFloat > pageCount) {
				pageCount++;
			}
			ctx.getRequest().setAttribute("pageCount", pageCount);
			ctx.getRequest().setAttribute("countResult", countResult);
			List<String> roles = StringHelper.stringToCollection(getFieldValue("roles"));
			if (AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentUser())) {
				ctx.getRequest().setAttribute("uncheckSize", socialService.getUnvalidedPostListSize(getWallName(ctx)));
				ctx.getRequest().setAttribute("countMessages", socialService.getPostListSize(getWallName(ctx)));
			}
			if (!currentUser.validForRoles(roles)) {
				ctx.getRequest().setAttribute("access", false);
			} else {
				ctx.getRequest().setAttribute("access", true);
			}
		}
	}

	public static String performCreatepost(ContentContext ctx, RequestService rs) throws Exception {
		
		if (ctx.getCurrentUser() == null) {
			return "security error !";
		}
		String text = rs.getParameter("text");
		if (text == null) {
			return "bad request structure, need at least 'text' as parameter";
		}
		String title = rs.getParameter("title");
		if (title == null && rs.getParameter("parent") == null) {
			return "bad request structure, need at least 'text' as parameter";
		}
		if (!StringHelper.isEmpty(text) && (!StringHelper.isEmpty(title) || rs.getParameter("parent") != null)) {
			Wall comp = (Wall)ComponentHelper.getComponentFromRequest(ctx);
			SocialLocalService socialService = SocialLocalService.getInstance(ctx.getGlobalContext());
			Post post = new Post();
			post.setGroup(comp.getWallName(ctx));
			post.setAuthor(ctx.getCurrentUserId());
			post.setTitle(title);
			post.setText(text);
			post.setAuthorIp(ctx.getRealRemoteIp(ctx.getGlobalContext().getStaticConfig().isAnonymisedTracking()));
			if (rs.getParameter("parent") != null) {
				post.setParent(Long.parseLong(rs.getParameter("parent")));
			}
			if (rs.getParameter("main") != null) {
				post.setMainPost(Long.parseLong(rs.getParameter("main")));
			}
			String msg = validPost(ctx,post);
			if (msg != null) {
				return msg;
			} else {
				socialService.createPost(post);
			}
		}
		return performGetpost(ctx, rs);
	}

	public static String performDeletepost(ContentContext ctx, RequestService rs) throws Exception {
		if (ctx.getCurrentUserId() == null) {
			return "security error !";
		}
		boolean admin = AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser());
		long id = Long.parseLong(rs.getParameter("id"));
		SocialLocalService.getInstance(ctx.getGlobalContext()).deletePost(admin, ctx.getCurrentUserId(), id);
		return performGetpost(ctx, rs);
	}
	
	public static String performDeletereply(ContentContext ctx, RequestService rs) throws Exception {
		if (ctx.getCurrentUserId() == null) {
			return "security error !";
		}
		boolean admin = AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser());
		long id = Long.parseLong(rs.getParameter("id"));
		SocialLocalService.getInstance(ctx.getGlobalContext()).deletePost(admin, ctx.getCurrentUserId(), id);
		return performGetpost(ctx, rs);
	}
	
	public static String performAdmin(ContentContext ctx, RequestService rs) throws Exception {
		if (!AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser())) {
			return "security error !";
		}
		if (rs.getParameter("valid", null) == null) {
			return "bad request structure !";
		}
		long id = Long.parseLong(rs.getParameter("id"));
		SocialLocalService socialService = SocialLocalService.getInstance(ctx.getGlobalContext());
		Post post = socialService.getPost(id, ctx.getCurrentEditUser().getLogin(), true, false);
		post.setAdminValided(true);
		post.setAdminMessage(rs.getParameter("msg", ""));
		post.setValid(StringHelper.isTrue(rs.getParameter("valid", null)));
		String msg = validPost(ctx,post);
		if (msg != null) {
			return msg;
		} else {
			socialService.updatePost(post);
		}
		ctx.setSpecificJson(JSONMap.JSON.toJson(post));
		return null;
	}
	
	/**
	 * valid the post
	 * @param ctx
	 * @param post
	 * @return null if post ok, error message otherwise.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private static String validPost(ContentContext ctx, Post post) throws FileNotFoundException, IOException {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		if (post.getAdminMessage() != null && post.getAdminMessage().length() > 6000) {
			return i18nAccess.getViewText("wall.error.admin-size", "Administrators message can not exceed 6000 characters");
		}
		if (post.getTitle() != null && post.getTitle().length() > 250) {
			return i18nAccess.getViewText("wall.error.title-size", "Title can not exceed 250 characters");
		}
		if (post.getText() != null && post.getText().length() > 7000) {
			return i18nAccess.getViewText("wall.error.text-size", "Message can not exceed 7000 characters");
		}
		return null;
	}

	public static String performGetpost(ContentContext ctx, RequestService rs) throws Exception {
		SocialLocalService socialService = SocialLocalService.getInstance(ctx.getGlobalContext());
		Map<String, Object> outMap = new HashMap<String, Object>();
		String masterPost = rs.getParameter("master");
		boolean admin = AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser());
		Wall comp = (Wall)ComponentHelper.getComponentFromRequest(ctx);
		if (masterPost == null) {
			int pageNumber = Integer.parseInt(rs.getParameter("page", "1"));
			outMap.put("posts", socialService.getPost(SocialFilter.getInstance(ctx.getRequest().getSession()), admin, comp.isNeedCheck(), ctx.getCurrentUserId(), comp.getWallName(ctx), PAGE_SIZE, (pageNumber-1)*PAGE_SIZE));
		} else {
			outMap.put("posts", socialService.getReplies(SocialFilter.getInstance(ctx.getRequest().getSession()), ctx.getCurrentUserId(), admin, comp.isNeedCheck(), Long.parseLong(masterPost)));
		}
		ctx.setSpecificJson(JSONMap.JSON.toJson(outMap));
		return null;
	}
	
	public static String performUpdatefilter(ContentContext ctx, RequestService rs) throws Exception {
		SocialFilter socialFilter = SocialFilter.getInstance(ctx.getRequest().getSession());
		if (rs.getParameter("reset") != null) {
			socialFilter.reset();
		} else {			
			socialFilter.setAuthor(rs.getParameter("text-author",null));
			socialFilter.setTitle(rs.getParameter("text-title",null));
			socialFilter.setQuery(rs.getParameter("text-filter",null));
			socialFilter.setOnlyMine(StringHelper.isTrue(rs.getParameter("filter-mine", null)));
			socialFilter.setNotValided(StringHelper.isTrue(rs.getParameter("notvalided", null)));
			socialFilter.setNoResponse(StringHelper.isTrue(rs.getParameter("noresponse", null)));
		}
		return null;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}
	
	public static void main(String[] args) {
		Post post = new Post();
		List<Post> list = new LinkedList<>();
		list.add(post);
		Map<String,Object> map = new HashMap<>();
		map.put("posts", list);
		System.out.println(JSONMap.JSON.toJson(map));
	}
}

