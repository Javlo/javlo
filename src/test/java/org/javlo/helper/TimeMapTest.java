package org.javlo.helper;

import org.javlo.utils.TimeMap;

import junit.framework.TestCase;

public class TimeMapTest  extends TestCase {

    public void testTime() throws InterruptedException {
        TimeMap<String,String> timeMap = new TimeMap<String, String>(1);
        timeMap.put("test", "value");
        assertEquals(timeMap.get("test"), "value");
        Thread.sleep(2*1000);
        assertNull(timeMap.get("test"));
    }
    
    public void testSize() throws InterruptedException {
        TimeMap<String,String> timeMap = new TimeMap<String, String>(1,3);
        timeMap.put("test-1", "v1");
        timeMap.put("test-2", "v2");
        timeMap.put("test-3", "v3");
        assertEquals(timeMap.size(), 3);
        timeMap.put("test-4", "v4");
        assertEquals(timeMap.size(), 3);        
        assertNull(timeMap.get("test-1"));
        assertEquals(timeMap.get("test-4"), "v4");
    }	

}
