package org.javlo.service.visitors;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import javax.servlet.http.Cookie;

import org.javlo.context.ContentContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.SecurityHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.utils.TimeMap;

public class UserDataService {

	private static final String KEY = UserDataService.class.getCanonicalName();
	private static final String KEY_COKKIES = "jvl_usr_data";

	private static final int TIME_IN_MAP = 60 * 60 * 24 * 60; // 60 days
	private TimeMap<String, String> data = new TimeMap<String, String>(TIME_IN_MAP);
	private File storageFile;

	public static UserDataService getInstance(ContentContext ctx) throws Exception {
		UserDataService userDataService = (UserDataService) ctx.getGlobalContext().getAttribute(KEY);
		if (userDataService == null) {
			userDataService = new UserDataService();
			userDataService.storageFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), "user_data.properties"));
			userDataService.load();
		}
		return userDataService;
	}

	public String getUserData(ContentContext ctx, String key) {
		Cookie cookie = NetHelper.getCookie(ctx.getRequest(), KEY_COKKIES);
		if (cookie != null) {
			return data.get(cookie.getValue()+key);
		} else {
			return null;
		}
	}
	
	public void addUserData(ContentContext ctx, String key, String inData) throws Exception {
		Cookie cookie = NetHelper.getCookie(ctx.getRequest(), KEY_COKKIES);
		if (cookie == null) {
			cookie = new Cookie(KEY_COKKIES, StringHelper.getLargeRandomIdBase64());
			cookie.setPath("/");
			cookie.setSecure(false);
			cookie.setVersion(0);
			cookie.setMaxAge(TIME_IN_MAP);
			ctx.getResponse().addCookie(cookie);		
		}
		this.data.put(cookie.getValue()+key, inData);
		store();
	}

	private void load() throws Exception {
		synchronized (KEY) {
			if (storageFile.exists()) {
				data.load(storageFile);
			}
		}
	}

	private void store() throws Exception {
		synchronized (KEY) {
			if (!storageFile.exists()) {
				storageFile.createNewFile();
			}
			data.store(storageFile);
		}
	}
	
	/**
	 * clear all key with a specific prefix 
	 * @param ctx
	 * @param prefix the start chars of the key
	 * @throws Exception 
	 */
	public void resetData(ContentContext ctx) throws Exception {
		Cookie cookie = NetHelper.getCookie(ctx.getRequest(), KEY_COKKIES);
		Collection<String> toDelete = new LinkedList<>();
		for (String key: data.keySet()) {
			if (key.startsWith(cookie.getValue())) {
				toDelete.add(key);
			}
		}
		for (String key : toDelete) {
			data.remove(key);
		}
		store();
		SecurityHelper.clearUserCode(ctx);
		cookie.setMaxAge(0);
		ctx.getResponse().addCookie(cookie);
	}

}
