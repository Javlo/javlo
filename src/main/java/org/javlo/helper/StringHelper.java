/*
 * Created on 08-janv.-2004
 */
package org.javlo.helper;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.javlo.component.list.FreeTextList;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.Comparator.StringComparator;
import org.javlo.i18n.I18nAccess;
import org.jsoup.Jsoup;
import org.owasp.encoder.Encode;

import com.beust.jcommander.ParameterException;

/**
 * @author pvandermaesen
 */
public class StringHelper {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(StringHelper.class.getName());

	public static final String SOMETHING = "st";

	public static final String REQUEST_KEY_FORM_VALID = "__form_request_valid__";

	public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

	public static final String NUMERIC = "0123456789";

	public static final String BASIC_CHAR = ALPHABET.toLowerCase() + ALPHABET.toUpperCase() + NUMERIC + "_-+= ,.;:*|&\"'";

	public static final String ALPHANUM = ALPHABET + NUMERIC;

	public static String specialChar = " &\u00e9\"'(\u00a7\u00e8!\u00e7\u00e0)-^$\u00f9\u00b5=:;,+/.?\u00a3%*\u00a8_\u00b0\u00b2\u00b3|@#{[^\u00e8!{})";

	public static String DEFAULT_SEPARATOR = "?";

	private static final char DEFAULT_ESCAPE = '\\';

	public static final String[][] TXT2HTML = { { "\u00c1", "&Aacute;" }, { "\u00e1", "&aacute;" }, { "\u00c0", "&Agrave;" }, { "\u00e0", "&agrave;" }, { "\u00e7", "&ccedil;" }, { "\u00c7", "&Ccedil;" }, { "\u00c9", "&Eacute;" }, { "\u00e9", "&eacute;" }, { "\u00c8", "&Egrave;" }, { "\u00e8", "&egrave;" }, { "\u00ca", "&Ecirc;" }, { "\u00ea", "&ecirc;" }, { "\u00cf", "&Iuml;" }, { "\u00ef", "&iuml;" }, { "\u00f9", "&ugrave;" }, { "\u00d9", "&Ugrave;" }, { "\u2019", "'" }, { "\u00D6", "&Ouml;" }, { "\u00F6", "&ouml;" } };

	public static SimpleDateFormat RFC822DATEFORMAT = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

	private static final String EU_ACCEPTABLE_CHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.\u0443\u0435\u0438\u0448\u0449\u043a\u0441\u0434\u0437\u0446\u044c\u044f\u0430\u043e\u0436\u0433\u0442\u043d\u0432\u043c\u0447\u044e\u0439\u044a\u044d\u0444\u0445\u043f\u0440\u043b\u0431\u044b\u0423\u0415\u0418\u0428\u0429\u041a\u0421\u0414\u0417\u0426\u042c\u042f\u0410\u041e\u0416\u0413\u0422\u041d\u0412\u041c\u0427\u042e\u0419\u042a\u042d\u0424\u0425\u041f\u0420\u041b\u0411\u03c2\u03b5\u03c1\u03c4\u03c5\u03b8\u03b9\u03bf\u03c0\u03b1\u03c3\u03b4\u03c6\u03b3\u03b7\u03be\u03ba\u03bb\u03b6\u03c7\u03c8\u03c9\u03b2\u03bd\u03bc\u0395\u03a1\u03a4\u03a5\u0398\u0399\u039f\u03a0\u0391\u03a3\u03a6\u0393\u0397\u039e\u039a\u039b\u0396\u03a7\u03a8\u03a9\u0392\u039d\u039c";

	private static final String EU_ACCEPTABLE_CHAR_NO_POINT = EU_ACCEPTABLE_CHAR.replace(".", "");

	private static final String ISO_ACCEPTABLE_CHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.";

	private static final String KEY_ACCEPTABLE_CHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	public static final String DEFAULT_LIST_SEPARATOR = "?";

	private static long previousRandomId = System.currentTimeMillis();

	private static long previousShortRandomId = 0;

	private static String previousDateId = "";

	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	public static final Pattern RANGE_MATCHER_LOWER = Pattern.compile("^[-<]([0-9]+)$");
	public static final Pattern RANGE_MATCHER_BETWEEN = Pattern.compile("^([0-9]+)-([0-9]+)$");
	public static final Pattern RANGE_MATCHER_GREATER = Pattern.compile("^[+>]([0-9]+)$|^([0-9]+)[+>]$");

	public static final long TIMED_TOKEN_DIVIDER = 1000 * 60; // Millis to
																// minutes

	public static String addSufixToFileName(String fileName, String sufix) {
		return FilenameUtils.removeExtension(fileName) + sufix + "." + FilenameUtils.getExtension(fileName);
	}

	public static String arrayToString(Object[] array) {
		return arrayToString(array, DEFAULT_SEPARATOR);
	}

	public static String arrayToString(Object[] array, String inSep) {
		if (array == null) {
			return null;
		}
		StringBuffer out = new StringBuffer();
		String sep = "";
		for (Object element : array) {
			out.append(sep + element);
			sep = inSep;
		}
		return out.toString();
	}

	/**
	 * generate a base 64 string.
	 * 
	 * @return a unique id.
	 */
	public static String asBase64(byte[] bytes) {
		return Base64.encodeBase64URLSafeString(bytes);
	}

	/**
	 * generate a base 64 string with a int
	 * 
	 * @return a unique id.
	 */
	public static String asBase64(int value) {
		return asBase64(JavaHelper.intToByteArray(value));
	}

	/**
	 * generate a base 64 string with a long
	 * 
	 * @return a unique id.
	 */
	public static String asBase64(long value) {
		return asBase64(JavaHelper.longToByteArray(value));
	}

	/**
	 * change the file extension of the path
	 * 
	 * @param inFileName
	 *            a path or a url to a file.
	 * @param newExt
	 *            the new extension of the file.
	 * @return the same path with the new extension.
	 */
	public static String changeFileExtension(String inFileName, String newExt) {
		String outExt = inFileName;
		int dotIndex = inFileName.lastIndexOf('.');
		if (dotIndex >= 0) {
			outExt = inFileName.substring(0, dotIndex);
		}
		return outExt + '.' + newExt;
	}

	public static String collectionToString(Collection<?> col) {
		return collectionToString(col, DEFAULT_SEPARATOR);
	}

	/**
	 * convert a collection to text. Each item of the collection will be a line
	 * if the text.
	 * 
	 * @param col
	 * @return
	 */
	public static String collectionToText(Collection<?> col) {
		if (col == null || col.size() == 0) {
			return "";
		}
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		for (Object object : col) {
			out.println(object);
		}
		out.close();
		return new String(outStream.toByteArray());
	}

	public static String collectionToString(Collection<?> col, String inSep) {
		return concat(col, inSep, DEFAULT_ESCAPE);
	}

	public static String colorToHexString(Color color) {
		String colorStr = Integer.toHexString(color.getRGB());
		return colorStr.substring(2, colorStr.length());
	}

	public static String colorToHexStringNotNull(Color color) {
		if (color == null) {
			return "";
		}
		return colorToHexString(color);
	}

	/**
	 * convert a string to another string if math pattern1 and convert to
	 * pattern2. pattern is a String with one '*' for any characters. sample :
	 * /*, /test/*, /view/fr --> /test/view/fr
	 * 
	 * @param pattern1
	 *            a pattern with one '*'
	 * @param pattern2
	 *            a pattern with one '*'
	 * @param text
	 * @return
	 */
	public static String convertString(String pattern1, String pattern2, String text) {
		String prefix = StringUtils.replace(pattern1, "*", "");
		String sufix = "";
		String[] pattern1Splited = StringUtils.split(pattern1, '*');
		if (pattern1Splited.length > 1) {
			prefix = pattern1Splited[0];
			sufix = pattern1Splited[1];
		}
		if (text.startsWith(prefix) && text.endsWith(sufix)) {
			String outPrefix = StringUtils.replace(pattern2, "*", "");
			String outSufix = "";
			String[] pattern2Splited = StringUtils.split(pattern2, '*');
			if (pattern2Splited.length > 1) {
				outPrefix = pattern2Splited[0];
				outSufix = pattern2Splited[1];
			}
			text = text.replaceFirst(prefix, outPrefix);
			text = StringUtils.removeEnd(text, sufix);
			return text + outSufix;
		} else {
			System.out.println("not match pattern 1.");
			return text;
		}
	}

	public static int countChar(String str, char c) {
		char[] chars = str.toCharArray();
		int count = 0;
		for (char d : chars) {
			if (d == c) {
				count++;
			}
		}
		return count;
	}

	/**
	 * replace CR with <br />
	 * 
	 * @param text
	 *            a simple text
	 * @return XHTML code
	 */
	public static String CR2BR(String text) {
		return replaceCR(text, "<br />");
	}

	public static String replaceCR(String text, String separator) {
		String res = text;
		StringReader reader = new StringReader(res);
		BufferedReader bReader = new BufferedReader(reader);
		StringBuffer CRres = new StringBuffer();
		try {
			String line = bReader.readLine();
			while (line != null) {
				CRres.append(line);
				line = bReader.readLine();
				if (line != null) {
					CRres.append(separator);
				}
			}
		} catch (IOException e) {
			// impossible
		}
		return CRres.toString();
	}

	public static String createASCIIString(String text) {
		return createCleanName(text, "azertyuiopqsdfghjklmwxcvbnAZERTYUIOPQSDFGHJKLMWXCVBN0123456789,._- ", '_');
	}

	private static String createCleanName(String fileName, String acceptableCharacters, char defaultReplaceChar) {
		fileName = fileName.trim();

		StringBuffer res = new StringBuffer();

		char[] source = fileName.toCharArray();
		for (char element : source) {
			if (acceptableCharacters.indexOf(element) >= 0) {
				res.append(element);
			} else {
				switch (element) {
				case '\u00e9':
				case '\u00e8':
				case '\u00eb':
				case '\u00ea':
					res.append('e');
					break;
				case '\u00c9':
				case '\u00c8':
				case '\u00cb':
				case '\u00ca':
					res.append('E');
					break;
				case '\u00e0':
				case '\u00e2':
				case '\u00e4':
				case '\u00e3':
				case '\u00e5':
					res.append('a');
					break;
				case '\u00c0':
				case '\u00c2':
				case '\u00c4':
				case '\u00c3':
				case '\u00c5':
					res.append('A');
					break;
				case '\u00ee':
				case '\u00ef':
				case '\u00ec':
				case '\u00ed':
					res.append('i');
					break;
				case '\u00ce':
				case '\u00cf':
				case '\u00cc':
				case '\u00cd':
					res.append('I');
					break;
				case '\u00f2':
				case '\u00f3':
				case '\u00f4':
				case '\u00f5':
					res.append('o');
					break;
				case '\u00d2':
				case '\u00d3':
				case '\u00d4':
				case '\u00d5':
					res.append('O');
					break;
				case '\u00f9':
				case '\u00fa':
				case '\u00fb':
				case '\u00fc':
					res.append('u');
					break;
				case '\u00d9':
				case '\u00da':
				case '\u00db':
				case '\u00dc':
					res.append('U');
					break;
				case '\u00fd':
				case '\u00fe':
				case '\u00ff':
					res.append('y');
					break;
				case '\u0160':
					res.append('S');
					break;
				case '\u0161':
					res.append('s');
					break;
				case '\u00dd':
				case '\u00de':
				case '\u0178':
					res.append('Y');
					break;
				case '\u00e7':
					res.append('c');
					break;
				case '\u00c7':
					res.append('C');
					break;
				case '\u20ac':
					res.append("EUR");
					break;
				case '$':
					res.append("USD");
					break;
				case '/':
				case '\\':
					res.append("-");
					break;
				default:
					res.append(defaultReplaceChar);
					break;
				}

			}
		}
		String cleanName = res.toString();
		while (cleanName.contains(("" + defaultReplaceChar) + defaultReplaceChar)) {
			cleanName = cleanName.replace(("" + defaultReplaceChar) + defaultReplaceChar, "" + defaultReplaceChar);
		}
		return cleanName;
	}

