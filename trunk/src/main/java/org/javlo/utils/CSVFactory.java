/*
 * Created on 20-f�vr.-2004
 */
package org.javlo.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.StringTokenizer;

import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;

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

				for (int c = 0; c < array.length; c++) {
					String sep = "";
					String line = "";
					for (int l = 0; l < array[c].length; l++) {
						String elem = array[c][l];
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

	public void exportRowCSV(OutputStream outStream, String separator, String[] row) {

		synchronized (lock) {

			PrintStream out = new PrintStream(outStream);
			String sep = "";
			String line = "";
			for (int l = 0; l < row.length; l++) {
				String elem = row[l];
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

	public static void main(String[] args) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("patrick; \"catherine;amour;sexe\"; alexi; barbara");
		out.println("Jacques; \"bernad\"\"ette\"; Albert; Goergette");
		out.print("Arnaud; Anne; St�phanie; Nicolas ");
		InputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
		CSVFactory fact = null;
		try {
			fact = new CSVFactory(";", inStream);

			InputStream in = new FileInputStream("c:/trans/dc-users-excel.txt");
			try {
				fact = new CSVFactory(in, null, Charset.forName("utf-16"));
			} finally {
				ResourceHelper.closeResource(in);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		String[][] a = fact.getArray();
		for (int x = 0; x < a.length; x++) {
			for (int y = 0; y < a[x].length; y++) {
				System.out.print(a[x][y]);
				System.out.print("	");
			}
			System.out.println("");
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

}
