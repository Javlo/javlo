package org.javlo.utils;

import org.apache.commons.lang3.StringUtils;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class StructuredProperties extends Properties {

	public static boolean isYAML(String content) {
		String yamlPattern = "^\\s*(\\w+\\s*:\\s*|-(\\s*\\w+\\s*:\\s*|\\s+\\w+))";
		return content.matches("(?s).*" + yamlPattern + ".*");
	}

	//private static final String INTERNAL_ENCODING = "8859_1";
	/** A table of hex digits */
	private static final char[] hexDigit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private boolean yaml = false;

	public StructuredProperties() {
		super();
	}

	public StructuredProperties(boolean yaml) {
		super();
		this.yaml = yaml;
	}

	/**
	 * Convert a nibble to a hex character
	 * 
	 * @param nibble
	 *            the nibble to convert.
	 */
	private static char toHex(int nibble) {
		return hexDigit[(nibble & 0xF)];
	}

	@Override
	public void store(Writer writer, String comments) throws IOException {
		store0((writer instanceof BufferedWriter) ? (BufferedWriter) writer : new BufferedWriter(writer), comments, false);
	}

	private static void convertMapToProps(String prefix, Map<String, Object> map, Properties properties) {
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			// Build the new key by adding the prefix
			String newKey = prefix.isEmpty() ? key : prefix + "." + key;

			// If the value is a map, apply the recursion
			if (value instanceof Map<?, ?>) {
				convertMapToProps(newKey, (Map<String, Object>) value, properties);
			} else {
				properties.setProperty(newKey, value.toString());
			}
		}
	}

	/**
	 * Sanitize YAML content by quoting values that contain special YAML characters
	 * This helps to read legacy YAML files that were not properly quoted
	 */
	private static String sanitizeYamlContent(String content) {
		StringBuilder result = new StringBuilder();
		String[] lines = content.split("\n");
		
		for (String line : lines) {
			// Check if line contains a key-value pair
			int colonIndex = line.indexOf(':');
			if (colonIndex > 0 && colonIndex < line.length() - 1) {
				String beforeColon = line.substring(0, colonIndex);
				String afterColon = line.substring(colonIndex + 1);
				
				// Check if the value part is not already quoted and contains special chars
				String trimmedValue = afterColon.trim();
				if (!trimmedValue.isEmpty() 
					&& !trimmedValue.startsWith("\"") 
					&& !trimmedValue.startsWith("'")
					&& !trimmedValue.startsWith("{")
					&& !trimmedValue.startsWith("[")
					&& needsQuoting(trimmedValue)) {
					
					// Extract leading whitespace from afterColon
					int firstNonSpace = 0;
					while (firstNonSpace < afterColon.length() && afterColon.charAt(firstNonSpace) == ' ') {
						firstNonSpace++;
					}
					String leadingSpaces = afterColon.substring(0, firstNonSpace);
					
					// Quote the value, escaping existing quotes
					String quotedValue = trimmedValue.replace("\"", "\\\"");
					result.append(beforeColon).append(":").append(leadingSpaces).append("\"").append(quotedValue).append("\"").append("\n");
				} else {
					result.append(line).append("\n");
				}
			} else {
				result.append(line).append("\n");
			}
		}
		
		return result.toString();
	}

	/**
	 * Check if a YAML value needs to be quoted
	 */
	private static boolean needsQuoting(String value) {
		// Check for YAML special characters that could cause parsing issues
		return value.contains("?") 
			|| value.contains(":") 
			|| value.contains("#")
			|| value.contains("[")
			|| value.contains("]")
			|| value.contains("{")
			|| value.contains("}")
			|| value.contains("&")
			|| value.contains("*")
			|| value.contains("|")
			|| value.contains(">")
			|| value.startsWith("-")
			|| value.startsWith("!")
			|| value.startsWith("%")
			|| value.startsWith("@")
			|| value.startsWith("`");
	}
	
	@Override
	public synchronized void load(InputStream inStream) throws IOException {
		if (yaml) {
			// load string in string
			String content = ResourceHelper.loadStringFromStream(inStream, Charset.forName(getInternalEncoding()));
			if (isYAML(content)) {
				Yaml yaml = new Yaml();
				try {
					Map<String, Object> map = yaml.load(content);
					convertMapToProps("", map, this);
				} catch (Exception e) {
					// If YAML parsing fails, try to sanitize the content and retry
					String sanitizedContent = sanitizeYamlContent(content);
					try {
						Map<String, Object> map = yaml.load(sanitizedContent);
						convertMapToProps("", map, this);
					} catch (Exception e2) {
						// If sanitization doesn't help, fall back to properties format
						super.load(new InputStreamReader(new ByteArrayInputStream(content.getBytes()), getInternalEncoding()));
					}
				}
			} else {
				super.load(new InputStreamReader(new ByteArrayInputStream(content.getBytes()), getInternalEncoding()));
			}
		} else {
			super.load(new InputStreamReader(inStream, getInternalEncoding()));
		}
	}

	private static void writeComments(BufferedWriter bw, String comments) throws IOException {
		bw.write("#");
		int len = comments.length();
		int current = 0;
		int last = 0;
		char[] uu = new char[6];
		uu[0] = '\\';
		uu[1] = 'u';
		while (current < len) {
			char c = comments.charAt(current);
			if (c > '\u00ff' || c == '\n' || c == '\r') {
				if (last != current)
					bw.write(comments.substring(last, current));
				if (c > '\u00ff') {
					uu[2] = toHex((c >> 12) & 0xf);
					uu[3] = toHex((c >> 8) & 0xf);
					uu[4] = toHex((c >> 4) & 0xf);
					uu[5] = toHex(c & 0xf);
					bw.write(new String(uu));
				} else {
					bw.newLine();
					if (c == '\r' && current != len - 1 && comments.charAt(current + 1) == '\n') {
						current++;
					}
					if (current == len - 1 || (comments.charAt(current + 1) != '#' && comments.charAt(current + 1) != '!'))
						bw.write("#");
				}
				last = current + 1;
			}
			current++;
		}
		if (last != current)
			bw.write(comments.substring(last, current));
		bw.newLine();
	}

	/*
	 * Converts unicodes to encoded &#92;uxxxx and escapes special characters with a preceding slash
	 */
	private String saveConvert(String theString, boolean escapeSpace, boolean escapeUnicode) {
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
					outBuffer.append(toHex((aChar >> 12) & 0xF));
					outBuffer.append(toHex((aChar >> 8) & 0xF));
					outBuffer.append(toHex((aChar >> 4) & 0xF));
					outBuffer.append(toHex(aChar & 0xF));
				} else {
					outBuffer.append(aChar);
				}
			}
		}
		return outBuffer.toString();
	}

	public String getInternalEncoding() {
		if (yaml) {
			return ContentContext.CHARACTER_ENCODING;
		}
		return "8859_1";
	}

	@Override
	public void store(OutputStream out, String comments) throws IOException {
		store0(new BufferedWriter(new OutputStreamWriter(out, getInternalEncoding())), comments, true);
	}

	private static Map<String, Object> excludePrefixKeys(Properties prop) {
		// TreeMap pour trier les clés
		Map<String, Object> sortedMap = new TreeMap<>();
		for (String key : prop.stringPropertyNames()) {
			sortedMap.put(key, prop.get(key));
		}

		Set<String> keysToRemove = new HashSet<>();
		List<String> keys = new ArrayList<>(sortedMap.keySet());

		// Vérification des préfixes
		for (int i = 0; i < keys.size(); i++) {
			String currentKey = keys.get(i);
			for (int j = i + 1; j < keys.size(); j++) {
				if (keys.get(j).startsWith(currentKey + ".")) {
					keysToRemove.add(currentKey);
					break;
				}
			}
		}

		// Construction de la map filtrée
		Map<String, Object> resultMap = new HashMap<>();
		for (Map.Entry<String, Object> entry : sortedMap.entrySet()) {
			if (!keysToRemove.contains(entry.getKey())) {
				resultMap.put(entry.getKey(), entry.getValue());
			}
		}

		return resultMap;
	}


	private void store0(BufferedWriter bw, String comments, boolean escUnicode) throws IOException {
		if (yaml) {
			storeYaml(bw, comments);
		} else {
			if (comments != null) {
				writeComments(bw, comments);
			}
			bw.write("#" + new Date().toString());
			bw.newLine();
			bw.newLine();
			synchronized (this) {

				List keysList = new LinkedList();
				for (Enumeration e = keys(); e.hasMoreElements(); ) {
					keysList.add(e.nextElement());
				}
				Collections.sort(keysList);

				String latestKeyPrefix = null;
				for (Object key : keysList) {

					String keyPrefix = StringUtils.split((String) key, ".")[0];
					if (latestKeyPrefix != null) {
						if (!latestKeyPrefix.equals(keyPrefix)) {
							bw.newLine();
							latestKeyPrefix = keyPrefix;
						}
					} else {
						latestKeyPrefix = keyPrefix;
					}

					String val = "" + get(key);
					key = saveConvert((String) key, true, escUnicode);
					/*
					 * No need to escape embedded and trailing spaces for value, hence pass false to flag.
					 */
					val = saveConvert(val, false, escUnicode);
					bw.write(key + "=" + val);
					bw.newLine();

				}
			}
		}
		bw.flush();
	}

	private void storeYaml(BufferedWriter bw, String comments) throws IOException {
			// Converting Properties into a nested Map structure
			Map<String, Object> yamlMap = new LinkedHashMap<>();
			Map<String,Object> cleanMap = excludePrefixKeys(this);
			for (String key : cleanMap.keySet()) {
				insertIntoMap(yamlMap, key, this.getProperty(key));
			}
			DumperOptions options = new DumperOptions();
			options.setIndent(6);
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			//options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
			Yaml yaml = new Yaml(options);
			yaml.dump(yamlMap, bw);
	}

	private static void insertIntoMap(Map<String, Object> map, String key, String value) {

		String[] parts = key.split("\\.");
		Map<String, Object> current = map;

		for (int i = 0; i < parts.length - 1; i++) {
			if (!(current.get(parts[i]) instanceof Map)) {
				current.put(parts[i], new LinkedHashMap<String, Object>());
			}
			current = (Map<String, Object>) current.get(parts[i]);
		}

		current.put(parts[parts.length - 1], value);
	}

	
	public void save (File file) throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		try {
			store(out, "structured properties");
		} finally {
			ResourceHelper.closeResource(out);
		}		
	}
	
	public void load (File file) throws IOException {
		if (!file.exists()) {
			return;
		}
		FileInputStream in = new FileInputStream(file);
		try {
			load(in);
		} finally {
			ResourceHelper.closeResource(in);
		}		
	}

	@Override
	public synchronized String toString() {
		StringWriter writer = new StringWriter();
        try {
            store(writer, null); // null pour ne pas ajouter de commentaire
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return writer.toString();
	}

	public static void main(String[] args) throws IOException {
		Map<String, Object> data = new LinkedHashMap<>();
		data.put("field.photo.image.value", "image");
		data.put("field.photo.image.value.file", "photo.jpg");
		data.put("field.photo.image.value.folder", "image");

		StructuredProperties prop = new StructuredProperties(true);
		prop.putAll(data);
		prop.store(new FileOutputStream("c:/trans/out.yaml"), "test");
	}
}
