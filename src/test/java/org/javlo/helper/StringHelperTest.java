package org.javlo.helper;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import junit.framework.TestCase;

public class StringHelperTest extends TestCase {
	
  public void testIsImage() throws Exception {
	  assertTrue(StringHelper.isImage("test.jpg"));
	  assertTrue(StringHelper.isImage("test.png"));
	  assertTrue(StringHelper.isImage("test.gif"));
	  assertTrue(!StringHelper.isImage("test.xml"));
	  assertTrue(!StringHelper.isImage("/test.xml?param=value"));
	  assertTrue(!StringHelper.isImage("/test"));
	  assertTrue(!StringHelper.isImage(""));
	  assertTrue(!StringHelper.isImage("/"));
	  assertTrue(!StringHelper.isImage("http://host.com/rss/rss.html;jsessionid=97F113FC3A1036324B2889FD5E795F8E.node1"));	  
	  assertTrue(!StringHelper.isImage(null));
  }
  
  public void testIsVideo() throws Exception {
	  assertTrue(StringHelper.isVideo("test.mp4"));
	  assertTrue(!StringHelper.isVideo("test.xml"));
	  assertTrue(!StringHelper.isVideo("/test.xml?param=value"));
	  assertTrue(!StringHelper.isVideo("/test"));
	  assertTrue(!StringHelper.isVideo(""));
	  assertTrue(!StringHelper.isVideo("/"));
	  assertTrue(!StringHelper.isVideo(null));
  }
  
  public void testGetFileExtension() throws Exception {
	  assertTrue(StringHelper.getFileExtension("test.jpg").equals("jpg"));
	  assertTrue(StringHelper.getFileExtension("test.xml").equals("xml"));
	  assertTrue(StringHelper.getFileExtension("test.xml?coucou=test").equals("xml"));
	  assertTrue(StringHelper.getFileExtension("test.xml;jsessionid=6D7A0CD25887B1A3C4CAECFBA17ACB0D").equals("xml"));
	  assertTrue(StringHelper.getFileExtension("test.xml;jsessionid=6D7A0CD25887B1A3C4CAECFBA17ACB0D?coucou=test").equals("xml"));
	  assertTrue(StringHelper.getFileExtension("test.xml?coucou=test;jsessionid=6D7A0CD25887B1A3C4CAECFBA17ACB0D").equals("xml"));
	  assertTrue(StringHelper.getFileExtension(null).equals(""));
	  assertTrue(StringHelper.getFileExtension("test").equals(""));
  }
  
  public void testGetLanguageFromFileName() throws Exception {
	  assertTrue(StringHelper.getLanguageFromFileName("test_en.jpg").equals("en"));
	  assertTrue(StringHelper.getLanguageFromFileName("test_nolang.jpg") == null);
	  assertTrue(StringHelper.getLanguageFromFileName("test.jpg") == null);	  
	  assertTrue(StringHelper.getLanguageFromFileName(null) == null);
  }
  
  public void testisFloat() throws Exception {
	  assertTrue(StringHelper.isFloat("2"));
	  assertTrue(StringHelper.isFloat("2.3"));
	  assertTrue(StringHelper.isFloat("0.22"));
	  assertTrue(StringHelper.isFloat("-0.22"));
	  assertFalse(StringHelper.isFloat("08-12-2011"));
	  assertFalse(StringHelper.isFloat("abc"));
  }
  
