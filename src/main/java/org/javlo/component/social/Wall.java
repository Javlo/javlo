package org.javlo.component.social;

import java.util.HashMap;
import java.util.Map;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.service.RequestService;
import org.javlo.social.SocialLocalService;
import org.javlo.social.bean.Post;
import org.javlo.utils.JSONMap;

public class Wall extends AbstractVisualComponent implements IAction {
	
	public static final String TYPE = "wall";
	
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
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		SocialLocalService socialService = SocialLocalService.getInstance(ctx.getGlobalContext());
		ctx.getRequest().setAttribute("posts", socialService.getPost(ctx.getCurrentUserId()));
	}
	
	public static String performCreatepost(ContentContext ctx, RequestService rs) throws Exception {
		if (ctx.getCurrentUser() == null) {
			return "security error !";
		}
		String text = rs.getParameter("text");
		if (text == null) {
			return "bad request structure, need at least 'text' as parameter";
		}		
		if (!StringHelper.isEmpty(text)) {
			SocialLocalService socialService = SocialLocalService.getInstance(ctx.getGlobalContext());
			Post post = new Post();
			post.setAuthor(ctx.getCurrentUserId());
			post.setText(text);
			if (rs.getParameter("parent")!=null) {
				post.setParent(Long.parseLong(rs.getParameter("parent")));
			}
			if (rs.getParameter("main")!=null) {
				post.setMainPost(Long.parseLong(rs.getParameter("main")));
			}
			socialService.createPost(post);
		}
		return performGetpost(ctx, rs);
	}
	
	public static String performDeletepost(ContentContext ctx, RequestService rs) throws Exception {
		if (ctx.getCurrentUserId() == null) {
			return "security error !";
		}
		long id = Long.parseLong(rs.getParameter("id"));
		SocialLocalService.getInstance(ctx.getGlobalContext()).deletePost(ctx.getCurrentUserId(), id);;
		return performGetpost(ctx, rs);
	}
	
	public static String performGetpost(ContentContext ctx, RequestService rs) throws Exception {
		SocialLocalService socialService = SocialLocalService.getInstance(ctx.getGlobalContext());
		Map<String, Object> outMap = new HashMap<String, Object>();
		String masterPost = rs.getParameter("master");
		if (masterPost == null) {
			outMap.put("posts", socialService.getPost(ctx.getCurrentUserId()));
		} else {
			outMap.put("posts", socialService.getReplies(Long.parseLong(masterPost)));
		}
		ctx.setSpecificJson(JSONMap.JSON.toJson(outMap));
		return null;
	}
	
	public static String performGetreplies(ContentContext ctx, RequestService rs) throws Exception {
		SocialLocalService socialService = SocialLocalService.getInstance(ctx.getGlobalContext());
		Map<String, Object> outMap = new HashMap<String, Object>();
		outMap.put("posts", socialService.getPost(ctx.getCurrentUserId()));
		ctx.setSpecificJson(JSONMap.JSON.toJson(outMap));
		return null;
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}
}
