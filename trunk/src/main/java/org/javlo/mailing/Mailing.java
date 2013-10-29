package org.javlo.mailing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletContext;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.utils.CSVFactory;

public class Mailing {

	public static class MailingDateSorting implements Comparator<Mailing> {

		@Override
		public int compare(Mailing o1, Mailing o2) {
			return (int) (o2.getDate().getTime() - o1.getDate().getTime());
		}

	}

	private static final String CONTENT_FILE = "content.txt";
	
	private static final String FEEDBACK_FILE = "feedback.csv";

	private static final String RECEIVERS_FILE = "receivers.properties";

	private static final String CONFIG_FILE = "mailing.properties";

	private static final String SENT_FILE = "sent.properties";

	public static final String DATA_TOKEN_UNSUBSCRIBE = "unsubscribe";

	public static final String DATA_TOKEN_UNSUBSCRIBE_URL = "unsubscribe-url";

	public static final String DATA_TOKEN_UNSUBSCRIBE_MESSAGE = "unsubscribe-message";

	public static final String DATA_TOKEN_UNSUBSCRIBE_HOST = "unsubscribe-host";

	public static final String DATA_TOKEN_UNSUBSCRIBE_POST = "unsubscribe-port";

	private Iterator<InternetAddress> currentReceiver = null;

	private PrintWriter sentOut = null;

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(Mailing.class.getName());

	private InternetAddress from;

	private Set<InternetAddress> receivers;

	private InternetAddress notif;

	private String subject;

	private String content;

	private String language;

	private String adminEmail = null;

	private Date sendDate;

	private String contextKey = null;

	private String encoding = null;

	private String id = StringHelper.getRandomId();

	private boolean send = false;

	private final Map<String, String> data = new HashMap<String, String>();

	private String unsubscribeURL = null;

	boolean html;

	private boolean TEST = false;

	private List<String> roles = Collections.EMPTY_LIST;

	private File dir = null;

	private File oldDir = null;

	private File loadedDir;

	private Date date = null;

	private String templateId = null;

