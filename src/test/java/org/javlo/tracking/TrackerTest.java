package org.javlo.tracking;

import java.io.IOException;

import junit.framework.TestCase;

public class TrackerTest extends TestCase {
	
	public void testgetLanguage() throws IOException {
		assertEquals(Tracker.getLanguage("/fr/articles/2019/home.html"), "fr");
		assertEquals(Tracker.getLanguage("/en/articles/2019/home.html"), "en");
		assertEquals(Tracker.getLanguage("/fr/articles/2019/home/"), "fr");
		assertEquals(Tracker.getLanguage("/articles/2019/home.html"), "?");
		assertEquals(Tracker.getLanguage((String)null), null);
	}

}
