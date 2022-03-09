package org.javlo.utils;

import java.beans.PropertyVetoException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.image.GlobalImage;
import org.javlo.component.list.DataList;
import org.javlo.component.text.WysiwygParagraph;
import org.javlo.component.title.Heading;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.RequestService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import fr.opensagres.xdocreport.converter.ConverterRegistry;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.IConverter;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.converter.XDocConverterException;
import fr.opensagres.xdocreport.core.document.DocumentKind;

public class DocxUtils {

	/**
	 * extract title level from class.
	 * 
	 * @param cssClass
	 * @return title level, 0 if is'nt a title.
	 */
	private static int getTitleLevel(String cssClass) {
		if (cssClass == null || cssClass.trim().length() == 0) {
			return 0;
		} else {
			String[] allCss = cssClass.split(" ");
			if (allCss.length > 1) {
				allCss = Arrays.copyOfRange(allCss, 1, allCss.length);
				for (String item : allCss) {
					item = item.trim();
					if (item.length() > 0) {
						char lastChar = item.charAt(item.length() - 1);
						if (Character.isDigit(lastChar)) {
							return Integer.parseInt("" + lastChar);
						}
					}
				}
			}
		}
		return 0;
	}

	private static boolean isList(String cssClass) {
		if (cssClass == null || cssClass.trim().length() == 0) {
			return false;
		} else {
			for (String item : cssClass.split(" ")) {
				item = item.trim();
				if (item.contains("list")) {
					return true;
				}
			}
		}
		return false;
	}

	public static List<ComponentBean> extractContent(GlobalContext globalContext, InputStream in, String resourceFolder) throws XDocConverterException, IOException {
		Options options = Options.getFrom(DocumentKind.DOCX).to(ConverterTypeTo.XHTML);
		IConverter converter = ConverterRegistry.getRegistry().getConverter(options);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		converter.convert(in, out, options);

		List<ComponentBean> outContent = new LinkedList<ComponentBean>();

		Document doc = Jsoup.parse(new ByteArrayInputStream(out.toByteArray()), ContentContext.CHARACTER_ENCODING, "/");

		ComponentBean listBean = null;
		for (Element item : doc.select("img,p")) {
			String cssClass = item.attr("class");
			String text = item.text().trim();
			ComponentBean bean = new ComponentBean();
			/* text */
			if (isList(cssClass)) {
				if (listBean == null) {
					listBean = new ComponentBean();
					listBean.setType(DataList.TYPE);
					listBean.setValue("");
				}
				if (listBean.getValue().length() == 0) {
					listBean.setValue(text);
				} else {
					listBean.setValue(listBean.getValue() + "\n" + text);
				}
			} else {
				if (listBean != null) {
					outContent.add(listBean);
					listBean = null;
				}
				if (text.trim().length() > 0 && item.tagName().equals("p")) {
					int titleLevel = getTitleLevel(cssClass);
					if (titleLevel == 0) {
						bean.setType(WysiwygParagraph.TYPE);
						bean.setValue(text);
					} else {
						bean.setType(Heading.TYPE);
						text = StringEscapeUtils.unescapeXml(StringEscapeUtils.escapeHtml4(text));
						bean.setValue("text=" + text + "\ndepth=" + titleLevel);
					}
				} else if (item.tagName().equals("img") && item.attr("src") != null && item.attr("src").trim().length() > 0) {
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					PrintStream outPrint = new PrintStream(outStream);
					String folder = item.attr("src");
					outPrint.println("dir=" + URLHelper.mergePath(resourceFolder, new File(folder).getParentFile().getPath()));
					outPrint.println("file-name=" + new File(folder).getName());
					outPrint.println(GlobalImage.IMAGE_FILTER + "=full");
					if (item.attr("alt") != null) {
						outPrint.println("label=" + item.attr("alt"));
					}
					outPrint.close();
					bean.setType(GlobalImage.TYPE);
					bean.setValue(new String(outStream.toByteArray()));
				}
			}

			if (bean.getType() != null && bean.getType().length() > 0) {
				outContent.add(bean);
				bean = new ComponentBean();
			}
		}
		if (listBean != null) {
			outContent.add(listBean);
		}
		return outContent;
	}

