package org.javlo.helper;

import java.util.Date;

import junit.framework.TestCase;

public class TimeHelperTest extends TestCase {
	
  public void testGetDaysDistance() throws Exception {	  
	  Date date1 = StringHelper.parseDate("01/01/1975");
	  Date date2 = StringHelper.parseDate("10/01/1975");	  
	  assertEquals(TimeHelper.getDaysDistance(date1, date2), 9);
	  
	  date2 = StringHelper.parseDate("01/01/1975");
	  date1 = StringHelper.parseDate("10/01/1975");	  
	  assertEquals(TimeHelper.getDaysDistance(date1, date2), 9);
	  
	  date1 = StringHelper.parseTime("01/01/1975 23:10:00");
	  date2 = StringHelper.parseTime("10/01/1975 10:10:10");
	  assertEquals(TimeHelper.getDaysDistance(date1, date2), 9);
	  
	  date1 = StringHelper.parseTime("01/01/1975 10:10:00");
	  date2 = StringHelper.parseTime("10/01/1975 23:10:10");
	  assertEquals(TimeHelper.getDaysDistance(date1, date2), 9);
	  
	  date1 = StringHelper.parseTime("01/01/1975 23:10:00");
	  date2 = StringHelper.parseTime("01/01/1975 10:10:10");
	  assertEquals(TimeHelper.getDaysDistance(date1, date2), 0);
	  
	  date1 = StringHelper.parseTime("01/01/1975 23:10:00");
	  date2 = StringHelper.parseTime("10/01/1976 10:10:10");
	  assertEquals(TimeHelper.getDaysDistance(date1, date2), 374);
  }   
 
}
