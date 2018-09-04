package org.javlo.helper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class FontHelper {
	
	public static String loadFont(String inFontIncluding) {		
		String outStr = inFontIncluding;
		if (NetHelper.isConnected()) {			
			try {
				String[] links = inFontIncluding.split("link");
				outStr="";
				for (int i = 0; i < links.length; i++) {
					int indexHttp = links[i].indexOf("http");
					if (indexHttp>=0) { 
						String fontIncluding = links[i].substring(indexHttp);
						String url = fontIncluding.substring(0, fontIncluding.indexOf("\""));
						String style = NetHelper.readPageGet(new URL(url));
						style = style.replace("}", "  -fs-pdf-font-embed: embed;\n  -fs-pdf-font-encoding: Identity-H;\n}");						
						outStr = outStr + style;
					}
				}
				outStr="<style>\n"+outStr+"</style>";
			} catch (Exception e) {
				outStr = inFontIncluding;
				e.printStackTrace();
			}
		}
		return outStr;
	}
	
	public static final String cleanSrc(String style) throws IOException {
		BufferedReader bufReader = new BufferedReader(new StringReader(style));
		String line = bufReader.readLine();
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		while (line != null) {
			line = line.trim();
			if (line.startsWith("src:")) {
				if (line.contains("url")) {
					line = "src: "+line.substring(line.indexOf("url")).trim();
					line = line.substring(0, line.indexOf(")")+1)+';';
				}
			}
			out.println(line);
			line = bufReader.readLine();
		}
		out.close();
		return new String(outStream.toByteArray());
	}
	
	public static String createLocalFont(String key, String inFontIncluding, File localFolder) throws MalformedURLException, IOException {
		int fileNumber = 1;
		for (String url : StringHelper.extractItem(inFontIncluding, "url(", ")")) {
			File fontFile = new File(URLHelper.mergePath(localFolder.getAbsolutePath(), StringHelper.createFileName(key)+'_'+fileNumber+'.'+StringHelper.getFileExtension(url)));
			if (!fontFile.exists()) {
				ResourceHelper.writeUrlToFile(new URL(url), fontFile);
			}
			inFontIncluding=cleanSrc(inFontIncluding.replace(url, "##BASE_URI##/fonts/"+fontFile.getName()));
			fileNumber++;
		}
		return inFontIncluding;
	}
	
	public static void main(String[] args) throws IOException {
		Properties src = ResourceHelper.loadProperties(new File("c:/trans/fonts/fonts_reference.properties"));
		File targetFile = new File("c:/trans/fonts/fonts_reference_local.properties");
		File targetFont = new File("c:/trans/fonts/fonts");
		targetFont.mkdirs();
		if (!targetFile.exists()) {
			targetFile.getParentFile().mkdirs();
		} else {
			targetFile.delete();
		}
		targetFile.createNewFile();
		Properties target = ResourceHelper.loadProperties(targetFile);
		for (Object key : src.keySet()) {
			target.setProperty(""+key, createLocalFont(""+key, src.getProperty(""+key), targetFont));
		}
		ResourceHelper.storeProperties(target, targetFile);
		
//		System.out.println(cleanSrc("first line\nsrc: local('Acme Regular'), local('Acme-Regular'), url(/javlo/document/wktp/poster/document/fonts/acme_1.ttf) format('truetype');"));
		
	}
}
