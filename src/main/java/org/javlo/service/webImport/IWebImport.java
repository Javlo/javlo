package org.javlo.service.webImport;

import org.javlo.context.ContentContext;

import java.io.IOException;
import java.util.List;

public interface IWebImport {

    public String getName();

    public List<String> extractUrls(ContentContext ctx, String url);

    public SimpleContentBean importContent(ContentContext ctx, String url) throws IOException;

}