  public void testStringToCollection() throws Exception {	  
		List<String> testList = Arrays.asList(new String[] { "item1", "item\\, 2", "item3\\" });
		String rawTest = StringHelper.collectionToString(testList, ", ");
		assertEquals(rawTest, "item1, item\\\\\\, 2, item3\\\\");
		List<String> list = StringHelper.stringToCollection(rawTest, ", ");
		int i = 0;
		for (String item : list) {
			assertEquals(item, testList.get(i));
			i++;
		}
		
		testList = Arrays.asList(new String[] { "item1", "item2", "item3", "item4" });
		rawTest = StringHelper.collectionToString(testList, ",");
		assertEquals(rawTest, "item1,item2,item3,item4");
		list = StringHelper.stringToCollection(rawTest, ",");
		i = 0;
		for (String item : list) {
			assertEquals(item, testList.get(i));
			i++;
		}
		
		testList = Arrays.asList(new String[] { });
		rawTest = StringHelper.collectionToString(testList, ",");
		assertEquals(rawTest, "");		
		
		list = StringHelper.stringToCollection("item1,,,item4", ",");
		assertEquals(list.get(0), "item1");
		assertEquals(list.get(1), "");
		assertEquals(list.get(2), "");
		assertEquals(list.get(3), "item4");		
  }
  
  public void testGetFileNameFromPath() throws Exception {
	  assertEquals(StringHelper.getFileNameFromPath("/folder/file.jpg"), "file.jpg");
	  assertEquals(StringHelper.getFileNameFromPath("/folder/file.jpg?param=value"), "file.jpg");
	  assertEquals(StringHelper.getFileNameFromPath("http://host.com/folder/file.jpg?param=value"), "file.jpg");
	  assertEquals(StringHelper.getFileNameFromPath("http://host.com/folder/1234"), "1234");
  }
  
  public void testEncodeAsStructuredCommunicationMod97() throws Exception {
	  assertEquals(StringHelper.encodeAsStructuredCommunicationMod97(null), null);
	  assertEquals(StringHelper.encodeAsStructuredCommunicationMod97("3542232806"), "354/2232/80695");
	  assertEquals(StringHelper.encodeAsStructuredCommunicationMod97("0000000007"), "000/0000/00707");
	  assertEquals(StringHelper.encodeAsStructuredCommunicationMod97("1417553926"), "141/7553/92697");	  
  }
  
  public void testGetItem() throws Exception {
	  assertEquals(StringHelper.getItem("patrick|barbara|catherine", "|", 1, null),"patrick");
	  assertEquals(StringHelper.getItem("patrick|barbara|catherine", "|", 2, null),"barbara");
	  assertEquals(StringHelper.getItem("patrick|barbara|catherine", "|", 3, null),"catherine");
	  assertEquals(StringHelper.getItem("patrick|barbara|catherine", "|", 4, null),null);
	  assertEquals(StringHelper.getItem("patrick|barbara|catherine", "|", 0, "default"),"default");
	  assertEquals(StringHelper.getItem("patrick123barbara123catherine", "123", 2, null),"barbara");
  }
  
  public void testTrim() {
	  assertEquals(StringHelper.trim("test", ')'),"test");
	  assertEquals(StringHelper.trim(null, ')'),null);
	  assertEquals(StringHelper.trim("", ')'),"");	  
	  assertEquals(StringHelper.trim("(test)", ')'),"(test");
	  assertEquals(StringHelper.trim("(test)", '('),"test)");
	  assertEquals(StringHelper.trim("-", '-'),"");
	  assertEquals(StringHelper.trim("--", '-'),"");
	  assertEquals(StringHelper.trim("--test-", '-'),"test");
	  assertEquals(StringHelper.trim("--te-st-", '-'),"te-st");
	  assertEquals(StringHelper.trim("-test", '-'),"test");
  }
  
  public void testCleanPath() {
	  assertEquals(StringHelper.cleanPath(null), null);
	  assertEquals(StringHelper.cleanPath("/folder1/folder1"), "/folder1/folder1");	  
	  assertEquals(StringHelper.cleanPath("/folder1/folder1/"), "/folder1/folder1/");
	  assertEquals(StringHelper.cleanPath("folder1/folder1/"), "folder1/folder1/");
	  assertEquals(StringHelper.cleanPath("//folder1/folder1/"), "/folder1/folder1/");
	  assertEquals(StringHelper.cleanPath("///////folder1///////folder1///"), "/folder1/folder1/");
	  assertEquals(StringHelper.cleanPath("//folder1//folder1"), "/folder1/folder1");
  }
  
