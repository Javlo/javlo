package org.javlo.helper;

import junit.framework.TestCase;

public class StringHelperTest extends TestCase {
	
  public void testIsImage() throws Exception {
	  assertTrue(StringHelper.isImage("test.jpg"));
	  assertTrue(StringHelper.isImage("test.png"));
	  assertTrue(StringHelper.isImage("test.gif"));
	  assertTrue(!StringHelper.isImage("test.xml"));
  }
  
  public void testGetFileExtension() throws Exception {
	  assertTrue(StringHelper.getFileExtension("test.jpg").equals("jpg"));
	  assertTrue(StringHelper.getFileExtension("test.xml").equals("xml"));
	  assertTrue(StringHelper.getFileExtension(null).equals(""));
	  assertTrue(StringHelper.getFileExtension("test").equals(""));
  }
  
  public void testGetLanguageFromFileName() throws Exception {
	  assertTrue(StringHelper.getLanguageFromFileName("test_en.jpg").equals("en"));
	  assertTrue(StringHelper.getLanguageFromFileName("test_nolang.jpg") == null);
	  assertTrue(StringHelper.getLanguageFromFileName("test.jpg") == null);	  
	  assertTrue(StringHelper.getLanguageFromFileName(null) == null);
  }
  

}
