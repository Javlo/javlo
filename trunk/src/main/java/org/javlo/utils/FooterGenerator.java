package org.javlo.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.javlo.helper.XHTMLHelper;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

public class FooterGenerator {

	public static void main(String[] args) {
		File file = new File("c:/trans/footer.ods");
		File templateFolder = new File("C:/Users/pvandermaesen/Dropbox/work/data/javlo/template/galaxy-template");
		try {
			OutputStream outStream = new FileOutputStream(new File(templateFolder.getAbsolutePath() + '/' + "footer_bottom.jsp"));
			PrintStream out = new PrintStream(outStream);
			out.println("<%@ taglib uri=\"http://java.sun.com/jsp/jstl/core\" prefix=\"c\"%><div class=\"galaxynav\">");			

			final Sheet sheet = SpreadSheet.createFromFile(file).getSheet(0);
			int indice = 20;
			int countGroup = 1;
			while (sheet.getCellAt("A"+indice).getValue().toString().trim().length() > 0) {
				indice++;
				String title = "<c:choose>";				
				for (char c : "CDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()) {
					String lang = sheet.getCellAt(c + "1").getValue().toString();					
					if (lang.trim().length() > 0) {
						lang = lang.toLowerCase();
						title = title+"<c:when test=\"${info.language == '"+lang+"'}\">"+XHTMLHelper.escapeXHTML(sheet.getCellAt(""+c + indice).getValue().toString())+"</c:when>";
					}				
				}
				title = title+"<c:otherwise>"+sheet.getCellAt("A"+indice).getValue().toString()+"</c:otherwise></c:choose>";
				System.out.println(title);
				String style = sheet.getCellAt("A" + indice).getStyle().getName();
				if (style.equalsIgnoreCase("ce7") || style.equalsIgnoreCase("ce9")) {
					//System.out.println(title+" >> "+style);	
					if (countGroup == 4 || countGroup == 6 || countGroup == 8 || countGroup == 10) {
						out.println("</li></ul></li></ul>"); 
					} else if (countGroup>1) {
						out.println("</li></ul></li>");
					}
					if (countGroup == 1 || countGroup == 4 || countGroup == 6 || countGroup == 8 || countGroup == 10) {
						out.println("<ul><li>"+title+"<ul>");
					} else {
						out.println("<li>"+title+"<ul>");	
					}
					countGroup++;
				} else {
					out.print("<li>");
					if (sheet.getCellAt("B" + indice).getValue().toString().trim().length() > 0) {
						String url = sheet.getCellAt("B" + indice).getValue().toString().replace("xx", "${info.language}");
						if (!url.startsWith("http://")) {
							url = "http://"+url;
						}
						out.print("<a href=\""+url+"\">");
					}
					out.print(title);
					if (sheet.getCellAt("B" + indice).getValue().toString().trim().length() > 0) {
						out.println("</a></li>");
					} else {
						out.println("");
					}
					out.println("</li>");
				}				
				
				
			/*	System.out.println(title);
				System.out.println(style); */
			}
			out.println("</ul></li></ul>");  
			out.println("</div>");
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
