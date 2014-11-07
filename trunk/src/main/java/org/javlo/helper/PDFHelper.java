package org.javlo.helper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

public class PDFHelper {

	public static BufferedImage getPDFImage(File pdfFile) throws IOException {
		PDDocument doc = PDDocument.load(pdfFile);
		BufferedImage out = null;
		try {
			@SuppressWarnings("unchecked")
			List<PDPage> pages = doc.getDocumentCatalog().getAllPages();
			if (pages.size() > 0) {
				out = pages.get(0).convertToImage(BufferedImage.TYPE_INT_ARGB, 300);
			}
		} finally {
			if (doc != null) {
				doc.close();
			}
		}
		return out;
	}

}
