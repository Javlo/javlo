package org.javlo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;

public class XLSTools {

	private static Logger logger = Logger.getLogger(XLSTools.class.getName());

	public static final String REQUEST_ATTRIBUTE_KEY = "array";

	public static final String TYPE = "array-file";

	private static final int MAX_SHEET_NAME_SIZE = 30;

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
		return getArray(ctx, file, null);
	}

	public static Cell[][] getArray(ContentContext ctx, File file, String sheetNames) throws Exception {
		Cell[][] outArray = null;
		if (outArray == null) {
			if (StringHelper.getFileExtension(file.getName()).equalsIgnoreCase("xlsx")) {
				outArray = getXLSXArray(ctx, file, sheetNames);
			} else if (StringHelper.getFileExtension(file.getName()).equalsIgnoreCase("xls")) {
				outArray = getXLSArray(ctx, file, sheetNames);
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
				outCell = StringHelper.renderDouble(cell.getNumericCellValue(), locale);
				break;
			case HSSFCell.CELL_TYPE_BOOLEAN:
				outCell = "" + cell.getBooleanCellValue();
				break;
			default:
				outCell = "?";
				break;
			}

		} else {
			try {
				outCell = formatter.formatCellValue(cell);
			} catch (RuntimeException r) {
				System.out.println("exception : " + r.getMessage());
				System.out.print("cell value :" + cell.getRawValue());
				System.out.print(" / cell type :" + cell.getCellType());
				System.out.print(" / cell col :" + cell.getColumnIndex());
				System.out.println(" / cell row :" + cell.getRowIndex());
				outCell = "ERR:" + r.getMessage();
			}
		}
		if (cell.getHyperlink() != null) {
			String target = "";
			String url = cell.getHyperlink().getAddress();
			if (ctx != null && ctx.getGlobalContext().isOpenExternalLinkAsPopup(url)) {
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
				outCell = StringHelper.renderDouble(cell.getNumericCellValue(), new Locale(ctx.getRequestContentLanguage()));
				break;
			case HSSFCell.CELL_TYPE_BOOLEAN:
				outCell = "" + cell.getBooleanCellValue();
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
			if (ctx.getGlobalContext().isOpenExternalLinkAsPopup(url)) {
				target = " target=\"_blank\"";
			}
			outCell = "<a class=\"cell-link\" href=\"" + url + "\"" + target + ">" + outCell + "</a>";
		}
		return outCell;
	}

	public static Cell[][] getCellArray(String[][] array) {
		Cell[][] outArray = new Cell[array.length][];
		for (int y = 0; y < array.length; y++) {
			outArray[y] = new Cell[array[y].length];
			for (int x = 0; x < array[y].length; x++) {
				String val = array[y][x];
				Double dblVal = null;
				if (StringHelper.isFloat(val)) {
					try {
						dblVal = Double.parseDouble(val);
					} catch (NumberFormatException e) {
						dblVal = (double) -1;
					}
				}
				if (val != null) {
					outArray[y][x] = new Cell(val, dblVal, outArray, x, y);
				}
			}
		}
		return outArray;
	}
	
	public static String cleanSheetName(String sheetName) {
		if (sheetName != null) {
			if (sheetName.length() > MAX_SHEET_NAME_SIZE) {
				return sheetName.substring(0, MAX_SHEET_NAME_SIZE-1);
			} else {
				return sheetName;
			}
		} else {
			return null;
		}
		
	}

	protected static Cell[][] getXLSXArray(ContentContext ctx, File xslxFile, String sheetName) throws Exception {
		InputStream in = new FileInputStream(xslxFile);
		XSSFWorkbook workbook=null;
		try {
			workbook = new XSSFWorkbook(in);
			
			int sheetIndex = 0;
			if (sheetName != null) {
				sheetName = cleanSheetName(sheetName);				
				sheetIndex = workbook.getSheetIndex(sheetName);
			}
			if (sheetIndex<0) {
				return null;
			}
			XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
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
						outArray[y][x].setValue(renderCell(readExcelCell(ctx, sheet.getRow(y).getCell(x))));
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
			if (workbook != null) {
				workbook.close();
			}
		}
	}

	public static Cell[][] createArray(int x, int y) throws Exception {
		Cell[][] outArray = new Cell[y][];
		for (int i = 0; i < outArray.length; i++) {
			outArray[i] = new Cell[x];
			for (int j = 0; j < outArray[i].length; j++) {
				outArray[i][j] = new Cell("", null, outArray, i, j);
			}
		}
		return outArray;
	}

	public static final List<Map<String, Cell>> getItems(ContentContext ctx, Cell[][] data) {
		List<Map<String, Cell>> outList = new LinkedList<Map<String, Cell>>();
		Cell[] firstLine = data[0];
		for (int i = 1; i < data.length; i++) {
			Map<String, Cell> item = new HashMap<String, Cell>();
			Cell[] line = data[i];
			for (int j = 0; j < line.length; j++) {
				if (j < firstLine.length) {
					item.put(firstLine[j].getValue(), line[j]);
				} else {
					logger.warning("error line " + i + " too big  : " + line.length + " #title=" + firstLine.length);
				}
			}
			outList.add(item);
		}
		return outList;
	}

	protected static Cell[][] getXLSArray(ContentContext ctx, File xslxFile, String sheetName) throws Exception {
		InputStream in = new FileInputStream(xslxFile);
		HSSFWorkbook workbook = null;
		try {
			workbook = new HSSFWorkbook(in);
			
			int sheetIndex = 0;
			if (sheetName != null) {
				sheetName = cleanSheetName(sheetName);
				sheetIndex = workbook.getSheetIndex(sheetName);
			}
			if (sheetIndex<0) {
				return null;
			}
			
			HSSFSheet sheet = workbook.getSheetAt(sheetIndex);

			Iterator<Row> rowIterator = sheet.iterator();
			int w = 0;
			int h = sheet.getLastRowNum() + 1;
			while (rowIterator.hasNext()) {
				// h++;
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
			if (workbook != null) {
				workbook.close();
			}
		}

	}

	protected static String renderCell(String content) {
		if (content.trim().length() == 0) {
			content = "&nbsp;";
		}
		return XHTMLHelper.autoLink(content);
	}

	public static void writeXLS(Cell[][] array, OutputStream out) throws IOException {
		HSSFWorkbook workbook = new HSSFWorkbook();
		try {
			HSSFSheet sheet = workbook.createSheet();
			int rowNum = 0;
			for (Cell[] row : array) {
				HSSFRow excelRow = sheet.createRow(rowNum);
				rowNum++;
				int cellNum = 0;
				for (Cell cell : row) {
					HSSFCell excelCell = excelRow.createCell(cellNum);
					if (cell == null) {
						excelCell.setCellType(HSSFCell.CELL_TYPE_BLANK);
					} else {
						try {
							excelCell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
							excelCell.setCellValue(Integer.parseInt(cell.getValue()));
						} catch (NumberFormatException e) {
							excelCell.setCellType(HSSFCell.CELL_TYPE_STRING);
							excelCell.setCellValue(cell.getValue());
						}
					}
					cellNum++;
				}
			}
			workbook.write(out);
		} finally {
			workbook.close();
		}
	}
	
	public static void writeXLSX(Cell[][] array, OutputStream out) throws IOException {
		writeXLSX(array,out,null,null);
	}

	public static void writeXLSX(Cell[][] array, OutputStream out, File sourceFile, String sheetName) throws IOException {
		XSSFWorkbook workbook;
		if (sourceFile == null || !sourceFile.exists() || sourceFile.getTotalSpace() == 0) {
			workbook = new XSSFWorkbook();
		} else {
			InputStream in = new FileInputStream(sourceFile);
			try {
				workbook = new XSSFWorkbook(in);
			} finally {
				in.close();
			}				
		}
		
		try {
			XSSFSheet sheet;
			if (sheetName == null) {			
				sheet = workbook.createSheet();
			} else {
				sheetName = cleanSheetName(sheetName);
				int sheedIndex = workbook.getSheetIndex(sheetName);
				if (sheedIndex>=0) {
					workbook.removeSheetAt(sheedIndex);
				}
				sheet = workbook.createSheet(sheetName);
			}
			int rowNum = 0;
			for (Cell[] row : array) {
				XSSFRow excelRow = sheet.createRow(rowNum);
				rowNum++;
				int cellNum = 0;
				for (Cell cell : row) {
					XSSFCell excelCell = excelRow.createCell(cellNum);
					if (cell == null) {
						excelCell.setCellType(HSSFCell.CELL_TYPE_BLANK);
					} else {
						try {
							excelCell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
							excelCell.setCellValue(Long.parseLong(cell.getValue()));
						} catch (NumberFormatException e) {
							excelCell.setCellType(HSSFCell.CELL_TYPE_STRING);
							excelCell.setCellValue(cell.getValue());
						}
					}
					cellNum++;
				}
			}
			workbook.write(out);
		} finally {
			workbook.close();
		}
	}

	public static void main(String[] args) {
		// File test = new File("C:/trans/member.csv");
		//
		//
		//
		// try {
		// CSVFactory csvFactory = new CSVFactory(test);
		// FileOutputStream out = new FileOutputStream(new
		// File("c:/trans/out.xlsx"));
		// XLSTools.writeXLSX(XLSTools.getCellArray(csvFactory.getArray()),
		// out);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		String value = "143152014916634422045";
		System.out.println("***** XLSTools.main : long = " + Long.decode(value)); // TODO:
																					// remove
																					// debug
																					// trace
	}
}
