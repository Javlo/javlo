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

		assertFalse(NetHelper.ipInRange("192.168.254.63", "192.168.254.64/27"));
		//assertFalse(NetHelper.ipInRange("192.168.254.95", "192.168.254.64/27"));		
		assertFalse(NetHelper.ipInRange("192.168.1.1", "192.168.0.0/24"));
		assertFalse(NetHelper.ipInRange("193.168.1.1", "192.168.0.0/8"));
		assertFalse(NetHelper.ipInRange("192.168.1.1", "192.12.0.0/16"));
	}

}
