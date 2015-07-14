package org.javlo.helper;

import java.util.Calendar;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;

public class StringSecurityUtil {

	private static char DELIMITER = '#';

	public static final String REQUEST_ATT_FOR_SECURITY_FORWARD = "__security_jsp_forward";

	private static final String charList = "ABCDEFGkHIKMLqMOPQRSTUVWXYZJ abfhijlmnoprstuvwxyz1234567890_-";

	// private static final String big_charList =
	// charList+charList+charList+charList+charList+charList+charList+charList+charList+charList;

	private static int getCharIndice(char c) {
		for (int i = 0; i < charList.length(); i++) {
			if (charList.charAt(i) == c) {
				return i;
			}
		}
		return -1;
	}

	private static char getCharWithInfinitePosition(String data, int pos) {
		pos = pos % data.length();
		return data.charAt(pos);
	}

	private static String generateFinalKey(String key) {
		String outKey = charList;
		char[] dataChars = key.toCharArray();
		for (int i = 0; i < dataChars.length; i++) {
			outKey = outKey.replace(dataChars[i], ' ');
		}
		outKey = StringUtils.deleteWhitespace(outKey.replace(DELIMITER, ' ')) + ' ';

		String finalKey = StringUtils.left(key + outKey, charList.length());
		finalKey = StringHelper.removeRepeatedChar(finalKey);
		return finalKey;

	}

	private static String keyOnBytes(String key, int sizeInByte) {
		while (key.getBytes().length < sizeInByte) {
			key = key + key;
		}
		while (key.getBytes().length > sizeInByte) {
			key = key.substring(0, key.length()-1);
		}
		return key;
	}

	public static String encode(String data, String inKey) throws Exception {
		byte[] keyBytes = keyOnBytes(inKey, 16).getBytes();

		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
		Cipher cipher = Cipher.getInstance("AES");

		cipher.init(Cipher.ENCRYPT_MODE, key);

		byte[] encryptData = cipher.doFinal(data.getBytes());

		return StringHelper.asBase64(encryptData);
	}

	public static String decode(String data, String inKey) throws Exception {
		byte[] input = StringHelper.decodeBase64(data);
		byte[] keyBytes =  keyOnBytes(inKey, 16).getBytes();

		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
		Cipher cipher = Cipher.getInstance("AES");

		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] original = cipher.doFinal(input);
		return new String(original);
	}

	public static void main(String[] args) {
		System.out.println("String Security test.");
		System.out.println("=====================");
		System.out.println("");
		// for (int i = 0; i < 100; i++) {
		try {
			for (int i = 0; i < 1; i++) {
				String data = "_dynpara_message_id=128574592114297345096&template=template-1";
				data = "_dynpara_message_id=1285745k&template=template-1";
				String key = StringHelper.getRandomString(10);
				// String data = "k";
				// String key = "sdlsjgc";

				String encodedData = encode(data, key);
				//encodedData = "XHXU56wmWZiWhVdTgSbJ/RlHj4rLvWVO+iYKrI7kPcFHze4521IenRx1yuswx3t1oFIMyz84WjL3N5mpP/k0I+7wmpKUaQpkJnc/y3yq9ZCkJHSM/J6OYE12RZyBwr2yiKQmKabAR7HsshvK1tfh4iRzCDMMxksJaGVeJ+Rarb0Ao8JWxEQvUvv0A4PRfo32qNgZqoloE6IcXMT2l5WHnXxfzsdKlPj356X/";
				//String decodedData = decode(encodedData, key);
				System.out.println("");
				/*if (!decodedData.equals(data)) {
					System.out.println("*** ERROR ***");
				}*/
				System.out.println("data        : " + data);
				System.out.println("encodedData : " + encodedData);
				System.out.println("decodedData : " + decode(encodedData, key));
				//System.out.println("decodedData : " + decodedData);

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * return a access token valid for specified time
	 * @param time time in seconds
	 * @return access token
	 */
	public static String getTimeAccessToken(int time) {
		return ""+((new Date()).getTime()+(time*1000))+StringHelper.getRandomString(12, StringHelper.ALPHANUM);
	}
	
	public static boolean isValidAccessToken(String token) {
		if (token == null || token.length() < 14) {
			return false;
		} else {
			String timeStr = token.substring(0, token.length()-12);
			Calendar now = Calendar.getInstance();
			Calendar endTokenCal = Calendar.getInstance();
			endTokenCal.setTime(new Date(Long.parseLong(timeStr)));
			return endTokenCal.after(now);
		}
	}

}