  public void testRemoveTag() {
	  assertEquals(StringHelper.removeTag(null), "");
	  assertEquals(StringHelper.removeTag("test"), "test");
	  assertEquals(StringHelper.removeTag("<p>test</p>"), "test");
	  assertEquals(StringHelper.removeTag("<ptest</p>"), "");
	  assertEquals(StringHelper.removeTag("<p>start <a href=\"#\">click</a> test</p>"), "start click test");	  
  }
  
  public void testRemoveRepeatedChar() {
	  assertEquals(StringHelper.removeRepeatedChar(null, '-'), null);
	  assertEquals(StringHelper.removeRepeatedChar("", '-'), "");
	  assertEquals(StringHelper.removeRepeatedChar("javlo", '-'), "javlo");
	  assertEquals(StringHelper.removeRepeatedChar("javlo-love", '-'), "javlo-love");
	  assertEquals(StringHelper.removeRepeatedChar("javlo--love", '-'), "javlo-love");
	  assertEquals(StringHelper.removeRepeatedChar("-javlo--love-", '-'), "-javlo-love-");
	  assertEquals(StringHelper.removeRepeatedChar("--javlo----love---------------", '-'), "-javlo-love-");
  }
  
  public void testCRC32() {
	  assertNotSame(StringHelper.getCRC32("javlo"), StringHelper.getCRC32("javlo2"));
  }
  
  public void testSearchStructuredEmail() {
	  assertEquals(StringHelper.searchStructuredEmail("first last <info@javlo.org>;first last <info2@javlo.org>").size(), 2);
	  assertEquals(StringHelper.searchStructuredEmail("first last <info@javlo.org>;first last2 <info2@javlo.org>").iterator().next().getPersonal(), "first last");
	  assertEquals(StringHelper.searchStructuredEmail("first last <info@javlo.org>;first last2 <info2@javlo.org>").iterator().next().getAddress(), "info@javlo.org");
	  
  }
  
  public void testMapToString() throws IOException {
	  Map<String,String> testMap = new HashMap<String, String>();
	  testMap.put("entry1", "value1");
	  testMap.put("entry2", "value2");
	  testMap.put("entry3", "value3");
	  String encodedMap = StringHelper.mapToString(testMap);
	  Map<String,String> decodedMap = StringHelper.stringToMap(encodedMap);
	  assertEquals(decodedMap.get("entry1"), testMap.get("entry1"));
	  assertEquals(decodedMap.get("entry2"), testMap.get("entry2"));
	  assertEquals(decodedMap.get("entry3"), testMap.get("entry3"));
	  assertEquals(decodedMap.size(), testMap.size());	  
  }
  
  public void testParseRangeDate() throws IOException, ParseException {
	  String range = "02/06/2015 - 04/06/2015";
	  Date[] date = StringHelper.parseRangeDate(range);
	  assertEquals(StringHelper.renderDate(date[0]),"02/06/2015");
	  assertEquals(StringHelper.renderDate(date[1]),"04/06/2015");
	  range = "02/06/2015";
	  date = StringHelper.parseRangeDate(range);
	  assertEquals(StringHelper.renderDate(date[0]),"02/06/2015");
	  assertEquals(date.length, 1);
  }
  
