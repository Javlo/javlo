/*
 * Created on 17-oct.-2004
 */
package org.javlo.context;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;

/**
 * @author pvandermaesen
 * 
 *         contain the state of the statistique pages
 * 
 */
public class StatContext implements Serializable {

	public static final String MAILING_VIEW = "mailing";

	private String[] statChoices;

	private static String SESSION_KEY = "stat-ctx";

	private String currentStat;
	private Date from = new Date(0);
	private Date to = new Date();
	private String parentPath = null;
	private String currentMailing = null;
	private String mailingFilter = "";

	private StatContext(HttpServletRequest request) {
		Calendar fromCal = GregorianCalendar.getInstance();
		fromCal.add(Calendar.MONTH, -1);
		from = fromCal.getTime();
		request.getSession().setAttribute(SESSION_KEY, this);
		GlobalContext glContext = GlobalContext.getInstance(request);
		if (!glContext.isMailing()) {
			statChoices = new String[] { "report", "pages", "language", "days", "days-session", "hours", "referer" };
		} else {
			statChoices = new String[] { "report", "pages", "language", "days", "days-session", "hours", "referer", MAILING_VIEW };
		}
		currentStat = statChoices[0];
	}

	public static final StatContext getInstance(HttpServletRequest request) {
		StatContext ctx = (StatContext) request.getSession().getAttribute(SESSION_KEY);
		if (ctx == null) {
			ctx = new StatContext(request);
		}
		return ctx;
	}

	/**
	 * @return Returns the statChoices.
	 */
	public String[] getStatChoices() {
		return statChoices;
	}

	/**
	 * @return Returns the currentStat.
	 */
	public String getCurrentStat() {
		return currentStat;
	}

	/**
	 * @param currentStat
	 *            The currentStat to set.
	 */
	public void setCurrentStat(String currentStat) {
		this.currentStat = currentStat;
	}

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public Date getTo() {
		return to;
	}

	public void setTo(Date to) {
		this.to = to;
	}

	public String getParentPath() {
		return parentPath;
	}

	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}

	public String getCurrentMailing() {
		return currentMailing;
	}

	public void setCurrentMailing(String currentMailing) {
		this.currentMailing = currentMailing;
	}

	public String getMailingFilter() {
		return mailingFilter;
	}

	public void setMailingFilter(String mailingFilter) {
		this.mailingFilter = mailingFilter;
	}
}
