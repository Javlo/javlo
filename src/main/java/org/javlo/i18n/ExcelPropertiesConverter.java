package org.javlo.i18n;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.utils.Cell;
import org.javlo.utils.XLSTools;

public class ExcelPropertiesConverter {

	public static void copy(Cell[] source, Cell[] target) {
		for (int i = 0; i < source.length; i++) {
			target[i] = source[i];
		}
	}

	private static Cell[][] addTrad(Cell[][] data, String lg, String key, String trad) {
		int langPos = -1;
		for (int i = 0; i < data[0].length; i++) {
			if (data[0][i].getValue().equalsIgnoreCase(lg)) {
				langPos = i;
				break;
			}
		}
		/** add lang **/
		if (langPos == -1) {
			Cell[] newTitle = new Cell[data[0].length + 1];
			copy(data[0], newTitle);
			data[0] = newTitle;
			data[0][newTitle.length - 1] = new Cell(lg, null, data, 0, newTitle.length);
			langPos = newTitle.length - 1;
			for (int i = 1; i < data.length; i++) {
				Cell[] newLine = new Cell[data[0].length + 1];
				copy(data[i], newLine);
				data[i] = newLine;
				data[i][newTitle.length - 1] = new Cell("", null, data, 0, newTitle.length);
				// if (data[i][0].getValue().equals(key)) {
				// data[i][newTitle.length - 1] = new Cell(trad, null, data, 0,
				// newTitle.length);
				// } else {
				// data[i][newTitle.length - 1] = new Cell("", null, data, 0, newTitle.length);
				// }
			}
		}
		/** add trad **/
		int keyPos = 0;
		for (int i = 1; i < data.length; i++) {
			if (data[i][0].getValue().equals(key)) {
				keyPos = i;
			}
		}
		if (keyPos == 0) {
			System.out.println(">> add : " + key);
			int width = data[0].length;
			Cell[][] newData = new Cell[data.length + 1][];
			for (int i = 0; i < data.length; i++) {
				newData[i] = data[i];
			}
			data = newData;
			data[data.length - 1] = new Cell[data[0].length + 1];
			data[data.length - 1][0] = new Cell(key, null, data, data.length - 1, 0);
			for (int i = 1; i < width; i++) {
				data[data.length - 1][i] = new Cell("", null, data, data.length - 1, i);
			}
			data[data.length - 1][langPos] = new Cell(trad, null, data, data.length - 1, langPos);
		} else {
			data[keyPos][langPos] = new Cell(trad, null, data, keyPos, langPos);
		}
		return data;
	}

	public static void convertPropertiesToExcel(File dir, File excel) throws IOException {
		Cell[][] data = { {} };
		data[0] = new Cell[1];
		data[0][0] = new Cell("", null, data, 0, 0);
		for (File file : dir.listFiles()) {
			if (StringHelper.getFileExtension(file.getName()).equalsIgnoreCase("properties")) {
				String fileName = StringHelper.getFileNameWithoutExtension(file.getName());
				if (fileName.length() > 3 && fileName.charAt(fileName.length() - 3) == '_') {
					String lg = fileName.substring(fileName.length() - 2, fileName.length());
					System.out.println(">>> " + lg + " <<<");
					Properties prop = new Properties();
					try (InputStream out = new FileInputStream(file)) {
						prop.load(out);
						for (Object key : prop.keySet()) {
							data = addTrad(data, lg, "" + key, "" + prop.get(key));
						}
					}
				}
			}
		}
		XLSTools.writeXLSX(data, excel);
	}

	public static void convertExcelToProperties(File excel, String prefix, File dir) throws Exception {
		if (dir.isFile()) {
			throw new IOException("dir is a file.");
		}
		dir.mkdirs();
		Cell[][] data = XLSTools.getXLSXArray(null, excel, null);
		Properties prop = new Properties();
		for (int i = 1; i < data[0].length; i++) {
			String lg = data[0][i].getValue();
			for (int j = 1; j < data.length; j++) {
				prop.setProperty(data[j][0].getValue(), data[j][i].getValue());
			}
			File propFile = new File(URLHelper.mergePath(dir.getAbsolutePath(), prefix+"_"+lg+".properties" ));
			try (OutputStream out = new FileOutputStream(propFile)) {
				prop.store(out, ExcelPropertiesConverter.class.getName());
			}
		}
	}

	public static void main(String[] args) throws Exception {
		//convertPropertiesToExcel(new File("C:\\work\\javlo2\\src\\main\\webapp\\WEB-INF\\i18n"), new File("c:/trans/out_i18n.xlsx"));
		convertExcelToProperties(new File("c:/trans/out_i18n.xlsx"), "view", new File("C:\\trans\\i18n"));
	}

}
