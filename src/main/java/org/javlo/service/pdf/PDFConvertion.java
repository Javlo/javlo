package org.javlo.service.pdf;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.javlo.context.ContentContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.w3c.dom.Document;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.JustificationInfo;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.resource.FSEntityResolver;

public class PDFConvertion {

	private static Logger logger = Logger.getLogger(PDFConvertion.class.getName());

	private static final PDFConvertion instance = new PDFConvertion();

	public static PDFConvertion getInstance() {
		return instance;
	}

	public void convertXHTMLToPDF(String url, OutputStream out) throws Exception {
		org.xhtmlrenderer.pdf.ITextRenderer pdfRenderer = new org.xhtmlrenderer.pdf.ITextRenderer();
		logger.info("create PDF : " + url);
		pdfRenderer.setDocument("" + url);
		pdfRenderer.layout();
		pdfRenderer.createPDF(out);
	}

	public static void convertXHTMLToPDF(URL url, final String userName, final String password, OutputStream out) {

		logger.info("create PDF from : " + url + "  user:" + userName + "  password found:" + (StringHelper.neverNull(password).length() > 1));

		if (null != userName && userName.trim().length() != 0 && null != password && password.trim().length() != 0) {

			java.net.Authenticator.setDefault(new java.net.Authenticator() {

				protected java.net.PasswordAuthentication getPasswordAuthentication() {

					return new java.net.PasswordAuthentication(userName, password.toCharArray());

				}

			});

		}

		try {
			String html = NetHelper.readPageGet(url);
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			builder.setEntityResolver(FSEntityResolver.instance());
			org.xhtmlrenderer.pdf.ITextRenderer pdfRenderer = new org.xhtmlrenderer.pdf.ITextRenderer();
			
			SharedContext sharedContext = pdfRenderer.getSharedContext();
			sharedContext.setPrint(true);
			sharedContext.setInteractive(false);
			sharedContext.setReplacedElementFactory(new ImageReplacedElementFactory(null));
			
			W3CDom w3cDom = new W3CDom();
			org.w3c.dom.Document doc = w3cDom.fromJsoup(Jsoup.parse(html));
			
			pdfRenderer.setDocument(doc, null);
			pdfRenderer.layout();
			pdfRenderer.createPDF(out);
		} catch (Exception e1) {
			e1.printStackTrace();

		}
	}

	public static void convertXHTMLToPDF(InputStream in, OutputStream out) {

		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
			builder.setEntityResolver(FSEntityResolver.instance());
			Document doc = builder.parse(in);
			org.xhtmlrenderer.pdf.ITextRenderer pdfRenderer = new org.xhtmlrenderer.pdf.ITextRenderer();
			
			SharedContext sharedContext = pdfRenderer.getSharedContext();
			sharedContext.setPrint(true);
			sharedContext.setInteractive(false);
			sharedContext.setReplacedElementFactory(new ImageReplacedElementFactory(null));
			pdfRenderer.setDocument(doc, null);
			pdfRenderer.layout();
			// LayoutContext layoutContext =
			// pdfRenderer.getSharedContext().newLayoutContextInstance();
			// BlockBox rootBox = pdfRenderer.getRootBox();
			// correctAllLines(layoutContext, rootBox);
			pdfRenderer.createPDF(out);
		} catch (Exception e1) {
			e1.printStackTrace();

		}

	}

	private static void correctAllLines(LayoutContext layout, Object box) {
		if (box != null) {
			if (box instanceof BlockBox) {
				for (Object child : ((BlockBox) box).getChildren()) {
					correctAllLines(layout, child);
				}
			} else if (box instanceof LineBox) {
				((LineBox) box).trimTrailingSpace(layout);
				InlineText text = ((LineBox) box).findTrailingText();
				/*
				 * text.getParent().isStartsHere() : for correct unalignement with link
				 */
				if (text != null && text.getParent() != null && text.getParent().getLineBox() != null && !text.getParent().isStartsHere()) {
					JustificationInfo info = text.getParent().getLineBox().getJustificationInfo();
					if (info != null) {
						text.getParent().getLineBox().align(true);
					}
				}
			}
		}
	}
	
//	public static void main(String[] args) throws IOException {
//		org.jsoup.nodes.Document doc = Jsoup.parse(new File("c:/trans/mep_ep.xml"), "UTF-8");
//		Elements ids = doc.getElementsByTag("id");
//		Iterator<Element> ite = ids.iterator();		
//		PrintStream out = new PrintStream(new FileOutputStream(new File("c:/trans/meps_id.txt")));
//		while(ite.hasNext()) {
//			out.println(ite.next().text());
//		}
//		out.close();				
//	}

	public static void main(String[] args) throws Exception {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
		builder.setEntityResolver(FSEntityResolver.instance());

		String html = ResourceHelper.getFileContent(new File("c:/trans/test_pdf.html"));

		InputStream in = new ByteArrayInputStream(html.getBytes(ContentContext.CHARACTER_ENCODING));

		W3CDom w3cDom = new W3CDom();
		org.w3c.dom.Document w3cDoc = w3cDom.fromJsoup(Jsoup.parse(html));
		
		
		org.xhtmlrenderer.pdf.ITextRenderer pdfRenderer = new org.xhtmlrenderer.pdf.ITextRenderer();
		pdfRenderer.setDocument(w3cDoc, "");
//		pdfRenderer.setDocument(doc, null);
		pdfRenderer.layout();
		// LayoutContext layoutContext =
		// pdfRenderer.getSharedContext().newLayoutContextInstance();
		// BlockBox rootBox = pdfRenderer.getRootBox();
		// correctAllLines(layoutContext, rootBox);
		pdfRenderer.createPDF(new FileOutputStream(new File("c:/trans/test_pdf.pdf")));
		
		in.close();
	}

	/*
	 * public static void main(String[] args) throws Exception { URL url = new URL(
	 * "http://localhost/javlo/mailing/en/data/anna/anna-16/anna-16-june/test-implementation-of-model/test-implementation-of-model-composition.html?nodmz=true&j_token=y7kvR6c5V0g-&force-device-code=pdf&_clear_session=true&clean-html=true&_absolute-url=true"
	 * ); com.itextpdf.text.Document doc = new
	 * com.itextpdf.text.Document(PageSize.A4); PdfWriter.getInstance(doc, new
	 * FileOutputStream(new File("c:/trans/test_itext.pdf"))); doc.open();
	 * HTMLWorker hw = new HTMLWorker(doc); String k =
	 * "<html><body> This is my Project </body></html>"; hw.parse(new
	 * StringReader(NetHelper.readPage(url))); doc.close();
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * }
	 */

}
