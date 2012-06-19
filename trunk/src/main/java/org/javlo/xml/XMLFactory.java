package org.javlo.xml;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import java.io.File;
import org.xml.sax.InputSource;

/**
 *
 * @author  pvandermasen
 * @version 1.3
 */

public class XMLFactory {
    
    /**
     * get the root node from an input stream (grh)
     */
    public static NodeXML getFirstNode(InputStream isXML) throws Exception   {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            docBuilderFactory.setNamespaceAware(true);
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();            
            Document doc = docBuilder.parse( new InputSource ( isXML ) );
            return new NodeXML(doc);
            
            /*return new NodeXML(doc.getDocumentElement());*/
        } catch (ParserConfigurationException e) {
            throw new Exception("cannot get DocumentBuilder : " + e.getMessage());
        } catch (SAXException e) {
            e.printStackTrace();
            throw new Exception("cannot parse XML : " + e.getMessage());
        } finally {
            try {
                isXML.close();
            } catch (Exception e) {
            }
        }
    }
    
    /**
     * get the root node from a file (grh)
     */
    public static NodeXML getFirstNode(File xmlFile) throws Exception   {
        InputStream stream = null;
        try {
            stream = new FileInputStream( xmlFile );
            NodeXML res = getFirstNode( stream );
            return res;
        } catch (ParserConfigurationException e) {
            throw new Exception("cannot get DocumentBuilder : " + e.getMessage());
        } catch (SAXException e) {
            throw new Exception("cannot parse XML : " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("error reading XML file : " + e.getMessage());
        } finally {
            stream.close();
        }
    }
    
    /**
     * get the root node from an URL (grh)
     */
    public static NodeXML getFirstNode(URL myURL) throws Exception   {
        InputStream stream = null;
        try {
            stream = myURL.openStream();
            return getFirstNode( stream );
        } catch (ParserConfigurationException e) {
            throw new Exception("cannot get DocumentBuilder : " + e.getMessage());
        } catch (SAXException e) {
            throw new Exception("cannot parse XML : " + e.getMessage());
        } catch (IOException e) {
            throw new Exception("error reading XML from URL : " + e.getMessage());
        } finally {
            stream.close();
        }
    }
}
