package org.javlo.helper;

import junit.framework.TestCase;

public class StringSecurityUtilTest extends TestCase {

	public void testGetTimeAccessToken() throws Exception {
		String token = StringSecurityUtil.getTimeAccessToken(2);
		assertTrue(StringSecurityUtil.isValidAccessToken(token));
		Thread.sleep(1*1000);
		assertTrue(StringSecurityUtil.isValidAccessToken(token));
		Thread.sleep(2*1000);
		assertFalse(StringSecurityUtil.isValidAccessToken(token));
	}
}
