package org.javlo.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Stack;

import org.javlo.helper.XHTMLHelper;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

public class FooterGenerator {

	private static Collection<String> getCols() {
		Collection<String> cols = new LinkedList<String>();
		for (char cc : "DEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()) {
			cols.add("" + cc);
		}
		cols.add("AA");
		return cols;
	}

	public static void main(String[] args) {
		// File file = new
		// File("C:/Users/pvandermaesen/Dropbox/Documents/pro/pe/siteplanet/footer.ods");
		// File templateFolder = new
		// File("C:/Users/pvandermaesen/Dropbox/work/data/javlo/template/galaxy-template");

		File file = new File("C:/Users/pvandermaesen/Dropbox/Documents/pro/pe/president_2014/work/footer.ods");
		// File templateFolder = new
		// File("C:/work/javlo2/target/javlo/work_template/galaxy-2014/the-president");
		File templateFolder = new File("C:/work/javlo2/target/javlo/work_template/galaxy-2014/the-president");
		try {
			OutputStream outStream = new FileOutputStream(new File(templateFolder.getAbsolutePath() + '/' + "footer_bottom.jsp"));
			PrintStream out = new PrintStream(outStream);
			out.println("<%@ taglib uri=\"http://java.sun.com/jsp/jstl/core\" prefix=\"c\"%><div class=\"galaxynav\">");

			final Sheet sheet = SpreadSheet.createFromFile(file).getSheet(0);
			int indice = 3;
			int col = 1;
			boolean titleOpen = false;
			String label = sheet.getCellAt("B" + indice).getValue().toString().trim();
			
			String globalCloseCode = "";
			String localCloseCode = "";
			while (label.length() > 0) {
				String title = "<c:choose>";
				for (String c : getCols()) {
					String lang = sheet.getCellAt(c + "1").getValue().toString();
					if (lang.trim().length() > 0) {
						lang = lang.toLowerCase();
						title = title + "<c:when test=\"${info.language == '" + lang + "'}\">" + XHTMLHelper.escapeXHTML(sheet.getCellAt("" + c + indice).getValue().toString()) + "</c:when>";
					}
				}
				title = title + "<c:otherwise>" + sheet.getCellAt("A" + indice).getValue().toString() + "</c:otherwise></c:choose>";
				//title = label;
				String style = "" + sheet.getCellAt("A" + indice).getValue();
				if (style.equalsIgnoreCase("ft") || style.equalsIgnoreCase("t")) {
					System.out.println(label);
					out.print(localCloseCode);										
					if (style.equalsIgnoreCase("ft")) {
						out.print(globalCloseCode);
						out.println("<ul class=\"col-"+col+"\"><li><div class=\"ep-title\">" + title + "</div><ul>");
						globalCloseCode = "</ul>";
						localCloseCode = "</ul></li>";
						col++;
					} else {
						out.println("<li><div class=\"ep-title\">" + title + "</div><ul>");
						localCloseCode = "</ul></li>";
					}
					titleOpen = true;				
				} else {
					System.out.println("   > " + label);
					out.print("<li>");
					if (sheet.getCellAt("C" + indice).getValue().toString().trim().length() > 0) {
						String url = sheet.getCellAt("C" + indice).getValue().toString().replace("xx", "${info.language}").replace("XX", "${info.language}").trim();
						if (!url.startsWith("http://")) {
							url = "http://" + url;
						}
						String linkTitle = "title=\"${i18n.view['link.go-to-the-page']}\"";
						if (!url.contains("www.europarl.europa.eu")) {
							linkTitle = "title=\"${i18n.view['global.new-window']}\" target=\"_blank\" ";
						}
						out.print("<a "+linkTitle+" href=\"" + url + "\">");
					}
					out.print(title);
					if (sheet.getCellAt("C" + indice).getValue().toString().trim().length() > 0) {
						out.print("</a>");
					} else {
						out.print("");
					}
					out.println("</li>");
				}
				indice++;
				label = sheet.getCellAt("B" + indice).getValue().toString().trim();
			}
			out.println("</ul></li></ul>");
			out.println("</div>");
			out.close();
			outStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
