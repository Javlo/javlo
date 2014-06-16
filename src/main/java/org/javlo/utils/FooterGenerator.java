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
//		File file = new File("C:/Users/pvandermaesen/Dropbox/Documents/pro/pe/siteplanet/footer.ods");
		//File templateFolder = new File("C:/Users/pvandermaesen/Dropbox/work/data/javlo/template/galaxy-template");

		File file = new File("C:/trans/footer_top.ods");
		//File templateFolder = new File("C:/work/javlo2/target/javlo/work_template/galaxy-2014/the-president");
		File templateFolder = new File("C:/work/javlo2/target/javlo/work_template/galaxy-2014/the-president");
		try {
			OutputStream outStream = new FileOutputStream(new File(templateFolder.getAbsolutePath() + '/' + "footer-top.jsp"));
			PrintStream out = new PrintStream(outStream);
			out.println("<%@ taglib uri=\"http://java.sun.com/jsp/jstl/core\" prefix=\"c\"%><div class=\"galaxynav\">");			

			final Sheet sheet = SpreadSheet.createFromFile(file).getSheet(0);
			int indice = 3;
			int countGroup = 1;
			for (int i=1; i<20; i++) {
				System.out.println("A"+i+" = "+sheet.getCellAt("A"+i).getValue());
			}
			System.out.println("A"+indice+" = "+sheet.getCellAt("A"+indice).getValue());
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
				String style = sheet.getCellAt("A" + indice).getStyle().getName();
				System.out.println("***** FooterGenerator.main : style = "+style+" name="+ sheet.getCellAt("A" + indice).getValue()); //TODO: remove debug trace
				if (style.equalsIgnoreCase("ce8") || style.equalsIgnoreCase("ce6") || style.equalsIgnoreCase("ce11")) {
					//System.out.println(title+" >> "+style);	
					if (countGroup == 4 || countGroup == 6 || countGroup == 8 || countGroup == 10) {
						out.println("</ul></li></ul>"); 
					} else if (countGroup>1) {
						out.println("</ul></li>");
					}
					if (countGroup == 1 || countGroup == 4 || countGroup == 6 || countGroup == 8 || countGroup == 10) {
						out.println("<ul><li><div class=\"ep-title\">"+title+"</div><ul>");
					} else {
						out.println("<li><div class=\"ep-title\">"+title+"</div><ul>");	
					}
					countGroup++;
				} else {
					out.println("<li>");
					if (sheet.getCellAt("B" + indice).getValue().toString().trim().length() > 0) {
						String url = sheet.getCellAt("B" + indice).getValue().toString().replace("xx", "${info.language}").replace("XX", "${info.language}").trim();
						if (!url.startsWith("http://")) {
							url = "http://"+url;
						}
						out.print("<a href=\""+url+"\">");
					}
					out.print(title);
					if (sheet.getCellAt("B" + indice).getValue().toString().trim().length() > 0) {
						out.println("</a>");
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
