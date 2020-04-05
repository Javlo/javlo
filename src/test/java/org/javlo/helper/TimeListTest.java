package org.javlo.helper;

import org.javlo.utils.TimeList;

import junit.framework.TestCase;

public class TimeListTest extends TestCase {

	public void testTime() throws InterruptedException {
		TimeList<String> timeList = new TimeList<String>(2);
		timeList.add("item1");
		Thread.sleep(1000);
		timeList.add("item2");
		assertEquals(timeList.size(), 2);
		assertEquals(timeList.get(0), "item1");		
		assertEquals(timeList.get(1), "item2");
		Thread.sleep(1100);
		assertEquals(timeList.size(), 1);
		assertEquals(timeList.get(0), "item2");
		assertFalse(timeList.contains("item1"));
		assertTrue(timeList.contains("item2"));
		try {
			timeList.get(1);
			assert(false);
		} catch (Exception e) {
			assert(e instanceof IndexOutOfBoundsException);
		}
		Thread.sleep(1100);
		assertEquals(timeList.size(), 0);
		try {
			timeList.get(0);
			assert(false);
		} catch (Exception e) {
			assert(e instanceof IndexOutOfBoundsException);
		}
	}
	
	public void testRemove() throws InterruptedException {
		TimeList<String> timeList = new TimeList<String>(2);
		timeList.add("item1");
		timeList.add("item2");
		assertEquals(timeList.size(), 2);
		assertEquals(timeList.get(0), "item1");		
		assertEquals(timeList.get(1), "item2");
		timeList.remove("item1");
		assertEquals(timeList.size(), 1);
		assertEquals(timeList.get(0), "item2");
		assertFalse(timeList.contains("item1"));
		assertTrue(timeList.contains("item2"));
		timeList.remove("item2");
		assertEquals(timeList.size(), 0);
		
	}

}
