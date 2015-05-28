package org.javlo.visualtesting;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {

	public static String encodeAsFileName(String text) {
		final char special = '%';
		Pattern wrongChars = Pattern.compile("[^-A-Za-z0-9\\._]+");

		Matcher m = wrongChars.matcher(text);
		StringBuffer sb = new StringBuffer();
		boolean result = m.find();
		if (result) {
			do {
				m.appendReplacement(sb, "");
				m.group().chars().forEach((c) -> {
					sb.append(special);
					sb.append(Integer.toHexString(c));
				});
				result = m.find();
			} while (result);
			m.appendTail(sb);
		} else {
			sb.append(text);
		}
		return sb.toString();
	}

}