	String getUnsubscribeURL(String mail) {
		String params = "?webaction=mailing.Unsubscriberole&mail=" + mail + "&roles=" + StringHelper.collectionToString(roles);
		return getUnsubscribeURL() + params;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public InternetAddress getFrom() {
		return from;
	}

	public void setFrom(InternetAddress from) {
		this.from = from;
	}

	public boolean isHtml() {
		return html;
	}

	public void setHtml(boolean html) {
		this.html = html;
	}

	public InternetAddress getNotif() {
		return notif;
	}

	public void setNotif(InternetAddress notif) {
		this.notif = notif;
		if (!getReceivers().contains(notif)) {// send a sample mail to notif
			getReceivers().add(notif);
		}
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setReceivers(Set<InternetAddress> receivers) {
		this.receivers = new LinkedHashSet<InternetAddress>(receivers);
	}

	public void addReceivers(Collection<String> to) {
		for (String internetAddress : to) {
			if (internetAddress != null) {
				if (PatternHelper.MAIL_PATTERN.matcher(internetAddress).matches()) {
					try {
						InternetAddress add = new InternetAddress(internetAddress);
						if (!receivers.contains(add)) {
							receivers.add(add);
						}
					} catch (AddressException e) {
					}
				}
			}
		}
	}

	public int getReceiversSize() {
		return receivers.size();
	}

	public void setRoles(List<String> inRoles) {
		roles = inRoles;
	}

	public boolean isExist(ServletContext application, String inID) throws IOException, ConfigurationException {

		StaticConfig staticConfig = StaticConfig.getInstance(application);

		dir = new File(staticConfig.getMailingFolder() + '/' + inID + '/');
		if (!dir.exists()) {
			dir = new File(staticConfig.getMailingHistoryFolder() + '/' + inID + '/');
			if (!dir.exists()) {
				return false;
			}
		}
		return true;
	}

	public boolean isExistInHistory(ServletContext application, String inID) throws IOException, ConfigurationException {
		StaticConfig staticConfig = StaticConfig.getInstance(application);

		dir = new File(staticConfig.getMailingHistoryFolder() + '/' + inID + '/');
		if (!dir.exists()) {
			return false;
		}

		return true;
	}

	public void load(ServletContext application, String inID) throws IOException, ConfigurationException {
		setId(application, inID);
		loadedDir = new File(dir.getAbsolutePath());
		File contentFile = new File(dir.getAbsolutePath() + '/' + CONTENT_FILE);
		if (!contentFile.exists()) {
			return;
		}
		File receiversFile = new File(dir.getAbsolutePath() + '/' + RECEIVERS_FILE);
		receivers = new LinkedHashSet<InternetAddress>();
		for (String line : FileUtils.readLines(receiversFile, ContentContext.CHARACTER_ENCODING)) {
			try {
				receivers.add(new InternetAddress(line));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		PropertiesConfiguration config = new PropertiesConfiguration();
		File configFile = new File(dir.getAbsolutePath() + '/' + CONFIG_FILE);
		InputStream in = new FileInputStream(configFile);
		try {
			config.load(in, ContentContext.CHARACTER_ENCODING);
		} finally {
			ResourceHelper.closeResource(in);
		}
		try {
			TEST = config.getBoolean("test", false);
			subject = config.getString("subject", "[subject not found]");
			language = config.getString("language", "en");
			contextKey = config.getString("context-key", null);
			encoding = config.getString("encoding", ContentContext.CHARACTER_ENCODING);
			unsubscribeURL = config.getString("unsubscribeURL", null);
			roles = StringHelper.stringToCollection(config.getString("roles", ""));
			templateId = config.getString("template", null);
			adminEmail = config.getString("admin.email", null);
			try {
				date = StringHelper.parseTime(config.getString("date"));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			String sendDateStr = config.getString("send-date", null);
			if (sendDateStr != null) {
				try {
					sendDate = StringHelper.parseTime(sendDateStr);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			setSend(config.getBoolean("send", true));
			content = FileUtils.readFileToString(contentFile, encoding);
		} catch (RuntimeException e1) {
			logger.warning(e1.getMessage());
		}
		try {
			from = new InternetAddress(config.getString("sender", ""));
			from.setPersonal(from.getPersonal(), encoding);
		} catch (AddressException e) {
			logger.finest("bad 'from' address found in '" + CONFIG_FILE + "' : " + config.getString("sender"));
		}
		try {
			notif = new InternetAddress(config.getString("notif", ""));
		} catch (AddressException e) {
			logger.finest("bad 'notif' address found in '" + CONFIG_FILE + "' : " + config.getString("notif"));
		}
		logger.finest("load mailing subject : " + subject);
	}

	Map<String, String> loadSent() throws IOException {
		Map<String, String> out = new HashMap<String, String>();
		File sentFile = new File(loadedDir.getAbsolutePath() + '/' + SENT_FILE);
		if (sentFile.exists()) {
			Properties sentProperties = new Properties();
			FileInputStream in = null;
			try {
				in = new FileInputStream(sentFile);
				sentProperties.load(new InputStreamReader(in, ContentContext.CHARSET_DEFAULT));
			} finally {
				ResourceHelper.safeClose(in);
			}
			for (String key : sentProperties.stringPropertyNames()) {
				out.put(key, sentProperties.getProperty(key));
			}
		}
		return out;
	}

	public void store(ServletContext application) throws IOException, ConfigurationException {

		setId(application, getId());
		if (!dir.exists()) {
			logger.info("create directory : " + dir);
			dir.mkdirs();
		}

		logger.info("create emailing directory : " + dir);

		File contentFile = new File(dir.getAbsolutePath() + '/' + CONTENT_FILE);
		ResourceHelper.writeStringToFile(contentFile, content, ContentContext.CHARACTER_ENCODING);
		File receiversFile = new File(dir.getAbsolutePath() + '/' + RECEIVERS_FILE);
		Collection<String> lines = new LinkedList<String>();
		for (InternetAddress receiver : receivers) {
			if (receiver != null) {
				lines.add(receiver.toUnicodeString());
			}
		}
		FileUtils.writeLines(receiversFile, lines);

		PropertiesConfiguration config = new PropertiesConfiguration();
		File configFile = new File(dir.getAbsolutePath() + '/' + CONFIG_FILE);
		config.setProperty("subject", subject);
		config.setProperty("language", language);
		config.setProperty("sender", from.toUnicodeString());
		if (notif != null) {
			config.setProperty("notif", notif.toUnicodeString());
		}
		config.setProperty("send", new Boolean(isSend()));
		config.setProperty("roles", StringHelper.collectionToString(roles));
		config.setProperty("encoding", encoding);
		config.setProperty("date", StringHelper.renderTime(new Date()));
		config.setProperty("test", TEST);
		config.setProperty("context-key", contextKey);
		if (sendDate != null) {
			config.setProperty("send-date", StringHelper.renderTime(sendDate));
		}
		if (templateId != null) {
			config.setProperty("template", templateId);
		}
		Collection<Map.Entry<String, String>> dataKeys = data.entrySet();
		for (Map.Entry<String, String> entry : dataKeys) {
			config.setProperty(entry.getKey(), entry.getValue());
		}
		if (getUnsubscribeURL() != null) {
			config.setProperty("unsubscribeURL", getUnsubscribeURL());
		}
		if (adminEmail != null) {
			config.setProperty("admin.email", adminEmail);
		}
		ResourceHelper.writePropertiesToFile(config, configFile);
	}

	public void close(ServletContext application) throws IOException {
		StaticConfig staticConfig = StaticConfig.getInstance(application);
		File sourceDir = new File(staticConfig.getMailingFolder() + '/' + id + '/');
		File targetDir = new File(staticConfig.getMailingHistoryFolder() + '/' + id + '/');
		FileUtils.copyDirectory(sourceDir, targetDir);
		FileUtils.deleteDirectory(sourceDir);
		loadedDir = targetDir;
	}

	@Override
	public String toString() {
		return "id:" + id + " - subject:" + subject + " - from:" + from + " - notif:" + notif + " - valid:" + isValid() + " - send:" + isSend();
	}

	/* ** MAILING CODE ** */

	public void onStartMailing() throws IOException {
		synchronized (receivers) {
			Map<String, String> sent = loadSent();
			List<InternetAddress> newList = new LinkedList<InternetAddress>();
			for (InternetAddress receiver : receivers) {
				if (!sent.containsKey(getSentKey(receiver))) {
					newList.add(receiver);
				}
			}
			currentReceiver = newList.iterator();
			File sentFile = new File(loadedDir.getAbsolutePath() + '/' + SENT_FILE);
			sentOut = new PrintWriter(new OutputStreamWriter(FileUtils.openOutputStream(sentFile, true), ContentContext.CHARSET_DEFAULT));
		}
	}

	public String getSentKey(InternetAddress key) {
		return key.getAddress().toLowerCase();
	}

	public void onMailSent(InternetAddress to) throws IOException {
		synchronized (sentOut) {
			String key = getSentKey(to);
			String value = StringHelper.renderSortableTime(new Date());
			String line = StringHelper.escapeProperty(key, true, true) + "=" + StringHelper.escapeProperty(value, false, true);
			sentOut.println(line);
			sentOut.flush();
		}
	}

	public InternetAddress getNextReceiver() {
		InternetAddress outAddress = null;
		while ((outAddress == null) && (currentReceiver.hasNext())) {
			outAddress = currentReceiver.next();
		}
		if (outAddress == null) {
			setSend(true);
		}
		return outAddress;
	}

	public void onEndMailing() {
		ResourceHelper.safeClose(sentOut);
	}

	public boolean isSend() {
		return send;
	}

	public void setSend(boolean send) {
		this.send = send;
	}

	public Set<InternetAddress> getReceivers() {
		if (receivers == null) {
			receivers = new LinkedHashSet<InternetAddress>();
		}
		return receivers;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getUnsubscribeURL() {
		return unsubscribeURL;
	}

	public void setUnsubscribeURL(String unsubscribeURL) {
		this.unsubscribeURL = unsubscribeURL;
	}

	public void addData(String key, String value) {
		data.put(key, value);
	}

	public String getData(String key) {
		return data.get(key);
	}

	public Collection<Map.Entry<String, String>> getAllData() {
		return data.entrySet();
	}

	public String getId() {
		return id;
	}

	public String getContextKey() {
		return contextKey;
	}

	public void setContextKey(String contextKey) {
		this.contextKey = contextKey;
	}

	public void setId(ServletContext application, String id) {
		StaticConfig staticConfig = StaticConfig.getInstance(application);
		this.id = id;
		dir = new File(staticConfig.getMailingFolder() + '/' + id + '/');
		if (!dir.exists()) {
			dir = new File(staticConfig.getMailingHistoryFolder() + '/' + id + '/');
			if (dir.exists()) {
				send = true;
			} else {
				dir = new File(staticConfig.getMailingFolder() + '/' + id + '/');
				dir.mkdirs();
			}
		}
		oldDir = new File(staticConfig.getMailingHistoryFolder() + '/' + id + '/');
	}

	public void addFeedBack(FeedBackMailingBean bean) throws IOException {
		synchronized (FEEDBACK_FILE) {
			if (!oldDir.exists()) {
				oldDir.mkdirs();
			}
			File file = new File(oldDir.getAbsolutePath() + '/' + FEEDBACK_FILE);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream out = new FileOutputStream(file, true);
			BufferedWriter outBuf = new BufferedWriter(new OutputStreamWriter(out));
			String[] line = bean.toArray();
			for (int i = 0; i < line.length; i++) {
				outBuf.append(line[i]);
				if (i < line.length) {
					outBuf.append(',');
				}
			}
			outBuf.newLine();
			outBuf.close();
		}
	}

	public List<FeedBackMailingBean> getFeedBack() throws IOException {
		File file = new File(dir.getAbsolutePath() + '/' + FEEDBACK_FILE);
		if (!file.exists()) {
			return Collections.emptyList();
		}
		CSVFactory fact = new CSVFactory(file, ",");
		List<FeedBackMailingBean> outFB = new LinkedList<FeedBackMailingBean>();
		String[][] data = fact.getArray();
		for (String[] element : data) {
			FeedBackMailingBean bean = new FeedBackMailingBean();
			bean.fromArray(element);
			outFB.add(bean);
		}
		return outFB;
	}

	public Date getDate() {
		return date;
	}

	public List<String> getRoles() {
		return roles;
	}

	public boolean isTest() {
		return TEST;
	}

	public void setTest(boolean test) {
		this.TEST = test;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public Date getSendDate() {
		return sendDate;
	}

	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public void setAdminEmail(String adminEmail) {
		try {
			new InternetAddress(adminEmail);
			this.adminEmail = adminEmail;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isValid() {
		File contentFile = new File(dir.getAbsolutePath() + '/' + CONTENT_FILE);
		File receiversFile = new File(dir.getAbsolutePath() + '/' + RECEIVERS_FILE);
		File configFile = new File(dir.getAbsolutePath() + '/' + CONFIG_FILE);
		return contentFile.exists() && receiversFile.exists() && configFile.exists();
	}
	
}
