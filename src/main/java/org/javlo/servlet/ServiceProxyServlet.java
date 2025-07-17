package org.javlo.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.javlo.context.ContentContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Properties;

public class ServiceProxyServlet extends HttpServlet {


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		proxyRequest(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		proxyRequest(req, resp);
	}

	private void proxyRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String key = req.getPathInfo();
		if (key == null || key.length() <= 1) {
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No key specified in path");
			return;
		}

        ContentContext ctx = null;
        try {
            ctx = ContentContext.getContentContext(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }

        Properties proxyMappings = ctx.getGlobalContext().getProxyMappings();

		key = key.substring(1); // remove leading "/"
		String targetUrl = proxyMappings.getProperty(key);

		if (targetUrl == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No URL mapped for key: " + key);
			return;
		}

		// Append query string if present
		String queryString = req.getQueryString();
		if (queryString != null) {
			targetUrl += "?" + queryString;
		}

		// Open connection to target URL
		HttpURLConnection connection = (HttpURLConnection) new URL(targetUrl).openConnection();
		connection.setRequestMethod(req.getMethod());
		connection.setDoInput(true);
		connection.setDoOutput("POST".equalsIgnoreCase(req.getMethod()));

		// Copy headers from the original request
		Collections.list(req.getHeaderNames()).forEach(header -> {
			Collections.list(req.getHeaders(header)).forEach(value -> {
				connection.setRequestProperty(header, value);
			});
		});

		// Forward body if POST
		if ("POST".equalsIgnoreCase(req.getMethod())) {
			try (OutputStream out = connection.getOutputStream();
				 InputStream in = req.getInputStream()) {
				in.transferTo(out);
			}
		}

		// Copy response
		int status = connection.getResponseCode();
		resp.setStatus(status);

		connection.getHeaderFields().forEach((header, values) -> {
			if (header != null) {
				for (String value : values) {
					resp.addHeader(header, value);
				}
			}
		});

		try (InputStream in = connection.getInputStream();
			 OutputStream out = resp.getOutputStream()) {
			in.transferTo(out);
		} catch (IOException e) {
			// In case of error stream
			try (InputStream err = connection.getErrorStream()) {
				if (err != null) err.transferTo(resp.getOutputStream());
			}
		}
	}
}
