package org.javlo.helper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

public class PDFHelper {

	public static Logger logger = Logger.getLogger(PDFHelper.class.getName());

	public static BufferedImage getPDFImage(File pdfFile) {
		BufferedImage out = null;
		PDDocument doc = null;
		try {
			doc = PDDocument.load(pdfFile);
			@SuppressWarnings("unchecked")
			List<PDPage> pages = doc.getDocumentCatalog().getAllPages();
			if (pages.size() > 0) {
				out = pages.get(0).convertToImage(BufferedImage.TYPE_INT_ARGB, 300);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception when generating PDF thumbnail: " + e.getMessage(), e);
		} finally {
			ResourceHelper.safeClose(doc);
		}
		return out;
	}

}
