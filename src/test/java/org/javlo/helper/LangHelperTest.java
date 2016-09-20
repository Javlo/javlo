package org.javlo.helper;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class LangHelperTest extends TestCase {
	
	public void testClearWeekReferenceCollection() {
		List<WeakReference<StringBuffer>> list = new LinkedList<WeakReference<StringBuffer>>();
		StringBuffer strBuf1 = new StringBuffer("test 1");
		StringBuffer strBuf2 = new StringBuffer("test 2");
		list.add(new WeakReference<StringBuffer>(strBuf1));
		list.add(new WeakReference<StringBuffer>(strBuf2));	
		assertEquals(list.size(), 2);
		strBuf1 = null;
		assertEquals(list.size(), 2);
		System.gc();
		LangHelper.clearWeekReferenceCollection(list);
		assertEquals(list.size(), 1);		
	}
	
	public void testClearWeekReferenceMap() {
		Map<String, WeakReference<StringBuffer>> list = new HashMap<String, WeakReference<StringBuffer>>();
		StringBuffer strBuf1 = new StringBuffer("test 1");
		StringBuffer strBuf2 = new StringBuffer("test 2");
		list.put("key1", new WeakReference<StringBuffer>(strBuf1));
		list.put("key2", new WeakReference<StringBuffer>(strBuf2));		
		assertEquals(list.size(), 2);
		strBuf1 = null;
		assertEquals(list.size(), 2);
		System.gc();
		LangHelper.clearWeekReferenceMap(list);
		assertEquals(list.size(), 1);		
	}
 
}
