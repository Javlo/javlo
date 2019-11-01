package org.javlo.service;

import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

public class PDFLayoutTest extends TestCase {
	
	public void testPDFLayoutStore() throws IllegalAccessException, InvocationTargetException {
		PDFLayout ly1 = new PDFLayout();
		String newWidth = "99999px";
		ly1.setWidth(newWidth);
		PDFLayout ly2 = new PDFLayout();
		ly2.setValues(ly1.store());
		assertEquals(ly2.getWidth(), newWidth);
		ly2.setValues(null);
		ly2.setValues("");
		
		ly1.setWidth("19cm");
		ly1.setMarginLeft("1cm");
		ly1.setMarginRight("2cm");
		assertEquals(ly1.getContainerWidth(), "16cm");
	}

}
