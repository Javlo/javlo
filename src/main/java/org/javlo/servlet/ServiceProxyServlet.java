package org.javlo.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.utils.TimeMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class ServiceProxyServlet extends HttpServlet {

	private static final TimeMap<String, String> largeCache = new TimeMap<>(60*60*24*30);
	private static final TimeMap<String, String> smallCache = new TimeMap<>(60);

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

		if (key.contains(".")) {
			throw new SecurityException("Invalid proxy key: " + key);
		}

		// Build a stable, deterministic cache key and forward the request
		String targetUrl = proxyMappings.getProperty(key);
		if (targetUrl == null) {
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No URL mapped for key: " + key);
			return;
		}

// Append query string if present
		String queryString = req.getQueryString();
		if (queryString != null && !queryString.isEmpty()) {
			targetUrl += "?" + queryString;
		}

// Use StringBuilder to mutate inside lambdas
		StringBuilder mapKey = new StringBuilder();

// Include method and URL in the key
		mapKey.append(req.getMethod()).append('|').append(targetUrl).append('|');

// Open connection to target URL
		HttpURLConnection connection = (HttpURLConnection) new URL(targetUrl).openConnection();
		connection.setRequestMethod(req.getMethod());
		connection.setDoInput(true);
		connection.setDoOutput("POST".equalsIgnoreCase(req.getMethod()));

// Collect headers into a list, then sort for deterministic key generation
		List<Map.Entry<String, String>> headerEntries = new ArrayList<>();

		Collections.list(req.getHeaderNames()).forEach(headerName -> {
			// Optional: skip hop-by-hop headers that shouldn't be forwarded
			// (comment in English as requested)
			// e.g., "connection", "keep-alive", "transfer-encoding", "te", "trailer", "upgrade", "host", "content-length"
			String lower = headerName.toLowerCase(Locale.ROOT);
			if (lower.equals("connection") ||
					lower.equals("keep-alive") ||
					lower.equals("te") ||
					lower.equals("trailer") ||
					lower.equals("upgrade") ||
					lower.equals("content-length")) {
				return;
			}

			Collections.list(req.getHeaders(headerName)).forEach(value -> {
				headerEntries.add(new AbstractMap.SimpleEntry<>(headerName, value));
			});
		});

// Sort by header name (case-insensitive), then by value for stability
		headerEntries.sort(
				Comparator.comparing((Map.Entry<String, String> e) -> e.getKey().toLowerCase(Locale.ROOT))
						.thenComparing(Map.Entry::getValue, Comparator.nullsFirst(String::compareTo))
		);

// Apply headers to the outbound connection and extend the key
		for (Map.Entry<String, String> e : headerEntries) {
			connection.setRequestProperty(e.getKey(), e.getValue());
			mapKey.append(e.getKey()).append('=').append(e.getValue()).append('|');
		}

// À ce stade, mapKey.toString() est stable et correctement construit.
// ... suite du proxy (copie du corps, gestion de la réponse, etc.)


		String bearerToken = proxyMappings.getProperty(key+".bearer-token");
		if (bearerToken != null) {
			connection.setRequestProperty("Authorization: Bearer", bearerToken);
		}

		mapKey.append("Method:"+req.getMethod());

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

		if (StringHelper.isTrue(proxyMappings.getProperty(key+".large-cache"))) {
			String cache = largeCache.get(mapKey.toString());
			if (cache != null) {

			}
		}

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
