package org.javlo.utils.downloader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import org.javlo.helper.NetHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.jcodec.common.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Html2Directory {
	
	public static class Status {
		int overwriteFiles = 0;		
		int exception = 0;
	}
		
	private static final int TIMEOUT = 10 * 1000;
	
	private static final String RESOURCE_FOLDER = "_resources";
	
	private static boolean isRessource(URL url) throws Exception {
		if (url == null) {
			return false;
		}
		String contentType = NetHelper.getContentType(url);
		if (contentType == null) {
			return false;
		}
		return !StringHelper.neverNull(contentType.toLowerCase()).contains("html");
	}
	
	private static String getRelativePath (File base, File finalFile) throws IOException {
		Path first = base.toPath();
		Path second = finalFile.toPath();
		String url = first.relativize(second).toString();
		while (url.length()>1 && url.startsWith("/")) {
			url = url.substring(1);
		}
		return url;	
	}
	
	private static URL getNewUrl(URL url, String path) throws MalformedURLException {
		if (url.getPort() > 0) {
			return new URL(url.getProtocol()+"://"+URLHelper.mergePath(url.getHost()+':'+url.getPort(),path));
		} else {
			return new URL(url.getProtocol()+"://"+URLHelper.mergePath(url.getHost(),path));
		}
	}

	public static File download (URL url, File baseDir, Status status, int depth) {	
		Logger.info("download url = "+url);
		try {			
			if (url == null) {
				return null;
			}
			if (isRessource(url)) {
				File file = new File(URLHelper.mergePath(baseDir.getAbsolutePath(), RESOURCE_FOLDER, url.getPath()));
				if (!file.exists()) {
					file.getParentFile().mkdirs();			
					if (file.exists()) {
						status.overwriteFiles++;
					}
					ResourceHelper.writeUrlToFile(url, file);
				}
				return file;
			} else {		
				Document doc = Jsoup.connect(url.toString()).userAgent(NetHelper.JAVLO_USER_AGENT).timeout(TIMEOUT).get();				
				File file = new File(URLHelper.mergePath(baseDir.getAbsolutePath(), url.getPath()));
				if (StringHelper.isEmpty(StringHelper.getFileExtension(file.getName()))) {
					file = new File (file.getAbsolutePath()+".html");
				}
				file.getParentFile().mkdirs();			
				if (!file.exists()) {
					file.createNewFile();
					Elements links = doc.select("[href]");					
					for (Element link : links) {						
						String path = link.attr("href");						
						if (!StringHelper.isEmpty(path) && !StringHelper.isURL(path) && path.trim().length() > 1) {							
//							if (depth==0) {								
								File newFile = download(getNewUrl(url, path), baseDir, status, depth+1);
								if (newFile != null) {
								 	link.attr("href", getRelativePath(file.getParentFile(), newFile));
								}
//							}
						}
					}
					links = doc.select("[src]");					
					for (Element link : links) {						
						String path = link.attr("src");						
						if (!StringHelper.isEmpty(path) && !StringHelper.isURL(path) && path.trim().length() > 1) {							
//							if (depth==0) {								
								File newFile = download(getNewUrl(url, path), baseDir, status, depth+1);
								if (newFile != null) {
									link.attr("src", getRelativePath(file.getParentFile(), newFile));
								}
//							}
						}
					}
					String html = doc.html();
					ResourceHelper.writeStringToFile(file, html);
				}		
				
				return file;
			}
		} catch (Exception e) {
			status.exception++;
			e.printStackTrace();
		}
		return null;		
	}
	
	public static void main(String[] args) throws Exception {
		File outDir = new File("c:/trans/test_html_download");
		if (!outDir.getParentFile().exists()) {
			outDir.getParentFile().mkdirs();
		}
		Status status = new Status();
		//download(new URL("https://galleries.tease-pics.com/onlysecretaries/1080u-p/?id=2174460"), outDir, status, 0);
		download(new URL("http://localhost/javlo/demo/"), outDir, status, 0);
		
		String ct = NetHelper.getContentType(new URL("https://content4.coedcherry.com/bryci/164210/03.jpg"));
		System.out.println(">>>>>>>>> Html2Directory.ct =  : "+ct); //TODO: remove debug trace
		ct = NetHelper.getContentType(new URL("https://www.coedcherry.com/models/bryci"));
		System.out.println(">>>>>>>>> Html2Directory.ct =  : "+ct); //TODO: remove debug trace
	}
	
}
