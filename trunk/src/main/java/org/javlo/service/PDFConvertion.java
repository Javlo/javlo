package org.javlo.service;

import java.io.OutputStream;
import java.util.logging.Logger;

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
	
	
}
