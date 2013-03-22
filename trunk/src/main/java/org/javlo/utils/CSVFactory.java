/*
 * Created on 20-fevr.-2004
 */
package org.javlo.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.CSVPrinter;
import com.Ostermiller.util.ExcelCSVParser;

/**
 * @author pvandermaesen
 */
public class CSVFactory {

	static final String STANDARD_SEPARATOR = ",";

	static final String SPECIAL_QUOTE = "\"";

	private String[][] array = null;

	private static final Object lock = new Object();

	/**
	 * constructor
	 * 
	 * @param separator
	 *            separator between col
	 */
	public CSVFactory(String separator, InputStream csvStream) throws IOException {
		array = initArray(separator, csvStream, ContentContext.CHARSET_DEFAULT);
	}

	/**
	 * constructor
	 * 
	 * @param separator
	 *            separator between col
	 */
	public CSVFactory(InputStream csvStream) throws IOException {
		array = initArray(null, csvStream, ContentContext.CHARSET_DEFAULT);
	}

	/**
	 * constructor
	 * 
	 * @param separator
	 *            separator between col
	 */
	public CSVFactory(File file) throws IOException {
		InputStream csvStream = new FileInputStream(file);
		try {
			array = initArray(CSVFactory.searchCSVSep(file), csvStream, ContentContext.CHARSET_DEFAULT);
		} finally {
			ResourceHelper.closeResource(csvStream);
		}
	}

	/**
	 * constructor
	 * 
	 * @param separator
	 *            separator between col
	 */
	public CSVFactory(File file, String sep) throws IOException {
		InputStream csvStream = new FileInputStream(file);
		try {
			array = initArray(sep, csvStream, ContentContext.CHARSET_DEFAULT);
		} finally {
			ResourceHelper.closeResource(csvStream);
		}
	}

	/**
	 * constructor
	 * 
	 * @param separator
	 *            separator between col
	 */
	public CSVFactory(InputStream csvStream, String sep) throws IOException {
		array = initArray(sep, csvStream, ContentContext.CHARSET_DEFAULT);
	}

	public CSVFactory(InputStream csvStream, String sep, Charset charset) throws IOException {
		array = initArray(sep, csvStream, charset);
	}

	public CSVFactory(String[][] newArray) {
		array = newArray;
	}

	public static String replace(String source, String token, String newToken) {
		StringBuffer res = new StringBuffer();
		StringTokenizer tokenizer = new StringTokenizer(source, token);
		while (tokenizer.hasMoreTokens()) {
			res.append(tokenizer.nextToken());
			if (tokenizer.hasMoreTokens()) {
				res.append(newToken);
			}
		}
		return res.toString();

	}

	/**
	 * transform a CSV file as stream to a String array (pvdm)
	 */
	private String[][] initArray(String separator, InputStream cvsStream, Charset charset) throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ResourceHelper.writeStreamToStream(cvsStream, out);

		String cvsContent = new String(out.toByteArray());

		if (separator == null) {
			separator = searchCSVSep(cvsContent);
		}

