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
	
	public void testAnonymisedIp() throws Exception {
		// ipv4
		assertEquals(StringSecurityUtil.anonymisedIp("192.168.0.1"), "192.168.0.0");
		assertEquals(StringSecurityUtil.anonymisedIp("192.168.1.24"), "192.168.1.0");
		assertEquals(StringSecurityUtil.anonymisedIp("192.168.0.1/22"), "192.168.0.0/22");
		assertEquals(StringSecurityUtil.anonymisedIp("IP:192.168.0.1 - host"), "IP:192.168.0.0 - host");
		assertEquals(StringSecurityUtil.anonymisedIp("IP:192.168.0.1 127.0.0.1 - host"), "IP:192.168.0.0 127.0.0.0 - host");
		assertEquals(StringSecurityUtil.anonymisedIp("IP:127.0.0.1 127.0.0.1 - host"), "IP:127.0.0.0 127.0.0.0 - host");
		// ipv6
		assertEquals(StringSecurityUtil.anonymisedIp("2001:0db8:0000:85a3:0000:85A3:ac1f:8001"), "2001:0db8:0000:85a3:0000:85A3:0000:0000");
		assertEquals(StringSecurityUtil.anonymisedIp("2001:0db8:0000:85a3:0000:85a3:ac1f:8001 / 2001:0db8:0000:85a3:0000:85a3:ac1f:8001"), "2001:0db8:0000:85a3:0000:85a3:0000:0000 / 2001:0db8:0000:85a3:0000:85a3:0000:0000");
		// both
		assertEquals(StringSecurityUtil.anonymisedIp("2001:0db8:0000:85a3:0000:85a3:ac1f:8001 / 192.168.1.24"), "2001:0db8:0000:85a3:0000:85a3:0000:0000 / 192.168.1.0");
	}
}
