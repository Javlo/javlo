package org.javlo.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringEscapeUtils;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.image.GlobalImage;
import org.javlo.component.list.DataList;
import org.javlo.component.text.WysiwygParagraph;
import org.javlo.component.title.Heading;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;
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

	public static void main(String[] args) throws IOException {
		// File docxFile = new File("c:/trans/modele de convention.docx");
		File docxFile = new File("c:/trans/REISOVEREENKOMST INDICAMP WIP.docx");

		// Map<String, String> tokens = new HashMap<>();
		// tokens.put("${company.name}", "Ma Myrtille à moi !");
		// tokens.put("${company.web}", "https://lescontesdemyrtille.be/");
		// replaceTokens(docxFile.getAbsolutePath(), tokens);

		String xml = getDocxXmlContent(docxFile.getAbsolutePath());
		xml = xml.replace("{{firstname}}", "Patrick aime l'été");

		System.out.println(xml);
		
		File docxFileTarget = new File("c:/trans/REISOVEREENKOMST INDICAMP WIP_target.docx");
		if (docxFileTarget.exists()) {
			docxFileTarget.delete();
		}

		FileOutputStream out = new FileOutputStream(docxFileTarget);
		
		writeDocxXmlContent(docxFile.getAbsolutePath(), xml, out);
		
		out.close();

	}

	public static String getDocxXmlContent(String docxFile) throws IOException {
		Path zipFilePath = Paths.get(docxFile);
		try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, null)) {
			Path source = fs.getPath("/word/document.xml");
			try (InputStream in = Files.newInputStream(source);) {
				return ResourceHelper.writeStreamToString(in, "UTF-8");
			}
		}
	}

	public static void writeDocxXmlContent(String docxFile, String xml, OutputStream out) throws IOException {
		Path zipFilePath = Paths.get(docxFile);
		ZipOutputStream zipOut = new ZipOutputStream(out);
		try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, null)) {
			Files.walkFileTree(fs.getPath("/"), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
					ZipEntry zipEntry = new ZipEntry(path.toString().substring(1));
					zipOut.putNextEntry(zipEntry);
					if (path.getFileName().toString().endsWith("/document.xml")) {
						try (InputStream is = Files.newInputStream(path)) {
							zipOut.write(xml.getBytes(), 0, xml.getBytes().length);
						}
					} else {
						try (InputStream is = Files.newInputStream(path)) {
							int length;
							int size=0;
							byte[] bytes = new byte[1024];
							while ((length = is.read(bytes)) >= 0) {
								size+=length;
								zipOut.write(bytes, 0, length);
							}
							System.out.println(path +" : "+size);
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
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
