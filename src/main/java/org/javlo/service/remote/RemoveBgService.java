package org.javlo.service.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;

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
	
}
