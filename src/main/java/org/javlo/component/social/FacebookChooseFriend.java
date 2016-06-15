package org.javlo.component.social;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.LocalLogger;
import org.javlo.helper.NetHelper;
import org.javlo.service.social.SocialService;
import org.javlo.user.TransientUserInfo;
import org.javlo.utils.JSONMap;

import com.google.gson.reflect.TypeToken;

public class FacebookChooseFriend extends AbstractVisualComponent {
	
	public static final String TYPE = "facebook-choose-friends";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {	
		super.prepareView(ctx);
		getFriends(ctx, TransientUserInfo.getInstance(ctx.getRequest().getSession()).getToken());
		
	}

	public List<FacebookFriend> getFriends(ContentContext ctx, String accessToken) throws Exception {
		String clientId = SocialService.getInstance(ctx).getFacebook().getClientId();
		URL url;
		url = new URL("https://graph.facebook.com/v2.5/me/friends?access_token=" + accessToken + "&api_key=" + clientId + "&format=json-strings");

		logger.info("read info from DB : " + url);
		

		String jsonStr = NetHelper.readPage(url);
		
		System.out.println("jsonStr:");
		System.out.println(jsonStr);

		TypeToken<ArrayList<Map<String, String>>> list = new TypeToken<ArrayList<Map<String, String>>>() {
		};
		List<Map<String, String>> info = (List<Map<String, String>>) JSONMap.JSON.fromJson(jsonStr, list.getType());
		
		System.out.println("***** FacebookChooseFriend.getFriends : info : "+info.size()); //TODO: remove debug trace
		
		return null;

	}

}