	/**
	 * convert a file name with space, accent and other bad character to a
	 * acceptable name. sample: "l'\u00e9l\u00e9phant rose.odt" ->
	 * "l_elephant_rose.odt"
	 * 
	 * @param fileName
	 *            a bad file name
	 * @return a correct file name
	 */
	public static String createFileName(String fileName) {
		return createFileName(fileName, '-');
	}

	private static String createFileName(String fileName, char defaultReplaceChar) {
		return createCleanName(fileName, ISO_ACCEPTABLE_CHAR, defaultReplaceChar).toLowerCase();
	}

	public static String createI18NURL(String value) {
		value = value.trim();
		value = createCleanName(value, EU_ACCEPTABLE_CHAR_NO_POINT, '-');
		return trim(value, '-').toLowerCase();
	}

	/**
	 * replace trimChar at the start and the end of text string. sample :
	 * --test-, - >>> test
	 * 
	 * @param text
	 * @param trimChar
	 * @return
	 */
	public static String trim(String text, char trimChar) {
		if (text == null || text.length() == 0 || (text.charAt(0) != trimChar && text.charAt(text.length() - 1) != trimChar)) {
			return text;
		} else {
			int s = 0;
			while (s < text.length() && text.charAt(s) == trimChar) {
				s++;
			}
			int e = text.length() - 1;
			while (e > 0 && text.charAt(e) == trimChar) {
				e--;
			}
			if (s >= e) {
				return "";
			} else {
				return text.substring(s, e + 1);
			}
		}
	}

	public String noRepeatChar(String text, char chr) {
		StringBuffer outStr = new StringBuffer();
		char latestChar = 0;
		for (char c : text.toCharArray()) {
			if (!(latestChar == chr && latestChar == c)) {
				outStr.append(c);
			}
			latestChar = c;
		}
		return outStr.toString();
	}

	/**
	 * replace CR with a space
	 * 
	 * @param text
	 *            a simple text
	 * @return one line text
	 */
	public static String CRtoSpace(String text) {
		String res = text;
		StringReader reader = new StringReader(res);
		BufferedReader bReader = new BufferedReader(reader);
		StringBuffer CRres = new StringBuffer();
		try {
			String line = bReader.readLine();
			while (line != null) {
				CRres.append(line);
				CRres.append(" ");
				line = bReader.readLine();
			}
		} catch (IOException e) {
			// impossible
		}
		return CRres.toString();
	}

	/**
	 * cut the end of a xhtml code. sample: "aaa&acute;coucou", 5 =
	 * "aaa&acute;c"
	 * 
	 * @param str
	 *            the string
	 * @param cut
	 *            the end charaster must be removed.
	 * @return a trunced XHTML String
	 */
	public static String cutEndXHTML(String str, int cut) {

		StringBuffer res = new StringBuffer();
		StringBuffer inCodeRes = new StringBuffer();

		int c = 0;
		int cInCode = 0;
		boolean inHTMLCode = false;
		for (int i = 0; i < str.length(); i++) {
			if (!inHTMLCode) {
				if (str.charAt(i) == '&') {
					inHTMLCode = true;
					cInCode++;
				} else {
					c++;
				}
			} else {
				cInCode++;
				if (str.charAt(i) == ';') {
					inHTMLCode = false;
					c++;
					cInCode = 0;
				} else if (str.charAt(i) == '&') {
					c = c + cInCode;
					cInCode = 0;
				}
			}
			if (cInCode != 0) {
				inCodeRes.append(str.charAt(i));
			} else if ((c <= cut)) {
				res.append(inCodeRes);
				inCodeRes = new StringBuffer();
				res.append(str.charAt(i));
			}
		}
		if (c <= cut) {
			int end = cut - c;
			if (end > inCodeRes.length()) {
				end = inCodeRes.length();
			}

			res.append(inCodeRes.substring(0, end));
		}
		return res.toString();
	}

	/**
	 * generate a base 64 string.
	 * 
	 * @return a unique id.
	 * @throws IOException
	 */
	public static byte[] decodeBase64(String data) throws IOException {
		return Base64.decodeBase64(data);
	}

	/**
	 * insert char from a Sting (encoded with encodeNoChar)
	 * 
	 * @param data
	 *            string to be encoded
	 * @param noChars
	 *            list of vorbiden char
	 * @return
	 */
	public static String decodeNoChar(String data, String noChars) {

		String codeLengthStr = data.substring(0, data.indexOf('a'));
		int codeLength = Integer.parseInt(codeLengthStr);

		data = data.substring(data.indexOf('a') + 1);
		String outData = data;
		for (int i = 0; i < noChars.length(); i++) {
			String code = data.substring(i * codeLength, i * codeLength + codeLength);
			outData = StringUtils.replace(outData, code, "" + noChars.charAt(i));
		}
		return outData.substring(noChars.length());
	}

	/**
	 * encode a string to a CDATA sequence.
	 * 
	 * @param value
	 * @return
	 */
	public static String encodeCDATA(String value) {
		return "<![CDATA[" + value.replaceAll("]]>", "]]>]]><![CDATA[") + "]]>";
	}

	/**
	 * remove char from a Sting
	 * 
	 * @param data
	 *            string to be encoded
	 * @param noChars
	 *            list of vorbiden char
	 * @return
	 */
	public static String encodeNoChar(String data, String noChars) {
		String footer = "";
		String outData = data;

		// find code size
		int codeLength = 1;
		double maxLenghtPosible = Math.pow(ALPHANUM.length(), codeLength) - noChars.length() * codeLength;
		while (maxLenghtPosible < data.length()) {
			codeLength++;
			maxLenghtPosible = Math.pow(ALPHANUM.length(), codeLength) - noChars.length() * codeLength;
		}

		for (int i = 0; i < noChars.length(); i++) {
			String code = RandomStringUtils.random(codeLength, ALPHANUM.toCharArray());
			while (outData.contains(code)) {
				code = RandomStringUtils.random(codeLength, ALPHANUM.toCharArray());
			}
			outData = StringUtils.replace(outData, "" + noChars.charAt(i), code);
			footer = footer + code;
		}
		return "" + codeLength + 'a' + footer + outData;
	}

