package org.javlo.mailing;

import java.io.File;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.javlo.config.StaticConfig;
import org.javlo.navigation.MenuElement;


public class MailingContext {

	protected static final String CURRENT_MAILING_KEY = "__current_mailing";

	public static int MAILING_VIEW = 1;
	public static int HISTORY_VIEW = 2;
	public static int HELP_VIEW = 3;

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(MailingContext.class.getName());

	File[] templatesFiles = null;

	String currentTemplate = null;

	PropertiesConfiguration prop = new PropertiesConfiguration();

	//private String unsubscribeHost;

	//private int unsubscribePort;

	private final String unsubscribePathPrefix="";

	private int position = 1;

	private int view = MAILING_VIEW;

	private MenuElement page = null;

	private String absoluteURL = null;

	private String relativeURL = null;

	private String currentContent = "";

	private String currentCategory = null;

	private static final String KEY = MailingContext.class.getName();

	public static final String HOME_RENDERER = "/jsp/mailing/choose_template.jsp";
	public static final String HOME_COMMAND_RENDERER = "/jsp/mailing/choose_template_categories.jsp";

	private String commandRenderer = HOME_COMMAND_RENDERER;
	private String renderer = HOME_RENDERER;
	private boolean displayStep = true;

	private Mailing currentMailing = null;

	StaticConfig staticConfig = null;

	private MailingContext(HttpSession session) {
		staticConfig = StaticConfig.getInstance(session);
	}

	public static MailingContext getInstance(HttpSession session) {		
		MailingContext outInstance = (MailingContext) session.getAttribute(KEY);
		if (outInstance == null) {
			outInstance = new MailingContext(session);
			session.setAttribute(KEY, outInstance);
		}
		return outInstance;
	}

	public File[] getTemplates() {
		return templatesFiles;
	}

	public String[] getTemplatesLabel() {
		String[] labels = new String[templatesFiles.length];
		for (int i = 0; i < labels.length; i++) {
			labels[i] = FilenameUtils.getBaseName(templatesFiles[i].getName()).replace('_', ' ');
		}
		return labels;
	}

	public String getCurrentTemplate() {
		return currentTemplate;
	}

	public void setCurrentTemplate(String currentTemplate) {
		this.currentTemplate = currentTemplate;
	}

	public String getDefaultSender() {
		return staticConfig.getDefaultSender();
	}

	public String getDefaultSubject() {
		return staticConfig.getDefaultSubject();
	}

	public String getDefaultReport() {
		return staticConfig.getDefaultReport();
	}

	/*public String getUnsubscribeHost() {
		return unsubscribeHost;
	}

	public void setUnsubscribeHost(String unsubscribeHost) {
		this.unsubscribeHost = unsubscribeHost;
	}

	public int getUnsubscribePort() {
		return unsubscribePort;
	}

	public void setUnsubscribePort(int unsubscribePort) {
		this.unsubscribePort = unsubscribePort;
	}*/

	public String getUnsubscribePathPrefix() {
		return unsubscribePathPrefix;
	}

	public String getCommandRenderer() {
		return commandRenderer;
	}

	public void setCommandRenderer(String commandRenderer) {
		this.commandRenderer = commandRenderer;
	}

	public String getRenderer() {
		return renderer;
	}

	public void setRenderer(String renderer) {
		this.renderer = renderer;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public MenuElement getPage() {
		return page;
	}

	public void setPage(MenuElement page) {
		this.page = page;
	}

	public Mailing getCurrentMailing() {
		return currentMailing;
	}

	public void setCurrentMailing(Mailing currentMailing) {
		this.currentMailing = currentMailing;
	}

	public boolean isDisplayStep() {
		return displayStep;
	}

	public void setDisplayStep(boolean displayStep) {
		this.displayStep = displayStep;
	}

	public int getView() {
		return view;
	}

	public void setView(int view) {
		this.view = view;
	}

	public static Mailing getCurrentSessionMailing(HttpSession session) {
		return (Mailing) session.getAttribute(CURRENT_MAILING_KEY);
	}

	public static void setCurrentSessionMailing(HttpSession session, Mailing mailing) {
		session.setAttribute(CURRENT_MAILING_KEY, mailing);
	}

	public String getCurrentContent() {
		return currentContent;
	}

	public void setCurrentContent(String currentContent) {
		this.currentContent = currentContent;
	}

	public String getCurrentCategory() {
		return currentCategory;
	}

	public void setCurrentCategory(String currentCategory) {
		this.currentCategory = currentCategory;
	}

	public void setAbsoluteURL(String absoluteURL) {
		this.absoluteURL = absoluteURL;
	}

	public String getAbsoluteURL() {
		return absoluteURL;
	}

	public void setRelativeURL(String relativeURL) {
		this.relativeURL = relativeURL;
	}

	public String getRelativeURL() {
		return relativeURL;
	}


}
