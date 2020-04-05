package org.javlo.service.log;

import junit.framework.TestCase;

public class LogContainerTest extends TestCase {

	public void testLogContainer() throws Exception {
		LogContainer logContainer = new LogContainer();
		Log log = new Log(Log.WARNING, "grtest1", "text");
		logContainer.add(log);
		log = new Log(Log.SEVERE, "grtest2", "text2");
		logContainer.add(log);
		log = new Log(Log.SEVERE, "grtest2", "text3");
		logContainer.add(log);
		
		assertEquals(logContainer.getGroups().size(), 2);
		assertEquals(logContainer.getGroups().get(0).length(), "grtest1".length());
	}
}