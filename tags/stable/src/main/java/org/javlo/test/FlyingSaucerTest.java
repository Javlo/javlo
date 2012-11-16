package org.javlo.test;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.util.FSImageWriter;

public class FlyingSaucerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String address = "http://localhost:8080/ensemble/fr/presse.html?force-template=president_newsletter";
		// render
		try {
			// Flying Saucer Release R8 User's Guide Page 17 of 27
			Java2DRenderer renderer = new Java2DRenderer(address, 1024);
			BufferedImage img = renderer.getImage();
			FSImageWriter imageWriter = new FSImageWriter();
			imageWriter.write(img, "d:/trans/flyingSaucer.png");

			String url = address;
			String outputFile = "d:/trans/firstdoc.pdf";
			OutputStream os = new FileOutputStream(outputFile);

			ITextRenderer pdfRenderer = new ITextRenderer();
			pdfRenderer.setDocument(url);
			pdfRenderer.layout();
			pdfRenderer.createPDF(os);

			os.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
