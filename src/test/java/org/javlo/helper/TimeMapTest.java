package org.javlo.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.javlo.utils.TimeMap;

import ch.qos.logback.core.encoder.ByteArrayUtil;
import junit.framework.TestCase;

public class TimeMapTest extends TestCase {

	public void testTime() throws InterruptedException {
		TimeMap<String, String> timeMap = new TimeMap<String, String>(1);
		timeMap.put("test", "value");
		assertEquals(timeMap.get("test"), "value");
		Thread.sleep(2 * 1000);
		assertNull(timeMap.get("test"));
	}

	public void testSize() throws InterruptedException {
		TimeMap<String, String> timeMap = new TimeMap<String, String>(1, 3);
		timeMap.put("test-1", "v1");
		timeMap.put("test-2", "v2");
		timeMap.put("test-3", "v3");
		assertEquals(timeMap.size(), 3);
		timeMap.put("test-4", "v4");
		assertEquals(timeMap.size(), 3);
		assertNull(timeMap.get("test-1"));
		assertEquals(timeMap.get("test-4"), "v4");
	}

	public void testStore() {		
		try {
			
			TimeMap<String, String> timeMap = new TimeMap<String, String>(1);
			timeMap.put("test-1", "v1");
			timeMap.put("test-2", "v2");
			timeMap.put("test-3", "v3");
			timeMap.put("test-4", "v4");
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			timeMap.store(out);
			TimeMap<String, String> newTimeMap = new TimeMap<String, String>(1);
			newTimeMap.load(new ByteArrayInputStream(out.toByteArray()));

			assertEquals(newTimeMap.size(), 4);			
			assertEquals(newTimeMap.get("test-4"), "v4");
			
			Thread.sleep(2 * 1000);

			assertEquals(timeMap.size(), 0);
			assertEquals(newTimeMap.size(), 0);			
			assertNull(newTimeMap.get("test-4"));
			newTimeMap = new TimeMap<String, String>(1);
			newTimeMap.load(new ByteArrayInputStream(out.toByteArray()));
			assertEquals(newTimeMap.size(), 0);			
			assertNull(newTimeMap.get("test-4"));
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
