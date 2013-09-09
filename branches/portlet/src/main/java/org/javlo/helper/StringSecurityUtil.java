package org.javlo.helper;

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

	/*
	 * public static String encode(String data, String inKey) throws Exception {
	 * if (data.length() > charList.length()) { throw new
	 * Exception("encoded data to big : "+data); } String key =
	 * generateFinalKey(inKey); StringBuffer encodedData = new StringBuffer();
	 * char[] dataChars = data.toCharArray(); for (int i = 0; i <
	 * dataChars.length; i++) { char newChar = DELIMITER; int keyPos =
	 * key.indexOf(dataChars[i]); if (keyPos != -1) { newChar =
	 * charList.charAt(keyPos); } if (newChar == DELIMITER) {
	 * encodedData.append(DELIMITER); encodedData.append(dataChars[i]); } else {
	 * encodedData.append(newChar); } } if (encodedData.length() >
	 * charList.length()) { throw new Exception("encoded data to big : "+data);
	 * } return encodedData.toString(); }
	 *
	 * public static String decode(String data, String inKey) { String key =
	 * generateFinalKey(inKey); StringBuffer decodedData = new StringBuffer();
	 * char[] dataChars = data.toCharArray(); int realPos = 0; for (int i = 0; i
	 * < dataChars.length; i++) { char newChar = dataChars[i]; if (newChar ==
	 * DELIMITER) { i++; newChar = dataChars[i]; } else { int keyPos =
	 * charList.indexOf(dataChars[i]); if (keyPos != -1) { newChar =
	 * getCharWithInfinitePosition(key, keyPos); } } realPos++;
	 * decodedData.append(newChar); } return decodedData.toString(); }
	 */

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

}
