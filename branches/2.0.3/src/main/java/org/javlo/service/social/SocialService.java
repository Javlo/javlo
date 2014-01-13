package org.javlo.service.social;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.javlo.context.GlobalContext;

public class SocialService {

	private static final String KEY_PREFIX = "socialnetwork";

	private GlobalContext globalContext;

	private Facebook facebook;
	private ISocialNetwork twitter;

	public static SocialService getInstance(GlobalContext globalContext) {
		final String KEY = "social";
		SocialService outService = (SocialService) globalContext.getAttribute(KEY);
		if (outService == null) {
			outService = new SocialService();
			outService.globalContext = globalContext;
			globalContext.setAttribute(KEY, outService);

		}
		return outService;
	}

	private void initSocialNetwork(ISocialNetwork socialNetwork) {
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

	public ISocialNetwork getTwitter() {
		if (twitter == null) {
			twitter = new Twitter();
			initSocialNetwork(twitter);
		}
		return twitter;
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
