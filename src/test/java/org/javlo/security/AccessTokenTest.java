package org.javlo.security;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javlo.helper.BeanHelper;
import org.javlo.helper.StringHelper;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserInfo;

import junit.framework.TestCase;

public class AccessTokenTest extends TestCase {

	public void testGenerateAndMatch() {
		String token = AccessToken.generate();
		assertTrue(AccessToken.isAccessToken(token));
		assertNotSame(token, AccessToken.generate());
		String hash = AccessToken.hash(token);
		assertTrue(AccessToken.isHash(hash));
		assertFalse(hash.contains(token));
		assertTrue(AccessToken.matches(hash, token));
		assertFalse(AccessToken.matches(hash, AccessToken.generate()));
		assertFalse(AccessToken.matches(hash, null));
		assertFalse(AccessToken.matches(null, token));
		/* a hash is not a valid token */
		assertFalse(AccessToken.matches(hash, hash));
	}

	public void testExpiration() {
		Date now = new Date();
		Date old = new Date(now.getTime() - 31L * 24 * 60 * 60 * 1000);
		assertFalse(AccessToken.isExpired(now, 30));
		assertTrue(AccessToken.isExpired(old, 30));
		assertFalse(AccessToken.isExpired(old, 0));
		assertTrue(AccessToken.isExpired(null, 30));
	}

	public void testUserInfoAccessToken() {
		UserInfo userInfo = new UserInfo();
		userInfo.setLogin("test");
		String clear = userInfo.createAccessToken();
		assertTrue(userInfo.checkAccessToken(clear, 30));
		assertFalse(userInfo.getAccessToken().contains(clear));
		assertNotNull(userInfo.accessTokenExpirationDate(30));
		/* a new token revoke the previous one */
		String clear2 = userInfo.createAccessToken();
		assertFalse(userInfo.checkAccessToken(clear, 30));
		assertTrue(userInfo.checkAccessToken(clear2, 30));
		/* expired */
		userInfo.setAccessTokenDate(StringHelper.renderTime(new Date(System.currentTimeMillis() - 40L * 24 * 60 * 60 * 1000)));
		assertFalse(userInfo.checkAccessToken(clear2, 30));
		assertTrue(userInfo.checkAccessToken(clear2, 0));
		userInfo.removeAccessToken();
		assertFalse(userInfo.checkAccessToken(clear2, 0));
	}

	public void testClearTokenNeverStored() {
		UserInfo userInfo = new UserInfo();
		userInfo.setAccessToken(AccessToken.generate());
		assertEquals("", userInfo.getAccessToken());
	}

	public void testStorageRoundTrip() throws Exception {
		UserInfo userInfo = new UserInfo();
		userInfo.setLogin("test");
		String clear = userInfo.createAccessToken();
		List<String> labels = Arrays.asList(userInfo.getAllLabels());
		assertTrue(labels.contains("accessToken"));
		assertTrue(labels.contains("accessTokenDate"));
		Map<String, String> values = new HashMap<String, String>();
		String[] allValues = userInfo.getAllValues();
		for (int i = 0; i < labels.size(); i++) {
			values.put(labels.get(i), allValues[i]);
		}
		/* same load as UserFactory */
		UserInfo loaded = new UserInfo();
		BeanHelper.copy(values, loaded, false);
		assertTrue(loaded.checkAccessToken(clear, 30));
	}

	public void testSecretFieldsNotSetFromRequest() throws Exception {
		UserInfo userInfo = new UserInfo();
		Map<String, String> request = new HashMap<String, String>();
		request.put("firstName", "Patrick");
		request.put("token", "forged");
		request.put("accessToken", AccessToken.hash("jat_forged"));
		request.put("accessTokenDate", StringHelper.renderTime(new Date()));
		BeanHelper.copy(request, userInfo, false, IUserInfo.SECRET_FIELDS);
		assertEquals("Patrick", userInfo.getFirstName());
		assertEquals("", userInfo.getToken());
		assertEquals("", userInfo.getAccessToken());
		userInfo.setValue("accessToken", AccessToken.hash("jat_forged"));
		assertEquals("", userInfo.getAccessToken());
	}

	public void testPublicMap() throws Exception {
		UserInfo userInfo = new UserInfo();
		userInfo.setLogin("test");
		userInfo.resetToken();
		userInfo.createAccessToken();
		Map<String, String> map = IUserInfo.toPublicMap(userInfo);
		assertEquals("test", map.get("login"));
		for (String secret : IUserInfo.SECRET_FIELDS) {
			assertFalse(secret, map.containsKey(secret));
		}
		/* no value of the public map / export labels can contain the clear internal token */
		for (Map.Entry<String, String> e : map.entrySet()) {
			assertFalse(e.getKey(), e.getValue() != null && e.getValue().contains(userInfo.getToken()));
		}
		for (String label : BeanHelper.getAllLabels(userInfo)) {
			assertFalse(label, label.equalsIgnoreCase("tokenCreateIfNotExist"));
		}
		for (String label : userInfo.getAllLabels()) {
			assertFalse(label, label.equalsIgnoreCase("tokenCreateIfNotExist"));
		}
	}

}
