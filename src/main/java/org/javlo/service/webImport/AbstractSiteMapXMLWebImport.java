package org.javlo.service.webImport;

import org.javlo.context.ContentContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSiteMapXMLWebImport implements IWebImport  {

    public List<String> extractUrls(ContentContext ctx, String inUrl) {
        List<String> urls = new ArrayList<>();
        try {
            // Create a URL object from the sitemap URL
            URL url = new URL(inUrl);
            // Open a connection and get the input stream
            InputStream inputStream = url.openStream();

            // Set up the XML document parser
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            // Parse the input stream and get the document
            Document doc = dBuilder.parse(inputStream);

            // Normalize the XML structure
            doc.getDocumentElement().normalize();

            // Get all "loc" elements (URLs are stored in "loc" tags within the sitemap)
            NodeList nList = doc.getElementsByTagName("loc");

            for (int i = 0; i < nList.getLength(); i++) {
                Element element = (Element) nList.item(i);
                // Add the text content of each "loc" element to the list
                urls.add(element.getTextContent());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return urls;
    }


}
