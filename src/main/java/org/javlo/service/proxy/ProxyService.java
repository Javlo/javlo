package org.javlo.service.proxy;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.javlo.actions.IAction;
import org.javlo.cache.HttpBean;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.service.RequestService;
import org.javlo.utils.TimeMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class ProxyService implements IAction {

    private static final TimeMap<String, HttpBean> SM_CACHE = new TimeMap<>(30);
    private static final TimeMap<String, HttpBean> LG_CACHE = new TimeMap<>(60 * 60);
    private static final TimeMap<String, HttpBean> XLG_CACHE = new TimeMap<>(60 * 60 * 24 * 32);

    protected static Logger logger = Logger.getLogger(ProxyService.class.getName());

    /** Extracts the logical key from path: /prefix/{key}/... -> key */
    private static String extractKey(ContentContext ctx) {
        String path = ctx.getRequest().getPathInfo();
        System.out.println("#### path = " + path);

        // Check for null or empty path
        if (path == null || path.isEmpty()) {
            return null;
        }

        // Trim leading/trailing '/'
        path = StringHelper.trim(path, '/');

        String[] segments = path.split("/");
        System.out.println("#### segments = " + StringHelper.arrayToString(segments, ","));

        // Expect at least prefix + key -> segments[1]
        if (segments.length > 1 && !segments[1].isEmpty()) {
            return segments[1];
        }
        return null;
    }

    /** Builds the remainder path after the first two segments: /prefix/key/{rest...} -> /{rest...} */
    private static String extractPath(ContentContext ctx) {
        String path = ctx.getRequest().getPathInfo();
        System.out.println("#### path = " + path);

        if (path == null || path.isEmpty()) {
            return "";
        }

        path = StringHelper.trim(path, '/');
        String[] segments = path.split("/");
        System.out.println("#### segments = " + java.util.Arrays.toString(segments));

        // Remove first two segments if possible
        if (segments.length > 2) {
            StringBuilder newPath = new StringBuilder();
            for (int i = 2; i < segments.length; i++) {
                newPath.append("/").append(segments[i]);
            }
            return newPath.toString();
        }
        return "";
    }

    public static String performNocache(ContentContext ctx, RequestService rs) throws ServletException, IOException {
        return proxy(ctx, rs, null);
    }

    public static String performSmcache(ContentContext ctx, RequestService rs) throws ServletException, IOException {
        return proxy(ctx, rs, SM_CACHE);
    }

    public static String performlgcache(ContentContext ctx, RequestService rs) throws ServletException, IOException {
        return proxy(ctx, rs, LG_CACHE);
    }

    public static String performxlgcache(ContentContext ctx, RequestService rs) throws ServletException, IOException {
        return proxy(ctx, rs, XLG_CACHE);
    }

    public static String proxy(ContentContext ctx, RequestService rs, TimeMap<String, HttpBean> cache) throws ServletException, IOException {

        // --- Resolve key ---
        String key = extractKey(ctx);
        if (key == null || key.length() <= 1) {
            logger.warning("No key specified in path");
            ctx.getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST, "No key specified in path");
            return "No key specified in path";
        }
        System.out.println("### proxy : " + key);

        // Basic hardening
        if (key.contains(".")) {
            throw new SecurityException("Invalid proxy key: " + key);
        }

        // --- URL assembly ---
        String uri = extractPath(ctx);
        String queryString = ctx.getRequest().getQueryString();

        Properties proxyMappings = ctx.getGlobalContext().getProxyMappings();
        String targetUrl = proxyMappings.getProperty(key);
        if (targetUrl == null) {
            logger.severe("No URL mapped for key: " + key);
            ctx.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND, "No URL mapped for key: " + key);
            return "No URL mapped for key: " + key;
        }
        targetUrl += uri;
        if (queryString != null && !queryString.isEmpty()) {
            targetUrl += "?" + queryString;
        }
        logger.info("proxy url : " + targetUrl);

        // --- Cache key ---
        String cacheKey = key + uri + (queryString != null ? "?" + queryString : "");

        // --- Try cache hit ---
        if (cache != null) {
            HttpBean cached = cache.get(cacheKey);
            if (cached != null) {
                logger.info("found in cache : " + cacheKey);

                // Set content type if present
                if (cached.getContentType() != null) {
                    ctx.getResponse().setContentType(cached.getContentType());
                }

                // Inline: apply cached headers to client, skipping hop-by-hop and sensitive ones
                for (Map.Entry<String, List<String>> e : cached.getHeaders().entrySet()) {
                    String header = e.getKey();
                    if (header == null) continue;
                    String lower = header.toLowerCase(Locale.ROOT);
                    if ("transfer-encoding".equals(lower)
                            || "content-length".equals(lower)
                            || "connection".equals(lower)
                            || "keep-alive".equals(lower)
                            || "proxy-authenticate".equals(lower)
                            || "proxy-authorization".equals(lower)
                            || "te".equals(lower)
                            || "trailer".equals(lower)
                            || "upgrade".equals(lower)
                            || "authorization".equals(lower)) {
                        continue;
                    }
                    for (String value : e.getValue()) {
                        ctx.getResponse().addHeader(header, value);
                    }
                }

                // Body as-is
                try (OutputStream out = ctx.getResponse().getOutputStream()) {
                    out.write(cached.getBody());
                }
                ctx.getResponse().flushBuffer();
                ctx.setStopRendering(true);
                return null;
            }
        }

        // --- Open upstream connection ---
        HttpURLConnection connection = (HttpURLConnection) new URL(targetUrl).openConnection();
        connection.setRequestMethod(ctx.getRequest().getMethod());
        connection.setInstanceFollowRedirects(false);
        connection.setDoInput(true);

        boolean hasRequestBody = "POST".equalsIgnoreCase(ctx.getRequest().getMethod())
                || "PUT".equalsIgnoreCase(ctx.getRequest().getMethod())
                || "PATCH".equalsIgnoreCase(ctx.getRequest().getMethod());
        connection.setDoOutput(hasRequestBody);

        // Forward incoming headers with filtering
        forwardRequestHeaders(ctx, connection);

        // Inject Authorization from mappings (Private App token / OAuth)
        String bearerToken = proxyMappings.getProperty(key + ".bearer-token");
        if (bearerToken != null && !bearerToken.isEmpty()) {
            // Overwrite any incoming Authorization with configured Bearer
            connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
        }

        // Mirror client's Accept-Encoding or default to gzip
        if (connection.getRequestProperty("Accept-Encoding") == null) {
            String clientAE = ctx.getRequest().getHeader("Accept-Encoding");
            connection.setRequestProperty("Accept-Encoding",
                    (clientAE != null && !clientAE.isEmpty()) ? clientAE : "gzip");
        }

        // --- Stream request body if needed ---
        if (hasRequestBody) {
            try (OutputStream out = connection.getOutputStream();
                 InputStream in = ctx.getRequest().getInputStream()) {
                in.transferTo(out);
            }
        }

        // --- Upstream response ---
        int status = connection.getResponseCode();
        ctx.getResponse().setStatus(status);

        InputStream rawIn = (status >= 400) ? connection.getErrorStream() : connection.getInputStream();
        if (rawIn == null) {
            ctx.getResponse().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No content from proxy");
            return null;
        }

        // Detect encodings
        String acceptEncoding = ctx.getRequest().getHeader("Accept-Encoding");
        boolean clientAcceptsGzip = acceptEncoding != null && acceptEncoding.toLowerCase(Locale.ROOT).contains("gzip");

        String upstreamContentEncoding = connection.getHeaderField("Content-Encoding");
        boolean upstreamIsGzip = upstreamContentEncoding != null && upstreamContentEncoding.equalsIgnoreCase("gzip");

        // Read body (decompress only if client cannot accept gzip)
        ByteArrayOutputStream contentBuffer = new ByteArrayOutputStream();
        try (InputStream actualIn = (upstreamIsGzip && !clientAcceptsGzip)
                ? new java.util.zip.GZIPInputStream(rawIn)
                : rawIn) {
            actualIn.transferTo(contentBuffer);
        }
        byte[] body = contentBuffer.toByteArray();

        // --- Headers map from upstream (filter hop-by-hop and sensitive) ---
        Map<String, List<String>> headers = collectUpstreamHeaders(connection);

        // If we decompressed for client, remove Content-Encoding
        if (upstreamIsGzip && !clientAcceptsGzip) {
            headers.remove("Content-Encoding");
        }

        // Ensure Vary contains Accept-Encoding
        ensureVaryAcceptEncoding(headers);

        // Determine content type (mapping override > upstream > default)
        String contentType = proxyMappings.getProperty(key + ".content-type");
        if (contentType == null) {
            contentType = (connection.getContentType() != null) ? connection.getContentType() : "application/octet-stream";
        }

        // --- Write to cache ---
        if (cache != null && body.length > 0) {
            cache.put(cacheKey, new HttpBean(body, contentType, headers));
        }

        // --- Send to client ---
        if (contentType != null) {
            ctx.getResponse().setContentType(contentType);
        }

        // Inline: apply upstream headers to client, skipping hop-by-hop and sensitive ones
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            String header = e.getKey();
            if (header == null) continue;
            String lower = header.toLowerCase(Locale.ROOT);
            if ("transfer-encoding".equals(lower)
                    || "content-length".equals(lower)
                    || "connection".equals(lower)
                    || "keep-alive".equals(lower)
                    || "proxy-authenticate".equals(lower)
                    || "proxy-authorization".equals(lower)
                    || "te".equals(lower)
                    || "trailer".equals(lower)
                    || "upgrade".equals(lower)
                    || "authorization".equals(lower)) {
                continue;
            }
            for (String value : e.getValue()) {
                ctx.getResponse().addHeader(header, value);
            }
        }

        try (OutputStream out = ctx.getResponse().getOutputStream()) {
            out.write(body);
        }

        ctx.getResponse().flushBuffer();
        ctx.setStopRendering(true);
        return null;
    }

    // ---------------------------------------
    // Helpers
    // ---------------------------------------

    /** Forward request headers from client to upstream, skipping hop-by-hop and sensitive ones. */
    private static void forwardRequestHeaders(ContentContext ctx, HttpURLConnection connection) {
        // Hop-by-hop headers to skip (RFC 7230)
        Set<String> skip = new HashSet<>(Arrays.asList(
                "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
                "te", "trailer", "transfer-encoding", "upgrade", "host", "content-length"
        ));

        Collections.list(ctx.getRequest().getHeaderNames()).forEach(headerName -> {
            String lower = headerName.toLowerCase(Locale.ROOT);
            if (skip.contains(lower)) {
                return;
            }
            // Forward all values for this header
            Collections.list(ctx.getRequest().getHeaders(headerName)).forEach(value ->
                    connection.addRequestProperty(headerName, value));
        });
    }

    /** Collect upstream headers, skipping hop-by-hop and sensitive ones. */
    private static Map<String, List<String>> collectUpstreamHeaders(HttpURLConnection connection) {
        Map<String, List<String>> headers = new HashMap<>();
        connection.getHeaderFields().forEach((header, values) -> {
            if (header == null) return; // skip status line

            String lower = header.toLowerCase(Locale.ROOT);
            if ("transfer-encoding".equals(lower)
                    || "content-length".equals(lower)
                    || "connection".equals(lower)
                    || "keep-alive".equals(lower)
                    || "proxy-authenticate".equals(lower)
                    || "proxy-authorization".equals(lower)
                    || "te".equals(lower)
                    || "trailer".equals(lower)
                    || "upgrade".equals(lower)
                    || "authorization".equals(lower)) {
                return; // skip hop-by-hop and sensitive
            }
            headers.put(header, new ArrayList<>(values));
        });
        return headers;
    }

    /** Ensure Vary contains Accept-Encoding (important when gzipping differs per client). */
    private static void ensureVaryAcceptEncoding(Map<String, List<String>> headers) {
        List<String> vary = headers.get("Vary");
        if (vary == null) {
            headers.put("Vary", new ArrayList<>(List.of("Accept-Encoding")));
            return;
        }
        boolean hasAE = false;
        for (String v : vary) {
            if (v != null && v.toLowerCase(Locale.ROOT).contains("accept-encoding")) {
                hasAE = true;
                break;
            }
        }
        if (!hasAE) {
            vary.add("Accept-Encoding");
        }
    }

    @Override
    public String getActionGroupName() {
        return "proxy";
    }

    @Override
    public boolean haveRight(ContentContext ctx, String action) {
        return true;
    }
}