		String[][] outArray;
		char sep = separator.charAt(0);
		synchronized (lock) {
			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			outArray = ExcelCSVParser.parse(new InputStreamReader(in, charset), sep);
		}
		return outArray;
	}

	public String[][] getArray() {
		return this.array;
	}

	public void exportCSV(OutputStream outStream) {
		exportCSV(outStream, STANDARD_SEPARATOR);
	}

	public void exportCSV(OutputStream outStream, String separator) {

		try {
			PrintStream out = new PrintStream(outStream, false, ContentContext.CHARACTER_ENCODING);
			synchronized (lock) {

				for (String[] element : array) {
					String sep = "";
					String line = "";
					for (String element2 : element) {
						String elem = element2;
						if (elem == null) {
							elem = "\"\"";
						} else {
							elem = "\"" + replace(elem, "\"", "\"\"") + "\"";
						}
						out.print(sep);
						out.print(elem);
						line = line + sep + elem;
						sep = separator;
					}
					out.println();
				}
				out.close();

			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	public void exportRowCSV(OutputStream outStream, String[] row) {
		exportRowCSV(outStream, STANDARD_SEPARATOR, row);
	}

	public void exportRowCSV(OutputStream outStream, Collection<String> row) {
		exportRowCSV(outStream, STANDARD_SEPARATOR, row);
	}

	public void exportRowCSV(OutputStream outStream, String separator, String[] row) {

		synchronized (lock) {

			PrintStream out = new PrintStream(outStream);
			String sep = "";
			String line = "";
			for (String element : row) {
				String elem = element;
				if (elem == null) {
					elem = "\"\"";
				} else {
					elem = "\"" + replace(elem, "\"", "\"\"") + "\"";
				}
				out.print(sep);
				out.print(elem);
				line = line + sep + elem;
				sep = separator;
			}
			out.println();

		}
	}

	public static final String exportLine(Collection<String> data, String separator) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		String sep = "";
		for (String element : data) {
			String elem = element;
			if (elem == null) {
				elem = "\"\"";
			} else {
				elem = "\"" + replace(elem, "\"", "\"\"") + "\"";
			}
			out.print(sep);
			out.print(elem);
			sep = separator;
		}
		out.close();
		return new String(outStream.toByteArray());

	}

	public void exportRowCSV(OutputStream outStream, String separator, Collection<String> row) {
		synchronized (lock) {
			PrintStream out = new PrintStream(outStream);
			String sep = "";
			String line = "";
			for (String item : row) {
				if (item == null) {
					item = "\"\"";
				} else {
					item = "\"" + replace(item, "\"", "\"\"") + "\"";
				}
				out.print(sep);
				out.print(item);
				line = line + sep + item;
				sep = separator;
			}
			out.println();
		}
	}

	@Override
	public String toString() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		exportCSV(out);
		return new String(out.toByteArray());
	}

	public static String searchCSVSep(String csvContent) throws IOException {
		int countComa = StringHelper.countChar(csvContent, ',');
		int countSemicolumn = StringHelper.countChar(csvContent, ';');
		int countTab = StringHelper.countChar(csvContent, '\t');
		if (countComa > countSemicolumn) {
			if (countComa > countTab) {
				return ",";
			} else {
				return "\t";
			}
		} else {
			if (countSemicolumn > countTab) {
				return ";";
			} else {
				return "\t";
			}
		}
	}

	public static String searchCSVSep(File csvFile) throws IOException {
		String csvContent = ResourceHelper.loadStringFromFile(csvFile);
		return searchCSVSep(csvContent);
	}

	public String[] readHeads(File file) throws IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			return readHeads(in);
		} finally {
			if (in != null) {
				ResourceHelper.closeResource(in);
			}
		}
	}

	public String[] readHeads(InputStream in) throws IOException {
		CSVParser csvParser = new CSVParser(in);
		return csvParser.getLine();
	}

	public static List<Map<String, String>> loadContentAsMap(File file) throws IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			return loadContentAsMap(in);
		} finally {
			if (in != null) {
				ResourceHelper.closeResource(in);
			}
		}
	}

	public static List<String> loadTitle(File file) throws IOException {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			return loadTitle(in);
		} finally {
			if (in != null) {
				ResourceHelper.closeResource(in);
			}
		}
	}

	public static List<String> loadTitle(InputStream in) throws IOException {
		CSVParser csvParser = new CSVParser(in);
		String[] line = csvParser.getLine();
		if (line == null) {
			return Collections.EMPTY_LIST;
		}
		return Arrays.asList(line);
	}

	public static List<Map<String, String>> loadContentAsMap(InputStream in) throws IOException {
		List<Map<String, String>> outMaps = new LinkedList<Map<String, String>>();
		CSVParser csvParser = new CSVParser(in);
		String[][] content = csvParser.getAllValues();
		if (content == null || content.length == 0) {
			return new LinkedList<Map<String, String>>();
		}
		for (int i = 1; i < content.length; i++) {
			Map<String, String> line = new HashMap<String, String>();
			for (int j = 0; j < content[i].length; j++) {
				line.put(content[0][j], content[i][j]);
			}
			outMaps.add(line);
		}
		return outMaps;
	}

	public static void storeContentAsMap(File file, List<Map<String, String>> content) throws IOException {
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			storeContentAsMap(out, content);
		} finally {
			ResourceHelper.closeResource(out);
		}
	}

	public static void storeContentAsMap(OutputStream out, List<Map<String, String>> content) throws IOException {
		if (content.size() == 0) {
			return;
		}
		List<String> keys = new LinkedList<String>();
		for (Map<String, String> map : content) {
			for (String key : map.keySet()) {
				if (!keys.contains(key)) {
					keys.add(key);
				}
			}
		}

		Collections.sort(keys);
		String[][] rawContent = new String[content.size() + 1][keys.size()];
		for (int j = 0; j < rawContent[0].length; j++) {
			rawContent[0][j] = keys.get(j);
		}
		for (int i = 1; i < rawContent.length; i++) {
			for (int j = 0; j < rawContent[i].length; j++) {
				rawContent[i][j] = StringHelper.neverNull(content.get(i - 1).get(rawContent[0][j]));
			}
		}

		CSVPrinter printer = new CSVPrinter(out);
		printer.setAlwaysQuote(true);
		printer.writeln(rawContent);
	}

	public static void appendContentAsMap(File file, Map<String, String> content) throws IOException {
		if (content.size() == 0) {
			return;
		}
		List<String> titles = loadTitle(file);

		Collections.sort(titles);
		String[] rawContent = new String[titles.size()];
		for (int i = 0; i < titles.size(); i++) {
			rawContent[i] = StringHelper.neverNull(content.get(titles.get(i)));
		}

		BufferedWriter out = null;
		Writer fstream = null;
		try {
			fstream = new FileWriter(file, true);
			out = new BufferedWriter(fstream);
			CSVPrinter printer = new CSVPrinter(out);
			printer.setAlwaysQuote(true);
			printer.writeln(rawContent);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
			fstream.close();
		}

	}

	public static void main(String[] args) {
		try {
			File file = new File("c:/trans/test.csv");
			List<Map<String, String>> data = loadContentAsMap(file);
			Map<String, String> newLine = new HashMap<String, String>();
			newLine.put("key1.2", "new value 1.2");
			newLine.put("lastname", "Vandermaesen");
			data.add(newLine);
			storeContentAsMap(file, data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