  public void testiIsMail() {
	  assertFalse(StringHelper.isMail(null));
	  assertFalse(StringHelper.isMail(""));
	  assertFalse(StringHelper.isMail("test_at_javlo.org"));
	  assertTrue(StringHelper.isMail("webmaster@javlo.org"));	  
	  assertTrue(StringHelper.isMail("webmaster <webmaster@javlo.org>"));
	  assertTrue(StringHelper.isMail("webmaster <webmaster@javlo.org>"));
	  assertTrue(StringHelper.isMail("webmaster's <webmaster@javlo.org>"));	  
  }
  public void testListContainsItem() {
	  assertTrue(StringHelper.listContainsItem("itemA,itemB,itemC,itemD", ",", "itemA"));
	  assertTrue(StringHelper.listContainsItem("itemA,itemB,itemC,itemD", ",", "itemC"));
	  assertTrue(StringHelper.listContainsItem("itemA,itemB,itemC,itemD", ",", "itemD"));	  
	  assertFalse(StringHelper.listContainsItem("itemA,itemB,itemC,itemD", ",", "itemE"));
	  assertFalse(StringHelper.listContainsItem("itemA,itemB,itemC,itemD", ",", "item"));
	  assertTrue(StringHelper.listContainsItem("itemA", ",", "itemA"));
  }

	@SuppressWarnings("unchecked")
	public void testRangeMatches() {
		List<Pair<List<String>, List<Pair<Integer, Boolean>>>> tests = Arrays.asList(
				Pair.of(Arrays.asList("<25", "-25"), Arrays.asList(
						Pair.of(-1, true),
						Pair.of(0, true),
						Pair.of(23, true),
						Pair.of(24, true),
						Pair.of(25, false),
						Pair.of(26, false)
						)),
				Pair.of(Arrays.asList("25-30"), Arrays.asList(
						Pair.of(-1, false),
						Pair.of(0, false),
						Pair.of(23, false),
						Pair.of(24, false),
						Pair.of(25, true),
						Pair.of(26, true),
						Pair.of(29, true),
						Pair.of(30, true),
						Pair.of(31, false),
						Pair.of(32, false),
						Pair.of(99, false)
						)),
				Pair.of(Arrays.asList("30+", "30>", ">30", "+30"), Arrays.asList(
						Pair.of(-1, false),
						Pair.of(0, false),
						Pair.of(29, false),
						Pair.of(30, false),
						Pair.of(31, true),
						Pair.of(32, true),
						Pair.of(99, true)
						))

				);
		for (Pair<List<String>, List<Pair<Integer, Boolean>>> test : tests) {
			for (String range : test.getLeft()) {
				for (Pair<Integer, Boolean> testCase : test.getRight()) {
					Integer value = testCase.getLeft();
					boolean result = testCase.getRight();
					String caseLabel = "StringHelper.rangeMatches(range='" + range + "',value='" + value + "')";
					boolean execResult = StringHelper.rangeMatches(range, value);					
					assertEquals(caseLabel, result, execResult);
				}
			}
		}
	}
	
	public void testIsDigit() {
		assertTrue(StringHelper.isDigit("0"));
		assertTrue(StringHelper.isDigit("125"));
		assertTrue(StringHelper.isDigit("-12"));
		assertFalse(StringHelper.isDigit("abc"));
		assertFalse(StringHelper.isDigit("12�"));
	}
	
	public void testIsLikeDigit() {
		assertTrue(StringHelper.isLikeNumber("0"));
		assertTrue(StringHelper.isLikeNumber("125"));
		assertTrue(StringHelper.isLikeNumber("-12"));
		assertFalse(StringHelper.isLikeNumber("abc"));
		assertTrue(StringHelper.isLikeNumber("12 %"));
		assertTrue(StringHelper.isLikeNumber("12,3 %"));
		assertTrue(StringHelper.isLikeNumber("-12,3"));
	}
	
	public void testCreateCleanName() {
		assertEquals(StringHelper.createFileName("test.jpg"), "test.jpg");		
		assertEquals(StringHelper.createFileName("test  end.jpg"), "test-end.jpg");
	}
	
	public void testIsAllEmpty() {
		assertFalse(StringHelper.isAllEmpty("test", "test2"));
		assertTrue(StringHelper.isAllEmpty("", null));
	}
	
	public void testIsOneEmpty() {
		assertFalse(StringHelper.isOneEmpty("test", "test2"));
		assertTrue(StringHelper.isOneEmpty("coucou", null));
	}
	
