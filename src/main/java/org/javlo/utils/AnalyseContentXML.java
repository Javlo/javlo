package org.javlo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XMLManipulationHelper;
import org.javlo.helper.XMLManipulationHelper.TagDescription;

public class AnalyseContentXML {

	private static final String CONTENT_FILE = "/tmp/content.xml";

	static String getContent() throws IOException {
		StringBuffer content = new StringBuffer();
		InputStream in = new FileInputStream(CONTENT_FILE);
		try {
			int read = in.read();
			while (read >= 0) {
				content.append((char) read);
				read = in.read();
			}
		} finally {
			ResourceHelper.closeResource(in);
		}
		return content.toString();
	}

	static String getContent(File file) throws IOException {
		StringBuffer content = new StringBuffer();
		InputStream in = new FileInputStream(file);
		try {
			int read = in.read();
			while (read >= 0) {
				content.append((char) read);
				read = in.read();
			}
		} finally {
			ResourceHelper.closeResource(in);
		}
		return content.toString();
	}

	static Collection<String> getCreators(String content) throws Exception {
		Collection<String> users = new TreeSet<String>();

		int countCreator = 0;
		TagDescription[] tags = XMLManipulationHelper.searchAllTag(content, false);
		for (TagDescription tag : tags) {
			if (tag.getAttributes().get("creator") != null) {
				String dateStr = tag.getAttributes().get("creationDate");
				Date date = StringHelper.parseDate(dateStr, "yyyy/MM/dd HH:mm:ss");
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);

				Date datePivot = StringHelper.parseDate("01/06/2009");
				Calendar pivot = Calendar.getInstance();
				pivot.setTime(datePivot);

				if (cal.after(pivot)) {
					users.add(tag.getAttributes().get("creator"));
					countCreator++;
				}
			}
		}
		System.out.println("countCreator = " + countCreator);
		return users;
	}

	public static void main(String[] args) {
		System.out.println("*** ANALYSE CONTENT ***");

		try {
			Collection<String> users = getCreators(getContent());
			int c = 0;
			for (String user : users) {
				c++;
				System.out.println("user = " + user);
			}
			System.out.println("count = " + c);

			String content = getContent(new File("/tmp/ecard_report.txt"));
			System.out.println("MAILING REPORT count : " + content.split("MAILING REPORT").length);

			System.out.println("**** email send : " + StringHelper.searchEmail(getContent(new File("/tmp/ecard_report.txt"))).size());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
