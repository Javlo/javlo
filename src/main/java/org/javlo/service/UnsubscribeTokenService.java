package org.javlo.service;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Génère et relit les tokens de désabonnement utilisés dans l'en-tête
 * List-Unsubscribe.
 *
 * Le token est un chiffré authentifié AES-256-GCM : toute modification, même
 * d'un seul bit, est détectée au déchiffrement, et l'adresse email n'apparaît
 * pas en clair dans l'URL.
 */
public class UnsubscribeTokenService {

	private static Logger logger = Logger.getLogger(UnsubscribeTokenService.class.getName());

	private static final String VERSION = "v1";

	private static final String TRANSFORMATION = "AES/GCM/NoPadding";

	private static final int IV_SIZE = 12;

	private static final int TAG_SIZE_BIT = 128;

	private static final char FIELD_SEPARATOR = '|';

	private static final char ROLE_SEPARATOR = ';';

	private final SecretKey key;

	private final SecureRandom random = new SecureRandom();

	public UnsubscribeTokenService(String secret) {
		this.key = buildKey(secret);
	}

	private static SecretKey buildKey(String secret) {
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
			return new SecretKeySpec(hash, "AES");
		} catch (Exception e) {
			throw new IllegalStateException("can not build unsubscribe key", e);
		}
	}

	public String create(TokenData data) {
		try {
			byte[] iv = new byte[IV_SIZE];
			random.nextBytes(iv);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_SIZE_BIT, iv));
			byte[] encrypted = cipher.doFinal(serialize(data).getBytes(StandardCharsets.UTF_8));
			ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
			buffer.put(iv);
			buffer.put(encrypted);
			return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
		} catch (Exception e) {
			throw new IllegalStateException("can not create unsubscribe token", e);
		}
	}

	/**
	 * @return les données du token, ou null si le token est invalide, corrompu,
	 *         ou émis pour un autre site.
	 */
	public TokenData read(String token, String expectedContextKey) {
		if (token == null || token.trim().isEmpty()) {
			return null;
		}
		try {
			byte[] raw = Base64.getUrlDecoder().decode(token);
			if (raw.length <= IV_SIZE) {
				return null;
			}
			byte[] iv = new byte[IV_SIZE];
			System.arraycopy(raw, 0, iv, 0, IV_SIZE);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_SIZE_BIT, iv));
			byte[] plain = cipher.doFinal(raw, IV_SIZE, raw.length - IV_SIZE);
			TokenData data = deserialize(new String(plain, StandardCharsets.UTF_8));
			if (data == null || !data.getContextKey().equals(expectedContextKey)) {
				return null;
			}
			return data;
		} catch (Exception e) {
			logger.fine("invalid unsubscribe token : " + e.getMessage());
			return null;
		}
	}

	private static String encode(String value) {
		return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
	}

	private static String decode(String value) {
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}

	private static String serialize(TokenData data) {
		StringBuilder roles = new StringBuilder();
		for (String role : data.getRoles()) {
			if (roles.length() > 0) {
				roles.append(ROLE_SEPARATOR);
			}
			roles.append(encode(role));
		}
		StringBuilder out = new StringBuilder();
		out.append(VERSION);
		out.append(FIELD_SEPARATOR).append(encode(data.getContextKey()));
		out.append(FIELD_SEPARATOR).append(encode(data.getMailingId()));
		out.append(FIELD_SEPARATOR).append(encode(data.getEmail()));
		out.append(FIELD_SEPARATOR).append(roles);
		out.append(FIELD_SEPARATOR).append(data.getTimestamp());
		return out.toString();
	}

	private static TokenData deserialize(String plain) {
		String[] parts = plain.split("\\" + FIELD_SEPARATOR, -1);
		if (parts.length != 6 || !VERSION.equals(parts[0])) {
			return null;
		}
		Collection<String> roles = new LinkedList<String>();
		if (!parts[4].isEmpty()) {
			for (String role : parts[4].split(String.valueOf(ROLE_SEPARATOR), -1)) {
				roles.add(decode(role));
			}
		}
		return new TokenData(decode(parts[1]), decode(parts[2]), decode(parts[3]), roles, Long.parseLong(parts[5]));
	}

	public static class TokenData {

		private final String contextKey;
		private final String mailingId;
		private final String email;
		private final Collection<String> roles;
		private final long timestamp;

		public TokenData(String contextKey, String mailingId, String email, Collection<String> roles, long timestamp) {
			this.contextKey = contextKey;
			this.mailingId = mailingId;
			this.email = email;
			this.roles = roles == null ? new LinkedList<String>() : roles;
			this.timestamp = timestamp;
		}

		public String getContextKey() {
			return contextKey;
		}

		public String getMailingId() {
			return mailingId;
		}

		public String getEmail() {
			return email;
		}

		public Collection<String> getRoles() {
			return roles;
		}

		public long getTimestamp() {
			return timestamp;
		}
	}
}
