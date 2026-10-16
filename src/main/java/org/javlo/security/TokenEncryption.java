package org.javlo.security;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.javlo.helper.StringHelper;

/**
 * encrypt user token at rest (user csv, mapdb, export).
 * The token stay in clear in memory because it must be put in url (mailing, mobile, j_token...).
 * Stored format : "enc1:" + base64url(iv + AES-GCM(token)).
 * A value without prefix is a legacy clear token, it will be encrypted on the next store.
 * The key is read from 'security.token-key' (base64, 32 bytes) or from the file 'token.key' next to the static config file (created if not exist).
 */
public class TokenEncryption {

	private static Logger logger = Logger.getLogger(TokenEncryption.class.getName());

	public static final String PREFIX = "enc1:";

	public static final String KEY_FILE_NAME = "token.key";

	private static final String ALGO = "AES/GCM/NoPadding";
	private static final int IV_SIZE = 12;
	private static final int TAG_SIZE = 128;
	private static final int KEY_SIZE = 32;

	private static final SecureRandom random = new SecureRandom();

	private static volatile SecretKeySpec key = null;

	private static boolean warningNoKey = false;

	/**
	 * init the key.
	 * @param configKey base64 key from config (can be null)
	 * @param configFolder folder used to store a generated key if no key defined in config.
	 */
	public static synchronized void init(String configKey, File configFolder) {
		try {
			byte[] keyBytes = null;
			if (!StringHelper.isEmpty(configKey) && !configKey.startsWith("${")) {
				keyBytes = Base64.getDecoder().decode(configKey.trim());
			} else if (configFolder != null) {
				File keyFile = new File(configFolder, KEY_FILE_NAME);
				if (keyFile.exists()) {
					keyBytes = Base64.getDecoder().decode(new String(Files.readAllBytes(keyFile.toPath()), StandardCharsets.UTF_8).trim());
				} else {
					keyBytes = new byte[KEY_SIZE];
					random.nextBytes(keyBytes);
					configFolder.mkdirs();
					Files.write(keyFile.toPath(), Base64.getEncoder().encodeToString(keyBytes).getBytes(StandardCharsets.UTF_8));
					keyFile.setReadable(false, false);
					keyFile.setReadable(true, true);
					logger.info("token encryption key created : " + keyFile);
				}
			}
			if (keyBytes == null) {
				logger.warning("no token encryption key, token will be stored in clear.");
				return;
			}
			if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
				logger.severe("bad token encryption key size (" + keyBytes.length + " bytes), token will be stored in clear.");
				return;
			}
			key = new SecretKeySpec(keyBytes, "AES");
		} catch (IOException | IllegalArgumentException e) {
			logger.log(Level.SEVERE, "error on init token encryption key, token will be stored in clear.", e);
		}
	}

	public static boolean isEncrypted(String value) {
		return value != null && value.startsWith(PREFIX);
	}

	/**
	 * encrypt a clear token for storage.
	 * @return encrypted token, or the token itself if empty, already encrypted or if no key.
	 */
	public static String encrypt(String token) {
		if (StringHelper.isEmpty(token) || isEncrypted(token)) {
			return token;
		}
		SecretKeySpec localKey = key;
		if (localKey == null) {
			if (!warningNoKey) {
				warningNoKey = true;
				logger.warning("token encryption not initialized, token stored in clear.");
			}
			return token;
		}
		try {
			byte[] iv = new byte[IV_SIZE];
			random.nextBytes(iv);
			Cipher cipher = Cipher.getInstance(ALGO);
			cipher.init(Cipher.ENCRYPT_MODE, localKey, new GCMParameterSpec(TAG_SIZE, iv));
			byte[] data = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));
			ByteBuffer out = ByteBuffer.allocate(iv.length + data.length);
			out.put(iv);
			out.put(data);
			return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(out.array());
		} catch (Exception e) {
			throw new IllegalStateException("error on token encryption", e);
		}
	}

	/**
	 * decrypt a stored token.
	 * @return clear token, the value itself if it is a legacy clear token, or "" if it can not be decrypted.
	 */
	public static String decrypt(String value) {
		if (!isEncrypted(value)) {
			return value;
		}
		SecretKeySpec localKey = key;
		if (localKey == null) {
			logger.severe("token encryption not initialized, encrypted token can not be read.");
			return "";
		}
		try {
			byte[] in = Base64.getUrlDecoder().decode(value.substring(PREFIX.length()));
			Cipher cipher = Cipher.getInstance(ALGO);
			cipher.init(Cipher.DECRYPT_MODE, localKey, new GCMParameterSpec(TAG_SIZE, in, 0, IV_SIZE));
			return new String(cipher.doFinal(in, IV_SIZE, in.length - IV_SIZE), StandardCharsets.UTF_8);
		} catch (Exception e) {
			logger.severe("error on token decryption (key changed ?) : " + e.getMessage());
			return "";
		}
	}

}
