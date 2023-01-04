package org.javlo.service.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.javlo.helper.ResourceHelper;

public class RemoveBgService {

	public static void removeBg(String apiKey, InputStream in, OutputStream out) throws IOException {
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			HttpPost httpPost = new HttpPost("https://api.remove.bg/v1.0/removebg");
			httpPost.addHeader("X-Api-Key", apiKey);
			HttpEntity httpEntiry = MultipartEntityBuilder.create().addBinaryBody("image_file", in) .addTextBody("size", "auto").build();
			httpPost.setEntity(httpEntiry);
			
			try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
				HttpEntity entity = response.getEntity();
				entity.writeTo(out);
			}
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		String token = "BBRbShRpxP1t4KaD7en4fnax";
		
		RemoteServiceProxyService proxyService = RemoteServiceProxyService.getInstance(null);
		Map<String,String> header = new HashMap<>();
		header.put("X-Api-Key", token);
		
		File file = new File("c:/trans/img3.jpg");
		File outFile = new File("c:/trans/img3_out.jpg");
		FileOutputStream out = new FileOutputStream(outFile);
		proxyService.executeRequest("https://www.javlo.org/test.webp", header, "POST", new FileInputStream(file), out);
		System.out.println(">>>>>>>>> RemoveBgService.main : DONE"); //TODO: remove debug trace
		
//		File file = new File("c:/trans/charley_source.jpg");
//		File outFile = new File("c:/trans/charley_out.jpg");
//		FileOutputStream out = new FileOutputStream(outFile);
//		removeBg(token, new FileInputStream(file), out, "http://localhost:9090");
//		out.close();
	}
	
}
