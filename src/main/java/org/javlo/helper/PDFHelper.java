package org.javlo.helper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.FSEntityResolver;

public class PDFHelper {

	public static Logger logger = Logger.getLogger(PDFHelper.class.getName());

	public static BufferedImage getPDFImage(File pdfFile, int page) {
		BufferedImage out = null;
		PDDocument doc = null;
		try {
			doc = PDDocument.load(pdfFile);
			PDFRenderer pdfRenderer = new PDFRenderer(doc);			
			PDPageTree pages = doc.getDocumentCatalog().getPages();			
			if (pages.getCount() > 0) {
				return pdfRenderer.renderImageWithDPI(page-1, 300, ImageType.RGB);				
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception when generating PDF thumbnail: " + e.getMessage(), e);
		} finally {
			ResourceHelper.safeClose(doc);
		}
		return out;
	}
	
	public static int getPDFPageSize(File pdfFile) {		
		PDDocument doc = null;
		try {
			doc = PDDocument.load(pdfFile);						
			return doc.getDocumentCatalog().getPages().getCount();			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception when generating PDF thumbnail: " + e.getMessage(), e);
		} finally {
			ResourceHelper.safeClose(doc);
		}
		return -1;
	}
	
	public static void main(String[] args) throws Exception {
        ITextRenderer renderer = new ITextRenderer();
        String content="<html><head><style>\n" +
          "div.header {\n" +
          "display: block; text-align: center;\n" + 
          "position: running(header);}\n" +
          "div.footer {\n" +
          "display: block; text-align: center;\n" + 
          "position: running(footer);}\n" +
          "div.content {page-break-after: always;}" +
          "@page { @top-center { content: element(header) }}\n " +
          "@page { @bottom-center { content: element(footer) }}\n" +
          "</style></head>\n" +
          "<body><div class='header'>Header</div><div class='footer'>Footer</div><div class='content'>Page1</div><div>Page2</div></body></html>";
        renderer.setDocumentFromString(content);
        renderer.layout();
        renderer.createPDF(new FileOutputStream("c:/trans/test.pdf"));
        
        
        java.net.HttpURLConnection con = (java.net.HttpURLConnection) new URL("http://localhost/sexy/resource/static/test.html").openConnection();			
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();	
		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		builder.setEntityResolver(FSEntityResolver.instance());
		Document doc = builder.parse(con.getInputStream());			
		org.xhtmlrenderer.pdf.ITextRenderer pdfRenderer = new org.xhtmlrenderer.pdf.ITextRenderer();
		pdfRenderer.setDocument(doc,null);
		pdfRenderer.layout();
		renderer.createPDF(new FileOutputStream("c:/trans/test_url.pdf"));

    }

}
