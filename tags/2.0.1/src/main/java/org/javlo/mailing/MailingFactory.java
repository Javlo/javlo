package org.javlo.mailing;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.apache.commons.configuration.ConfigurationException;
import org.javlo.config.StaticConfig;
import org.javlo.filter.NumericDirectoryFilter;


public class MailingFactory {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(MailingFactory.class.getName());

	public static final String KEY = MailingFactory.class.getName();

	private ServletContext application = null;

	private String mailingFolder = null;
	private String mailingHistoryFolder = null;

	public static MailingFactory getInstance(ServletContext application) {
		MailingFactory outInstance = (MailingFactory) application.getAttribute(KEY);
		if (outInstance == null) {
			outInstance = new MailingFactory();

			outInstance.mailingFolder = StaticConfig.getInstance(application).getMailingFolder();
			outInstance.mailingHistoryFolder = StaticConfig.getInstance(application).getMailingHistoryFolder();
			outInstance.application = application;
			
			application.setAttribute(KEY, outInstance);
		}
		return outInstance;
	}

	/**
	 * return the list of mailing id stored in this webapps.
	 * 
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public Mailing[] getMailingList() throws ConfigurationException, IOException {
		Collection<Mailing> arrayList = new LinkedList<Mailing>();
		File mailingDir = new File(mailingFolder + '/');
		if (mailingDir.exists()) {
			File[] currentMailingDir = mailingDir.listFiles(new NumericDirectoryFilter());
			for (int i = 0; i < currentMailingDir.length; i++) {
				String mailingID = currentMailingDir[i].getName();
				Mailing mailing = new Mailing();
				mailing.setId(application, mailingID);
				if (mailing.isValid()) {
					mailing.load(application, mailingID);
					arrayList.add(mailing);
				}
			}
		} else {
			logger.finest("mailing directory: " + mailingDir + " not found.");
		}
		Mailing[] outFinal = new Mailing[arrayList.size()];
		arrayList.toArray(outFinal);
		return outFinal;
	}

	/**
	 * return the list of mailing id stored in this webapps.
	 * 
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public Mailing[] getOldMailingList() throws ConfigurationException, IOException {
		Collection<Mailing> arrayList = new LinkedList<Mailing>();
		File mailingDir = new File(mailingHistoryFolder + '/');
		if (mailingDir.exists()) {
			File[] currentMailingDir = mailingDir.listFiles(new NumericDirectoryFilter());
			for (int i = 0; i < currentMailingDir.length; i++) {
				String mailingID = currentMailingDir[i].getName();
				Mailing mailing = new Mailing();
				mailing.setId(application, mailingID);
				if (mailing.isValid()) {
					mailing.load(application, mailingID);
					arrayList.add(mailing);
				}
			}
		} else {
			logger.finest("mailing directory: " + mailingDir + " not found.");
		}
		Mailing[] outFinal = new Mailing[arrayList.size()];
		arrayList.toArray(outFinal);
		Arrays.sort(outFinal, new Mailing.MailingDateSorting());
		return outFinal;
	}

	public List<Mailing> getOldMailingList(String sender) throws ConfigurationException, IOException {
		List<Mailing> outList = new LinkedList<Mailing>();
		Mailing[] mailingList = getOldMailingList();
		for (Mailing mailing : mailingList) {
			if (mailing.getFrom().getAddress().equals(sender)) {
				outList.add(mailing);
			}
		}
		return outList;
	}

	public List<Mailing> getOldMailingListByContext(String contextKey) throws ConfigurationException, IOException {
		List<Mailing> outList = new LinkedList<Mailing>();
		Mailing[] mailingList = getOldMailingList();
		for (Mailing mailing : mailingList) {
			if (mailing.getContextKey().equals(contextKey)) {
				outList.add(mailing);
			}
		}
		return outList;
	}

	public Mailing getMailing(String id) throws ConfigurationException, IOException {
		Mailing[] mailingList = getOldMailingList();
		for (Mailing mailing : mailingList) {
			if (mailing.getId().equals(id)) {
				return mailing;
			}
		}
		return null;
	}

}
