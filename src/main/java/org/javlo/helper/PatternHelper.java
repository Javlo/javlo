package org.javlo.helper;

import java.util.regex.Pattern;

public class PatternHelper {
	
	public static final Pattern MAIL_PATTERN = Pattern.compile("^['\\w\\s\u00E9\u00E8\u00E0\u00E7\u00EF\u00E4\u00F6\u00FC\u00EB\u00E2\u00EE\u00F4\u00FB\u00EA\u00F9S\u00C9\u00C8\u00C0\u00C7\u00CF\u00C4\u00D6\u00DC\u00CB\u00C2\u00CE\u00D4\u00DB\u00CA\u00D9-]*<?[a-zA-Z][\\w\\.-]*[a-zA-Z0-9]?@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]>?$");
	
	public static final Pattern MULTI_MAIL_PATTERN = Pattern.compile("^([\\w\\s]*<?[a-zA-Z][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]>?\\,?)*$"); //TODO: terminate this pattern
	public static final Pattern PHONE_PATTERN = Pattern.compile("\\+?+[\\d\\s()]++$");
	public static final Pattern ALPHANNUM_NOSPACE_PATTERN = Pattern.compile("([a-z]|[A-Z]|[0-9]|_|-)*");
	public static final Pattern EXTERNAL_LINK_PATTERN = Pattern.compile(".*://.*");
	public static final Pattern HOST_PATTERN = Pattern.compile("([a-z]|[A-Z]|[0-9]|_|-|\\.)*");
	
	public static void main(String[] args) {
		String testString = "pvandermaesen@gmail.com";
		
		System.out.println("MAIL PATTERN");
		System.out.println("");
		System.out.println("host test 192.168.0.1 = "+HOST_PATTERN.matcher("192.168.0.1").matches());
		System.out.println("");
		System.out.println(testString+" : "+MAIL_PATTERN.matcher(testString).matches());
		testString = "<Patrick Vandermaesen>p-vandermaesen@gmail.com";
		System.out.println(testString+" : "+MAIL_PATTERN.matcher(testString).matches());
		testString = "pvandermaesen@gmail.com";
		System.out.println(testString+" : "+MAIL_PATTERN.matcher(testString).matches());
		testString = "p@gmail.com";
		System.out.println(testString+" : "+MAIL_PATTERN.matcher(testString).matches());
		testString = "Maïté Gobert  <maite@melongalia.com>";
		System.out.println(testString+" : "+MAIL_PATTERN.matcher(testString).matches());
		System.out.println("");
		System.out.println("");
		System.out.println("MULTI MAIL PATTERN");
		System.out.println("");
		testString = "<Patrick Vandermaesen>pvandermaesen@gmail.com";
		System.out.println(testString+" : "+MAIL_PATTERN.matcher(testString).matches());
		testString = "pvandermaesen@gmail.com-";
		System.out.println(testString+" : "+MAIL_PATTERN.matcher(testString).matches());
		testString = "patrick Vandermaesen l'homme <pvandermaesen@gmail.com>";
		System.out.println(testString+" : "+MAIL_PATTERN.matcher(testString).matches());
		testString = "<Patrick Vandermaesen>pvandermaesen@gmail.com";
		System.out.println(testString+" : "+MAIL_PATTERN.matcher(testString).matches());
		testString = "pvandermaesen@gmail.com,plemarchand@gmail.com";
		System.out.println(testString+" : "+MAIL_PATTERN.matcher(testString).matches());
		testString = "patrick Vandermaesen <pvandermaesen@gmail.com>";
		System.out.println(testString+" : "+MAIL_PATTERN.matcher(testString).matches());
		System.out.println("");
		System.out.println("");
		System.out.println("PHONE PATTERN");
		System.out.println("");
		testString = "msmkj";
		System.out.println(testString+" : "+PHONE_PATTERN.matcher(testString).matches());
		testString = "+32 486 95 74 35";
		System.out.println(testString+" : "+PHONE_PATTERN.matcher(testString).matches());
		testString = "022157795";
		System.out.println(testString+" : "+PHONE_PATTERN.matcher(testString).matches());
		testString = "112+23";
		System.out.println(testString+" : "+PHONE_PATTERN.matcher(testString).matches());
		testString = "(02)2156696";
		System.out.println(testString+" : "+PHONE_PATTERN.matcher(testString).matches());
		testString = "21";
		System.out.println(testString+" : "+PHONE_PATTERN.matcher(testString).matches());
	}

}
