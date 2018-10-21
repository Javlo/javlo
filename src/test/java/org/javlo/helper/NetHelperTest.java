package org.javlo.helper;

import junit.framework.TestCase;

public class NetHelperTest extends TestCase {

	public void testIpRange() throws Exception {
		assertTrue(NetHelper.ipInRange("192.168.0.1", "192.168.0.1"));
		assertFalse(NetHelper.ipInRange("192.168.0.1", "192.168.0.2"));
		
		assertTrue(NetHelper.ipInRange("192.168.0.1", "192.168.0.0/8"));
		assertTrue(NetHelper.ipInRange("192.168.0.1", "192.168.0.0/16"));
		assertTrue(NetHelper.ipInRange("192.168.0.1", "192.168.0.0/24"));
		assertTrue(NetHelper.ipInRange("192.168.1.1", "192.168.0.0/8"));
		assertTrue(NetHelper.ipInRange("192.168.1.1", "192.168.0.0/16"));
		assertTrue(NetHelper.ipInRange("192.168.254.65", "192.168.254.64/27"));
		assertTrue(NetHelper.ipInRange("192.168.254.72", "192.168.254.64/27"));
		assertTrue(NetHelper.ipInRange("192.168.254.94", "192.168.254.64/27"));
		assertTrue(NetHelper.ipInRange("136.173.60.47", "136.173.0.0/16"));
		assertTrue(NetHelper.ipInRange("10.22.247.121", "10.0.0.0/8"));

		assertFalse(NetHelper.ipInRange("192.168.254.63", "192.168.254.64/27"));		
		//assertFalse(NetHelper.ipInRange("192.168.254.95", "192.168.254.64/27"));		
		assertFalse(NetHelper.ipInRange("192.168.1.1", "192.168.0.0/24"));
		assertFalse(NetHelper.ipInRange("193.168.1.1", "192.168.0.0/8"));
		assertFalse(NetHelper.ipInRange("192.168.1.1", "192.12.0.0/16"));
	}
	
	public void testIsMobile() {
		assertTrue(NetHelper.isMobile("Mozilla/5.0 (Android 8.0.0; Mobile; rv:62.0) Gecko/62.0 Firefox/62.0"));
		assertFalse(NetHelper.isMobile("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML like Gecko) Chrome/70.0.3538.67 Safari/537.36"));
	}

}
