package org.javlo.service.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;

public class RemoteServiceProxyService {
	
	private static final String KEY = RemoteServiceProxyService.class.getCanonicalName();
	
	private String proxyUrl = "http://localhost:9090/usp/";

	public static RemoteServiceProxyService getInstance(GlobalContext globalContext) {
		
		if (globalContext == null)  {
			return new RemoteServiceProxyService();
		}
		
		RemoteServiceProxyService out = (RemoteServiceProxyService) globalContext.getAttribute(KEY);
		if (out == null) {
			out = new RemoteServiceProxyService();
			out.setProxyUrl(globalContext.getSpecialConfig().get("usp.url", null));
			globalContext.setAttribute(KEY, out);
		}
		return out;
	}

	public String getProxyUrl() {
		return proxyUrl;
	}

	public void setProxyUrl(String proxyUrl) {
		this.proxyUrl = proxyUrl;
	}
	
	public void executeRequest(String inUrl, Map<String,String> header, String method, InputStream body, OutputStream out) throws IOException {
		URL url = new URL(inUrl);
		String host = url.getHost();
		String proxyUrl = getProxyUrl()+host;
		
		proxyUrl = proxyUrl+url.getPath();
		if (url.getQuery() != null && url.getQuery().length() > 0) {
			proxyUrl += "?"+url.getQuery();
		}
		
		ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
		ResourceHelper.writeStreamToStream(body, outBytes);
		String hash = ResourceHelper.sha512(new ByteArrayInputStream(outBytes.toByteArray()));
		
		System.out.println(">>>>>>>>> RemoteServiceProxyService.executeRequest : proxyUrl = "+proxyUrl); //TODO: remove debug trace
		
		HttpURLConnection connection = (HttpURLConnection) new URL(proxyUrl).openConnection();
		connection.setRequestMethod(method);
		for (Map.Entry<String, String> entry : header.entrySet()) {
			connection.setRequestProperty(entry.getKey(), entry.getValue());
		}
		
		connection.setUseCaches(true);
		connection.connect();
		
		if (connection.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED) {
			System.out.println(">>>>>>>>> RemoteServiceProxyService.executeRequest : FOUND IN PROXY CACHE."); //TODO: remove debug trace
		} else {
			try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
				HttpPost httpPost = new HttpPost(proxyUrl);
				httpPost.addHeader("_USP_HASH", hash);
				for (Map.Entry<String, String> entry : header.entrySet()) {
					//httpPost.addHeader(entry.getKey(), entry.getValue());
				}
				HttpEntity httpEntiry = MultipartEntityBuilder.create().addBinaryBody("image_file", new ByteArrayInputStream(outBytes.toByteArray())) .addTextBody("size", "auto").build();
				outBytes = null;
				httpPost.setEntity(httpEntiry);
				
				try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
					HttpEntity entity = response.getEntity();
					entity.writeTo(out);
				}
			}
		}
	}

}