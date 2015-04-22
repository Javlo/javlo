package org.javlo.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.utils.TimeMap;

public class ProxyServlet extends HttpServlet {
	
	private static String TEST_URL = "http://upload.wikimedia.org/wikipedia/commons/5/5a/Wikipedia-logo-v2-fr.png";
	
	private static Map<String,URL> urls = Collections.synchronizedMap(new TimeMap<String, URL>(60*60));  
	private static Map<URL,String> keys = Collections.synchronizedMap(new TimeMap<URL,String>(70*60));

	private static final long serialVersionUID = 1L;
	
	public synchronized static String getURLCode(URL url) {
		if (urls.containsKey(url)) {
			return keys.get(url);
		} else {
			String id = StringHelper.getRandomId();
			urls.put(id, url);
			keys.put(url, id);
			return id;
		}		
	}	

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getRequestURI();
		GlobalContext globalContext = GlobalContext.getInstance(request);
		if (path.startsWith('/' + globalContext.getContextKey())) {
			path = path.replaceFirst('/' + globalContext.getContextKey(), "");
		}
		if (path.contains("/")) {
			String key = StringUtils.split(path, '/')[1];
			URL url;			
			if (key.equals("test")) {
				url = new URL(TEST_URL);
			} else {
				url = urls.get(key);
				if (url == null) {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					return;
				} 
			}
			InputStream in = url.openStream();
			try {
				ResourceHelper.writeStreamToStream(in, response.getOutputStream());
			} finally {
				ResourceHelper.closeResource(in);
			}
		}
	}
}
