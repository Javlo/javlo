package org.javlo.helper;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;

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
	
	/**
	 * remove latest number if all IPs in the string
	 * @param ip
	 * @return
	 */
	public static final String anonymisedIp(String ip) {
		Pattern ipv4Pattern = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
		Matcher matcherIp = ipv4Pattern.matcher(ip);
		while(matcherIp.find()) {
			ip = ip.replace(matcherIp.group(), matcherIp.group().substring(0, matcherIp.group().lastIndexOf(".")+1)+"0");
		}
		Pattern ipv6Pattern = Pattern.compile("([\\p{XDigit}]{1,4}:){7}[\\p{XDigit}]{1,4}");
		matcherIp = ipv6Pattern.matcher(ip);
		while(matcherIp.find()) {
			String newIP = matcherIp.group().replaceAll("\\p{XDigit}{1,4}:[\\p{XDigit}]{1,4}$", "0000:0000");
			ip = ip.replace(matcherIp.group(), newIP);
		}
		return ip;
	}
	
	public static void main(String[] args) {
		System.out.println(("2001:0db8:2564:85a3:0000:85B4:ac1f:8001".replaceAll("(\\p{XDigit}{1,4}:){7}[\\p{XDigit}]{1,4}", "><")));
		System.out.println(">>> "+anonymisedIp("2001:0db8:2564:85a3:0000:85B4:ac1f:8001"));
	}

}
