package org.javlo.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;

import org.javlo.helper.StringHelper;

/**
 * user access token : give access to javlo with a "Authorization: Bearer" header (remote api, files...).
 * Only the hash of the token is stored (field 'accessToken' of the user), the clear token is
 * displayed one time when it is created. The token have a limited life time (static config :
 * 'security.access-token.days').
 * The token contains 256 random bits, a simple sha-256 (without salt) is enough and allows
 * the lookup of the user.
 */
public class AccessToken {

	/** prefix of the clear token, used to distinguish it from the legacy user token **/
	public static final String TOKEN_PREFIX = "jat_";

	/** prefix of the stored hash **/
	public static final String HASH_PREFIX = "sha256:";

	private static final int TOKEN_BYTES = 32;

	private static final SecureRandom random = new SecureRandom();

	private AccessToken() {
	}

	/**
	 * @return a new clear token.
	 */
	public static String generate() {
		byte[] bytes = new byte[TOKEN_BYTES];
		random.nextBytes(bytes);
		return TOKEN_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	public static boolean isAccessToken(String token) {
		return token != null && token.startsWith(TOKEN_PREFIX);
	}

	public static boolean isHash(String value) {
		return value != null && value.startsWith(HASH_PREFIX);
	}

	/**
	 * @return the value to store for a clear token.
	 */
	public static String hash(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HASH_PREFIX + HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * check a clear token against a stored hash (constant time comparison).
	 */
	public static boolean matches(String storedHash, String token) {
		if (!isHash(storedHash) || !isAccessToken(token)) {
			return false;
		}
		return MessageDigest.isEqual(storedHash.getBytes(StandardCharsets.US_ASCII), hash(token).getBytes(StandardCharsets.US_ASCII));
	}

	/**
	 * @param creationDate creation date of the token (null = unknown >> expired)
	 * @param lifeTimeDays life time in days, &lt;= 0 for no expiration.
	 * @return expiration date, null if no expiration.
	 */
	public static Date getExpirationDate(Date creationDate, int lifeTimeDays) {
		if (lifeTimeDays <= 0 || creationDate == null) {
			return null;
		}
		return new Date(creationDate.getTime() + lifeTimeDays * 24L * 60 * 60 * 1000);
	}

	public static boolean isExpired(Date creationDate, int lifeTimeDays) {
		if (lifeTimeDays <= 0) {
			return false;
		}
		if (creationDate == null) {
			return true;
		}
		return System.currentTimeMillis() > getExpirationDate(creationDate, lifeTimeDays).getTime();
	}

	/**
	 * @return a short not secret version of the token for logs.
	 */
	public static String toLog(String token) {
		if (StringHelper.isEmpty(token)) {
			return "[empty]";
		}
		return "[" + token.length() + "#]";
	}

}