	public static void main(String[] args) {
		try {
			performFillDocument(null, null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String performFillDocument(ContentContext ctx, RequestService rs) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, PropertyVetoException, Exception {
		File docxFile = new File("C:\\Users\\user\\data\\kidoo\\data-ctx\\data-localhost\\static\\files\\dynamic\\reisovereenkomst-indicamp-wip.docx");

		// Map<String, String> tokens = new HashMap<>();
		// tokens.put("${company.name}", "Ma Myrtille à moi !");
		// tokens.put("${company.web}", "https://lescontesdemyrtille.be/");
		// replaceTokens(docxFile.getAbsolutePath(), tokens);

		String xml = DocxUtils.getDocxXmlContent(docxFile.getAbsolutePath());
		System.out.println(">>>>>>>>> DocxUtils.main : #xml = " + xml.length()); // TODO: remove debug trace
		// xml = xml.replace("{{firstname}}", "Patrick aime l'été");

		// ResourceHelper.writeStringToFile(new File("c:/trans/doc.xml"), xml);

		// System.out.println(xml);

		File docxFileTarget = new File("c:/trans/REISOVEREENKOMST INDICAMP WIP_target.docx");
		if (docxFileTarget.exists()) {
			docxFileTarget.delete();
		}

		ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
		DocxUtils.writeDocxXmlContent(docxFile.getAbsolutePath(), xml, outBytes);
		System.out.println(">>>>>>>>> DocxUtils.main : #outBytes = " + outBytes.size()); // TODO: remove debug trace
		outBytes.flush();
		ResourceHelper.writeStreamToFile(new ByteArrayInputStream(outBytes.toByteArray()), new File("c:/trans/response.docx"));

		return null;
	}

	public static void _main(String[] args) throws IOException {
		// File docxFile = new File("c:/trans/modele de convention.docx");
		File docxFile = new File("C:\\Users\\user\\data\\kidoo\\data-ctx\\data-localhost\\static\\files\\dynamic\\reisovereenkomst-indicamp-wip.docx");

		// Map<String, String> tokens = new HashMap<>();
		// tokens.put("${company.name}", "Ma Myrtille à moi !");
		// tokens.put("${company.web}", "https://lescontesdemyrtille.be/");
		// replaceTokens(docxFile.getAbsolutePath(), tokens);

		String xml = getDocxXmlContent(docxFile.getAbsolutePath());
		System.out.println(">>>>>>>>> DocxUtils.main : #xml = " + xml.length()); // TODO: remove debug trace
		// xml = xml.replace("{{firstname}}", "Patrick aime l'été");

		// ResourceHelper.writeStringToFile(new File("c:/trans/doc.xml"), xml);

		// System.out.println(xml);

		File docxFileTarget = new File("c:/trans/REISOVEREENKOMST INDICAMP WIP_target.docx");
		if (docxFileTarget.exists()) {
			docxFileTarget.delete();
		}

		ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
		DocxUtils.writeDocxXmlContent(docxFile.getAbsolutePath(), xml, outBytes);
		System.out.println(">>>>>>>>> DocxUtils.main : #outBytes = " + outBytes.size()); // TODO: remove debug trace
		outBytes.flush();
		ResourceHelper.writeStreamToFile(new ByteArrayInputStream(outBytes.toByteArray()), new File("c:/trans/response.docx"));

		// FileOutputStream out = new FileOutputStream(docxFileTarget);
		//
		// writeDocxXmlContent(docxFile.getAbsolutePath(), xml, out);
		//
		// out.close();
		//
		// readDocxFile(docxFile);

		// String content = "<w:r w:rsidR=\"00514593\" w:rsidRPr=\"00514593\">\r\n"
		// + " <w:rPr>\r\n"
		// + " <w:rFonts w:ascii=\"Filson Pro Regular\"\r\n"
		// + " w:hAnsi=\"Filson Pro Regular\" />\r\n"
		// + " <w:color w:val=\"5DBDB1\" />\r\n"
		// + " <w:sz w:val=\"22\" />\r\n"
		// + " <w:szCs w:val=\"22\" />\r\n"
		// + " <w:lang w:val=\"nl-BE\" />\r\n"
		// + " </w:rPr>\r\n"
		// + " <w:t>{{</w:t>\r\n"
		// + " </w:r>\r\n"
		// + " <w:proofErr w:type=\"spellStart\" />\r\n"
		// + " <w:r w:rsidR=\"00514593\" w:rsidRPr=\"00514593\">\r\n"
		// + " <w:rPr>\r\n"
		// + " <w:rFonts w:ascii=\"Filson Pro Regular\"\r\n"
		// + " w:hAnsi=\"Filson Pro Regular\" />\r\n"
		// + " <w:color w:val=\"5DBDB1\" />\r\n"
		// + " <w:sz w:val=\"22\" />\r\n"
		// + " <w:szCs w:val=\"22\" />\r\n"
		// + " <w:lang w:val=\"nl-BE\" />\r\n"
		// + " </w:rPr>\r\n"
		// + " <w:t>parent.firstname</w:t>\r\n"
		// + " </w:r>\r\n"
		// + " <w:proofErr w:type=\"spellEnd\" />\r\n"
		// + " <w:r w:rsidR=\"00514593\" w:rsidRPr=\"00514593\">\r\n"
		// + " <w:rPr>\r\n"
		// + " <w:rFonts w:ascii=\"Filson Pro Regular\"\r\n"
		// + " w:hAnsi=\"Filson Pro Regular\" />\r\n"
		// + " <w:color w:val=\"5DBDB1\" />\r\n"
		// + " <w:sz w:val=\"22\" />\r\n"
		// + " <w:szCs w:val=\"22\" />\r\n"
		// + " <w:lang w:val=\"nl-BE\" />\r\n"
		// + " </w:rPr>\r\n"
		// + " <w:t>}}</w:t>\r\n"
		// + " </w:r>\r\n";
		//
		// System.out.println(cleanTokensDocx(content));

	}

	public static String getDocxXmlContent(String docxFile) throws IOException {
		Path zipFilePath = Paths.get(docxFile);
		try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, null)) {
			Path source = fs.getPath("/word/document.xml");
			try (InputStream in = Files.newInputStream(source);) {
				return cleanTokensDocx(ResourceHelper.writeStreamToString(in, "UTF-8"));
			}
		}
	}

	public static void _writeDocxXmlContent(String docxFile, String xml, OutputStream out) throws IOException {
		System.out.println("WRITE 1.1 : " + docxFile);
		Path zipFilePath = Paths.get(docxFile);
		ZipOutputStream zipOut = new ZipOutputStream(out);
		try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, null)) {
			Files.walkFileTree(fs.getPath("/"), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
					ZipEntry zipEntry = new ZipEntry(path.toString().substring(1));
					zipOut.putNextEntry(zipEntry);
					if (path.toString().endsWith("/document.xml")) {
						try (InputStream is = Files.newInputStream(path)) {
							zipOut.write(xml.getBytes(), 0, xml.getBytes().length);
						}
					} else {
						try (InputStream is = Files.newInputStream(path)) {
							int length;
							byte[] bytes = new byte[2048];
							while ((length = is.read(bytes)) >= 0) {
								zipOut.write(bytes, 0, length);
							}
						}
					}
					zipOut.flush();
					return FileVisitResult.CONTINUE;
				}
			});
		}
		zipOut.close();
	}

	public static void writeDocxXmlContent(String docxFile, String xml, OutputStream out) throws IOException {
		ZipOutputStream zipOut = new ZipOutputStream(out);
		try (FileInputStream fis = new FileInputStream(docxFile); BufferedInputStream bis = new BufferedInputStream(fis); ZipInputStream zis = new ZipInputStream(bis)) {
			ZipEntry zipEntry;
			while ((zipEntry = zis.getNextEntry()) != null) {
				zipOut.putNextEntry(zipEntry);
				if (xml != null && zipEntry.getName().endsWith("/document.xml")) {
					ResourceHelper.writeStringToStream(xml, zipOut, "UTF-8");
//					zipOut.write(xml.getBytes(), 0, xml.getBytes().length);
				} else {
					int length;
					byte[] bytes = new byte[2048];
					while ((length = zis.read(bytes)) >= 0) {
						zipOut.write(bytes, 0, length);
					}
				}
				zipOut.flush();
			}
		}
		zipOut.close();
	}

	public static void replaceTokens(String docxFile, Map<String, String> tokens) throws IOException {
		Path zipFilePath = Paths.get(docxFile);
		try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, null)) {

			// Files.walkFileTree(fs.getPath("/"), new SimpleFileVisitor<Path>() {
			// @Override
			// public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws
			// IOException {
			// System.out.println(path);
			// return FileVisitResult.CONTINUE;
			// }
			// });

			Path source = fs.getPath("/word/document.xml");
			Path temp = fs.getPath("/word/document.xml.temp");
			if (!Files.exists(source)) {
				System.out.println("not found : " + source);
			}

			// if (Files.exists(temp)) {
			// throw new IOException("temp file exists, generate another name");
			// }
			Files.move(source, temp);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(temp))); BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(source)))) {
				String line;
				while ((line = br.readLine()) != null) {
					for (Map.Entry<String, String> token : tokens.entrySet()) {
						line = line.replace(token.getKey(), token.getValue());
					}
					bw.write(line);
					bw.newLine();
				}
			}
			Files.delete(temp);
		}
	}

	public static List<XWPFParagraph> readDocxFile(File file) throws IOException {
		FileInputStream fis = null;
		XWPFDocument document = null;
		try {
			fis = new FileInputStream(file.getAbsolutePath());
			document = new XWPFDocument(fis);
			return document.getParagraphs();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			ResourceHelper.closeResource(document, fis);
		}
	}

	public static String cleanTokensDocx(String content) {
		int openPos = content.indexOf("{{<");
		while (openPos > 0) {
			int closePos = content.indexOf(">}}");
			if (closePos > openPos) {
				int contentPos = content.indexOf("<w:t>", openPos);
				if (contentPos < closePos) {
					String realContent = content.substring(contentPos + 5, content.indexOf("</w:t>", contentPos));
					String newContent = StringHelper.replaceBloc(content, "", "<w:r ", "</w:r>", openPos);
					content = StringHelper.replaceBloc(newContent, "", "<w:r ", "</w:r>", closePos - (content.length() - newContent.length()));
					content = content.replace('>' + realContent + '<', ">{{" + realContent + "}}<");
				} else {
					openPos = -1;
				}
				if (openPos > 0) {
					openPos = content.indexOf("{{<");
				}
			} else {
				openPos = -1;
			}
		}
		return content;
	}

	// public static void replaceTokens(File docxFile, Map<String, String> tokens)
	// throws IOException {
	// ZipFile zipFile = new ZipFile("C:/test.zip");
	//
	// Enumeration<? extends ZipEntry> entries = zipFile.entries();
	//
	// while (entries.hasMoreElements()) {
	// ZipEntry entry = entries.nextElement();
	// if (entry.getName().endsWith("document.xml")) {
	// InputStream stream = zipFile.getInputStream(entry);
	// String document = ResourceHelper.writeStreamToString(stream, "UTF-8");
	// for (Map.Entry<String,String> token : tokens.entrySet()) {
	// document = document.replace(token.getKey(), token.getValue());
	// }
	// entry.get
	// }
	// }
	//
	// zipFile.close();
	//
	// }

}
