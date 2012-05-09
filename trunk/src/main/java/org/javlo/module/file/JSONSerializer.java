package org.javlo.module.file;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class JSONSerializer {

	/**
	 * Encode an object into JSON text and write it to out.
	 * <p>
	 * If this object is a Map or a List, and it's also a JSONStreamAware or a JSONAware, JSONStreamAware or JSONAware will be considered firstly.
	 * <p>
	 * DO NOT call this method from writeJSONString(Writer) of a class that implements both JSONStreamAware and (Map or List) with
	 * "this" as the first parameter, use JSONObject.writeJSONString(Map, Writer) or JSONArray.writeJSONString(List, Writer) instead.
	 *
	 * @see org.json.simple.JSONObject#writeJSONString(Map, Writer)
	 * @see org.json.simple.JSONArray#writeJSONString(List, Writer)
	 *
	 * @param value
	 * @param writer
	 */
	public static void writeJSONString(Object value, Writer out) throws IOException {
		if (value == null) {
			out.write("null");
			return;
		}

		if (value.getClass().isArray()) {
			writeJSONString((Object[]) value, out);
			return;
		}

		if (value instanceof String) {
			out.write('\"');
			out.write(escape((String) value));
			out.write('\"');
			return;
		}

		if (value instanceof Double) {
			if (((Double) value).isInfinite() || ((Double) value).isNaN())
				out.write("null");
			else
				out.write(value.toString());
			return;
		}

		if (value instanceof Float) {
			if (((Float) value).isInfinite() || ((Float) value).isNaN())
				out.write("null");
			else
				out.write(value.toString());
			return;
		}

		if (value instanceof Number) {
			out.write(value.toString());
			return;
		}

		if (value instanceof Boolean) {
			out.write(value.toString());
			return;
		}

		if (value instanceof Map) {
			writeJSONString((Map) value, out);
			return;
		}

		if (value instanceof Collection) {
			writeJSONString((Collection) value, out);
			return;
		}

		out.write(value.toString());
	}
	/**
	* Encode a map into JSON text and write it to out.
	* If this map is also a JSONAware or JSONStreamAware, JSONAware or JSONStreamAware specific behaviours will be ignored at this top level.
	* 
	* @see org.json.simple.JSONValue#writeJSONString(Object, Writer)
	* 
	* @param map
	* @param out
	*/
	public static void writeJSONString(Map map, Writer out) throws IOException {
		if (map == null) {
			out.write("null");
			return;
		}

		boolean first = true;
		Iterator iter = map.entrySet().iterator();

		out.write('{');
		while (iter.hasNext()) {
			if (first)
				first = false;
			else
				out.write(',');
			Map.Entry entry = (Map.Entry) iter.next();
			out.write('\"');
			out.write(escape(String.valueOf(entry.getKey())));
			out.write('\"');
			out.write(':');
			writeJSONString(entry.getValue(), out);
		}
		out.write('}');
	}

	public static void writeJSONString(Object[] array, Writer out) throws IOException {
		writeJSONString(Arrays.asList(array), out);
	}

	/**
	 * Encode a list into JSON text and write it to out. 
	 * If this list is also a JSONStreamAware or a JSONAware, JSONStreamAware and JSONAware specific behaviours will be ignored at this top level.
	 * 
	 * @see org.json.simple.JSONValue#writeJSONString(Object, Writer)
	 * 
	 * @param list
	 * @param out
	 */
	public static void writeJSONString(Collection list, Writer out) throws IOException {
		if (list == null) {
			out.write("null");
			return;
		}

		boolean first = true;
		Iterator iter = list.iterator();

		out.write('[');
		while (iter.hasNext()) {
			if (first)
				first = false;
			else
				out.write(',');

			Object value = iter.next();
			if (value == null) {
				out.write("null");
				continue;
			}

			writeJSONString(value, out);
		}
		out.write(']');
	}

	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters (U+0000 through U+001F).
	 * @param s
	 * @return
	 */
	public static String escape(String s) {
		if (s == null)
			return null;
		StringBuffer sb = new StringBuffer();
		escape(s, sb);
		return sb.toString();
	}

	/**
	 * @param s - Must not be null.
	 * @param sb
	 */
	static void escape(String s, StringBuffer sb) {
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			switch (ch) {
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '/':
				sb.append("\\/");
				break;
			default:
				//Reference: http://www.unicode.org/versions/Unicode5.1.0/
				if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
					String ss = Integer.toHexString(ch);
					sb.append("\\u");
					for (int k = 0; k < 4 - ss.length(); k++) {
						sb.append('0');
					}
					sb.append(ss.toUpperCase());
				}
				else {
					sb.append(ch);
				}
			}
		}//for
	}

}
