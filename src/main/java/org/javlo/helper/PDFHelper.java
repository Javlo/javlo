package org.javlo.helper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
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

}
