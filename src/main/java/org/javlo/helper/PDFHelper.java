package org.javlo.helper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

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
	
	public static List<BufferedImage> getPDFImages(File pdfFile) {
		List<BufferedImage> out = new LinkedList<BufferedImage>();
		PDDocument doc = null;
		try {
			doc = PDDocument.load(pdfFile);
			PDFRenderer pdfRenderer = new PDFRenderer(doc);			
			PDPageTree pages = doc.getDocumentCatalog().getPages();
			for (int p=1; p<=pages.getCount(); p++) {
				if (pages.getCount() > 0) {
					out.add(pdfRenderer.renderImageWithDPI(p, 300, ImageType.RGB));
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception when generating PDF thumbnail: " + e.getMessage(), e);
		} finally {
			ResourceHelper.safeClose(doc);
		}
		return out;
	}
	
	public static int getPDFPageSize(File pdfFile) {
		if (pdfFile == null || !pdfFile.isFile()) {
			return -1;
		}
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
	
	public static PDDocumentInformation getPdfMeta(File pdfFile) throws IOException {
		PDDocument doc = PDDocument.load(pdfFile);
		if (doc == null) {
			return null;
		} else {
			return doc.getDocumentInformation();
		}
	}
	
	public static String getPdfTitle(File pdfFile) throws IOException {
		PDDocumentInformation data = getPdfMeta(pdfFile);
		if (data == null) {
			return null;
		} else {
			return data.getTitle();
		}
	}
	
	public static void main(String[] args) throws Exception {
//        ITextRenderer renderer = new ITextRenderer();
//        String content="<html><head><style>\n" +
//          "div.header {\n" +
//          "display: block; text-align: center;\n" + 
//          "position: running(header);}\n" +
//          "div.footer {\n" +
//          "display: block; text-align: center;\n" + 
//          "position: running(footer);}\n" +
//          "div.content {page-break-after: always;}" +
//          "@page { @top-center { content: element(header) }}\n " +
//          "@page { @bottom-center { content: element(footer) }}\n" +
//          "</style></head>\n" +
//          "<body><div class='header'>Header</div><div class='footer'>Footer</div><div class='content'>Page1</div><div>Page2</div></body></html>";
//        renderer.setDocumentFromString(content);
//        renderer.layout();
//        renderer.createPDF(new FileOutputStream("c:/trans/test.pdf"));
//        
//        
//        java.net.HttpURLConnection con = (java.net.HttpURLConnection) new URL("http://localhost/sexy/resource/static/test.html").openConnection();			
//		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();	
//		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
//		builder.setEntityResolver(FSEntityResolver.instance());
//		Document doc = builder.parse(con.getInputStream());			
//		org.xhtmlrenderer.pdf.ITextRenderer pdfRenderer = new org.xhtmlrenderer.pdf.ITextRenderer();
//		pdfRenderer.setDocument(doc,null);
//		pdfRenderer.layout();
//		renderer.createPDF(new FileOutputStream("c:/trans/test_url.pdf"));
		File testPdf = new File("c:/trans/KCE_313B_Rapport_Performance_2019_Rapport FR.pdf");
		System.out.println("title = "+getPdfTitle(testPdf));
    }

}
