package org.javlo.cache;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class HttpBean implements Serializable {
    private final byte[] gzippedContent;
    private final String contentType;
    private final Map<String, List<String>> headers;

    public HttpBean(byte[] gzippedContent, String contentType, Map<String, List<String>> headers) {
        this.gzippedContent = gzippedContent;
        this.contentType = contentType;
        this.headers = headers;
    }

    public byte[] getGzippedContent() {
        return gzippedContent;
    }

    public String getContentType() {
        return contentType;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }
}

