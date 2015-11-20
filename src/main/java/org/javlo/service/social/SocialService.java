package org.javlo.service.social;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class SocialService {

	private static final String KEY_PREFIX = "socialnetwork";

	private GlobalContext globalContext;

	private Facebook facebook;	
	private ISocialNetwork twitter;
	private ISocialNetwork google;
	private ISocialNetwork linkedin;
	private ISocialNetwork pushbullet;
	private String redirectURL = null;

	public static SocialService getInstance(ContentContext ctx) {
		GlobalContext globalContext = ctx.getGlobalContext();
		final String KEY = "social";
		SocialService outService = (SocialService) globalContext.getAttribute(KEY);
		if (outService == null) {
			outService = new SocialService();
			outService.globalContext = globalContext;
			outService.redirectURL = URLHelper.createStaticURL(ctx.getContextForAbsoluteURL(), "/oauth2callback");
			globalContext.setAttribute(KEY, outService);

		}
		
		return outService;
	}
	
	/**
	 * prepare rendering
	 * @param ctx
	 * @throws Exception 
	 */
	public void prepare(ContentContext ctx) throws Exception {
		for (ISocialNetwork network : getAllNetworks()) {
			network.prepare(ctx);
		}
	}

	private void initSocialNetwork(ISocialNetwork socialNetwork) {
		socialNetwork.setRedirectURL(redirectURL);
		Collection<Object> objects = globalContext.getDataKeys();
		for (Object keyObj : objects) {
			String key = "" + keyObj;
			if (key.startsWith(KEY_PREFIX + "." + socialNetwork.getName())) {
				String value = globalContext.getData(key);
				key = StringUtils.replaceOnce(key, KEY_PREFIX + "." + socialNetwork.getName(), "");
				socialNetwork.set(key, value);
			}
		}
	}

	public List<ISocialNetwork> getAllNetworks() {
		List<ISocialNetwork> networks = new LinkedList<ISocialNetwork>();
		networks.add(getFacebook());
		networks.add(getTwitter());
		networks.add(getGoogle());
		networks.add(getLinkedin());
		networks.add(getPushbullet());
		return networks;
	}

	public ISocialNetwork getNetwork(String name) {
		for (ISocialNetwork network : getAllNetworks()) {
			if (network.getName().equals(name)) {
				return network;
			}
		}
		return null;
	}

	public Facebook getFacebook() {
		if (facebook == null) {
			facebook = new Facebook();
			initSocialNetwork(facebook);
		}
		return facebook;
	}
	
	public ISocialNetwork getLinkedin() {
		if (linkedin == null) {
			linkedin = new Linkedin();
			initSocialNetwork(linkedin);
		}
		return linkedin;
	}
	
	public ISocialNetwork getPushbullet() {
		if (pushbullet == null) {
			pushbullet = new Pushbullet();
			initSocialNetwork(pushbullet);
		}
		return pushbullet;
	}

	public ISocialNetwork getTwitter() {
		if (twitter == null) {
			twitter = new Twitter();
			initSocialNetwork(twitter);
		}
		return twitter;
	}
	
	public ISocialNetwork getGoogle() {		
		if (google == null) {
			google = new Google();
			initSocialNetwork(google);
		}
		return google;
	}
	
	public boolean isActive() {
		return StringHelper.isEmpty(getFacebook().getURL()) || StringHelper.isEmpty(getTwitter().getURL()) || StringHelper.isEmpty(getGoogle().getURL());
	}

	public void store() {
		for (ISocialNetwork network : getAllNetworks()) {
			for (Map.Entry entry : network.getData().entrySet()) {
				String key = KEY_PREFIX + "." + network.getName() + entry.getKey();
				globalContext.setData(key, "" + entry.getValue());
			}
		}
	}
}
