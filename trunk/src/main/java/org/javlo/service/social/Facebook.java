package org.javlo.service.social;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.javlo.helper.NetHelper;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserInfo;
import org.javlo.utils.JSONMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Facebook extends AbstractSocialNetwork {
	
	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Facebook.class.getName());
	
	public static final Gson JSON = new GsonBuilder().setDateFormat("yyyy-MM-dd_HH-mm-ss-SSS ").create();

	@Override
	public String getName() {
		return "facebook";
	}

	public String getClientId() {
		return data.get("client_id");
	}

	public String getClientSecret() {
		return data.get("client_secret");
	}

	public void setClientId(String id) {
		data.put("client_id", id);
	}

	public void setClientSecret(String secret) {
		data.put("client_secret", secret);
	}

	@Override
	public void update(Map map) {
		super.update(map);
		setClientSecret("" + map.get("client_secret"));
		setClientId("" + map.get("client_id"));
	}
	
	public IUserInfo getInitialUserInfo(String accessToken) throws Exception {
		URL url;
		url = new URL("https://api-read.facebook.com/restserver.php?access_token="+accessToken+"&api_key="+getClientId()+"&format=json-strings&method=fql.query&pretty=0&query=SELECT%20name%2C%20first_name%2C%20last_name%2C%20uid%2C%20email%20FROM%20user%20WHERE%20uid%3D693608149&sdk=joey");
		String jsonStr = NetHelper.readPage(url);
		
		TypeToken<ArrayList<Map<String,String>>> list = new TypeToken<ArrayList<Map<String,String>>>(){};
		List<Map<String,String>> info = (List<Map<String,String>>)JSONMap.JSON.fromJson(jsonStr,list.getType());

		IUserInfo ui = new UserInfo();
		ui.setLogin(""+info.get(0).get("email"));
		ui.setEmail(""+info.get(0).get("email"));
		ui.setFirstName(""+info.get(0).get("first_name"));
		ui.setLastName(""+info.get(0).get("last_name"));
		return ui;
	}

}

