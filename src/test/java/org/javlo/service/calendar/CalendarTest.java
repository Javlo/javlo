package org.javlo.service.calendar;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.javlo.helper.StringHelper;

import junit.framework.TestCase;

public class CalendarTest extends TestCase {

	public void testICal() throws Exception {
		ICal ical = new ICal(true);

		ical.setStartDate(StringHelper.parseDate("27/11/1975"));
		assertTrue(ical.storeToString().contains("19751127"));
		assertFalse(ical.storeToString().contains("19771127"));
		ical.setEndDate(StringHelper.parseDate("27/11/1977"));
		assertTrue(ical.storeToString().contains("19771127"));
		ical.setSummary("Javlo Event");
		assertTrue(ical.storeToString().contains("Javlo Event"));

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("BEGIN:VCALENDAR");
		out.println("VERSION:2.0");
		out.println("PRODID:-//hacksw/handcal//NONSGML v1.0//EN");
		out.println("BEGIN:VEVENT");
		out.println("DTSTART:19970714T170000Z");
		out.println("DTEND:19970715T035959Z");
		out.println("SUMMARY:Fête à la Bastille");
		out.println("END:VEVENT");
		out.println("END:VCALENDAR");
		out.close();
		ical = new ICal(true);
		ical.loadFromString(new String(outStream.toByteArray()));
		 
		assertEquals(ical.getSummary(), "Fête à la Bastille");
		assertEquals(StringHelper.renderDate(ical.getStartDate()), "14/07/1997");
		assertEquals(StringHelper.renderDate(ical.getEndDate()), "15/07/1997");
	
	}

}
