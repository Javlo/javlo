package org.javlo.service;

import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.javlo.helper.StringHelper;
import org.w3c.dom.Document;
import org.xhtmlrenderer.resource.FSEntityResolver;

public class PDFConvertion {

	private static Logger logger = Logger.getLogger(PDFConvertion.class.getName());

	private static final PDFConvertion instance = new PDFConvertion();

	public static PDFConvertion getInstance() {
		return instance;
	}

	public void convertXHTMLToPDF(String url, OutputStream out) throws com.lowagie.text.DocumentException {
		org.xhtmlrenderer.pdf.ITextRenderer pdfRenderer = new org.xhtmlrenderer.pdf.ITextRenderer();
		logger.info("create PDF : " + url);
		pdfRenderer.setDocument("" + url);
		pdfRenderer.layout();
		pdfRenderer.createPDF(out);
	}

	public void convertXHTMLToPDF(URL url, final String userName, final String password, OutputStream out) {
		
		logger.info("create PDF from : "+url+"  user:"+userName+"  password found:"+(StringHelper.neverNull(password).length()>1));

		if (null != userName && userName.trim().length() != 0

		&& null != password && password.trim().length() != 0)

		{

			java.net.Authenticator.setDefault(new java.net.Authenticator() {

				protected java.net.PasswordAuthentication getPasswordAuthentication() {

					return new java.net.PasswordAuthentication(userName, password.toCharArray());

				}

			});

		}

		try {

			java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
			
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			builder.setEntityResolver(FSEntityResolver.instance());
			Document doc = builder.parse(con.getInputStream());			
			org.xhtmlrenderer.pdf.ITextRenderer pdfRenderer = new org.xhtmlrenderer.pdf.ITextRenderer();
			pdfRenderer.setDocument(doc,null);
			pdfRenderer.layout();
			pdfRenderer.createPDF(out);
		} catch (Exception e1) {
			e1.printStackTrace();

		}
		

	}

}
