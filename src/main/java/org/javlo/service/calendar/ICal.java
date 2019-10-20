package org.javlo.service.calendar;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.javlo.helper.IStringSeralizable;
import org.javlo.helper.StringHelper;

public class ICal implements IStringSeralizable {
	
	private String id = StringHelper.getRandomId();
	private String uid = null;
	private Date startDate;
	private Date endDate;
	private String summary;
	private String location;
	private String description;
	private String transp;
	private String categories;
	private String status;
	private boolean editable;
	private int sequence = 1;
	private boolean next = false;
	private int colorGroup=1;
	
	private static DateFormat getDateFormat()  {
		return new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	}
	
	private static DateFormat getDateDayFormat()  {
		return new SimpleDateFormat("yyyyMMdd");
	}
	
	public ICal(boolean editable) {
		this.editable = editable;
	}
	
	@Override
	public boolean loadFromString(String data) {
		return loadFromReader(new BufferedReader(new StringReader(data)));
	}
	
	public Date parseDate(String line) throws ParseException {
		if (line.length() == 8) {
			return getDateDayFormat().parse(line); 
		} else {
			return getDateFormat().parse(line);
		}		
	}
	
	public boolean loadFromReader(BufferedReader reader) {		
		try {			
			boolean endEvent = false;
			String line = reader.readLine();
			while (!endEvent && line != null) {				
				if (line.startsWith("DTSTART")) {
					if (line.startsWith("DTSTART:")) {
						line = line.substring("DTSTART:".length());
					} else {
						line = line.substring("DTSTART;VALUE=DATE:".length());						
					}					
					if (line.length() > 6) {
						try {
							startDate = parseDate(line);							
						} catch (ParseException e) {						
							e.printStackTrace();
						}
					}
				}
				if (line.startsWith("DTEND")) {
					boolean dateOnly=false;
					if (line.startsWith("DTEND:")) {
						line = line.substring("DTEND:".length());
					} else {
						line = line.substring("DTEND;VALUE=DATE:".length());
						dateOnly=true;
					}
					if (line.length() > 6) {
						try {
							endDate = parseDate(line);
							if (dateOnly) {
								Calendar cal = Calendar.getInstance();
								cal.setTime(endDate);
								cal.add(Calendar.DAY_OF_MONTH, -1);
								endDate = cal.getTime();
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
				}
				if (line.startsWith("SUMMARY:")) {
					summary = line.substring("SUMMARY:".length());					
				}
				if (line.startsWith("LOCATION:")) {
					location = line.substring("LOCATION:".length());					
				}
				if (line.startsWith("CATEGORIES:")) {
					categories = line.substring("CATEGORIES:".length());					
				}
				if (line.startsWith("STATUS:")) {
					status = line.substring("STATUS:".length());					
				}
				if (line.startsWith("DESCRIPTION:")) {
					description = line.substring("DESCRIPTION:".length());					
				}
				if (line.startsWith("TRANSP:")) {
					transp = line.substring("TRANSP:".length());					
				}
				if (line.startsWith("SEQUENCE:")) {
					sequence = Integer.parseInt(line.substring("SEQUENCE:".length()));					
				}
				if (line.startsWith("UID:")) {
					uid = line.substring("UID:".length());					
				}
				if (line.startsWith("END:")) {
					endEvent = true;					
				} else {
					line = reader.readLine();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return !StringHelper.isEmpty(uid);
	}
	
	public static String getOpenCalendarString() {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("BEGIN:VCALENDAR");
		out.println("VERSION:2.0");
		out.println("PRODID:-//hacksw/handcal//NONSGML v1.0//EN");
		out.close();
		return new String(outStream.toByteArray());
	}
	
	public static String getEndCalendarString() {
		return "BEGIN:VCALENDAR";
	}
	
	@Override
	public String storeToString() {
		return getOpenCalendarString()+storeEventString()+getEndCalendarString();
	}
	
	public String storeEventString() {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("BEGIN:VEVENT");
		DateFormat dateFormat = getDateFormat();
		if (getUid() != null) {
			out.println("UID:"+getUid());
		}
		if (startDate != null) {
			out.println("DTSTART:"+dateFormat.format(startDate));
		}
		if (endDate != null) {
			out.println("DTEND:"+dateFormat.format(endDate));
		}
		if (summary != null) {
			out.println("SUMMARY:"+summary);
		}
		if (categories != null) {
			out.println("CATEGORIES:"+categories);
		}
		if (location != null) {
			out.println("LOCATION:"+location);
		}
		if (description != null) {
			out.println("DESCRIPTION:"+description);
		}
		if (transp != null) {
			out.println("TRANSP:"+transp);
		}
		if (status != null) {
			out.println("STATUS:"+status);
		}
		out.println("DTSTAMP:"+dateFormat.format(new Date()));
		out.println("SEQUENCE:"+sequence);
		out.println("END:VEVENT");		
		out.close();
		return new String(outStream.toByteArray());
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getSummary() {		
		return summary;
	}
	
	public String getSummaryOrCategories() {
		if (StringHelper.isEmpty(getSummary())) {
			return getCategories();
		}
		return getSummary();
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTransp() {
		return transp;
	}

	public void setTransp(String transp) {
		this.transp = transp;
	}

	public String getCategories() {
		return categories;
	}

	public void setCategories(String categories) {
		this.categories = categories;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	
	public Date getDate() {
		Date date = getStartDate();
		if (date == null) {
			date = getEndDate();
		}
		return date;
	}
	
	public int getDay() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getDate());
		return cal.get(Calendar.DAY_OF_MONTH);
	}

	public String getId() {
		if (id == null) {
			return getUid();
		} else {
			return id;
		}
	}

	public void setId(String id) {
		this.id = id;
		if (uid == null) {
			uid= id+"@javlo.org";
		}
	}

	public String getUid() {
		if (uid == null) {
			return id+"@javlo.org";
		} else {
			return uid;
		}
			
	}

	public void setUid(String mainId) {
		this.uid = mainId;
	}
	
	public boolean isOneDay() {
		if (getStartDate() == null || getEndDate() == null) {
			return true;
		} else {
			return DateUtils.isSameDay(getStartDate(), getEndDate());
		}
	}
	
	public boolean isOneMonth() {
		if (getStartDate() == null || getEndDate() == null) {
			return true;
		} else {
			Calendar cal1 = Calendar.getInstance();
			cal1.setTime(getStartDate());
			Calendar cal2 = Calendar.getInstance();
			cal2.setTime(getEndDate());
			return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
		}
	}
	
	public boolean isOneYear() {
		if (getStartDate() == null || getEndDate() == null) {
			return true;
		} else {
			Calendar cal1 = Calendar.getInstance();
			cal1.setTime(getStartDate());
			Calendar cal2 = Calendar.getInstance();
			cal2.setTime(getEndDate());
			return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
		}
	}
	
	public boolean isPrevious() {
		return false;
	}

	public boolean isNext() {
		return next;
	}

	public void setNext(boolean next) {
		this.next = next;
	}
	
	public boolean isSameMonth(ICal ical) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(getDate());
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(ical.getDate());
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
	}
	
	public boolean isSameMonth(int year, int month) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(getDate());		
		return cal1.get(Calendar.YEAR) == year && cal1.get(Calendar.MONTH) == month;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public int getColorGroup() {
		return colorGroup;
	}

	public void setColorGroup(int group) {
		this.colorGroup = group;
	}
	
}
