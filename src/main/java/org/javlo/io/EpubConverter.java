package org.javlo.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;

import coza.opencollab.epub.creator.model.EpubBook;
import fr.opensagres.xdocreport.core.io.internal.ByteArrayOutputStream;

public class EpubConverter {

	private static Logger logger = Logger.getLogger(EpubConverter.class.getName());

	public static void convertPDFtoEPub(File in, File out) throws Exception {
		try (FileInputStream inStream = new FileInputStream(in); FileOutputStream outStream = new FileOutputStream(out);) {
			convertPDFtoEPub(inStream, outStream);
		}
	}

	public static void convertPDFtoEPub(InputStream in, OutputStream out) throws Exception {
		EpubBook book = new EpubBook("en", "ExportPDF", "ExportPDF", "");
		book.setAutoToc(false);
		PDDocument doc = null;
		try {
			doc = PDDocument.load(in);
			PDFRenderer pdfRenderer = new PDFRenderer(doc);
			PDPageTree pages = doc.getDocumentCatalog().getPages();
			int pageCount = pages.getCount();
			
			
			String css = "body {margin: 0; padding: 0} "
					+ "img {height: auto; width: auto; display: block;} "
					+ "@page { margin-bottom: 5pt; margin-top: 5pt}"
					+ "img {"
					+ "    page-break-before: auto;"
					+ "    page-break-after: auto;"
					+ "    page-break-inside: avoid;"
					+ "}";
			
			String xhtml = "<html><head><style>"+css+"</style></head><body>";
			if (pageCount > 0) {
				for (int p = 0; p < pageCount; p++) {
					ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
					ImageIO.write(pdfRenderer.renderImageWithDPI(p, 150, ImageType.RGB), "jpg", outBytes);
					String number = StringHelper.renderNumber(p+1, (""+pageCount).length());
					String outFileName = "img/image_" + number + ".jpg";
					
					if (p==0) {
						book.addCoverImage(outBytes.toByteArray(), "jpg", "img/cover.jpg");
					}
					
					xhtml+="<img src='../" + outFileName + "' />";
					
					System.out.println("create : " + outFileName);
					book.addContent(new ByteArrayInputStream(outBytes.toByteArray()), "image/jpg", outFileName, false, false);
				}
				xhtml += "</body></html>";
				book.addContent(xhtml.getBytes(), "pdf", "application/xhtml+xml", false, true);
			}
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception when generating PDF thumbnail: " + e.getMessage(), e);
		} finally {
			ResourceHelper.safeClose(doc);
		}
		book.writeToStream(out);
	}

	public static void main(String[] args) throws Exception {
		// File file = new File("c:/trans/test_javlo.epub");
		//
		// EpubBook book = new EpubBook("en", "Samuel .-__Id1", "Patrick Vandermaesen
		// test", "Patrick Vandermaesen ");
		//
		// book.addTextContent("TestHtml", "xhtml/test1.xhtml", "Coucou c'est
		// Javlo").setToc(true);
		// book.addTextContent("TestHtml", "xhtml/img.xhtml", "Coucou, c'est Patrick
		// <img src='../img/short_hair.png' alt='short air' />").setToc(true);
		//
		// book.addContent(new FileInputStream(new File("c:/trans/short_hair.png")),
		// "image/png", "img/short_hair.png", false, false);
		//
		//
		// book.writeToStream(new FileOutputStream(file));

		convertPDFtoEPub(new File("c:/trans/source7.pdf"), new File("c:/trans/target7_false_true_one_page_notoc2_150_pb.epub"));
	}

}
