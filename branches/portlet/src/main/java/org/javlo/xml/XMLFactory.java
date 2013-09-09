package org.javlo.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.javlo.helper.ResourceHelper;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * @author pvandermasen
 * @version 1.3
 */

public class XMLFactory {

	/**
	 * get the root node from an input stream (grh)
	 */
	public static NodeXML getFirstNode(InputStream inXML) throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		// docBuilderFactory.setNamespaceAware(false);
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(new InputSource(inXML));
		return new NodeXML(doc);
	}
	
	/**
	 * get the root node from an input stream (pvdm)
	 */
	public static NodeXML getFirstNode(Reader inXML) throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		// docBuilderFactory.setNamespaceAware(false);
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(new InputSource(inXML));
		return new NodeXML(doc);
	}

	/**
	 * get the root node from a file (grh)
	 */
	public static NodeXML getFirstNode(File xmlFile) throws Exception {
		InputStream stream = null;
		try {
			stream = new FileInputStream(xmlFile);
			return getFirstNode(stream);
		} catch (ParserConfigurationException e) {
			throw new Exception("cannot get DocumentBuilder : " + e.getMessage());
		} catch (SAXException e) {
			throw new Exception("cannot parse XML : " + e.getMessage());
		} catch (Exception e) {
			throw new Exception("error reading XML file : " + e.getMessage());
		} finally {
			ResourceHelper.closeResource(stream);
		}
	}

	/**
	 * get the root node from an URL (grh)
	 */
	public static NodeXML getFirstNode(URL myURL) throws Exception {
		InputStream stream = null;
		try {
			stream = myURL.openStream();
			return getFirstNode(stream);
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

	public static void main(String[] args) {

		String xml = "<body><jahia:page xmlns:jahia=\"http://www.jahia.org/\" xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" xmlns:intrasg=\"http://www.jahia.org/intrasg\" xmlns:intrasgnt=\"http://www.jahia.org/intrasg/nt\" jahia:template=\"Article\" jahia:pageKey=\"2012RA18HemicycleClosure\" jahia:title=\"Temporary closure of the Hemicycle area in Brussels\" jcr:uuid=\"fee8e6d6-e5fc-4414-bf0d-8bcc1e20672d\" jahia:lastPublisher=\"srogowski\" jahia:lastPublishingDate=\"2012-09-19T10:18:00\" jahia:lastContributor=\"srogowski\" jcr:lastModified=\"2012-09-19T10:16:59\" jahia:creator=\"srogowski\" jcr:created=\"2012-09-19T09:44:20\" jahia:acl=\"g:guest:r--|g:webmaster:rwa\" jahia:pid=\"327\">";

		xml = xml + "<intrasg:subTitle jcr:primaryType=\"intrasgnt:contentTitle\" jahia:value=\"The Secretary General discusses measures taken with the Crisis Management Team\" jcr:uuid=\"a4d4e65b-e6ab-42c0-89fa-782037ee4b94\"></intrasg:subTitle>";

		xml = xml + "</jahia:page></body>";

		String xml2 = "<body><page>";
		xml2 = xml2 + "<subTitle value=\"The Secretary General discusses measures taken with the Crisis Management Team\" uuid=\"a4d4e65b-e6ab-42c0-89fa-782037ee4b94\"></subTitle>";
		xml2 = xml2 + "<subTitle value=\"node1\" uuid=\"a4d4e65b-e6ab-42c0-89fa-782037ee4b94\">coucou</subTitle>";
		xml2 = xml2 + "</page></body>";

		try {
			NodeXML node = getFirstNode(new ByteArrayInputStream(xml2.getBytes()));
			System.out.println("***** XMLFactory.main : subTitle size : " + node.searchChildren("//subTitle[@*]").size()); // TODO: remove debug trace

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
