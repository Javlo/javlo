package org.javlo.helper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.javlo.i18n.I18nAccess;

public class BusinessHelper {

	public static class TVAInfo {
		String name;
		String addresse;

		public String getAddresse() {
			return addresse;
		}

		public void setAddresse(String addresse) {
			this.addresse = addresse;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	private static String TVA_CHECK_URL = "http://ec.europa.eu/taxation_customs/vies/cgi-bin/viesquer";

	public static boolean checkTVA(String country, String tva) throws IOException {
		StringBuffer bufURL = new StringBuffer(TVA_CHECK_URL);
		bufURL.append("?Lang=FR&MS=");
		bufURL.append(country.toUpperCase());
		bufURL.append("&ISO=");
		bufURL.append(country.toUpperCase());
		bufURL.append("&VAT=");
		bufURL.append(tva);
		String url = bufURL.toString();

		URL urlReader = new URL(url);
		InputStream in = urlReader.openStream();
		StringBuffer responseContent = new StringBuffer();
		try {
			int read = in.read();
			while (read >= 0) {
				responseContent.append((char) read);
				read = in.read();
			}
		} finally {
			ResourceHelper.closeResource(in);
		}

		return responseContent.toString().contains("TVA valide");

	}

	public static TVAInfo getTVAInfo(String country, String tva) throws IOException {
		StringBuffer bufURL = new StringBuffer(TVA_CHECK_URL);
		bufURL.append("?Lang=FR&MS=");
		bufURL.append(country.toUpperCase());
		bufURL.append("&ISO=");
		bufURL.append(country.toUpperCase());
		bufURL.append("&VAT=");
		bufURL.append(tva);
		String url = bufURL.toString();

		URL urlReader = new URL(url);
		InputStream in = urlReader.openStream();
		StringBuffer responseContent = new StringBuffer();
		try {
			int read = in.read();
			while (read >= 0) {
				responseContent.append((char) read);
				read = in.read();
			}
		} finally {
			ResourceHelper.closeResource(in);
		}

		String content = responseContent.toString();
		String searchContent = content.toLowerCase();

		if (!responseContent.toString().contains("TVA valide")) {
			return null;
		}

		TVAInfo outInfo = new TVAInfo();

		try {
			int nameIndex = searchContent.indexOf("nom");
			int tagIndex = searchContent.substring(nameIndex).indexOf("<font face=\"verdana\" size=\"2\">") + nameIndex + "<font face=\"verdana\" size=\"2\">".length();
			int endTagIndex = searchContent.substring(tagIndex).indexOf("</td>") + tagIndex;
			outInfo.setName(content.substring(tagIndex, endTagIndex));
			int addressIndex = searchContent.indexOf("adresse");
			tagIndex = searchContent.substring(addressIndex).indexOf("<font face=\"verdana\" size=\"2\">") + addressIndex + "<font face=\"verdana\" size=\"2\">".length();
			endTagIndex = searchContent.substring(tagIndex).indexOf("</td>") + tagIndex;
			outInfo.setAddresse(content.substring(tagIndex, endTagIndex).replaceAll("\\<br\\>", " "));
		} catch (Throwable t) {
			outInfo = null;
		}
		return outInfo;

	}

	public static void main(String[] args) {
		try {
			TVAInfo tvaInfo = getTVAInfo("BE", "472575122");
			if (tvaInfo != null) {
				System.out.println("nom : " + tvaInfo.getName());
				System.out.println("adresse : " + tvaInfo.getAddresse());
			} else {
				System.out.println("TVA not found.");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * get a array for same precalculed date in the past (one day later, one week later...)
	 * 
	 * @param i18nAccess
	 *            a access to translation
	 * @return a array of string and date (date as string)
	 */
	public static String[][] getTimeArray(I18nAccess i18nAccess) {
		Calendar cal = GregorianCalendar.getInstance();
		Date currentDate = new Date();
		cal.setTime(currentDate);
		cal.roll(Calendar.DAY_OF_YEAR, false);
		Date onDayLater = cal.getTime();
		cal.setTime(currentDate);
		cal.roll(Calendar.WEEK_OF_YEAR, false);
		Date onWeekLater = cal.getTime();
		cal.setTime(currentDate);
		Calendar saveTime = GregorianCalendar.getInstance();
		saveTime.setTime(currentDate);
		cal.roll(Calendar.MONTH, false);
		if (saveTime.before(cal)) {
			cal.roll(Calendar.YEAR, false);
		}
		Date onMountLater = cal.getTime();
		cal.setTime(currentDate);
		cal.roll(Calendar.YEAR, false);
		Date onYearLater = cal.getTime();

		String[][] dateChoice = { { "", i18nAccess.getText("template.search.all") }, { StringHelper.renderDate(onDayLater), i18nAccess.getText("template.search.onedaylater") }, { StringHelper.renderDate(onWeekLater), i18nAccess.getText("template.search.oneweeklater") }, { StringHelper.renderDate(onMountLater), i18nAccess.getText("template.search.onemountlater") }, { StringHelper.renderDate(onYearLater), i18nAccess.getText("template.search.oneyearlater") }, };

		return dateChoice;
	}

	/**
	 * get a array for same precalculed date in the past (one day later, one week later...)
	 * 
	 * @param i18nAccess
	 *            a access to translation
	 * @return a array of string and date (date as string)
	 */
	public static String[][] getTimeArrayInView(I18nAccess i18nAccess) {
		Calendar cal = GregorianCalendar.getInstance();
		Date currentDate = new Date();
		cal.setTime(currentDate);
		cal.roll(Calendar.DAY_OF_YEAR, false);
		Date onDayLater = cal.getTime();
		cal.setTime(currentDate);
		cal.roll(Calendar.WEEK_OF_YEAR, false);
		Date onWeekLater = cal.getTime();
		cal.setTime(currentDate);
		Calendar saveTime = GregorianCalendar.getInstance();
		saveTime.setTime(currentDate);
		cal.roll(Calendar.MONTH, false);
		if (saveTime.before(cal)) {
			cal.roll(Calendar.YEAR, false);
		}
		Date onMountLater = cal.getTime();
		cal.setTime(currentDate);
		cal.roll(Calendar.YEAR, false);
		Date onYearLater = cal.getTime();

		String[][] dateChoice = { { "", i18nAccess.getContentViewText("template.search.all") }, { StringHelper.renderDate(onDayLater), i18nAccess.getContentViewText("template.search.onedaylater") }, { StringHelper.renderDate(onWeekLater), i18nAccess.getContentViewText("template.search.oneweeklater") }, { StringHelper.renderDate(onMountLater), i18nAccess.getContentViewText("template.search.onemountlater") }, { StringHelper.renderDate(onYearLater), i18nAccess.getContentViewText("template.search.oneyearlater") }, };

		return dateChoice;
	}

}