	public void testGetColNum() {
		assertEquals(1, StringHelper.getColNum("A"));
		assertEquals(1, StringHelper.getColNum("a"));
		assertNotSame(1, StringHelper.getColNum("b"));
		assertEquals(4, StringHelper.getColNum("d"));
		assertEquals(27, StringHelper.getColNum("aa"));
		assertEquals(30, StringHelper.getColNum("ad"));
	}
	
	public void testGetColName() {
		assertEquals("A", StringHelper.getColName(1));
		assertEquals("AA", StringHelper.getColName(27));
		assertEquals("AD", StringHelper.getColName(30));
	}
	
	public void testOnlyAlphaNumeric() {		
		assertEquals("anz", StringHelper.onlyAlphaNumeric("anz", true));
		assertEquals("a2a", StringHelper.onlyAlphaNumeric("a2a", true));
		assertEquals("aaabc", StringHelper.onlyAlphaNumeric("aaa bc", false));
		assertEquals("aaabc", StringHelper.onlyAlphaNumeric("aaa (bc){?./}", false));
		assertEquals("AANzZ", StringHelper.onlyAlphaNumeric("AANzZ (bc){?./}", true));		
	}
	
	public void testIsEmail() {		
		assertTrue(StringHelper.isMail("info@javlo.org"));		
		assertTrue(StringHelper.isMail("first last <info@javlo.org>"));
		assertTrue(StringHelper.isMail("info&contact@javlo.org"));
		assertTrue(StringHelper.isMail("info@23.23.23.23"));
		assertFalse(StringHelper.isMail("info@.javlo.org"));
		assertFalse(StringHelper.isMail("info"));		
	}

	
	public void testToLatin() {		
		assertEquals("a", StringHelper.toLatin("α"));
		assertEquals("aaa", StringHelper.toLatin("aαa"));
		assertEquals("aaab", StringHelper.toLatin("aαaβ"));
	}
	
	public void testCreateI18NURL() {
		assertEquals("lorem", StringHelper.createI18NURL("lorem"));
		assertEquals("lorem", StringHelper.createI18NURL("lorem/-* $ "));
		assertEquals("lorem-lorem", StringHelper.createI18NURL("lorem/lorem"));		
		assertEquals("%C3%A9t%C3%A9-lorem-lorem", StringHelper.createI18NURL("$été+lorem/#;,'\"[lorem]"));
	}
	
	public void testRemoveQuote() {
		assertEquals(null, StringHelper.removeQuote(null));
		assertEquals("lorem", StringHelper.removeQuote("lorem"));
		assertEquals("\"lorem", StringHelper.removeQuote("\"lorem"));
		assertEquals("lorem", StringHelper.removeQuote("\"lorem\""));
		assertEquals("lorem", StringHelper.removeQuote("  \"lorem\""));
		assertEquals("lorem", StringHelper.removeQuote("\"lorem\"  "));
		assertEquals("lorem", StringHelper.removeQuote("  \"lorem\"  "));
	}
	
	public void testNormalizedPhone() {
		assertEquals(StringHelper.normalizePhoneNumber("+32486123456", "+32"), "+32486123456");
		assertEquals(StringHelper.normalizePhoneNumber("+32(486)12 34 56", "+32"), "+32486123456");
		assertEquals(StringHelper.normalizePhoneNumber("0486-12-34-56", "+32"), "+32486123456");
		assertEquals(StringHelper.normalizePhoneNumber("(0486) 12/34/56", "+32"), "+32486123456");
		assertEquals(StringHelper.normalizePhoneNumber("0486.12.34.56", "+32"), "+32486123456");
	}
	
	public void testEncryptPasswordSHA256() {
		assertEquals("d61730e9c8132d8e308fafdd9ae09999b54f65fa68bef1889e1f11e3fc9cda65", StringHelper.encryptPasswordSHA256("javlo"));
	}
	
