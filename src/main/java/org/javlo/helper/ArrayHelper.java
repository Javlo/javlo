package org.javlo.helper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.javlo.component.files.Cell;
import org.javlo.component.files.Cell.ColInfo;
import org.javlo.context.ContentContext;

public class ArrayHelper {

	/**
	 * insert a value at the fist position in a array of String
	 * 
	 * @param inArray
	 *            a Array of String
	 * @param firstValue
	 *            the value of the first element
	 * @return a Array with a new first element (size+1)
	 */
	public static final String[] addFirstElem(String[] inArray, String firstValue) {
		String[] outArray = new String[inArray.length + 1];
		outArray[0] = firstValue;
		for (int i = 0; i < inArray.length; i++) {
			outArray[i + 1] = inArray[i];
		}
		return outArray;
	}

	public static void addCol(String[][] array, String colName) {
		if (array.length == 0) {
			return;
		} else {
			for (int i = 0; i < array.length; i++) {
				String[] line = array[i];
				String[] newLine = new String[array[i].length + 1];
				for (int j = 0; j < line.length; j++) {
					newLine[j] = line[j];
				}
				array[i] = newLine;
				newLine[newLine.length - 1] = "";
			}
			array[0][array[0].length - 1] = colName;
		}
	}

	public static String readExcelCell(ContentContext ctx, XSSFCell cell) {
		String lg = "en";
		if (ctx != null) {
			lg = ctx.getRequestContentLanguage();
		}
		String outCell;
		if (cell.getCellType() == CellType.FORMULA) {
			if (cell.getCachedFormulaResultType() == CellType.STRING) {
				outCell = cell.getStringCellValue();
			} else if (cell.getCachedFormulaResultType() == CellType.NUMERIC) {
				outCell = StringHelper.renderDouble(cell.getNumericCellValue(), new Locale(lg));
			} else if (cell.getCachedFormulaResultType() == CellType.BOOLEAN) {
				outCell = "" + cell.getBooleanCellValue();
			} else {
				outCell = "?";
			}
		} else {
			HSSFDataFormatter formatter = new HSSFDataFormatter(new Locale(lg));
			outCell = formatter.formatCellValue(cell);
		}
		XSSFHyperlink link = cell.getHyperlink();
		if (link != null) {
			String target = "";
			String url = link.getAddress();
			if (ctx != null && ctx.getGlobalContext().isOpenExternalLinkAsPopup(url)) {
				target = " target=\"_blank\"";
			}
			outCell = "<a class=\"cell-link\" href=\"" + url + "\"" + target + ">" + outCell + "</a>";
		}
		return outCell;
	}

	protected static void calcMax(Cell[][] array) {
		for (int y = 0; y < array.length; y++) {
			ColInfo info = new ColInfo();
			for (int x = 0; x < array.length; x++) {
				if (array[x] != null) {
					for (int posy = 0; posy < array[x].length; posy++) {
						if (array[x][posy] != null) {
							array[x][posy].info = info;
							if (StringHelper.isDigit(array[x][posy].getValue())) {
								if (array[x][posy] != null) {
									double doubleVal = Double.parseDouble(array[x][posy].getValue());
									if (info.total == null) {
										info.total = (double) 0;
									}
									info.total = info.total + doubleVal;
									if (info.max < doubleVal) {
										info.max = doubleVal;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public static List<String> getTitles(Cell[][] cells) {
		if (cells.length == 0) {
			return Collections.EMPTY_LIST;
		} else if (cells[0].length == 0) {
			return Collections.EMPTY_LIST;
		} else {
			ArrayList<String> titles = new ArrayList();
			int p = 0;
			int maxRowSpan = 1;
			for (Cell title : cells[0]) {
				if (title != null) {
					if (maxRowSpan < title.getRowSpan()) {
						System.out.println(">>>>>>>>> ArrayHelper.getTitles : title = " + title.getValue()); // TODO: remove debug trace
						maxRowSpan = title.getRowSpan();
					}
				}
			}
			System.out.println(">>>>>>>>> ArrayHelper.getTitles : maxRowSpan = " + maxRowSpan); // TODO: remove debug trace
			for (Cell title : cells[maxRowSpan - 1]) {
				String t = "";
				if (title != null) {
					int addCellTitlePos = title.getRowSpan();
					t = title.getValue();
					Cell subTitle = title;
					while (addCellTitlePos < maxRowSpan && subTitle != null) {
						addCellTitlePos = addCellTitlePos + subTitle.getRowSpan();
						subTitle = cells[addCellTitlePos][p];
						t = t + " - " + subTitle;
					}

				}
				titles.add(t);
				p++;
			}
			return titles;
		}

	}

	public static XSSFWorkbook loadWorkBook(InputStream in) throws IOException {
		return new XSSFWorkbook(in);
	}

	public static Cell[][] getXLSXArray(ContentContext ctx, XSSFWorkbook workbook, int sheetNumber) throws Exception {
		try {
			XSSFSheet sheet = workbook.getSheetAt(sheetNumber);
			Iterator<Row> rowIterator = sheet.iterator();
			int w = 0;
			int h = 0;
			while (rowIterator.hasNext()) {
				h++;
				Row row = rowIterator.next();
				if (row.getLastCellNum() > w) {
					w = row.getLastCellNum();
				}
			}
			Cell[][] outArray = new Cell[h][];
			for (int y = 0; y < h; y++) {
				outArray[y] = new Cell[w];
				for (int x = 0; x < w; x++) {
					outArray[y][x] = new Cell(null, outArray, x, y);
					XSSFRow row = sheet.getRow(y);
					if (row != null) {
						XSSFCell cell = sheet.getRow(y).getCell(x);
						if (cell != null) {
							outArray[y][x].setValue(readExcelCell(ctx, cell));
						}
					}
				}
			}
			for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
				CellRangeAddress cellRange = sheet.getMergedRegion(i);
				for (int x = cellRange.getFirstColumn(); x <= cellRange.getLastColumn(); x++) {
					for (int y = cellRange.getFirstRow(); y <= cellRange.getLastRow(); y++) {
						if (x > cellRange.getFirstColumn()) {
							outArray[cellRange.getFirstRow()][cellRange.getFirstColumn()].setColSpan(outArray[cellRange.getFirstRow()][cellRange.getFirstColumn()].getColSpan() + 1);
							outArray[y][x] = null;
						}
						if (y > cellRange.getFirstRow()) {
							outArray[cellRange.getFirstRow()][cellRange.getFirstColumn()].setRowSpan(outArray[cellRange.getFirstRow()][cellRange.getFirstColumn()].getRowSpan() + 1);
							outArray[y][x] = null;
						}
					}
				}
			}
			calcMax(outArray);
			return outArray;
		} finally {
			ResourceHelper.closeResource(workbook);
		}
	}

}
