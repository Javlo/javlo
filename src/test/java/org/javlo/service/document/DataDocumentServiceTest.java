package org.javlo.service.document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class DataDocumentServiceTest extends TestCase {
	
	public void testDataDocument() throws IOException {
		DataDocumentService service = DataDocumentService.getInstance(null);
		Map<String,String> data = new HashMap<String,String>();
		data.put("data", "test");
		DataDocument doc = new DataDocument("test", data);
		assertEquals(doc.getId(), 0);
		service.createDocumentData(doc);
		assertNotSame(doc.getId(), 0);
		assertNotNull(doc.getToken());
		assertNotNull(service.getDocumentData("test", doc.getId(), doc.getToken()));
		assertEquals(service.getDocumentData("test", doc.getId(), doc.getToken()).getData().get("data"), "test");
		data.put("data", "test 2");
		DataDocument doc2 = new DataDocument("test", data);
		service.createDocumentData(doc2);
		assertNotSame(doc2.getId(), doc.getId());
		assertNotSame(doc2.getToken(), doc.getToken());
		assertEquals(service.getDocumentData("test", doc2.getId(), doc2.getToken()).getData().get("data"), "test 2");
	}
}