	public void testRemoveSpecialChars() {		
		/*assertEquals("lorem", StringHelper.removeSpecialChars("lorem"));
		assertEquals("ete", StringHelper.removeSpecialChars("été"));		
		assertEquals("lorem_ss_ipsum", StringHelper.removeSpecialChars("lorem ß ipsum"));*/
	}
	
	public void testMatchStarPattern() {
		assertTrue(StringHelper.matchStarPattern("eeA", "ee*"));
		assertFalse(StringHelper.matchStarPattern("eeA", "ee"));
		assertTrue(StringHelper.matchStarPattern("Aee", "*ee"));
		assertFalse(StringHelper.matchStarPattern("Aee", "ee"));
		assertTrue(StringHelper.matchStarPattern("eAe", "e*e"));
		assertFalse(StringHelper.matchStarPattern("eAe", "ee"));
		assertTrue(StringHelper.matchStarPattern("ee", "ee"));
	}
	
	public void testMatchQuestionPattern() {
		assertTrue(StringHelper.matchQuestionPattern("eeA", "ee?"));
		assertFalse(StringHelper.matchQuestionPattern("eeA", "ee"));
		assertTrue(StringHelper.matchQuestionPattern("Aee", "?ee"));
		assertFalse(StringHelper.matchQuestionPattern("Aee", "ee"));
		assertTrue(StringHelper.matchQuestionPattern("eAe", "e?e"));
		assertFalse(StringHelper.matchQuestionPattern("eAe", "ee"));
		assertTrue(StringHelper.matchQuestionPattern("ee", "ee"));
	}
	
	
	
	public void testMatchSimplePattern() {
		assertTrue(StringHelper.matchSimplePattern("eeA", "ee*", false));
		assertFalse(StringHelper.matchSimplePattern("eeA", "ee", false));
		assertTrue(StringHelper.matchSimplePattern("Aee", "*ee", false));
		assertFalse(StringHelper.matchSimplePattern("Aee", "ee", false));
		assertTrue(StringHelper.matchSimplePattern("eAe", "e*e", false));
		assertFalse(StringHelper.matchSimplePattern("eAe", "ee", false));
		assertTrue(StringHelper.matchSimplePattern("ee", "ee", false));
		assertTrue(StringHelper.matchSimplePattern("eeA", "ee?", false));
		assertFalse(StringHelper.matchSimplePattern("eeA", "ee", false));
		assertTrue(StringHelper.matchSimplePattern("Aee", "?ee", false));
		assertFalse(StringHelper.matchSimplePattern("Aee", "ee", false));
		assertTrue(StringHelper.matchSimplePattern("eAe", "e?e", false));
		assertFalse(StringHelper.matchSimplePattern("eAe", "ee", false));
		assertTrue(StringHelper.matchSimplePattern("ee", "ee", false));
		
		assertFalse(StringHelper.matchSimplePattern("Aee", "ee", false));
		assertTrue(StringHelper.matchSimplePattern("eAe", "e?e*", false));
		assertFalse(StringHelper.matchSimplePattern("eAe", "ee", false));
		assertTrue(StringHelper.matchSimplePattern("eeaaa", "ee???", false));
		assertTrue(StringHelper.matchSimplePattern("eeaaab", "ee?*b", false));
		assertFalse(StringHelper.matchSimplePattern("eeaab", "ee???*b", false));
		
		assertFalse(StringHelper.matchSimplePattern("eeaab", "eE?*b", false));
		assertTrue(StringHelper.matchSimplePattern("eeaab", "eE?*b", true));
	}
	
	public void testIsLetter() {
		assertTrue(StringHelper.isAlpha("javlo"));
		assertFalse(StringHelper.isAlpha("javlo1"));
		assertTrue(StringHelper.isAlpha(""));
		assertFalse(StringHelper.isAlpha("jav lo"));
	}
	
 
}
