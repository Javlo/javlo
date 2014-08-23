package org.javlo.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.image.GlobalImage;
import org.javlo.component.list.FreeTextList;
import org.javlo.component.text.WysiwygParagraph;
import org.javlo.component.title.SubTitle;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
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

	public static void main(String[] args) {
		try {
			InputStream in = new FileInputStream(new File("c:/trans/test.docx"));
			for (ComponentBean bean : extractContent(in, "/")) {
				System.out.println(bean.getType());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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

	public static List<ComponentBean> extractContent(InputStream in, String resourceFolder) throws XDocConverterException, IOException {
		Options options = Options.getFrom(DocumentKind.DOCX).to(ConverterTypeTo.XHTML);
		IConverter converter = ConverterRegistry.getRegistry().getConverter(options);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		converter.convert(in, out, options);

		//ResourceHelper.writeStreamToFile(new ByteArrayInputStream(out.toByteArray()), new File("c:/trans/docx/test.html"));

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
					listBean.setType(FreeTextList.TYPE);
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
						if (titleLevel == 1) {
							bean.setType(Title.TYPE);
							bean.setValue(text);
						} else {
							bean.setType(SubTitle.TYPE);
							bean.setValue(text);
							bean.setStyle("" + titleLevel);
						}
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
		System.out.println("***** DocxUtils.extractContent : size = " + outContent.size()); // TODO:
																							// remove
																							// debug
																							// trace
		return outContent;
	}

}
