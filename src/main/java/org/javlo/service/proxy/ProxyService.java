package org.javlo.service.proxy;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.javlo.actions.IAction;
import org.javlo.cache.HttpBean;
import org.javlo.context.ContentContext;
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
import java.util.zip.GZIPOutputStream;

public class ProxyService implements IAction {

    private static final TimeMap<String, HttpBean> SM_CACHE = new TimeMap<String, HttpBean>(30);
    private static final TimeMap<String, HttpBean> LG_CACHE = new TimeMap<String, HttpBean>(60*60);

    protected static Logger logger = Logger.getLogger(ProxyService.class.getName());

    private static String extraceKey(ContentContext ctx) {
        String path = ctx.getRequest().getPathInfo();
        String[] segments = path.split("/");

        // Find the last segment that is not empty
        String result = "";
        for (int i = segments.length - 1; i >= 0; i--) {
            if (!segments[i].isEmpty()) {
                result = segments[i];
                break;
            }
        }
        return result;
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

    public static String proxy(ContentContext ctx, RequestService rs, TimeMap<String, HttpBean> cache) throws ServletException, IOException {

        String key = extraceKey(ctx);
        if (key == null || key.length() <= 1) {
            ctx.getResponse().sendError(HttpServletResponse.SC_BAD_REQUEST, "No key specified in path");
            return "No key specified in path";
        }

        String queryString = ctx.getRequest().getQueryString();
        String cacheKey = key + (queryString != null ? "?" + queryString : "");

        // === CACHE ===
        if (cache != null) {
            HttpBean cached = cache.get(cacheKey);
            if (cached != null) {
                ctx.getResponse().setContentType(cached.getContentType());
                ctx.getResponse().setHeader("Content-Encoding", "gzip");
                ctx.getResponse().setHeader("Vary", "Accept-Encoding");

                for (Map.Entry<String, List<String>> entry : cached.getHeaders().entrySet()) {
                    String header = entry.getKey();
                    if (header != null &&
                            !"Content-Encoding".equalsIgnoreCase(header) &&
                            !"Transfer-Encoding".equalsIgnoreCase(header) &&
                            !"Content-Length".equalsIgnoreCase(header)) {
                        for (String value : entry.getValue()) {
                            ctx.getResponse().addHeader(header, value);
                        }
                    }
                }

                try (OutputStream out = ctx.getResponse().getOutputStream()) {
                    out.write(cached.getGzippedContent());
                }

                ctx.getResponse().flushBuffer();
                ctx.setStopRendering(true);
                return null;
            }
        }

        // === Proxy vers la source ===
        Properties proxyMappings = ctx.getGlobalContext().getProxyMappings();
        String targetUrl = proxyMappings.getProperty(key);
        if (targetUrl == null) {
            ctx.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND, "No URL mapped for key: " + key);
            return "No URL mapped for key: " + key;
        }
        if (queryString != null) {
            targetUrl += "?" + queryString;
        }

        logger.info("proxy url : "+targetUrl);

        HttpURLConnection connection = (HttpURLConnection) new URL(targetUrl).openConnection();
        connection.setRequestMethod(ctx.getRequest().getMethod());
        connection.setDoInput(true);
        connection.setDoOutput("POST".equalsIgnoreCase(ctx.getRequest().getMethod()));

        Collections.list(ctx.getRequest().getHeaderNames()).forEach(header ->
                Collections.list(ctx.getRequest().getHeaders(header)).forEach(value ->
                        connection.addRequestProperty(header, value)));

        if ("POST".equalsIgnoreCase(ctx.getRequest().getMethod())) {
            try (OutputStream out = connection.getOutputStream();
                 InputStream in = ctx.getRequest().getInputStream()) {
                in.transferTo(out);
            }
        }

        int status = connection.getResponseCode();
        ctx.getResponse().setStatus(status);

        InputStream in = (status >= 400) ? connection.getErrorStream() : connection.getInputStream();
        if (in == null) {
            ctx.getResponse().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No content from proxy");
            return null;
        }

        InputStream actualIn = "gzip".equalsIgnoreCase(connection.getHeaderField("Content-Encoding")) ? new java.util.zip.GZIPInputStream(in) : in;

        // Capture dans un buffer
        ByteArrayOutputStream contentBuffer = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(contentBuffer)) {
            actualIn.transferTo(gzipOut);
            gzipOut.finish();
        }

        byte[] gzippedData = contentBuffer.toByteArray();

        // Headers
        Map<String, List<String>> headers = new HashMap<>();
        connection.getHeaderFields().forEach((header, values) -> {
            if (header != null &&
                    !"Content-Encoding".equalsIgnoreCase(header) &&
                    !"Transfer-Encoding".equalsIgnoreCase(header) &&
                    !"Content-Length".equalsIgnoreCase(header)) {
                headers.put(header, new ArrayList<>(values));
            }
        });

        String contentType = proxyMappings.getProperty(key + ".content-type");
        if (contentType == null) {
            contentType = connection.getContentType() != null ? connection.getContentType() : "application/octet-stream";
        }

        // Enregistrement dans le cache
        if (cache != null) {
            cache.put(cacheKey, new HttpBean(gzippedData, contentType, headers));
        }

        // Envoi au client
        ctx.getResponse().setContentType(contentType);
        ctx.getResponse().setHeader("Content-Encoding", "gzip");
        ctx.getResponse().setHeader("Vary", "Accept-Encoding");
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            for (String value : entry.getValue()) {
                ctx.getResponse().addHeader(entry.getKey(), value);
            }
        }

        try (OutputStream out = ctx.getResponse().getOutputStream()) {
            out.write(gzippedData);
        }

        ctx.getResponse().flushBuffer();
        ctx.setStopRendering(true);
        return null;
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
