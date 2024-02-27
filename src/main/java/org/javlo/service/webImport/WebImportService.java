package org.javlo.service.webImport;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.navigation.MenuElement;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class WebImportService {

    Logger logger = Logger.getLogger(WebImportService.class.getName());

    private static List<IWebImport> webImports = Arrays.asList(new ImportFlexo());


    private static final String KEY = "WebImportService";

    public static WebImportService getInstance(GlobalContext globalContext) {
        if (globalContext == null) {
            return new WebImportService();
        }
        WebImportService out = (WebImportService) globalContext.getAttribute(KEY);
        if (out == null) {
            out = new WebImportService();
            globalContext.setAttribute(KEY, out);
        }
        return out;
    }

    public static IWebImport getWebImport(String importEngine) {
        for (IWebImport webImport : webImports) {
            if (webImport.getName().equals(importEngine)) {
                return webImport;
            }
        }
        return null;
    }

    public void ImportContentAsChildren(ContentContext ctx, String importEngine, MenuElement parent, String url) {
        IWebImport webImport = getWebImport(importEngine);
        if (webImport != null) {
            List<String> urls = webImport.extractUrls(ctx, url);
            if (urls == null || urls.size() == 0) {
                logger.warning("No urls found for " + url);
            } else {
                for (String u : urls) {
                    try {
                        SimpleContentBean content = webImport.importContent(ctx, u);
                        if (content != null) {
                            System.out.println("title : " + content.getTitle());
                            System.out.println("date : " + content.getDate());
                            System.out.println("img : " + content.getImage());
                            System.out.println("#content : " + content.getContent().length());
                            System.out.println("");
                        }
                    } catch (Exception e) {
                        logger.warning("Error during import of " + u + " : " + e.getMessage());
                    }
                }
            }
        } else {
            logger.warning("No import engine found for " + importEngine);
        }
    }

    public static void main(String[] args) {
        WebImportService webImportService = getInstance(null);
        webImportService.ImportContentAsChildren(null,"flexo", null, "https://www.portdeliege.be/sitemap-news-fr.xml");
    }
}
