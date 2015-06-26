package org.javlo.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;

import org.javlo.helper.NetHelper;

public class ContentManipulation {
	
	public static void main(String[] args) throws Exception {
		
		File sourceFile = new File("c:/trans/content_3_18503.xml");
		File targetFile = new File("c:/trans/content_3_18504.xml");
		
		//String content = ResourceHelper.getFileContent(sourceFile);
		
		System.out.println("start.");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile), Charset.forName("UTF-8")));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetFile),Charset.forName("UTF-8")));
		String line = br.readLine();
		int countLine = 0;
		int countVideo = 0;
		int countOldVideo = 0;
		while (line != null)  {
			countLine++;
			if (line.contains("play.cfm")) {				
				String ref = line.substring(line.indexOf("play.cfm?ref\\=")+"play.cfm?ref\\=".length(), line.indexOf("&videolang"));				
				line = "link=http://ec.europa.eu/avservices/video/player.cfm?ref="+ref+"&sitelang=en";
				countVideo++;
			} else if (line.contains("http\\://ec.europa.eu/avservices/video/config.cfm?id")) {
				countOldVideo++;
				
				String configURL = line.substring(line.indexOf("http\\://ec.europa.eu/avservices/video/config.cfm?id"), line.indexOf("\"></embed>"));
				configURL = configURL.replace("\\", "");				
				String ref = null;
				try {
					String contentConfig = NetHelper.readPageGet(new URL(configURL));
					ref = contentConfig.trim().substring(contentConfig.indexOf("?ref=")+5, contentConfig.indexOf("?ref=")+10);					
					line = "link=http://ec.europa.eu/avservices/video/player.cfm?ref="+ref+"&sitelang=en";
				} catch (Exception e) {				
					System.out.println("error "+ref+" : "+e.getMessage());
					line = "link=http://ec.europa.eu/avservices/";
				}				
			}
			bw.write(line);
			bw.newLine();
			line = br.readLine();
		}
		bw.close();
		br.close();
		System.out.println("done. #lines="+countLine+"    #videos="+countVideo+"    #countOldVideo="+countOldVideo);
	}
}