	public synchronized static String encryptPassword(String plaintext) {
		if (plaintext == null) {
			return null;
		}
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA"); // step 2
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		try {
			md.update(plaintext.getBytes("UTF-8")); // step 3
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		byte raw[] = md.digest(); // step 4
		String hash = Base64.encodeBase64String(raw); // step 5
		return hash.trim(); // step 6
	}

	public static String escapeWordChar(String text) {

		if (text.startsWith("************")) {
			System.out.println("before text = " + text.charAt(text.length() - 1) + " " + (int) text.charAt(text.length() - 1));
			String outText = text.replace((char) 146, '\'');
			System.out.println("after text = " + outText.charAt(text.length() - 1) + " " + (int) outText.charAt(text.length() - 1));
		}
		String outText = text.replace((char) 146, '\'');
		return outText;
	}

	public static String expandSystemProperties(String str) {
		// TODO parse with a Pattern in place of barbarian replace > faster
		for (Entry<Object, Object> property : System.getProperties().entrySet()) {
			if (property.getValue() != null) {
				str = str.replace("${" + property.getKey() + "}", (String) property.getValue());
			}
		}
		return str;
	}

	public static String firstLetterLower(String string) {
		if (string.length() <= 1) {
			return string.toLowerCase();
		} else if (Character.isLowerCase(string.charAt(1))) {
			return string.substring(0, 1).toLowerCase() + string.substring(1, string.length());
		} else {
			return string;
		}
	}

	public static String firstLetterUpper(String string) {
		return string.substring(0, 1).toUpperCase() + string.substring(1, string.length());
	}

	/**
	 * split a String, if there are no character between two token this method
	 * place a empty String ( != String.split )
	 * 
	 * @param str
	 *            a standard str
	 * @param token
	 *            a token ( not pattern !!! )
	 * @return a array of String without the token
	 */
	public static String frameTokenCaseUnsensitive(String str, String token, String prefix, String sufix) {

		int start = 0;
		StringBuffer outStr = new StringBuffer();
		String workStr = str.toLowerCase();
		String workToken = token.toLowerCase();
		int i = workStr.indexOf(workToken);
		while (i >= 0) {
			outStr.append(str.substring(start, i));
			outStr.append(prefix);
			outStr.append(str.substring(i, i + token.length()));
			outStr.append(sufix);

			start = i + token.length();
			i = workStr.indexOf(workToken, start);
		}
		outStr.append(str.substring(start, str.length()));
		return outStr.toString();
	}

	/**
	 * return a ID contruct on with the current date : sample : 20101223-4687
	 * 
	 * @return
	 */
	public static String getDateRandomId() {
		DateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);

		NumberFormat formatter = new DecimalFormat("00000");

		String newDateId;

		do {
			Calendar today = Calendar.getInstance();
			int daySec = today.get(Calendar.HOUR_OF_DAY) * 24 * 60 + today.get(Calendar.MINUTE) * 60 + today.get(Calendar.SECOND);
			String currentDate = format.format(today.getTime());
			newDateId = currentDate + '-' + formatter.format(daySec);

			if (newDateId.equals(previousDateId)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		} while (newDateId.equals(previousDateId));

		previousDateId = newDateId;

		return newDateId;
	}

	/**
	 * return a ID contruct on with the current date : sample : 20101223-4687
	 * 
	 * @return
	 */
	public static String getDateRandomIdWithCheck() {
		DateFormat format = new SimpleDateFormat("yyyyMMdd", Locale.US);

		NumberFormat formatter = new DecimalFormat("00000");

		String newDateId;

		do {
			Calendar today = Calendar.getInstance();
			int daySec = today.get(Calendar.HOUR_OF_DAY) * 24 * 60 + today.get(Calendar.MINUTE) * 60 + today.get(Calendar.SECOND);
			String currentDate = format.format(today.getTime());
			String prefix = currentDate + '-' + formatter.format(daySec);
			newDateId = prefix + '-' + getDigitMod9(prefix);

			if (newDateId.equals(previousDateId)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		} while (newDateId.equals(previousDateId));

		previousDateId = newDateId;

		return newDateId;
	}

	public static int getDigitMod9(String value) {
		int sum = 0;
		for (char c : value.toCharArray()) {
			if (StringHelper.isDigit(c)) {
				sum = sum + Integer.parseInt("" + c);
			}
		}
		return sum % 9;
	}

	/**
	 * retreive the file extension.
	 * 
	 * @param inFileName
	 *            a file name
	 * @return a file extension without dot ( pdf, zip, ... )
	 */
	public static String getFileExtension(String inFileName) {
		if (inFileName == null) {
			return "";
		}
		String outExt = "";
		int dotIndex = inFileName.lastIndexOf('.');
		int endIndex = inFileName.lastIndexOf('?');
		int jsessionIndex = inFileName.lastIndexOf(';');
		if (jsessionIndex >= 0 && dotIndex > jsessionIndex) {
			dotIndex = inFileName.substring(0, jsessionIndex).lastIndexOf('.');
		}
		if (endIndex <= 0 || endIndex < dotIndex) {
			if (jsessionIndex > -1) {
				endIndex = jsessionIndex;
			} else {
				endIndex = inFileName.length();
			}
		} else {
			if (jsessionIndex >= 0 && jsessionIndex < endIndex) {
				endIndex = jsessionIndex;
			}
		}
		if (dotIndex >= 0) {
			outExt = inFileName.substring(dotIndex + 1, endIndex);
		}

		return outExt;
	}

	/**
	 * retreive the file extension.
	 * 
	 * @param inFileName
	 *            a file name
	 * @return a file name
	 */
	public static String getFileNameFromPath(String path) {
		if (path == null) {
			return null;
		}
		if (path.contains("?")) {
			path = path.substring(0, path.indexOf('?'));
		}
		String outExt = path;
		path = StringUtils.replace(path, "\\", "/");
		int slashIndex = path.lastIndexOf('/');
		if (slashIndex >= 0) {
			outExt = path.substring(slashIndex + 1, path.length());
		}
		return outExt;
	}

	public static String getLanguageFromFileName(String filename) {
		if (filename == null || !filename.contains("_")) {
			return null;
		} else {
			filename = getFileNameWithoutExtension(filename);
			String lg = filename.substring(filename.lastIndexOf("_") + 1);
			if (lg.length() != 2) {
				return null;
			} else {
				return lg;
			}
		}
	}

	public static String getDirNameFromPath(String path) {
		if (path.contains("?")) {
			path = path.substring(0, path.indexOf('?'));
		}
		String outExt = path;
		path = path.replace('\\', '/');
		int dotIndex = path.lastIndexOf('/');
		if (dotIndex >= 0) {
			outExt = path.substring(0, dotIndex);
		} else {
			outExt = "";
		}
		return outExt;
	}

	/**
	 * retreive the file extension.
	 * 
	 * @param inFileName
	 *            a file name
	 * @return a file extension without dot ( pdf, zip, ... )
	 */
	public static String getFileNameWithoutExtension(String inFileName) {
		String outExt = inFileName;
		int dotIndex = inFileName.lastIndexOf('.');
		if (dotIndex >= 0) {
			outExt = inFileName.substring(0, dotIndex);
		}
		return outExt;
	}

	/**
	 * return a formated size in a String.
	 * 
	 * @param filePath
	 *            a path to a file
	 * @return a string represent a size (sample : 45KB)
	 */
	public static final String getFileSize(String filePath) {
		long size = ResourceHelper.getFileSize(filePath);
		return renderSize(size);
	}

	public static String getFirstNotNull(String value1, String value2) {
		if (value1 != null) {
			return value1;
		} else {
			return value2;
		}
	}

	public static String getPropertieskey(String key) {
		key = key.replace(" ", "__BLK__");
		key = key.replace("=", "__EQL__");
		return key;
	}

	public synchronized static String getRandomId() {
		return getRandomIdBase10();
	}

	/**
	 * return a short id (length 10 chars).
	 * 
	 * @return
	 */
	public synchronized static String getShortRandomId() {
		long newId = Math.round(System.currentTimeMillis() / 10000);
		if (newId <= previousShortRandomId) {
			logger.fine("to mutch random is generated : " + previousRandomId);
			newId = previousShortRandomId + 1;
		}
		previousShortRandomId = newId;
		String shortBase10 = "" + newId + Math.round(Math.random() * 9);
		return shortBase10;
	}

	/**
	 * transform a string of number (length 10) to a structured communication,
	 * last number is the mod 97 of the first number
	 * 
	 * @param code
	 *            a string of length 10 with only number
	 * @return a string on length 12 with the the last number is the mod 97
	 */
	public static String encodeAsStructuredCommunicationMod97(String code) {
		if (code == null) {
			return null;
		}
		if (code.length() != 10) {
			throw new ParameterException("length of code must be 10.");
		} else {
			Long codeAsLong = Long.parseLong(code);
			Long mod = codeAsLong % 97;
			code = code.substring(0, 3) + '/' + code.substring(3, 7) + '/' + code.substring(7);
			if (mod < 10) {
				if (mod == 0) {
					return code + "97";
				} else {
					return code + '0' + mod;
				}
			} else {
				return code + mod;
			}
		}
	}

	/**
	 * generate a id in a String.
	 * 
	 * @return a unique id.
	 */
	public synchronized static String getRandomIdBase10() {
		long newId = System.currentTimeMillis();
		if (newId <= previousRandomId) {
			newId = previousRandomId + Math.round(Math.random() * 100) + 1;
		}
		previousRandomId = newId;
		String randomBase10 = "" + newId + Math.round(Math.random() * 89999999 + 10000000);
		return randomBase10;
	}

	/**
	 * generate a id in a String.
	 * 
	 * @return a unique id.
	 */
	public synchronized static String getRandomIdBase64() {
		long newId = System.currentTimeMillis();
		if (newId <= previousRandomId) {
			newId = previousRandomId + Math.round(Math.random() * 100) + 1;
		}
		previousRandomId = newId;
		long randomBase10 = newId * 10000000 + Math.round(Math.random() * 89999999 + 10000000);
		byte[] b = new byte[8];
		for (int i = 0; i < 8; i++) {
			b[7 - i] = (byte) (randomBase10 >>> (i * 8));
		}
		String base64code = Base64.encodeBase64String(b);
		base64code = base64code.replace('=', '-');
		base64code = base64code.replace('+', '_');
		base64code = base64code.replace('/', ',');
		return base64code;
	}

	public static String getRandomString(int lenght) {
		StringBuffer res = new StringBuffer();
		for (int i = 0; i < lenght; i++) {
			char newChar = BASIC_CHAR.charAt((int) Math.round(Math.random() * (BASIC_CHAR.length() - 1)));
			res.append(newChar);
		}
		return res.toString();
	}

	public static String getRandomString(int lenght, String acceptedChars) {
		StringBuffer res = new StringBuffer();
		for (int i = 0; i < lenght; i++) {
			char newChar = acceptedChars.charAt((int) Math.round(Math.random() * (acceptedChars.length() - 1)));
			res.append(newChar);
		}
		return res.toString();
	}

	public static String html2txt(String html) {
		return Jsoup.parse(html).text();
	}

	/**
	 * retreive the size of a String with html code. sample: "aaa&acute;" = 4.
	 * 
	 * @param str
	 *            the string
	 * @return the size of the String;
	 */
	public static int htmlSize(String str) {
		int c = 0;
		int cInCode = 0;
		boolean inHTMLCode = false;
		for (int i = 0; i < str.length(); i++) {
			if (!inHTMLCode) {
				if (str.charAt(i) == '&') {
					inHTMLCode = true;
					cInCode++;
				} else {
					c++;
				}
			} else {
				cInCode++;
				if (str.charAt(i) == ';') {
					inHTMLCode = false;
					c++;
					cInCode = 0;
				} else if (str.charAt(i) == '&') {
					c = c + cInCode;
					cInCode = 0;
				}
			}
		}
		c = c + cInCode;
		return c;
	}

	public static boolean isCharset(byte[] b, String inCharset) {
		CharsetDecoder cd = Charset.availableCharsets().get(inCharset).newDecoder();
		try {
			cd.decode(ByteBuffer.wrap(b));
		} catch (CharacterCodingException e) {
			return false;
		}
		return true;
	}

	public static boolean isDigit(char c) {
		switch (c) {
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
		case '-':
			return true;
		default:
			return false;
		}
	}

	/**
	 * check if string contains somthing similar to digital number
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isLikeNumber(String str) {
		str = str.replaceAll("€|\\$|\\.|\\,|\\%| ", "0");
		return isDigit(str);
	}

	/**
	 * check if stirng contains only digital number
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isDigit(String str) {
		if (str == null || str.length() == 0) {
			return false;
		} else {
			for (char c : str.toCharArray()) {
				if (!isDigit(c)) {
					return false;
				}
			}
			return true;
		}
	}

	public static boolean isFloat(String str) {
		int countSep = 0;
		if (str == null || str.length() == 0) {
			return false;
		} else {
			for (char c : str.toCharArray()) {
				if (!isDigit(c)) {
					if (c == '.') {
						countSep++;
					} else {
						return false;
					}
				}
			}
			return countSep < 2;
		}
	}

	/**
	 * return true if the filename in a html image).
	 * 
	 * @param fileName
	 *            file name with extension
	 * @return true if file name is a image
	 */
	public static final boolean isHTML(String fileName) {
		String ext = getFileExtension(fileName);
		boolean res = ext.equalsIgnoreCase("html");
		res = res || ext.equalsIgnoreCase("htm");
		res = res || ext.equalsIgnoreCase("php");
		res = res || ext.equalsIgnoreCase("jsp");
		res = res || ext.equalsIgnoreCase("asp");
		return res;
	}

	public static final boolean isProperties(String fileName) {
		String ext = getFileExtension(fileName);
		boolean res = ext.equalsIgnoreCase("properties");
		return res;
	}

	/**
	 * return true if the filename in a image for wcms (sp. : tif or psd in not
	 * a image).
	 * 
	 * @param fileName
	 *            file name with extension
	 * @return true if file name is a image
	 */
	public static final boolean isImage(String fileName) {
		if (fileName == null) {
			return false;
		}
		if (fileName.contains("?")) {
			fileName = fileName.substring(0, fileName.indexOf('?'));
		}
		String ext = getFileExtension(fileName);
		return isImageExtension(ext);
	}

	/**
	 * return true if the filename in a image for wcms (sp. : tif or psd in not
	 * a image).
	 * 
	 * @param fileName
	 *            file name with extension
	 * @return true if file name is a image
	 */
	public static final boolean isJpeg(String fileName) {
		if (fileName == null) {
			return false;
		}
		if (fileName.contains("?")) {
			fileName = fileName.substring(0, fileName.indexOf('?'));
		}
		String ext = getFileExtension(fileName);
		return isJpgExtension(ext);
	}

	/**
	 * return true if the filename in a image for wcms (sp. : tif or psd in not
	 * a image).
	 * 
	 * @param fileName
	 *            file name with extension
	 * @return true if file name is a image
	 */
	public static final boolean isExcelFile(String fileName) {
		if (fileName == null) {
			return false;
		}
		if (fileName.contains("?")) {
			fileName = fileName.substring(0, fileName.indexOf('?'));
		}
		String ext = getFileExtension(fileName).trim();
		return ext.equalsIgnoreCase("xls") || ext.equalsIgnoreCase("xlsx");
	}

	/**
	 * return true if the file extension is an image for wcms (sp. : tif or psd
	 * is not an image).
	 * 
	 * @param fileExtension
	 *            file extension
	 * @return true if file name is a image
	 * @see #getFileExtension(String)
	 */
	public static final boolean isImageExtension(String fileExtension) {
		boolean res = fileExtension.equalsIgnoreCase("jpg");
		res = res || fileExtension.equalsIgnoreCase("jpeg");
		res = res || fileExtension.equalsIgnoreCase("gif");
		res = res || fileExtension.equalsIgnoreCase("png");
		return res;
	}

	/**
	 * return true if the file extension is an image for wcms (sp. : tif or psd
	 * is not an image).
	 * 
	 * @param fileExtension
	 *            file extension
	 * @return true if file name is a image
	 * @see #getFileExtension(String)
	 */
	public static final boolean isJpgExtension(String fileExtension) {
		boolean res = fileExtension.equalsIgnoreCase("jpg");
		res = res || fileExtension.equalsIgnoreCase("jpeg");
		return res;
	}

	/**
	 * return true if the filename in a document (sp. : word, libreoffice...).
	 * 
	 * @param fileName
	 *            file name with extension
	 * @return true if file name is a image
	 */
	public static final boolean isDoc(String fileName) {
		if (fileName.contains("?")) {
			fileName = fileName.substring(0, fileName.indexOf('?'));
		}
		String ext = getFileExtension(fileName);
		boolean res = ext.equalsIgnoreCase("xls");
		res = res || ext.equalsIgnoreCase("doc");
		res = res || ext.equalsIgnoreCase("docx");
		res = res || ext.equalsIgnoreCase("ppt");
		res = res || ext.equalsIgnoreCase("odt");
		res = res || ext.equalsIgnoreCase("ppt");
		res = res || ext.equalsIgnoreCase("pps");
		res = res || ext.equalsIgnoreCase("ods");
		res = res || ext.equalsIgnoreCase("odp");
		return res;
	}

	/**
	 * return true if the filename in a PDF file.
	 * 
	 * @param fileName
	 *            file name with extension
	 * @return true if file name is a image
	 */
	public static final boolean isPDF(String fileName) {
		if (fileName.contains("?")) {
			fileName = fileName.substring(0, fileName.indexOf('?'));
		}
		String ext = getFileExtension(fileName);
		boolean res = ext.equalsIgnoreCase("pdf");
		return res;
	}

	public static boolean isMail(String email) {
		if (email == null || email.length() == 0) {
			return false;
		}
		return PatternHelper.MAIL_PATTERN.matcher(email).matches();
	}

	// TODO: create a better method
	public static boolean isMailURL(String url) {
		return url.trim().toLowerCase().startsWith("mailto");
	}

	public static final boolean isPasswordMath(String pwdData, String userInput, boolean crypted) {
		if (pwdData == null || userInput == null) {
			return false;
		}
		if (!crypted) {
			return pwdData.equals(userInput);
		} else {
			return pwdData.equals(encryptPassword(userInput));
		}

	}

	public static boolean isPhoneNumber(String phone) {
		return PatternHelper.PHONE_PATTERN.matcher(phone).matches();
	}

	/**
	 * return true if the filename in a sound file image.
	 * 
	 * @param fileName
	 *            file name with extension
	 * @return true if file name is a sound
	 */
	public static final boolean isSound(String fileName) {
		String ext = getFileExtension(fileName);
		boolean res = ext.equalsIgnoreCase("mp3");
		res = res || ext.equalsIgnoreCase("wav");
		res = res || ext.equalsIgnoreCase("ogg");
		res = res || ext.equalsIgnoreCase("midi");
		res = res || ext.equalsIgnoreCase("flac");
		return res;
	}

	public static boolean isTrue(Object inBool) {
		return isTrue(inBool, false);
	}

	public static boolean isTrue(Object inBool, boolean defaultValue) {
		if (inBool == null) {
			return defaultValue;
		}
		if (inBool instanceof Boolean) {
			return (Boolean) inBool;
		}
		String bool = "" + inBool;
		boolean res = defaultValue;

		bool = bool.trim();
		if (bool.equalsIgnoreCase("true")) {
			res = true;
		} else if (bool.equalsIgnoreCase("on")) {
			res = true;
		} else if (bool.equalsIgnoreCase("yes")) {
			res = true;
		} else if (bool.equalsIgnoreCase("jes")) {
			res = true;
		} else if (bool.equalsIgnoreCase("oui")) {
			res = true;
		} else if (bool.equalsIgnoreCase("ya")) {
			res = true;
		} else if (bool.equalsIgnoreCase("selected")) {
			res = true;
		} else if (bool.equalsIgnoreCase("1")) {
			res = true;
		}

		return res;
	}

	public static boolean isTrue(String bool, boolean defaultValue) {
		if (bool == null) {
			return defaultValue;
		} else {
			return isTrue(bool);
		}
	}

	// TODO: create a better method
	public static boolean isURL(String url) {
		if (url == null) {
			return false;
		}
		return url.contains("://");
	}

	/**
	 * return true if the filename in a url file).
	 * 
	 * @param fileName
	 *            file name with extension
	 * @return true if file name is a url file
	 */
	public static final boolean isURLFile(String fileName) {
		String ext = getFileExtension(fileName);
		boolean res = ext.equalsIgnoreCase("url");
		return res;
	}

	/**
	 * return true if the filename in a video file image).
	 * 
	 * @param fileName
	 *            file name with extension
	 * @return true if file name is a video
	 */
	public static final boolean isVideo(String fileName) {
		String ext = getFileExtension(fileName);
		boolean res = ext.equalsIgnoreCase("wmv");
		res = res || ext.equalsIgnoreCase("avi");
		res = res || ext.equalsIgnoreCase("mov");
		res = res || ext.equalsIgnoreCase("mpg");
		res = res || ext.equalsIgnoreCase("mpeg");
		res = res || ext.equalsIgnoreCase("ru");
		res = res || ext.equalsIgnoreCase("mp4");
		res = res || ext.equalsIgnoreCase("flv");
		return res;
	}

	public static void main(String[] args) {
		StringBuilder outCleanData = new StringBuilder();
		outCleanData.append("patrick");
		System.out.println("outCleanData=" + outCleanData);
	}

	/**
	 * Test if a string match with a pattern using stars (*). <br/>
	 * assert matchStarPattern("eeA", "ee*"); <br/>
	 * assert !matchStarPattern("eeA", "ee"); <br/>
	 * assert matchStarPattern("Aee", "*ee"); <br/>
	 * assert !matchStarPattern("Aee", "ee"); <br/>
	 * assert matchStarPattern("eAe", "e*e"); <br/>
	 * assert !matchStarPattern("eAe", "ee"); <br/>
	 * assert matchStarPattern("ee", "ee");
	 * 
	 * @param str
	 * @param pattern
	 * @return
	 */
	public static boolean matchStarPattern(String str, String pattern) {
		String[] parts = pattern.split("\\*+");
		int currentPos = 0;
		boolean first = true;
		for (String part : parts) {
			int pos = str.indexOf(part, currentPos);
			if (pos < 0 || (first && part.length() > 0 && pos > 0)) {
				return false;
			}
			currentPos = pos + part.length();
			first = false;
		}
		return currentPos == str.length() || pattern.endsWith("*");
	}

	/**
	 * transform a string empty to a other string
	 * 
	 * @param inStr
	 *            a string can be null
	 * @param replaceWith
	 *            replace with this if null.
	 * @return never null ( empty string if input is null)
	 */
	public static String neverEmpty(String inStr, String replaceWith) {
		if ((inStr == null) || (inStr.trim().length() == 0)) {
			return replaceWith;
		} else {
			return inStr;
		}
	}

	/**
	 * transform a string null in a empty String.
	 * 
	 * @param inStr
	 *            a string can be null
	 * @return never null ( empty string if input is null)
	 */
	public static String neverNull(Object inStr) {
		return neverNull(inStr, "");
	}

	/**
	 * transform a string null in a empty String.
	 * 
	 * @param inStr
	 *            a string can be null
	 * @param replaceWith
	 *            replace with this if null.
	 * @return never null ( empty string if input is null)
	 */
	public static String neverNull(Object inStr, String replaceWith) {
		if (inStr == null) {
			return replaceWith;
		} else {
			return "" + inStr;
		}
	}

	public static Date parseDate(String inDate) throws ParseException {
		return parseDate(inDate, "dd/MM/yyyy");
	}
	
	/**
	 * null save parse int.
	 * @param value a string with a int inside
	 * @param defaultValue the value if string value is null
	 * @return
	 */
	public static int parseInt(String value, int defaultValue) {
		if (value == null) {
			return defaultValue;
		} else {
			return Integer.parseInt(value);
		}
	}

	public static Date[] parseRangeDate(String date) throws ParseException {
		Date[] outDate;
		if (date.contains("-")) {
			int i = date.indexOf("-");
			String startDate = date.substring(0, i);
			String endDate = date.substring(i + 1);
			outDate = new Date[2];
			outDate[0] = StringHelper.parseDate(startDate);
			outDate[1] = StringHelper.parseDate(endDate);
		} else {
			outDate = new Date[1];
			outDate[0] = StringHelper.parseDate(date);
		}
		return outDate;
	}

	public static Date parseDate(String inDate, char sep) throws ParseException {
		Date outDate = null;
		try {
			outDate = StringHelper.parseDate(inDate, "dd" + sep + "MM" + sep + "yyyy");
		} catch (ParseException e) {
			try {
				outDate = StringHelper.parseDate(inDate, "dd" + sep + "MM" + sep + "yy");
			} catch (ParseException e1) {
			}
		}
		return outDate;
	}

	public static Date parseDateTime(String inDate, char sep) throws ParseException {
		Date outDate = null;
		try {
			outDate = StringHelper.parseDate(inDate, "dd" + sep + "MM" + sep + "yyyy HH:mm:ss");
		} catch (ParseException e) {
			try {
				outDate = StringHelper.parseDate(inDate, "dd" + sep + "MM" + sep + "yy HH:mm:ss");
			} catch (ParseException e1) {
			}
		}
		return outDate;
	}

	public static Date smartParseDate(String inDate) {
		if (inDate == null) {
			return null;
		}
		if (inDate.length() > "yyyy-MM-dd".length()) {
			return smartParseDateTime(inDate);
		}
		Date outDate = null;
		inDate = inDate.trim();
		if (inDate.length() == 0) {
			return null;
		} else {
			if (inDate.contains("/")) {
				try {
					outDate = parseDate(inDate, '/');
				} catch (ParseException e) {
				}
			} else if (inDate.contains(":")) {
				try {
					outDate = parseDate(inDate, ':');
				} catch (ParseException e) {
				}
			} else if (inDate.contains("-")) {
				try {
					outDate = parseDate(inDate, '-');
				} catch (ParseException e) {
				}
			} else if (inDate.contains(" ")) {
				try {
					outDate = parseDate(inDate, ' ');
				} catch (ParseException e) {
				}
			}
		}
		if (outDate == null) {
			try {
				outDate = parseDate(inDate, "yyyy-MM-dd");
			} catch (ParseException e) {
			}
		}
		return outDate;
	}

	public static Date smartParseDateTime(String inDate) {
		if (inDate == null) {
			return null;
		}
		if (inDate.length() <= "yyyy-MM-dd".length()) {
			return smartParseDate(inDate);
		}
		Date outDate = null;
		inDate = inDate.trim();
		if (inDate.length() == 0) {
			return null;
		} else {
			if (inDate.contains("/")) {
				try {
					outDate = parseDateTime(inDate, '/');
				} catch (ParseException e) {
				}
			} else if (inDate.contains(":")) {
				try {
					outDate = parseDateTime(inDate, ':');
				} catch (ParseException e) {
				}
			} else if (inDate.contains("-")) {
				try {
					outDate = parseDateTime(inDate, '-');
				} catch (ParseException e) {
				}
			} else if (inDate.contains(" ")) {
				try {
					outDate = parseDateTime(inDate, ' ');
				} catch (ParseException e) {
				}
			}
		}
		if (outDate == null) {
			try {
				outDate = parseDate(inDate, "yyyy-MM-dd");
			} catch (ParseException e) {
			}
		}
		return outDate;
	}

	public static Date parseDate(String inDate, String pattern) throws ParseException {
		if (inDate == null) {
			return null;
		}
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		return format.parse(inDate);
	}

	public static Date parseDateOrTime(String inDate) throws ParseException {
		if (inDate == null || inDate.trim().length() == 0) {
			return null;
		}
		Date outDate = null;
		try {
			outDate = StringHelper.parseDate(inDate, "dd/MM/yyyy HH:mm:ss");
		} catch (ParseException e1) {
			try {
				outDate = StringHelper.parseDate(inDate, "dd/MM/yyyy HH:mm");
			} catch (ParseException e2) {
				outDate = StringHelper.parseDate(inDate);
			}
		}
		return outDate;
	}

	public static Date parseFileTime(String fileTime) throws ParseException {
		if (fileTime == null) {
			return null;
		}
		return parseDate(fileTime, "yyyy-MM-dd_HH-mm-ss-SSS");
	}

	public static Date parseSecondFileTime(String fileTime) throws ParseException {
		if (fileTime == null) {
			return null;
		}
		return parseDate(fileTime, "yyyy-MM-dd_HH-mm-ss");
	}

	public static Date parseSortableTime(String date) throws ParseException {
		if (date == null) {
			return null;
		}
		return parseDate(date, "yyyy-MM-dd HH:mm:ss");
	}

	public static Date parseSortableDate(String date) throws ParseException {
		if (date == null) {
			return null;
		}
		return parseDate(date, "yyyy-MM-dd");
	}

	/*
	 * public static String encodeBase64ToURLParam(String base64Code) { return
	 * base64Code.replace('+', '_').replace('/', '-').replace('=','*'); }
	 * 
	 * public static String decodeURLParamToBase64(String urlParam) { return
	 * urlParam.replace('_', '+').replace('-', '/').replace('*','='); }
	 */

	public static Date parseTime(String inDate) throws ParseException {
		return parseDate(inDate, "dd/MM/yyyy HH:mm:ss");
	}

	public static Date parseTimeOnly(String inDate) throws ParseException {
		return parseDate(inDate, "HH:mm:ss");
	}

	public static String[] readLines(String text) {
		StringReader reader = new StringReader(text);
		BufferedReader bufReader = new BufferedReader(reader);
		Collection<String> tmpOut = new LinkedList<String>();
		String line;
		try {
			line = bufReader.readLine();
			while (line != null) {
				tmpOut.add(line);
				line = bufReader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] out = new String[tmpOut.size()];
		tmpOut.toArray(out);
		return out;
	}

	public static String removeAccents(String textWithAccent) {
		String normalizeFileName = Normalizer.normalize(textWithAccent, Normalizer.Form.NFD);
		return normalizeFileName.replaceAll("[^\\p{ASCII}]", "");
	}

	/**
	 * remove CR from a String
	 * 
	 * @param text
	 *            a simple String
	 * @return a string without CR.
	 */
	public static String removeCR(String text) {
		StringReader reader = new StringReader(text);
		BufferedReader bReader = new BufferedReader(reader);
		StringBuffer res = new StringBuffer();
		try {
			String line = bReader.readLine();
			while (line != null) {
				res.append(line.trim());
				line = bReader.readLine();
			}
		} catch (IOException e) {
			// impossible
		}
		return res.toString();
	}

	public static final String removeFirstChar(String txt, char c) {
		if (txt == null) {
			return null;
		}
		int count = 0;
		int i = 0;
		while ((i < txt.length()) && (txt.charAt(i) == c)) {
			count++;
			i++;
		}
		if (count == txt.length()) {
			return "";
		} else {
			return txt.substring(count);
		}
	}

	public static String removeRepeatedChar(String str) {
		StringBuffer out = new StringBuffer();
		Set<Character> charFounded = new HashSet<Character>();
		for (int i = 0; i < str.length(); i++) {
			Character character = str.charAt(i);
			if (!charFounded.contains(character)) {
				out.append(character);
				charFounded.add(character);
			}
		}
		return out.toString();
	}

	/**
	 * remove repeated char inside string. sample : "javlo--love" "-" =
	 * "javlo-love".
	 * 
	 * @param str
	 * @param c
	 * @return
	 */
	public static String removeRepeatedChar(String str, char c) {
		if (str == null) {
			return null;
		}
		String doubleString = c + "" + c;
		while (str.contains(doubleString)) {
			str = str.replace(doubleString, "" + c);
		}
		return str;
	}

	/**
	 * remove sequence from a string. sample : removeSequence
	 * ("slkqfj #dlskj# sdljf", "#", "#") -> slkqfj sdljf.
	 * 
	 * @param text
	 *            a simple text
	 * @param prefix
	 *            the prefix for identify the sequence
	 * @param sufix
	 *            the sufix for identify the sequence
	 * @return
	 */
	public static final String removeSequence(String text, String prefix, String sufix) {
		StringRemplacementHelper remp = new StringRemplacementHelper();
		int pos = text.indexOf(prefix);
		while (pos >= 0) {
			if (pos < text.length()) {
				int endPos = text.indexOf(sufix, pos + 1);
				if (endPos < 0) {
					pos = -1;
				} else {
					remp.addReplacement(pos, endPos + sufix.length(), "");
					pos = text.indexOf(prefix, endPos);
				}
			} else {
				pos = -1;
			}
		}
		return remp.start(text);
	}

	/**
	 * remove tag. sample: <a href="#">link</a> -> link
	 * 
	 * @param text
	 *            XHTML Code
	 * @return simple text
	 */
	public static String removeTag(String text) {
		if (text == null) {
			return "";
		}
		StringBuffer notTagStr = new StringBuffer();
		boolean inTag = false;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if ((!inTag) && (c == '<')) {
				inTag = true;
			} else if (inTag && (c == '>')) {
				inTag = false;
			} else if (!inTag) {
				notTagStr.append(c);
			}
		}
		return notTagStr.toString();
	}

	public static String renderShortTime(ContentContext ctx, Date date) throws FileNotFoundException, IOException {
		String outDate = renderShortDate(ctx, date);
		return outDate + ' ' + renderDate(date, "HH:mm");
	}

	/**
	 * render a date, search the format in I18n files.
	 * 
	 * @param session
	 * @param locale
	 * @param date
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static String renderShortDate(ContentContext ctx, Date date) throws FileNotFoundException, IOException {
		if (date == null) {
			return "";
		}

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		String dateFormatString = null;
		Locale locale;
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (ctx.getRenderMode() == ContentContext.EDIT_MODE) {
			dateFormatString = i18nAccess.getText("date.short", (String) null);
			if (dateFormatString == null && globalContext.getShortDateFormat().trim().length() > 0) {
				dateFormatString = globalContext.getShortDateFormat();
			}
			locale = new Locale(globalContext.getEditLanguage(ctx.getRequest().getSession()));
		} else {
			dateFormatString = i18nAccess.getContentViewText("date.short", (String) null);
			if (dateFormatString == null && globalContext.getShortDateFormat().trim().length() > 0) {
				dateFormatString = globalContext.getShortDateFormat();
			}
			locale = new Locale(ctx.getRequestContentLanguage());
		}

		DateFormat dateFormat;
		if (dateFormatString != null) {
			try {
				dateFormat = new SimpleDateFormat(dateFormatString, locale);
				return dateFormat.format(date);
			} catch (Throwable t) {
				logger.warning(t.getMessage() + "   (context:" + globalContext.getContextKey() + ')');
				t.printStackTrace();
			}
		}

		dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		return dateFormat.format(date);
	}

	public static String renderDate(Date date) {
		if (date == null) {
			return null;
		}
		return renderDate(date, "dd/MM/yyyy");
	}

	public static String renderDate(Date date, String pattern) {
		if (date == null) {
			return null;
		}
		if (pattern == null) {
			return renderDate(date);
		}
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		return format.format(date);

	}

	public static String renderSortableDate(Date date) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(date);

	}

	public static String renderDate(Locale locale, Date date, String pattern) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat format = new SimpleDateFormat(pattern, locale);
		return format.format(date);

	}

	public static String renderDateAsRFC822String(Date date) {
		if (date == null) {
			return null;
		}
		return RFC822DATEFORMAT.format(date);
	}

	public static String renderDateWithDefaultValue(Date date, String defaultValue) {
		if (date == null) {
			return defaultValue;
		}
		return renderDate(date, "dd/MM/yyyy");
	}

	public static final String renderTimeInSecond(long time) {
		long sec = time / 1000;
		long dec = (time - sec * 1000) / 100;
		return "" + sec + '.' + dec;
	}

	public static String renderDouble(double value, int precision) {
		return renderDouble(value, precision, ',');
	}

	public static String renderDouble(double value, Locale locale) {
		NumberFormat f = NumberFormat.getInstance(locale);
		return f.format(value);
	}

	public static String renderDouble(double value, int precision, char sep) {
		long deca = Math.round(Math.pow(10, precision));
		long intValue = Math.round(value * deca);
		String decimal = "" + intValue;
		decimal = decimal.substring(decimal.length() - precision, decimal.length());
		while ((decimal.length() > 0) && (decimal.charAt(decimal.length() - 1) == '0')) {
			decimal = decimal.substring(0, decimal.length() - 1);
		}
		if (decimal.length() > 0) {
			decimal = sep + decimal;
		}
		return Math.round(intValue / deca) + decimal;
	}

	public static String renderDoubleAsPercentage(double value) {
		long deca = Math.round(Math.pow(10, 2));
		long intValue = Math.round(value * deca);
		return "" + Math.round(intValue) + " %";
	}

	public static String renderFileTime(Date date) {
		if (date == null) {
			return null;
		}
		return renderDate(date, "yyyy-MM-dd_HH-mm-ss-SSS");
	}

	public static String renderLightDate(Date date) {
		if (date == null) {
			return null;
		}
		return renderDate(date, "dd/MM/yy");
	}

	public static String renderMediumDate(ContentContext ctx, Date date) {

		if (date == null) {
			return "";
		}

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		DateFormat dateFormat;
		String manualDateFormat = globalContext.getMediumDateFormat();
		if (manualDateFormat != null && manualDateFormat.trim().length() > 0) {
			dateFormat = new SimpleDateFormat(manualDateFormat);
		} else {
			dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, new Locale(ctx.getContextRequestLanguage()));
		}

		return dateFormat.format(date);
	}

	public static String renderFullDate(ContentContext ctx, Date date) {

		if (date == null) {
			return "";
		}

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		DateFormat dateFormat;
		String manualDateFormat = globalContext.getFullDateFormat();
		if (manualDateFormat != null && manualDateFormat.trim().length() > 0) {
			dateFormat = new SimpleDateFormat(manualDateFormat);
		} else {
			dateFormat = DateFormat.getDateInstance(DateFormat.FULL, new Locale(ctx.getContextRequestLanguage()));
		}

		return dateFormat.format(date);
	}

	public static String renderNumber(int n, int size) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(size);
		nf.setGroupingUsed(false);
		return nf.format(n);
	}

	public static String renderOnlyTime(Date date) {
		if (date == null) {
			return null;
		}
		return renderDate(date, "HH:mm:ss");
	}

	public static String renderSecondFileTime(Date date) {
		if (date == null) {
			return null;
		}
		return renderDate(date, "yyyy-MM-dd_HH-mm-ss");
	}

	public static String renderShortDateWidthDay(ContentContext ctx, Date date) {

		if (date == null) {
			return "";
		}

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		DateFormat dateFormat;
		String manualDateFormat = globalContext.getShortDateFormat();
		if (manualDateFormat != null && manualDateFormat.trim().length() > 0) {
			dateFormat = new SimpleDateFormat(manualDateFormat);
		} else {
			dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, new Locale(ctx.getContextRequestLanguage()));
		}

		DateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale(ctx.getContextRequestLanguage()));

		return dayFormat.format(date) + ' ' + dateFormat.format(date);
	}

	/**
	 * return a formated size in a String.
	 * 
	 * @param filePath
	 *            a path to a file
	 * @return a string represent a size (sample : 45KB)
	 */
	public static final String renderSize(long size) {
		if (size > 1024 * 1024 * 10) {
			return size / (1024 * 1024) + "MB";
		} else if (size > 1024 * 10) {
			return size / 1024 + "KB";
		} else {
			return size + "B";
		}
	}

	public static String renderSortableTime(Date date) {
		if (date == null) {
			return null;
		}
		return renderDate(date, "yyyy-MM-dd HH:mm:ss");
	}

	public static String renderTime(ContentContext ctx, Date date) throws FileNotFoundException, IOException {
		return renderShortDate(ctx, date) + ' ' + renderDate(date, "HH:mm");
	}

	public static String renderTime(Date date) {
		if (date == null) {
			return null;
		}
		return renderDate(date, "dd/MM/yyyy HH:mm:ss");
	}

	public static String renderTimeOnly(Date date) {
		if (date == null) {
			return null;
		}
		return renderDate(date, "HH:mm:ss");
	}

	/**
	 * render a date, search the format in I18n files.
	 * 
	 * @param session
	 * @param locale
	 * @param date
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static String renderUserFriendlyDate(ContentContext ctx, Date date) throws FileNotFoundException, IOException {
		if (date == null) {
			return null;
		}

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		String dateFormatString = null;
		Locale locale;
		if (ctx.getRenderMode() == ContentContext.EDIT_MODE) {
			dateFormatString = i18nAccess.getText("date.full", (String) null);
			locale = new Locale(globalContext.getEditLanguage(ctx.getRequest().getSession()));
		} else {
			dateFormatString = i18nAccess.getContentViewText("date.full", (String) null);
			locale = new Locale(ctx.getRequestContentLanguage());
		}

		DateFormat dateFormat;
		if (dateFormatString != null) {
			try {
				dateFormat = new SimpleDateFormat(dateFormatString, locale);
				return dateFormat.format(date);
			} catch (Throwable t) {
				logger.warning("error with date format : " + dateFormatString);
				logger.warning(t.getMessage());
			}
		}

		String manualDateFormat = globalContext.getFullDateFormat();
		if (manualDateFormat == null || manualDateFormat.trim().length() == 0) {
			if (locale.getLanguage().equals("el")) { // in Greek the full
				// rendering of date is
				// not correct.
				dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
			} else {
				dateFormat = DateFormat.getDateInstance(DateFormat.FULL, locale);
			}
		} else {
			dateFormat = new SimpleDateFormat(manualDateFormat);
		}
		return dateFormat.format(date);
	}

	public static String renderUserFriendlyDate(Date date) {
		if (date == null) {
			return null;
		}
		return renderDate(date, "EEEEEEE, d MMMMMMMM yyyy");
	}

	public static String renderUserFriendlyDate(Locale locale, Date date) {
		if (date == null) {
			return null;
		}
		DateFormat dateFormat;
		if (locale.getLanguage().equals("el")) { // in Greek the full rendering
			// of date is not correct.
			dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
		} else {
			dateFormat = DateFormat.getDateInstance(DateFormat.FULL, locale);
		}
		return dateFormat.format(date);
	}

	public static String renderUserFriendlyTime(Date date) {
		if (date == null) {
			return null;
		}
		return renderDate(date, "EEEEEEE, d MMMMMMMM yyyy HH:mm:ss");
	}

	public static Long safeParseLong(String string, Long defaultValue) {
		try {
			return Long.parseLong(string);
		} catch (Throwable e) {
			return defaultValue;
		}
	}

	public static Double safeParseDouble(String string, Double defaultValue) {
		try {
			return Double.parseDouble(string);
		} catch (Throwable e) {
			return defaultValue;
		}
	}

	public static Integer safeParseInt(String string, Integer defaultValue) {
		try {
			return Integer.parseInt(string);
		} catch (Throwable e) {
			return defaultValue;
		}
	}

	/**
	 * extract email from a free text
	 * 
	 * @param text
	 *            a free text
	 * @return a collection of email address
	 */
	public static Collection<InternetAddress> searchStructuredEmail(String text) {
		Collection<InternetAddress> outEmails = new LinkedList<InternetAddress>();
		BufferedReader reader = new BufferedReader(new StringReader(text));
		try {
			String line = reader.readLine();
			while (line != null) {
				String[] mailCandidate = line.split(",|;|:");
				for (String element : mailCandidate) {
					try {
						InternetAddress email = new InternetAddress(element.trim());
						outEmails.add(email);
					} catch (AddressException e) {
					}
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return outEmails;
	}

	/**
	 * extract email from a free text
	 * 
	 * @param text
	 *            a free text
	 * @return a collection of email address
	 */
	public static Collection<String> searchEmail(String text) {
		Collection<String> outEmails = new LinkedList<String>();

		BufferedReader reader = new BufferedReader(new StringReader(text));

		try {
			String line = reader.readLine();
			while (line != null) {
				String[] mailCandidate = line.split(" |\\t|,|\"|;|:|\\(|\\)|\\[|\\]|<|>");
				for (String element : mailCandidate) {
					if (PatternHelper.MAIL_PATTERN.matcher(element).matches()) {
						outEmails.add(element);
					}
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return outEmails;
	}

	/**
	 * extract external link from a free text
	 * 
	 * @param text
	 *            a free text, can be html
	 * @return a collection of URL
	 */
	public static List<URL> searchLinks(String text) {
		List<URL> outLinks = new LinkedList<URL>();
		BufferedReader reader = new BufferedReader(new StringReader(text));
		try {
			String line = reader.readLine();
			while (line != null) {
				String[] linksCandidate = line.split(" |\\t|\\||\"|;|<|>");
				for (String element : linksCandidate) {
					if (PatternHelper.EXTERNAL_LINK_PATTERN.matcher(element).matches()) {
						try {
							outLinks.add(new URL(element));
						} catch (MalformedURLException e) {
						}
					}
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return outLinks;
	}

	/**
	 * split a String, if there are no character between two token this method
	 * place a empty String ( != String.split )
	 * 
	 * @param str
	 *            a standard str
	 * @param token
	 *            a token ( not pattern !!! )
	 * @return a array of String without the separator
	 */
	public static String[] split(String str, String token) {
		int i = str.indexOf(token);
		int start = 0;
		ArrayList<String> arrayList = new ArrayList<String>();
		while (i >= 0) {
			arrayList.add(str.substring(start, i));
			start = i + 1;
			i = str.indexOf(token, start);
		}
		arrayList.add(str.substring(start, str.length()));
		String[] res = new String[arrayList.size()];
		arrayList.toArray(res);
		return res;
	}

	/**
	 * split a String, if there are no character between two token this method
	 * place a empty String ( != String.split )
	 * 
	 * @param str
	 *            a standard str
	 * @param token
	 *            a token ( not pattern !!! )
	 * @return a array of String with the separator
	 */
	public static String[] splitStaySeparator(String str, char token) {
		int i = str.indexOf(token);
		int start = 0;
		ArrayList<String> arrayList = new ArrayList<String>();

		while (i >= 0) {
			arrayList.add(str.substring(start, i));
			start = i + 1;
			arrayList.add(str.substring(i, i + 1));
			i = str.indexOf(token, start);
		}

		arrayList.add(str.substring(start, str.length()));
		String[] res = new String[arrayList.size()];
		arrayList.toArray(res);
		return res;
	}

	/**
	 * split a String, if there are no character between two token this method
	 * place a empty String ( != String.split )
	 * 
	 * @param str
	 *            a standard str
	 * @param token
	 *            a token ( not pattern !!! )
	 * @return a array of String with the separator
	 */
	public static String[] splitStaySeparator(String str, String token) {
		if ((token == null) || (token.length() == 0)) {
			return new String[] { str };
		}

		ArrayList<String> arrayList = new ArrayList<String>();
		arrayList.add(str);

		char[] sep = token.toCharArray();
		for (char c : sep) {
			ArrayList<String> newArrayList = new ArrayList<String>();
			for (String string : arrayList) {
				String[] splitedString = splitStaySeparator(string, c);
				for (String string2 : splitedString) {
					newArrayList.add(string2);
				}
			}
			arrayList = newArrayList;
		}

		String[] res = new String[arrayList.size()];
		arrayList.toArray(res);
		return res;
	}

	public static int stringInArray(String[] array, String token) {
		int res = -1;
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(token)) {
				res = i;
			}
		}
		return res;
	}

	public static String[] stringToArray(String str) {
		if (str == null) {
			return null;
		}
		return str.split("\\?");
	}

	public static String[] stringToArray(String str, String token) {
		if (str == null) {
			return null;
		}
		return str.split(token);
	}

	public static String[] stringToArrayRemoveEmpty(String str) {
		Collection<String> outCol = new LinkedList<String>();
		String[] array = stringToArray(str);
		for (String cell : array) {
			if (cell != null && cell.trim().length() > 0) {
				outCol.add(cell);
			}
		}
		String[] outArray = new String[outCol.size()];
		outCol.toArray(outArray);
		return outArray;
	}

	public static List<String> stringToCollection(String str) {
		if (str == null) {
			return null;
		}
		return stringToCollection(str, DEFAULT_LIST_SEPARATOR);
	}

	public static List<String> stringToCollectionTrim(String str) {
		if (str == null) {
			return null;
		}
		return stringToCollectionTrim(str, DEFAULT_LIST_SEPARATOR);
	}

	public static List<String> stringToCollectionTrim(String str, String token) {
		List<String> listTrim = new LinkedList<String>();
		for (String item : splitAsList(str, token, DEFAULT_ESCAPE, false)) {
			listTrim.add(item.trim());
		}
		return listTrim;
	}

	public static List<String> stringToCollection(String str, String token) {
		return splitAsList(str, token, DEFAULT_ESCAPE, false);
	}
	
	public static List<String> stringToCollection(String str, String token, boolean trim) {
		return splitAsList(str, token, DEFAULT_ESCAPE, trim);
	}

	public static String stringToFileName(String inStr) {
		String outStr = inStr.replaceAll("[^a-zA-Z0-9_-]", "_");
		return outStr;
	}

	public static String stringWithoutSpecialChar(String inStr) {
		String outStr = 'a' + inStr.replaceAll("[^a-zA-Z0-9_]", "_");
		return outStr;
	}

	public static List<String> textToList(String text) {
		List<String> outText = new LinkedList<String>();
		BufferedReader read = new BufferedReader(new StringReader(text));
		String rawLine;
		try {
			rawLine = read.readLine();
			while (rawLine != null) {
				outText.add(rawLine);
				rawLine = read.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outText;
	}

	public static String textToList(GlobalContext globalContext, String text, String sep, String layout, boolean autoLink) {
		return textToList(globalContext, text, sep, layout, autoLink, null);
	}

	/**
	 * @param sep
	 *            separation between title and text. sample "1.2 great section"
	 *            sep=" " -> "1.2" is title and "great section" is text.
	 * @param layout
	 *            the layout of the list : ("ul-ul", "ol-ol", "ul-ol", "ol-ul")
	 *            the first element is the first level and the next is all the
	 *            next level.
	 * @return a xhtml list.
	 */
	public static String textToList(GlobalContext globalContext, String text, String sep, String layout, boolean autoLink, String ulClass) {

		String firstTag = "ul";
		String secondTag = "ul";
		if (layout != null) {
			if (layout.equals("ol-ol")) {
				firstTag = "ol";
				secondTag = "ol";
			} else if (layout.equals("ul-ol")) {
				firstTag = "ul";
				secondTag = "ol";
			} else if (layout.equals("ol-ul")) {
				firstTag = "ol";
				secondTag = "ul";
			}
		}

		BufferedReader read = new BufferedReader(new StringReader(text));
		StringWriter out = new StringWriter();

		try {
			BufferedWriter writer = new BufferedWriter(out);

			String rawLine = read.readLine();
			if (rawLine == null) {
				return "";
			}
			String line = removeFirstChar(rawLine, FreeTextList.SUBLIST_CHAR);
			int depth = rawLine.length() - line.length();

			if (ulClass == null) {
				writer.write("<" + firstTag + ">");
			} else {
				writer.write("<" + firstTag + " class=\"" + ulClass + "\">");
			}
			boolean firstPass = true;
			int cd = 0;
			String cssClass = " class=\"first\"";
			while (rawLine != null) {
				if (depth == cd) {
					if (!firstPass) {
						writer.write("</li>");
						writer.newLine();
					}
				}
				while (depth > cd) {
					writer.newLine();
					writer.write("<" + secondTag + ">");
					cd++;
				}
				while (depth < cd) {
					writer.write("</li>");
					writer.write("</" + secondTag + ">");
					writer.newLine();
					writer.write("</li>");
					writer.newLine();
					cd--;
				}

				cd = depth;
				firstPass = false;

				String title = "";
				String content = line;
				if ((sep != null) && sep.length() > 0) {
					int sepIndex = line.indexOf(sep);
					if (sepIndex >= 0) {
						title = line.substring(0, sepIndex + 1);
						content = line.substring(sepIndex + 1);
					}
				} else {
					content = line;
				}

				String nextLine = read.readLine();
				if (nextLine == null) {
					cssClass = " class=\"last\"";
				}
				writer.write("<li" + cssClass + ">");
				cssClass = "";
				if (title.trim().length() > 0) {
					writer.write("<strong class=\"title\">");
					writer.write(title);
					writer.write("</strong>");
				}
				writer.write("<span class=\"text\">");
				if (autoLink) {
					writer.write(XHTMLHelper.autoLink(content, globalContext));
				} else {
					writer.write(content);
				}
				writer.write("</span>");

				rawLine = nextLine;
				if (rawLine != null) {
					line = removeFirstChar(rawLine, FreeTextList.SUBLIST_CHAR);
					depth = rawLine.length() - line.length();
				}

			}
			writer.write("</li></" + firstTag + ">");
			writer.close();
		} catch (IOException e) {
			// impossible (becauce reader from string)
			e.printStackTrace();
		}

		return out.toString();
	}

	/**
	 * transform a String to insert in a JavaScript.
	 * 
	 * @param inStr
	 *            a Simple String
	 * @return a String with replacement for insert into a javascript.
	 */
	public static String toJSString(String inStr) {
		ByteArrayInputStream in = new ByteArrayInputStream(inStr.getBytes());
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String outStr = "";
		try {
			String line = br.readLine();
			while (line != null) {
				outStr = outStr + line;
				line = br.readLine();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return outStr.replaceAll("'", "\\\\'").replaceAll("\"", "&quot;");
	}

	/**
	 * transform a String to a max size. sample: "Welcome in the new wave party"
	 * --> "Welcome in the..."
	 * 
	 * @param str
	 *            the string must be cut.
	 * @param size
	 *            the max size of the final string.
	 * @param sufix
	 *            the sufix of new String if cutted (sp. ... ).
	 * @return
	 */
	public static String toMaxSize(String str, int size, String sufix) {
		String res = str;
		if (res != null) {
			if (res.length() > size) {
				res = res.substring(0, size - (htmlSize(sufix) + 1)).trim() + sufix;
			}
		}
		return res;
	}

	/**
	 * transform a free value to a attribute xml value
	 * 
	 * @param value
	 * @return
	 */
	public static String toXMLAttribute(String value) {
		if (value == null) {
			return "";
		}
		value = value.replace("&", "&amp;");
		return Encode.forXmlAttribute(XHTMLHelper.escapeXML(removeTag(value).replace("\"", "&quot;").replace("\n", "")));
	}

	/**
	 * transform a free value to a attribute xml value
	 * 
	 * @param value
	 * @return
	 */
	public static String toHTMLAttribute(String value) {
		if (value == null) {
			return "";
		}
		try {
			return Encode.forHtmlAttribute(URLEncoder.encode(value, ContentContext.CHARACTER_ENCODING));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String trimAndNullify(String str) {
		if (str != null) {
			str = str.trim();
			if (str.isEmpty()) {
				str = null;
			}
		}
		return str;
	}

	public static String txt2html(String text) {
		String res = text;
		for (String[] element : TXT2HTML) {
			res = res.replaceAll(element[0], element[1]);
		}
		return res;
	}

	/**
	 * replace CR with <br />
	 * and replace special char to html code
	 * 
	 * @param text
	 *            a simple text
	 * @return XHTML code
	 */
	public static String txt2htmlCR(String text) {
		String res = text;
		// res = StringEscapeUtils.escapeHtml(text);
		for (String[] element : TXT2HTML) {
			res = res.replace(element[0], element[1]);
		}
		StringReader reader = new StringReader(res);
		BufferedReader bReader = new BufferedReader(reader);
		StringBuffer CRres = new StringBuffer();
		try {
			String line = bReader.readLine();
			while (line != null) {
				CRres.append(line);
				CRres.append("<br />");
				line = bReader.readLine();
			}
		} catch (IOException e) {
			// impossible
		}
		return CRres.toString();
	}

	public static Map<String, String> uriParamToMap(String uri) {
		Map<String, String> outMap = new HashMap<String, String>();
		if (uri == null) {
			return outMap;
		}
		int start = uri.indexOf("?");
		boolean inParamName = true;
		StringBuffer currentName = new StringBuffer();
		StringBuffer currentValue = new StringBuffer();
		for (int i = start + 1; i < uri.length(); i++) {
			char currentChar = uri.charAt(i);
			if (inParamName) {
				if (currentChar != '=') {
					currentName.append(currentChar);
				} else {
					inParamName = false;
				}
			} else {
				if (currentChar != '&') {
					currentValue.append(currentChar);
				} else {
					outMap.put(currentName.toString(), currentValue.toString());
					currentName.setLength(0);
					currentValue.setLength(0);
					inParamName = true;
				}
			}
		}
		outMap.put(currentName.toString(), currentValue.toString());
		currentName.setLength(0);
		currentValue.setLength(0);
		return outMap;
	}

	public static String writeLines(String... text) {
		StringWriter writer = new StringWriter();
		BufferedWriter bufWriter = new BufferedWriter(writer);
		try {
			for (String element : text) {
				bufWriter.write(element);
				bufWriter.newLine();
			}
			bufWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return writer.toString();
	}

	/**
	 * return the type of a path (video, image...)
	 * 
	 * @param path
	 * @param prefix
	 * @return
	 */
	public static String getPathType(String path, String prefix) {
		if (StringHelper.isVideo(path)) {
			return prefix + "video";
		} else if (StringHelper.isImage(path)) {
			return prefix + "image";
		} else if (StringHelper.isHTML(path)) {
			return prefix + "html";
		} else if (StringHelper.isSound(path)) {
			return prefix + "sound";
		} else if (StringHelper.isMail(path)) {
			return prefix + "mail";
		} else if (StringHelper.isPDF(path)) {
			return prefix + "pdf";
		} else if (StringHelper.isDoc(path)) {
			return prefix + "doc";
		} else {
			return "";
		}
	}

	public static boolean isEmpty(Object value) {
		return (value == null || value.toString() == null || value.toString().trim().length() == 0);
	}

	public static boolean isAllEmpty(Object... values) {
		for (Object val : values) {
			if (!isEmpty(val)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isOneEmpty(Object... values) {
		for (Object val : values) {
			if (isEmpty(val)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * extract a subtext from a text. sample : Hi patrick how are you ?, extr
	 * 
	 * @param text
	 * @param prefix
	 * @param suffix
	 * @return
	 */
	public static List<String> extractItem(String text, String prefix, String suffix) {
		List<String> items = new LinkedList<String>();
		int startIndex = text.indexOf(prefix);
		int endIndex = text.indexOf(suffix);
		while (startIndex > -1 && endIndex > startIndex + prefix.length()) {
			String item = text.substring(startIndex + prefix.length(), endIndex);
			if (!items.contains(item)) {
				items.add(item);
			}
			text = text.substring(endIndex + suffix.length());
			startIndex = text.indexOf(prefix);
			endIndex = text.indexOf(suffix);
		}
		return items;
	}

	public static Collection<? extends String> stringToCollectionRemoveEmpty(String addRolesAsRaw) {
		List<String> outList = new LinkedList<String>();
		outList.addAll(stringToCollection(addRolesAsRaw));
		Iterator<String> iterator = outList.iterator();
		while (iterator.hasNext()) {
			if (iterator.next().trim().length() == 0) {
				iterator.remove();
			}
		}
		return outList;
	}

	/**
	 * sort line of a text
	 */
	public static String sortText(String text) {
		BufferedReader sr = new BufferedReader(new StringReader(text));
		List<String> lines = new LinkedList<String>();
		String line;
		try {
			line = sr.readLine();
			while (line != null) {
				lines.add(line);
				line = sr.readLine();
			}
		} catch (IOException e) {

			e.printStackTrace();
		}
		Collections.sort(lines, new StringComparator());
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		for (String sortedLine : lines) {
			out.println(sortedLine);
		}
		out.close();
		return new String(outStream.toByteArray());
	}

	private static boolean incrementArray(int[] array, int i, int max) {
		if (array[i] + 1 > max) {
			if (i == array.length - 1) {
				return false;
			}
			array[i] = 0;
			return incrementArray(array, i + 1, max);
		} else {
			array[i] = array[i] + 1;
			return true;
		}
	}

	/**
	 * create a new unic key.
	 * 
	 * @param length
	 *            the length of the key.
	 * @param keys
	 *            set of keys allready exist
	 * @return null if all keys are in keys set
	 */
	public static String createKey(int length, Set<String> keys) {
		char[] newKey = new char[length];
		int[] position = new int[length];
		boolean found = false;
		String key = null;
		while (!found) {
			for (int i = 0; i < length; i++) {
				newKey[i] = KEY_ACCEPTABLE_CHAR.charAt(position[i]);
			}
			key = new String(newKey);
			if (keys.contains(key)) {
				if (!incrementArray(position, 0, KEY_ACCEPTABLE_CHAR.length() - 1)) {
					return null;
				}
			} else {
				found = true;
			}
		}
		return key;
	}

	public static String removeNumber(String text) {
		return text.replaceAll("0|1|2|3|4|5|6|7|8|9", "");
	}

	public static String trimSpaceAndUnderscore(String inText) {
		String text = inText;
		text = text.trim();
		while (text.startsWith("_") && text.length() > 0) {
			text = text.substring(1).trim();
		}
		while (text.endsWith("_") && text.length() > 0) {
			text = text.substring(0, text.length() - 1).trim();
		}
		return text;
	}

	public static String trimOn(String inText, String token) {
		String text = inText;
		text = text.trim();
		while (text.startsWith(token) && text.length() > 0) {
			text = text.substring(1).trim();
		}
		while (text.endsWith(token) && text.length() > 0) {
			text = text.substring(0, text.length() - 1).trim();
		}
		return text;
	}

	public static String trimLineReturn(String string) {
		if (string == null) {
			return null;
		}
		return string.replaceAll("(^[\r\n]+)|([\r\n]+$)", "");
	}

	public String cleanString(String text) {
		try {
			return java.net.URLDecoder.decode(text, ContentContext.CHARACTER_ENCODING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return text;
		}
	}

	/**
	 * clean path, remove double "/" and replace "\" by "/"
	 * 
	 * @param path
	 * @return "//web/path" >> "/web/path"
	 */
	public static String cleanPath(String path) {
		if (path == null) {
			return null;
		} else {
			path = path.replace('\\', '/');
			while (path.indexOf("//") >= 0) {
				path = path.replace("//", "/");
			}
			return path;
		}

	}

	/**
	 * check if a text contains uppercase char. test > false, Test > true, TEST
	 * > true
	 * 
	 * @param text
	 * @return
	 */
	public static boolean containsUppercase(String text) {
		for (int i = 0; i < text.length(); i++) {
			if (StringUtils.isAllUpperCase("" + text.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Method copied from the private method
	 * java.util.Properties#saveConvert(...)
	 * 
	 * @param theString
	 * @param escapeSpace
	 * @param escapeUnicode
	 * @return
	 */
	public static String escapeProperty(String theString, boolean escapeSpace, boolean escapeUnicode) {
		int len = theString.length();
		int bufLen = len * 2;
		if (bufLen < 0) {
			bufLen = Integer.MAX_VALUE;
		}
		StringBuffer outBuffer = new StringBuffer(bufLen);

		for (int x = 0; x < len; x++) {
			char aChar = theString.charAt(x);
			// Handle common case first, selecting largest block that
			// avoids the specials below
			if ((aChar > 61) && (aChar < 127)) {
				if (aChar == '\\') {
					outBuffer.append('\\');
					outBuffer.append('\\');
					continue;
				}
				outBuffer.append(aChar);
				continue;
			}
			switch (aChar) {
			case ' ':
				if (x == 0 || escapeSpace)
					outBuffer.append('\\');
				outBuffer.append(' ');
				break;
			case '\t':
				outBuffer.append('\\');
				outBuffer.append('t');
				break;
			case '\n':
				outBuffer.append('\\');
				outBuffer.append('n');
				break;
			case '\r':
				outBuffer.append('\\');
				outBuffer.append('r');
				break;
			case '\f':
				outBuffer.append('\\');
				outBuffer.append('f');
				break;
			case '=': // Fall through
			case ':': // Fall through
			case '#': // Fall through
			case '!':
				outBuffer.append('\\');
				outBuffer.append(aChar);
				break;
			default:
				if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode) {
					outBuffer.append('\\');
					outBuffer.append('u');
					outBuffer.append(hexDigit[(aChar >> 12) & 0xF]);
					outBuffer.append(hexDigit[(aChar >> 8) & 0xF]);
					outBuffer.append(hexDigit[(aChar >> 4) & 0xF]);
					outBuffer.append(hexDigit[aChar & 0xF]);
				} else {
					outBuffer.append(aChar);
				}
			}
		}
		return outBuffer.toString();
	}

	public static boolean validUTF8(byte[] input) {
		int i = 0;
		// Check for BOM
		if (input.length >= 3 && (input[0] & 0xFF) == 0xEF && (input[1] & 0xFF) == 0xBB & (input[2] & 0xFF) == 0xBF) {
			i = 3;
		}

		int end;
		for (int j = input.length; i < j; ++i) {
			int octet = input[i];
			if ((octet & 0x80) == 0) {
				continue; // ASCII
			}

			// Check for UTF-8 leading byte
			if ((octet & 0xE0) == 0xC0) {
				end = i + 1;
			} else if ((octet & 0xF0) == 0xE0) {
				end = i + 2;
			} else if ((octet & 0xF8) == 0xF0) {
				end = i + 3;
			} else {
				// Java only supports BMP so 3 is max
				return false;
			}

			while (i < end) {
				i++;
				octet = input[i];
				if ((octet & 0xC0) != 0x80) {
					// Not a valid trailing byte
					return false;
				}
			}
		}
		return true;
	}

	private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String getNewToken() {
		return getRandomString(32, KEY_ACCEPTABLE_CHAR) + getRandomId();
	}

	public static String escapeHTML(String html) {
		String outHTML = html;
		outHTML = outHTML.replaceAll("&scaron;", "&#352;");
		outHTML = outHTML.replaceAll("&ndash;", "-");
		outHTML = outHTML.replaceAll("&laquo;", "&#171;");
		outHTML = outHTML.replaceAll("&raquo;", "&#187;");
		outHTML = outHTML.replaceAll("&oslash;", "&#216;");
		return outHTML;
	}

	private static boolean isEscaped(String in, int index, char escape) {
		if (index < 0) {
			return false;
		}
		if (in.charAt(index) == escape) {
			return !isEscaped(in, index - 1, escape);
		} else {
			return false;
		}
	}

	private static List<String> splitAsList(String in, String delimiter, char escape, boolean trim) {
		List<String> out = new LinkedList<String>();
		if (in != null && in.length() > 0) {
			int pos = 0, last = 0, len = in.length();
			boolean isOut = false;
			while (pos >= 0 && !isOut) {
				pos = in.indexOf(delimiter, pos);
				if (pos == -1) {
					pos = len;
					isOut = true;
				}
				if (!isEscaped(in, pos - 1, escape)) {
					String s = in.substring(last, pos);
					s = StringUtils.replace(s, "" + escape + delimiter, "" + delimiter);
					s = StringUtils.replace(s, "" + escape + escape, "" + escape);
					if (trim) {
						s = s.trim();
					}
					out.add(s);
					last = pos + delimiter.length(); // length of delimiter
				}
				pos = pos + delimiter.length(); // length of delimiter
			}
		}
		return out;
	}

	private static String concat(Iterable<?> in, String delimiter, char escape) {
		StringWriter out = new StringWriter();
		if (in != null) {
			String actDelimiter = "";
			for (Object s : in) {
				s = StringUtils.replace("" + s, "" + escape, "" + escape + escape);
				s = StringUtils.replace("" + s, "" + delimiter, "" + escape + delimiter);
				out.write(actDelimiter);
				out.write("" + s);
				actDelimiter = "" + delimiter;
			}
		}
		return out.toString();
	}

	public static String renderPrice(double price, String currency) {
		return String.format("%.2f " + currency, price);
	}

	public static String getNumberAsAlphabetic(int number) {
		StringBuffer outStr = new StringBuffer();
		while (number > 25) {
			int modNumber = number % 25;
			number = number - 26;
			outStr.insert(0, StringHelper.ALPHABET.charAt(modNumber));
		}
		outStr.insert(0, StringHelper.ALPHABET.charAt(number));
		return outStr.toString();
	}

	/**
	 * parse error without fault, return null if color is unidentified.
	 * 
	 * @param color
	 * @return
	 */
	public static Color parseColor(String color) {
		try {
			color = color.trim();
			if (!color.startsWith("#")) {
				color = '#' + color;
			}
			Color outColor = Color.decode(color);
			return outColor;
		} catch (Throwable t) {
			logger.warning(t.getMessage());
			return null;
		}
	}

	/**
	 * trim all items of the list.
	 * 
	 * @param list
	 * @return
	 */
	public static List<String> trimList(Collection<String> list) {
		List<String> outList = new LinkedList<String>();
		for (String item : list) {
			outList.add(item.trim());
		}
		return outList;
	}

	/**
	 * get the item from a string with separator. sample : text1,text2,text3
	 * getItem(sample,",",1) = text1
	 * 
	 * @param text
	 * @param sep
	 * @param i
	 * @param defaultValue
	 *            default value if item not found
	 * @return
	 */
	public static String getItem(String text, String sep, int i, String defaultValue) {
		if (text == null || sep == null || i < 1) {
			return defaultValue;
		}
		String[] splitted = StringUtils.split(text, sep);
		if (splitted.length < i) {
			return defaultValue;
		} else {
			return splitted[i - 1];
		}

	}

	/**
	 * transform a string with size in pixel in integer.
	 * 
	 * @param pxSize
	 *            a size in px (sp. '12px').
	 * @return null if bad param (sp. 12%, tralala, null) and the value in pixel
	 *         if corrent param (12px, 0px, 23 px).
	 */
	public static Integer getPixelValue(String pxSize) {
		if (pxSize == null) {
			return null;
		}
		pxSize = pxSize.trim().toLowerCase();
		if (pxSize.contains("px")) {
			pxSize = pxSize.replace("px", "");
			pxSize = pxSize.trim();
			try {
				return Integer.parseInt(pxSize);
			} catch (NumberFormatException e) {
			}
		}
		return null;
	}

	public static String hex(byte[] array) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < array.length; ++i) {
			sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
		}
		return sb.toString();
	}

	public static String md5Hex(String message) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return hex(md.digest(message.getBytes("CP1252")));
		} catch (NoSuchAlgorithmException e) {
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	public static long getCRC32(String text) {
		CRC32 crc = new CRC32();
		crc.update(text.getBytes());
		return crc.getValue();
	}

	public static String mapToString(Map<String, String> maps) {
		List<String> mapList = new LinkedList<String>();
		for (String key : maps.keySet()) {
			String[] keyValue = new String[2];
			keyValue[0] = key;
			keyValue[1] = maps.get(key);
			mapList.add(arrayToString(keyValue));
		}
		String mapString = collectionToString(mapList);
		String base64 = asBase64(mapString.getBytes());
		return base64;
	}

	public static Map<String, String> stringToMap(String encodedMap) throws IOException {
		String mapStr = new String(decodeBase64(encodedMap));
		List<String> mapList = stringToCollection(mapStr);
		Map<String, String> outMap = new HashMap<String, String>();
		for (String mapEntry : mapList) {
			String[] entry = stringToArray(mapEntry);
			outMap.put(entry[0], entry[1]);
		}
		return outMap;
	}

	/**
	 * Test if the value is included in the range.
	 * 
	 * @param range
	 *            Range values are like -25,25-30,31-35,35+
	 * @param value
	 *            the integer to test
	 * @return <code>true</code> is the value is in the range <code>false</code>
	 *         otherwise
	 */
	public static boolean rangeMatches(String range, Integer value) {
		Matcher m;
		m = RANGE_MATCHER_BETWEEN.matcher(range);
		if (m.matches()) {
			int bottom = Integer.parseInt(m.group(1));
			int top = Integer.parseInt(m.group(2));
			return value >= bottom && value <= top;
		}
		m = RANGE_MATCHER_LOWER.matcher(range);
		if (m.matches()) {
			int bound = Integer.parseInt(m.group(1));
			return value < bound;
		}
		m = RANGE_MATCHER_GREATER.matcher(range);
		if (m.matches()) {
			String str = m.group(1);
			if (str == null) {
				str = m.group(2);
			}
			int bound = Integer.parseInt(str);
			return value > bound;
		}
		throw new IllegalArgumentException("Wrong range parameter.");
	}

	public static boolean listContainsItem(String list, String sep, String item) {
		if (list.contains(sep + item + sep)) {
			return true;
		} else if (list.startsWith(item + sep)) {
			return true;
		} else if (list.endsWith(sep + item)) {
			return true;
		} else {
			return list.equals(item);
		}
	}

	public static int getColNum(String colName) {
		colName = colName.trim();
		StringBuffer buff = new StringBuffer(colName);
		char chars[] = buff.reverse().toString().toLowerCase().toCharArray();
		int retVal = 0, multiplier = 0;
		for (int i = 0; i < chars.length; i++) {
			multiplier = (int) chars[i] - 96;
			retVal += multiplier * Math.pow(26, i);
		}
		return retVal;
	}

	public static String getColName(int colIndex) {
		int div = colIndex;
		String colLetter = "";
		int mod = 0;

		while (div > 0) {
			mod = (div - 1) % 26;
			colLetter = (char) (65 + mod) + colLetter;
			div = (int) ((div - mod) / 26);
		}
		return colLetter;
	}

	public static String timedTokenGenerate(String data, long timeInMillis) {
		long now = timeInMillis / TIMED_TOKEN_DIVIDER;
		return md5Hex(data + now);
	}

	public static boolean timedTokenValidate(String tokenData, String orignalData, int validityRangeInMinutes, long timeInMillis) {
		if (tokenData == null) {
			return false;
		}
		long now = timeInMillis / TIMED_TOKEN_DIVIDER;
		long start = now - validityRangeInMinutes;
		long end = now + validityRangeInMinutes;
		for (long current = start; current <= end; current++) {
			if (md5Hex(orignalData + current).equals(tokenData)) {
				return true;
			}
		}
		return false;
	}

	public static String onlyAlphaNumeric(String data, boolean stopOnBadChar) {
		StringBuilder outCleanData = new StringBuilder();
		for (int i = 0; i < data.length(); i++) {
			/*
			 * JDK17 if (Character.isAlphabetic(data.charAt(i)) ||
			 * Character.isDigit(data.charAt(i))) {
			 * outCleanData.append(data.charAt(i)); } else if (stopOnBadChar) {
			 * break; }
			 */
			if ((data.charAt(i) >= 'a' && data.charAt(i) <= 'z') || (data.charAt(i) >= '0' && data.charAt(i) <= '9') || (data.charAt(i) >= 'A' && data.charAt(i) <= 'Z')) {
				outCleanData.append(data.charAt(i));
			} else if (stopOnBadChar) {
				break;
			}
		}

		return outCleanData.toString();
	}

}
