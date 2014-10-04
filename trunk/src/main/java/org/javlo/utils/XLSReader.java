package org.javlo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;

public class XLSReader {

		public static final String REQUEST_ATTRIBUTE_KEY = "array";

		public static final String TYPE = "array-file";

		/**
		 * get the size of empty cell after the current cell (for colspan)
		 * 
		 * @param cell
		 * @param startCell
		 * @return
		 */
		private static int getEmptyLength(String[] cell, int startCell) {
			int out = 0;
			int i = startCell + 1;
			while (i < cell.length && cell[i].length() == 0) {
				out++;
				i++;
			}
			return out;
		}

		public static Cell[][] getArray(ContentContext ctx, File file) throws Exception {
			Cell[][] outArray = null;
			if (outArray == null) {
				if (StringHelper.getFileExtension(file.getName()).equalsIgnoreCase("xlsx")) {
					outArray = getXLSXArray(ctx, file);
				} else if (StringHelper.getFileExtension(file.getName()).equalsIgnoreCase("xls")) {
					outArray = getXLSArray(ctx, file);
				}
				optimizeRowSpan(outArray);				
			}
			return outArray;
		}

		private static void optimizeRowSpan(Cell[][] outArray) {
			if (outArray == null) {
				return;
			}
			for (int x = 0; x < outArray.length; x++) {
				int minRawSpan = Integer.MAX_VALUE;
				for (int y = 0; y < outArray[x].length; y++) {
					if (outArray[x][y] != null && outArray[x][y].getRowSpan() < minRawSpan) {
						minRawSpan = outArray[x][y].getRowSpan();
					}
				}
				if (minRawSpan > 1) {
					minRawSpan--;
					for (int y = 0; y < outArray[x].length; y++) {
						if (outArray[x][y] != null) {
							outArray[x][y].setRowSpan(outArray[x][y].getRowSpan() - minRawSpan);
						}
					}
				}
			}
		}

		private static String renderCell(ContentContext ctx, String cell) throws Exception {	
			if (ctx == null) {
				return cell;
			}
			return XHTMLHelper.textToXHTML(cell, ctx.getGlobalContext());
		}

		private static String readExcelCell(ContentContext ctx, XSSFCell cell) {
			Locale locale = Locale.ENGLISH;
			if (ctx != null) {
				locale = new Locale(ctx.getRequestContentLanguage());
			}
			HSSFDataFormatter formatter = new HSSFDataFormatter(locale);
			String outCell;
			if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
				switch (cell.getCachedFormulaResultType()) {
				case HSSFCell.CELL_TYPE_STRING:
					outCell = cell.getStringCellValue();
					break;
				case HSSFCell.CELL_TYPE_NUMERIC:
					outCell = StringHelper.renderDouble(cell.getNumericCellValue(),locale);				
					break;
				case HSSFCell.CELL_TYPE_BOOLEAN:
					outCell = ""+cell.getBooleanCellValue();
					break;
				default:
					outCell = "?";
					break;
				}			
			} else {
				outCell = formatter.formatCellValue(cell);			
			}
			if (cell.getHyperlink() != null) {
				String target = "";
				String url = cell.getHyperlink().getAddress();
				if (ctx != null && ctx.getGlobalContext().isOpenExernalLinkAsPopup(url)) {
					target = " target=\"_blank\"";
				}
				outCell = "<a class=\"cell-link\" href=\"" + url + "\"" + target + ">" + outCell + "</a>";
			}
			return outCell;
		}

		private static String readExcelCell(ContentContext ctx, HSSFCell cell) {
			HSSFDataFormatter formatter = new HSSFDataFormatter(new Locale(ctx.getRequestContentLanguage()));
			String outCell;
			if (cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
				switch (cell.getCachedFormulaResultType()) {
				case HSSFCell.CELL_TYPE_STRING:
					outCell = cell.getStringCellValue();
					break;
				case HSSFCell.CELL_TYPE_NUMERIC:
					outCell = StringHelper.renderDouble(cell.getNumericCellValue(),new Locale(ctx.getRequestContentLanguage()));				
					break;
				case HSSFCell.CELL_TYPE_BOOLEAN:
					outCell = ""+cell.getBooleanCellValue();
					break;
				default:
					outCell = "?";
					break;
				}			
			} else {
				outCell = formatter.formatCellValue(cell);			
			}
			if (cell.getHyperlink() != null) {
				String target = "";
				String url = cell.getHyperlink().getAddress();
				if (ctx.getGlobalContext().isOpenExernalLinkAsPopup(url)) {
					target = " target=\"_blank\"";
				}
				outCell = "<a class=\"cell-link\" href=\"" + url + "\"" + target + ">" + outCell + "</a>";
			}
			return outCell;
		}

		protected static Cell[][] getXLSXArray(ContentContext ctx, File xslxFile) throws Exception {
			InputStream in = new FileInputStream(xslxFile);
			try {
				XSSFWorkbook workbook = new XSSFWorkbook(in);
				XSSFSheet sheet = workbook.getSheetAt(0);
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
						outArray[y][x] = new Cell(null, null, outArray, x, y);
						if (sheet.getRow(y) != null && sheet.getRow(y).getCell(x) != null) {
							if (sheet.getRow(y).getCell(x).getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
								outArray[y][x].setDoubleValue(sheet.getRow(y).getCell(x).getNumericCellValue());	
							}
							outArray[y][x].setValue(renderCell(ctx, readExcelCell(ctx, sheet.getRow(y).getCell(x))));
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

				return outArray;
			} finally {
				ResourceHelper.closeResource(in);
			}
		}

		protected static Cell[][] getXLSArray(ContentContext ctx, File xslxFile) throws Exception {
			InputStream in = new FileInputStream(xslxFile);
			try {
				HSSFWorkbook workbook = new HSSFWorkbook(in);
				HSSFSheet sheet = workbook.getSheetAt(0);
				
				
				
				Iterator<Row> rowIterator = sheet.iterator();
				int w = 0;
				int h = sheet.getLastRowNum()+1;
				while (rowIterator.hasNext()) {
					//h++;
					Row row = rowIterator.next();
					if (row.getLastCellNum() > w) {
						w = row.getLastCellNum();
					}
				}

				Cell[][] outArray = new Cell[h][];
				for (int y = 0; y < h; y++) {
					outArray[y] = new Cell[w];
					for (int x = 0; x < w; x++) {
						outArray[y][x] = new Cell(null, null, outArray, x, y);
						if (sheet.getRow(y) != null && sheet.getRow(y).getCell(x) != null) {
							if (sheet.getRow(y).getCell(x).getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
								outArray[y][x].setDoubleValue(sheet.getRow(y).getCell(x).getNumericCellValue());	
							}
							outArray[y][x].setValue(renderCell(ctx, readExcelCell(ctx, sheet.getRow(y).getCell(x))));
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

				return outArray;
			} finally {
				ResourceHelper.closeResource(in);
			}

		}

		protected String renderCell(String content) {
			if (content.trim().length() == 0) {
				content = "&nbsp;";
			}
			return XHTMLHelper.autoLink(content);
		}

		public static void main(String[] args) {
			File test = new File("C:/Users/pvandermaesen/Dropbox/Documents/pro/volpaiole/in/price_list_javlo.xlsx");			
			try {
				Cell[][] array = getArray(null, test);
				for (int x = 0; x < array.length; x++) {
					System.out.println("");
					for (int y = 0; y < array[x].length; y++) {
						System.out.print(array[x][y]+",");
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		

	}


}
