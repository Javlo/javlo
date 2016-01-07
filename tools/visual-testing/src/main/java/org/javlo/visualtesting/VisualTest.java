package org.javlo.visualtesting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.javlo.visualtesting.helper.ForwardException;
import org.javlo.visualtesting.model.PageSet;
import org.javlo.visualtesting.model.Snapshot;
import org.javlo.visualtesting.model.SnapshotComparison;
import org.javlo.visualtesting.model.SnapshotComparisonPage;
import org.javlo.visualtesting.model.SnapshotedPage;

public class VisualTest {

//	public Collection<String> getUrlToProcessFromSiteMap() {
//		try {
//			Collection<String> out = new LinkedList<>();
//			URL siteMapUrl = new URL(siteUrl + "/sitemap.xml");
//			Document doc = Jsoup.parse(siteMapUrl, 5000);
//			Elements urls = doc.select("urlset url");
//			for (Element urlNode : urls) {
//				String pageUrl = urlNode.select("loc").text();
//				out.add(pageUrl);
//			}
//			return out;
//		} catch (IOException ex) {
//			throw new ForwardException(ex);
//		}
//	}

	public static void main(String[] args) throws Exception {
		Path workDirFolder = Paths.get(System.getProperty("user.dir"), "workdir");
		Path tmpFolder = workDirFolder.resolve("tmp");

		try (SnapshotMaker snapshotMaker = new SnapshotMaker(tmpFolder);
				SnapshotComparisonMaker comparisonMaker = new SnapshotComparisonMaker(tmpFolder)) {
			snapshotMaker.configure(1024);
			comparisonMaker.configure();
			new VisualTest(snapshotMaker, comparisonMaker, workDirFolder).run();
		}
	}

	private final SnapshotComparisonMaker comparisonMaker;
	private final SnapshotMaker snapshotMaker;
	private final Path sourceFolder;
	private final Path rootOutputFolder;

	public VisualTest(SnapshotMaker snapshotMaker, SnapshotComparisonMaker comparisonMaker, Path workDirFolder) {
		this.snapshotMaker = snapshotMaker;
		this.comparisonMaker = comparisonMaker;
		this.sourceFolder = workDirFolder.resolve("source-files");
		this.rootOutputFolder = workDirFolder.resolve("output");
	}

	private void run() {
		System.out.println("Start run");
		try (DirectoryStream<Path> s = Files.newDirectoryStream(sourceFolder)) {
			for (Path workbookFile : s) {
				String fileName = workbookFile.getFileName().toString();
				if (fileName.endsWith(".xlsx")) {
					Path outputFolder = rootOutputFolder.resolve(fileName.replaceAll("\\.xlsx$", ""));
//					Path outWorkbookFile = rootOutputFolder.resolve(fileName);
					System.out.println("#################################################");
					System.out.println("Start file: " + fileName);
					processExcelFile(workbookFile, outputFolder, workbookFile);
					System.out.println("END file: " + fileName);
				}
			}
		} catch (IOException e) {
			throw new ForwardException(e);
		}
		System.out.println("END run");
	}

	private void processExcelFile(Path workbookFile, Path workbookOutputFolder, Path outWorkbookFile) throws IOException {
		Workbook workbook = loadWorkbook(workbookFile);
		int sheetCount = workbook.getNumberOfSheets();
		for (int i = 0; i < sheetCount; i++) {
			String name = workbook.getSheetName(i);
			if (!isStringIgnored(name)) {
				System.out.println("=====================================");
				System.out.println("Start sheet: " + name);
				Path sheetOutputFolder = workbookOutputFolder.resolve(name);
				processSheet(workbook.getSheetAt(i), sheetOutputFolder);
				System.out.println("END sheet: " + name);
			}
		}
		Files.createDirectories(outWorkbookFile.getParent());
		storeWorkbook(outWorkbookFile, workbook);
	}

	private void processSheet(Sheet sheet, Path sheetOutputFolder) throws IOException {
		PageSet pageSet = new PageSet(sheetOutputFolder);
		Integer newSnapshotColIndex = null;
		String newSnapshotKey = null;
		String previousSnapshotKey = null;
		int rowIndex = 0;
		Row keysRow = sheet.getRow(rowIndex++);
		if (keysRow == null) {
			return;
		}
		for (int colIndex = 1; colIndex < keysRow.getLastCellNum(); colIndex++) {
			String key = getCellString(keysRow, colIndex);
			if (key != null && !isStringIgnored(key)) {
				previousSnapshotKey = newSnapshotKey;
				newSnapshotColIndex = colIndex;
				newSnapshotKey = key;
			}
		}
		if (newSnapshotColIndex != null) {
			Map<String, Row> rowsByUrl = new HashMap<>();
			Row datesRow = sheet.getRow(rowIndex++);
			for (int i = rowIndex; i <= sheet.getLastRowNum(); i++) {
				Row pageRow = sheet.getRow(i);
				String url = getCellString(pageRow, 0);
				if (url != null && !isStringIgnored(url)) {
					System.out.println(url);
					pageSet.addUrlToProcess(url);
					rowsByUrl.put(url, pageRow);
				}
			}

			System.out.println("Start snapshot: " + newSnapshotKey);
			Snapshot newSnap = createSnapshot(pageSet, newSnapshotKey);
			setCellString(datesRow, newSnapshotColIndex, formatSnapDate(newSnap.getTime()));
			if (previousSnapshotKey != null) {
				Snapshot oldSnap = pageSet.getSnapshotByName(previousSnapshotKey);
				System.out.println("Start comparison: " + previousSnapshotKey);
				SnapshotComparison comparison = compareSnapshots(pageSet, oldSnap, newSnap);
				for (SnapshotComparisonPage result : comparison.getResults()) {
					Row row = rowsByUrl.get(result.getUrl());
					setCellString(row, newSnapshotColIndex, new DecimalFormat("0%").format(result.getMatchScore()));
				}
			} else {
				for (SnapshotedPage page : newSnap.getPages()) {
					Row row = rowsByUrl.get(page.getUrl());
					setCellString(row, newSnapshotColIndex, "snap");
				}
			}
		}
	}

	private static String formatSnapDate(Date time) {
		return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(time);
	}

	private static String getCellString(Row row, int colIndex) {
		String out = null;
		Cell cell = row.getCell(colIndex);
		if (cell != null) {
			out = cell.getStringCellValue();
		}
		return StringUtils.trimToNull(out);
	}

	private static void setCellString(Row row, Integer colIndex, String str) {
		Cell cell = row.getCell(colIndex, Row.CREATE_NULL_AS_BLANK);
		cell.setCellValue(str);
	}

	private static boolean isStringIgnored(String str) {
		return str.trim().startsWith("#");
	}

	private static Workbook loadWorkbook(Path workbookFile) throws IOException {
		try (InputStream in = Files.newInputStream(workbookFile)) {
			return new XSSFWorkbook(in);
		}
	}

	private static void storeWorkbook(Path workbookFile, Workbook workbook) throws IOException {
		try (OutputStream out = Files.newOutputStream(workbookFile,
				StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
			workbook.write(out);
		}
	}

	private Snapshot createSnapshot(PageSet site, String newSnapshotName) throws IOException {
		return snapshotMaker.snapshot(site, newSnapshotName, true);
	}

	private SnapshotComparison compareSnapshots(PageSet site, Snapshot oldSnap, Snapshot newSnap) throws IOException {
		return comparisonMaker.compare(site, oldSnap, newSnap, true);
	}

}
