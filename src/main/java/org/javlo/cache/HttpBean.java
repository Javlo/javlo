package org.javlo.cache;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class HttpBean implements Serializable {
    private static final long serialVersionUID = 1L;

    // Store raw body; gzip (if any) is indicated by headers (Content-Encoding: gzip)
    private final byte[] body;
    private final String contentType;
    private final Map<String, List<String>> headers;

    // Preferred constructor: raw body + contentType + headers
    public HttpBean(byte[] body, String contentType, Map<String, List<String>> headers) {
        this.body = body;
        this.contentType = contentType;
        this.headers = headers;
    }

    /** Return raw body (could be gzipped if headers contain Content-Encoding: gzip) */
    public byte[] getBody() {
        return body;
    }

    public String getContentType() {
        return contentType;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    // Backward compatibility: old callers used getGzippedContent().
    // Now it simply returns the raw body.
    @Deprecated
    public byte[] getGzippedContent() {
        return body;
    }
}
