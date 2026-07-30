package org.javlo.helper;

import junit.framework.TestCase;

public class ElementaryURLHelperTest extends TestCase {

	public void testIsImage() throws Exception {
		  assertEquals(ElementaryURLHelper.mergePath("path1", "path2"), "path1/path2");
		  assertEquals(ElementaryURLHelper.mergePath("path1", "/path2"), "path1/path2");
		  assertEquals(ElementaryURLHelper.mergePath("path1/", "/path2"), "path1/path2");
	  }

	/**
	 * every url of the site is reflected in the rendered page, so it must not be
	 * able to break out of an html attribute nor out of a javascript string.
	 */
	public void testEncodeUnsafeCharacters() throws Exception {
		assertEquals("/fr/page.html%22%3E%3Cscript%3Ealert(1)%3C/script%3E", ElementaryURLHelper.encodeUnsafeCharacters("/fr/page.html\"><script>alert(1)</script>"));
		assertEquals("/fr/page.html%27", ElementaryURLHelper.encodeUnsafeCharacters("/fr/page.html'"));
	}

	/** a clean url must come out untouched */
	public void testEncodeUnsafeCharactersKeepsNormalURL() throws Exception {
		assertEquals("/fr/my-page.html", ElementaryURLHelper.encodeUnsafeCharacters("/fr/my-page.html"));
		assertEquals("/fr/page.html?a=1&b=2", ElementaryURLHelper.encodeUnsafeCharacters("/fr/page.html?a=1&b=2"));
		assertNull(ElementaryURLHelper.encodeUnsafeCharacters(null));
	}

	/**
	 * an already encoded accented path must not be encoded a second time, which
	 * is why the percent sign is left alone.
	 */
	public void testEncodeUnsafeCharactersKeepsPercentEncoding() throws Exception {
		assertEquals("/fr/pl%C3%A9ni%C3%A8re.html", ElementaryURLHelper.encodeUnsafeCharacters("/fr/pl%C3%A9ni%C3%A8re.html"));
	}

}
