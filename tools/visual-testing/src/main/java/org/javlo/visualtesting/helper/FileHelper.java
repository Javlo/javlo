package org.javlo.visualtesting.helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileHelper {

	public static String encodeAsFileName(String text) {
		final char special = '%';
		Pattern wrongChars = Pattern.compile("[^-A-Za-z0-9\\._]+");

		Matcher m = wrongChars.matcher(text);
		StringBuffer sb = new StringBuffer();
		boolean result = m.find();
		if (result) {
			do {
				m.appendReplacement(sb, "");
				String str = m.group();
				int length = str.length();
				for (int i = 0; i < length; i++) {
					sb.append(special);
					sb.append(Integer.toHexString(str.charAt(i)));
				}
				result = m.find();
			} while (result);
			m.appendTail(sb);
		} else {
			sb.append(text);
		}
		return sb.toString();
	}

	public static String createFileName(Date dateTime) {
		return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(dateTime);
	}

}
