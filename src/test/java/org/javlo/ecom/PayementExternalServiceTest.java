package org.javlo.ecom;

import junit.framework.TestCase;

public class PayementExternalServiceTest extends TestCase {
	
	public void testPayementExternalService() throws Exception {		
		String rawContent = "testname,appId,secretKey,url,page";		
		PayementExternalService service = new PayementExternalService(rawContent);		
		assertEquals(service.getName(), "testname");
		assertEquals(service.getAppId(), "appId");
		assertEquals(service.getSecretKey(), "secretKey");
		assertEquals(service.getURL(), "url");		
		assertEquals(service.getReturnPage(), "page");
		assertEquals(service.toString(), rawContent);
	}

}
