package org.javlo.mailing;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.javlo.config.MailingStaticConfig;
import org.javlo.config.StaticConfig;
import org.javlo.filter.NumericDirectoryFilter;

public class MailingFactory {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(MailingFactory.class.getName());

	public static final String KEY = MailingFactory.class.getName();

	// private ServletContext application = null;

	private MailingStaticConfig mailingStaticConfig;

	private String mailingFolder = null;
	private String mailingHistoryFolder = null;

	public static MailingFactory getInstance(ServletContext application) {
		MailingFactory outInstance = (MailingFactory) application.getAttribute(KEY);
		if (outInstance == null) {
			outInstance = new MailingFactory();

			outInstance.mailingFolder = StaticConfig.getInstance(application).getMailingFolder();
			outInstance.mailingHistoryFolder = StaticConfig.getInstance(application).getMailingHistoryFolder();
			// outInstance.application = application;

			outInstance.mailingStaticConfig = StaticConfig.getInstance(application).getMailingStaticConfig();

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
	public List<Mailing> getMailingList() throws IOException {
		List<Mailing> outMailing = new LinkedList<Mailing>();
		File mailingDir = new File(mailingFolder + '/');
		if (mailingDir.exists()) {
			File[] currentMailingDir = mailingDir.listFiles(new NumericDirectoryFilter());
			if (currentMailingDir != null) {
				for (int i = 0; i < currentMailingDir.length; i++) {
					String mailingID = currentMailingDir[i].getName();
					Mailing mailing = new Mailing();
					mailing.setId(mailingStaticConfig, mailingID);
					if (mailing.isValid()) {
						mailing.load(mailingStaticConfig, mailingID);
						outMailing.add(mailing);
					}
				}
			} else {
				logger.severe("problem on access file in : '" + mailingDir + "' please check this folder.");
			}
		} else {
			logger.finest("mailing directory: " + mailingDir + " not found.");
		}
		return outMailing;
	}

	/**
	 * return the list of mailing id stored in this webapps.
	 * 
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public List<Mailing> getOldMailingList() throws IOException {
		List<Mailing> outMailing = new LinkedList<Mailing>();
		File mailingDir = new File(mailingHistoryFolder + '/');
		if (mailingDir.exists()) {
			File[] currentMailingDir = mailingDir.listFiles(new NumericDirectoryFilter());
			for (int i = 0; i < currentMailingDir.length; i++) {
				String mailingID = currentMailingDir[i].getName();
				Mailing mailing = new Mailing();
				mailing.setId(mailingStaticConfig, mailingID);
				if (mailing.isValid()) {
					mailing.load(mailingStaticConfig, mailingID);
					outMailing.add(mailing);
				}
			}
		} else {
			logger.finest("mailing directory: " + mailingDir + " not found.");
		}
		return outMailing;
	}

	public List<Mailing> getOldMailingList(String sender) throws IOException {
		List<Mailing> outList = new LinkedList<Mailing>();
		for (Mailing mailing : getOldMailingList()) {
			if (mailing.getFrom().getAddress().equals(sender)) {
				outList.add(mailing);
			}
		}
		return outList;
	}

	public List<Mailing> getOldMailingListByContext(String contextKey) throws IOException {
		List<Mailing> outList = new LinkedList<Mailing>();
		for (Mailing mailing : getOldMailingList()) {
			if (mailing.getContextKey() != null) {
				if (mailing.getContextKey().equals(contextKey)) {
					outList.add(mailing);
				}
			} else {
				logger.warning("mailing without context : " + mailing.getSubject());
			}
		}
		return outList;
	}

	public List<Mailing> getMailingListByContext(String contextKey) throws IOException {
		List<Mailing> outList = new LinkedList<Mailing>();
		for (Mailing mailing : getMailingList()) {
			if (mailing.getContextKey() != null) {
				if (mailing.getContextKey().equals(contextKey)) {
					outList.add(mailing);
				}
			} else {
				logger.warning("mailing without context : " + mailing.getSubject());
			}
		}
		return outList;
	}

	public Mailing getMailing(String id) throws IOException {
		for (Mailing mailing : getOldMailingList()) {
			if (mailing.getId().equals(id)) {
				return mailing;
			}
		}
		return null;
	}

	public Mailing getLiveMailing(String id) throws IOException {
		for (Mailing mailing : getMailingList()) {
			if (mailing.getId().equals(id)) {
				return mailing;
			}
		}
		return null;
	}

}
