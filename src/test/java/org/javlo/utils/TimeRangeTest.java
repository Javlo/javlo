package org.javlo.utils;

import junit.framework.TestCase;
import org.javlo.helper.StringHelper;

import java.util.Date;

public class TimeRangeTest extends TestCase {
	
	public void testBefore() throws Exception {
		Date startDate = StringHelper.parseDate("27/11/1975");
		Date endDate = StringHelper.parseDate("23/04/1976");
		TimeRange dateRange = new TimeRange(startDate, endDate);
		assertTrue(dateRange.isBefore(StringHelper.parseDate("20/11/1975")));
		assertFalse(dateRange.isBefore(StringHelper.parseDate("20/12/1975")));
		assertFalse(dateRange.isBefore(StringHelper.parseDate("20/11/1977")));
		dateRange = new TimeRange(startDate, null);
		assertTrue(dateRange.isBefore(StringHelper.parseDate("20/11/1975")));
		assertFalse(dateRange.isBefore(StringHelper.parseDate("20/12/1975")));
		assertFalse(dateRange.isBefore(StringHelper.parseDate("20/11/1977")));
		dateRange = new TimeRange(null, endDate);
		assertTrue(dateRange.isBefore(StringHelper.parseDate("20/11/1975")));
		assertTrue(dateRange.isBefore(StringHelper.parseDate("20/12/1975")));
		assertFalse(dateRange.isBefore(StringHelper.parseDate("20/11/1977")));
		dateRange = new TimeRange((Date)null, null);
		assertTrue(dateRange.isBefore(StringHelper.parseDate("20/11/1975")));
		assertTrue(dateRange.isBefore(StringHelper.parseDate("20/12/1975")));
		assertTrue(dateRange.isBefore(StringHelper.parseDate("20/11/1977")));
	}
	
	public void testAfter() throws Exception {
		Date startDate = StringHelper.parseDate("27/11/1975");
		Date endDate = StringHelper.parseDate("23/04/1976");
		TimeRange dateRange = new TimeRange(startDate, endDate);
		assertFalse(dateRange.isAfter(StringHelper.parseDate("20/11/1975")));
		assertFalse(dateRange.isAfter(StringHelper.parseDate("20/12/1975")));
		assertTrue(dateRange.isAfter(StringHelper.parseDate("20/11/1977")));
		dateRange = new TimeRange(startDate, null);
		assertFalse(dateRange.isAfter(StringHelper.parseDate("20/11/1975")));
		assertTrue(dateRange.isAfter(StringHelper.parseDate("20/12/1975")));
		assertTrue(dateRange.isAfter(StringHelper.parseDate("20/11/1977")));
		dateRange = new TimeRange(null, endDate);
		assertFalse(dateRange.isAfter(StringHelper.parseDate("20/11/1975")));
		assertFalse(dateRange.isAfter(StringHelper.parseDate("20/12/1975")));
		assertTrue(dateRange.isAfter(StringHelper.parseDate("20/11/1977")));
		dateRange = new TimeRange((Date)null, null);
		assertTrue(dateRange.isAfter(StringHelper.parseDate("20/11/1975")));
		assertTrue(dateRange.isAfter(StringHelper.parseDate("20/12/1975")));
		assertTrue(dateRange.isAfter(StringHelper.parseDate("20/11/1977")));
	}
	
	public void testInside() throws Exception {
		Date startDate = StringHelper.parseDate("27/11/1975");
		Date endDate = StringHelper.parseDate("23/04/1976");
		TimeRange dateRange = new TimeRange(startDate, endDate);
		assertFalse(dateRange.isInside(StringHelper.parseDate("20/11/1975")));
		assertTrue(dateRange.isInside(StringHelper.parseDate("20/12/1975")));
		assertFalse(dateRange.isInside(StringHelper.parseDate("20/11/1977")));
		dateRange = new TimeRange(startDate, null);
		assertFalse(dateRange.isInside(StringHelper.parseDate("20/11/1975")));
		assertFalse(dateRange.isInside(StringHelper.parseDate("20/12/1975")));
		assertFalse(dateRange.isInside(StringHelper.parseDate("20/11/1977")));
		dateRange = new TimeRange(null, endDate);
		assertFalse(dateRange.isInside(StringHelper.parseDate("20/11/1975")));
		assertFalse(dateRange.isInside(StringHelper.parseDate("20/12/1975")));
		assertFalse(dateRange.isInside(StringHelper.parseDate("20/11/1977")));
		dateRange = new TimeRange((Date)null, null);
		assertTrue(dateRange.isInside(StringHelper.parseDate("20/11/1975")));
		assertTrue(dateRange.isInside(StringHelper.parseDate("20/12/1975")));
		assertTrue(dateRange.isInside(StringHelper.parseDate("20/11/1977")));
	}

}
