package org.javlo.visualtesting.helper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

	public static final DateTimeFormatter FILE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");

	public static String createFileName(LocalDateTime dateTime) {
		return FILE_DATE_TIME_FORMATTER.format(ZonedDateTime.of(dateTime, ZoneId.systemDefault()));
	}

}
