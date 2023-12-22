package org.javlo.component.form;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.mail.internet.InternetAddress;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload2.core.FileItem;
import org.javlo.actions.IAction;
import org.javlo.component.config.ComponentConfig;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.Comparator.StringComparator;
import org.javlo.mailing.EMail;
import org.javlo.mailing.MailConfig;
import org.javlo.mailing.MailService;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.CaptchaService;
import org.javlo.service.ContentService;
import org.javlo.service.ListService;
import org.javlo.service.RequestService;
import org.javlo.service.participation.ParticipationListService;
import org.javlo.user.IUserFactory;
import org.javlo.user.UserFactory;
import org.javlo.utils.CSVFactory;
import org.javlo.utils.CollectionAsMap;
import org.javlo.utils.JSONMap;
import org.javlo.ztatic.StaticInfo;

/**
 * store html form in csv file and send email with parameters. For use this
 * component you need to create a renderer with a html form. this form need at
 * least two field : <code>
 * &lt;input type=&quot;hidden&quot; name=&quot;webaction&quot; value=&quot;gform-registering.submit&quot; /&gt;<br/>&lt;input type=&quot;hidden&quot; name=&quot;comp_id&quot; value=&quot;${comp.id}&quot; /&gt; </code>
 * . You can define required field with uppercase letter : "Firstname" >
 * requierd, "firstname" > not requiered. for use captacha you need to tag :
 * <code>&lt;img src=&quot;${info.captchaURL}&quot; alt=&quot;captcha&quot; /&gt;&lt;/label&gt;<br/>&lt;input type=&quot;text&quot; id=&quot;captcha&quot; name=&quot;captcha&quot; value=&quot;&quot; /&gt;</code>
 * <h4>JSTL variable :</h4>
 * <ul>
 * <li>inherited from {@link AbstractVisualComponent}</li>
 * <li>{@link String} msg : message to display.</li>
 * <li>{@link Map} errorFields: field with error.</li>
 * <li>{@link String} valid: contains true if form is valid.</li>
 * </ul>
 * <h5>Local JSTL</h5>
 * <ul>
 * <li>ci18n : local i18n map.</li>
 * </ul>
 * <h4>keys for message and config can be use in content</h4>
 * <ul>
 * <li>captcha : true for use captacha</li>
 * <li>error.required : error message if requiered field is'nt filled.</li>
 * <li>error.captcha : message if catacha value is'nt correct.</li>
 * <li>message.thanks : confirmation message.</li>
 * <li>field.fake : name of fake field.</li>
 * </ul>
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class GenericForm extends AbstractVisualComponent implements IAction {
	
	public static final String DYNAMIC_FORM_RESULT_FOLDER = "dynamic-form-result";

	private static final String RECAPTCHASECRETKEY = "recaptchasecretkey";

	private static final String RECAPTCHAKEY = "recaptchakey";

	private static Logger logger = Logger.getLogger(GenericForm.class.getName());

	private Properties bundle;

	protected static final Object LOCK = new Object();

	@Override
	protected void init(ComponentBean bean, ContentContext ctx) throws Exception {
		super.init(bean, ctx);
		if (getValue() == null || getValue().trim().length() == 0) {
			setValue(StringHelper.sortText(getConfig(ctx).getRAWConfig(ctx, ctx.getCurrentTemplate(), getType())));
		}
	}

	@Override
	public Map<String, String> getRenderes(ContentContext ctx) {
		Properties properties = getLocalConfig(false);
		if (properties == null) {
			return Collections.EMPTY_MAP;
		}
		Map<String, String> outRenderers = new Hashtable<String, String>();
		Enumeration<Object> keys = properties.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if (key.startsWith("renderer.") && key.split(".").length < 3) {
				String value = properties.getProperty(key);
				key = key.replaceFirst("renderer.", "");
				outRenderers.put(key, value);
			}
		}

		if (outRenderers.size() == 0) {
			return super.getRenderes(ctx);
		} else {
			try {
				if (ctx.getCurrentTemplate() != null) {
					for (String key : outRenderers.keySet()) {
						String value = outRenderers.get(key);
						outRenderers.put(key, value);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return outRenderers;
		}
	}

	@Override
	public ComponentConfig getConfig(ContentContext ctx) {
		if ((ctx == null) || (ctx.getRequest() == null) || ((ctx.getRequest().getSession() == null))) {
			return ComponentConfig.getInstance();
		}
		return ComponentConfig.getInstance(ctx, getType());
	}

	@Override
	public String getType() {
		return "generic-form";
	}

	protected boolean isCaptcha(ContentContext ctx) {
		return StringHelper.isTrue(getLocalConfig(false).getProperty("captcha", "" + isCaptcha()));
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		Properties prop = getLocalConfig(false);
		Map<String,String> ci18n = new HashMap<String, String>();
		for (Object key : prop.keySet()) {			
			String value = prop.getProperty(key.toString());
			value = XHTMLHelper.autoLink(value, ctx.getGlobalContext());
			ci18n.put(key.toString(), value);
		}
		ctx.getRequest().setAttribute("ci18n", ci18n);

		ListService listService = ListService.getInstance(ctx);
		for (Object key : prop.keySet()) {
			if (((String) key).startsWith("list.")) {
				listService.addList(((String) key).replaceFirst("list.", ""), StringHelper.stringToCollection(prop.getProperty(((String) key)), ";"));
			}
		}
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		IUserFactory userFactory = UserFactory.createUserFactory(ctx.getRequest());
		if (userFactory.getCurrentUser(ctx.getGlobalContext(), ctx.getRequest().getSession()) != null) {
			Map<String, String> userInfo = BeanHelper.bean2Map(userFactory.getCurrentUser(ctx.getGlobalContext(), ctx.getRequest().getSession()).getUserInfo());
			for (String key : userInfo.keySet()) {
				if (!StringHelper.isEmpty(userInfo.get(key)) && StringHelper.isEmpty(rs.getParameter(key, null))) {
					rs.setParameter(key, userInfo.get(key));
				}
			}
		}
	}

	@Override
	public String getActionGroupName() {
		return "gform";
	}

	@Override
	public String getHexColor() {
		return IContentVisualComponent.WEB2_COLOR;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

	public Properties getLocalConfig(boolean reload) {
		if (bundle == null || reload) {
			bundle = new Properties();
			try {
				bundle.load(new StringReader(getValue()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bundle;
	}

	protected File getFile(ContentContext ctx) throws IOException {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String fileName = "df-" + getId() + ".csv";
		if (getLocalConfig(false).get("filename") != null && getLocalConfig(false).get("filename").toString().trim().length() > 0) {
			fileName = getLocalConfig(false).getProperty("filename");
		}
		File file = new File(URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getStaticFolder(), DYNAMIC_FORM_RESULT_FOLDER, fileName));
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		return file;
	}

	protected File getAttachFolder(ContentContext ctx) throws IOException {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String fileName = "df-" + getId();
		if (getLocalConfig(false).get("filename") != null) {
			fileName = getLocalConfig(false).getProperty("filename");
		}
		fileName = StringHelper.getFileNameWithoutExtension(fileName);
		File dir = new File(URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getStaticFolder(), DYNAMIC_FORM_RESULT_FOLDER, fileName));
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}

	protected void storeResult(ContentContext ctx, Map<String, String> data) throws IOException {

		synchronized (LOCK) {
			File file = getFile(ctx);
			Collection<String> titles = CSVFactory.loadTitle(file);
			boolean newTitleFound = false;
			for (String newTitle : data.keySet()) {
				if (!titles.contains(newTitle)) {
					newTitleFound = true;
				}
			}
			if (newTitleFound) {
				List<Map<String, String>> newData = CSVFactory.loadContentAsMap(file);
				newData.add(data);
				CSVFactory.storeContentAsMap(file, newData, true);
			} else {
				CSVFactory.appendContentAsMap(file, data, true);
			}
		}
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		String msg = super.performEdit(ctx);
		getLocalConfig(true);
		return msg;
	}

	public boolean isCaptcha() {
		return StringHelper.isTrue(getLocalConfig(false).getProperty("captcha", "true"));
	}

	protected boolean isSendEmail() {
		return true;
	}

	protected boolean isStorage() {
		return StringHelper.isTrue(getLocalConfig(false).getProperty("mail.store", null), true);
	}

	protected String getMailHeader(ContentContext ctx) {
		return "";
	}

	protected String getMailFooter(ContentContext ctx) {
		return "";
	}

	protected boolean isHTMLMail() {
		return false;
	}

	protected String getConfig(ContentContext ctx, String field, String condition) {
		return getLocalConfig(false).getProperty("config." + field + '.' + condition, null);
	}

	protected String getConfigMessage(ContentContext ctx, String field, String condition) {
		return getConfig(ctx).getProperty("config." + field + '.' + condition + ".message", null);
	}

	protected Integer getMaxSize(ContentContext ctx, String field) {
		String maxSize = getConfig(ctx, field, "max-size");
		if (maxSize == null) {
			return Integer.MAX_VALUE;
		} else {
			return Integer.parseInt(maxSize);
		}
	}
	
	public String getRecaptchaKey() {
		return getLocalConfig(false).getProperty(RECAPTCHAKEY, null);
	}

	public String getRecaptchaSecretKey() {
		return getLocalConfig(false).getProperty(RECAPTCHASECRETKEY, null);
	}

	public static String performSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception {

		RequestService rs = RequestService.getInstance(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		ContentService content = ContentService.getInstance(request);
		GenericForm comp = (GenericForm) content.getComponent(ctx, rs.getParameter("comp_id", null));

		/** check captcha **/
		String captcha = rs.getParameter("captcha", null);

		if (comp.isCaptcha(ctx)) {
			
			if (StringHelper.isEmpty(comp.getRecaptchaSecretKey())) {
			
				if (captcha == null || CaptchaService.getInstance(request.getSession()).getCurrentCaptchaCode() == null || !CaptchaService.getInstance(request.getSession()).getCurrentCaptchaCode().equals(captcha)) {
					logger.warning("bad captcha");
					GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("error.captcha", "bad captcha."), GenericMessage.ERROR);
					MessageRepository.getInstance(ctx).setGlobalMessage(msg);
					request.setAttribute("msg", msg);
					request.setAttribute("error_captcha", "true");
					logger.info("error captcha local");
					return null;
				} else {
					CaptchaService.getInstance(request.getSession()).reset();
				}
			
			} else {			
				String userIP = request.getHeader("x-real-ip");
				String url = URLHelper.addAllParams("https://www.google.com/recaptcha/api/siteverify", "secret=" + comp.getRecaptchaSecretKey(), "response=" + rs.getParameter("g-recaptcha-response", ""), "remoteip=" + userIP);				
				String captchaResponse = NetHelper.readPage(new URL(url));
				JSONMap map = JSONMap.parseMap(captchaResponse);
				
				if (map == null || !StringHelper.isTrue(map.get("success"))) {
					GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("error.captcha", "bad captcha."), GenericMessage.ERROR);
					request.setAttribute("msg", msg);
					request.setAttribute("error_captcha", "true");
					logger.info("error captcha google");
					return null;
				}
			
			}
		}

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		EMail email = new EMail();
		String subject = "GenericForm submit : '" + globalContext.getGlobalTitle() + "' ";

		String subjectField = comp.getLocalConfig(false).getProperty("mail.subject.field", null);
		if (subjectField != null && rs.getParameter(subjectField, null) != null) {
			subject = comp.getLocalConfig(false).getProperty("mail.subject", "") + rs.getParameter(subjectField, null);
		} else {			
			subject = comp.getLocalConfig(false).getProperty("mail.subject", subject);
		}
		
		Map<String, Object> params = rs.getParameterMap();
		Map<String, String> result = new HashMap<String, String>();
		List<String> errorFields = new LinkedList<String>();
		result.put("__registration time", StringHelper.renderSortableTime(new Date()));
		result.put("__local addr", request.getLocalAddr());
		result.put("__remote addr", request.getRemoteAddr());
		result.put("__X-Forwarded-For", request.getHeader("x-forwarded-for"));
		result.put("__X-Real-IP", request.getHeader("x-real-ip"));
		result.put("__referer", request.getHeader("referer"));

		String fakeField = comp.getLocalConfig(false).getProperty("field.fake", "fake");
		boolean withXHTML = StringHelper.isTrue(comp.getLocalConfig(false).getProperty("field.xhtml", null));
		boolean withAttachment = StringHelper.isTrue(comp.getLocalConfig(false).getProperty("mail.attachment", null), true);		
		boolean fakeFilled = false;
		String readRole = comp.getLocalConfig(false).getProperty("mail.read-role", null);

		List<String> keys = new LinkedList<String>(params.keySet());
		Collections.sort(keys, new StringComparator());

		/** store attach files **/

		Map<String, String> specialValues = new HashMap<String, String>();

		String badFileFormatRAW = comp.getLocalConfig(false).getProperty("file.bad-file", "exe,bat,scr,bin,obj,lib,dll,bat,sh,com,cmd,msi,jsp,xml,html,htm,vbe,wsf,wsc,asp");
		List<String> badFileFormat = StringHelper.stringToCollection(badFileFormatRAW, ",");
		String maxFileSizeRAW = comp.getLocalConfig(false).getProperty("file.max-size", "" + (10 * 1024 * 1024));
		long maxFileSize = Long.parseLong(maxFileSizeRAW);

		Set<String> attachField = new HashSet<String>();
		for (FileItem file : rs.getAllFileItem()) {
			attachField.add(file.getFieldName());
			String ext = StringHelper.getFileExtension(file.getName()).toLowerCase();
			if (badFileFormat.contains(ext)) {
				logger.warning("file blocked because bad extenstion : " + file.getName());
				GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("message.bad-file", "bad file format."), GenericMessage.ERROR);
				request.setAttribute("msg", msg);
				return null;
			}
			InputStream in = file.getInputStream();
			if (in != null) {
				try {
					if (!withAttachment) {
						if (file.getName().trim().length() > 0) {
							String fileName = URLHelper.mergePath(comp.getAttachFolder(ctx).getAbsolutePath(), StringHelper.createFileName(file.getName()));
							File freeFile = ResourceHelper.getFreeFileName(new File(fileName));
							if (ResourceHelper.writeStreamToFile(in, freeFile, maxFileSize) < 0) {
								GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("message.tobig-file", "file to big."), GenericMessage.ERROR);
								request.setAttribute("msg", msg);
								return null;
							}
							StaticInfo staticInfo = StaticInfo.getInstance(ctx, freeFile);
							if (readRole != null) {
								staticInfo.addReadRole(ctx, readRole);
							}
							String fileURL = URLHelper.createResourceURL(ctx.getContextForAbsoluteURL(), URLHelper.mergePath(globalContext.getStaticConfig().getStaticFolder(), staticInfo.getStaticURL()));
							result.put(file.getFieldName(), fileURL);
							specialValues.put(file.getFieldName(), fileURL);
						}
					} else {
						email.addAttachement(file.getName(), in);						
					}
				} finally {
					ResourceHelper.closeResource(in);
				}
			}
		}

		Map<String,String> adminMailData = new LinkedHashMap<String,String>();
		boolean noAttach = rs.getParameter("no_attach", null) != null;
		String formEmail = null;
		for (String key : keys) {
			if (!key.equals("webaction") && !key.equals("comp_id") && !key.equals("captcha")) {
				Object value = params.get(key);
				if (specialValues.get(key) != null) {
					value = specialValues.get(key);
				}
				String finalValue = rs.getParameter(key, "");
				if (specialValues.get(key) != null) {
					finalValue = specialValues.get(key);
				}

				/* validation */
				if (finalValue.length() > comp.getMaxSize(ctx, key)) {
					errorFields.add(key);
					GenericMessage msg = new GenericMessage(comp.getConfigMessage(ctx, key, "max-size"), GenericMessage.ERROR);
					request.setAttribute("msg", msg);
				}

				if (key.equals(fakeField) && finalValue.trim().length() > 0) {
					fakeFilled = true;
				} else if (!withXHTML && (finalValue.toLowerCase().contains("</a>") || finalValue.toLowerCase().contains("</div>"))) {
					fakeFilled = true;
				}
				if (finalValue.trim().length() == 0 && StringHelper.containsUppercase(key)) { // needed
					if (!attachField.contains(key)) {
						errorFields.add(key);
						GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("error.required", "please could you fill all required fields."), GenericMessage.ERROR);
						request.setAttribute("msg", msg);
					} else {
						if (!noAttach) {
							errorFields.add(key);
							GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("error.required", "please could you fill all required fields."), GenericMessage.ERROR);
							request.setAttribute("msg", msg);
						}
					}
				}
				if (finalValue.trim().length() > 0 && key.toLowerCase().trim().endsWith("email")) {
					if (!PatternHelper.MAIL_PATTERN.matcher(finalValue).matches()) {
						errorFields.add(key);
						GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("error.email-format", "your email format is'nt correct."), GenericMessage.ERROR);
						request.setAttribute("msg", msg);
					} else {
						formEmail = finalValue;
					}
				}

				if (value instanceof Object[]) {
					finalValue = StringHelper.arrayToString((Object[]) params.get(key), ",");
					if (!key.contains("g-recaptcha")) {
						adminMailData.put(key, finalValue);
					}
				} else {
					if (!key.contains("g-recaptcha")) {
						adminMailData.put(key, finalValue);
					}
				}
				result.put(key, finalValue);
			}
		}

		if (fakeFilled) {
			logger.warning("spam detected fake field filled : " + comp.getPage().getPath());
		}
		
		if (errorFields.size() == 0) {
			/** participation code **/
			if (StringHelper.isTrue(comp.getLocalConfig(false).getProperty("pcode.activate"))) {
				String alwaysOkCode = comp.getLocalConfig(false).getProperty("pcode.alwaysok");
				String pcode = rs.getParameter("pcode", null);
				if (StringHelper.isEmpty(pcode)) {
						errorFields.add("pcode");
						GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("message.no-code", "set participation code."), GenericMessage.ERROR);
						request.setAttribute("msg", msg);
						return null;				
				} else {
					if ((!StringHelper.isEmpty(alwaysOkCode) && alwaysOkCode.equals(pcode)) || ParticipationListService.getInstance(ctx).checkNumber(pcode)) {
						GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("message.participation.thanks", "thanks for your participation."), GenericMessage.INFO);
						request.setAttribute("msg", msg);							
					} else {
						errorFields.add("pcode");
						GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("message.bad-code", "bad participation code."), GenericMessage.ERROR);
						request.setAttribute("msg", msg);
						return null;	
					}
				}
			}
		}

		if (errorFields.size() == 0) {
			ContentContext absCtx = ctx.getContextForAbsoluteURL();
			String mailContent = XHTMLHelper.createAdminMail(ctx.getCurrentPage().getTitle(ctx), "From submit : "+ctx.getCurrentPage().getPageTitle(ctx) , adminMailData, URLHelper.createURL(absCtx), "go on page >>", null);
			mailContent = comp.getMailHeader(ctx) + mailContent + comp.getMailFooter(ctx);
			logger.info("mail content : " + mailContent);

			if (comp.isStorage()) {
				comp.storeResult(ctx, result);
			}

			if (comp.isSendEmail() && !fakeFilled) {
				String emailFrom = comp.getLocalConfig(false).getProperty("mail.from", StringHelper.neverEmpty(globalContext.getAdministratorEmail(), StaticConfig.getInstance(request.getSession()).getSiteEmail()));
				String emailFromField = comp.getLocalConfig(false).getProperty("mail.from.field", null);
				if (emailFromField != null && rs.getParameter(emailFromField, "") != null) {
					String tmpEmail = rs.getParameter(emailFromField, "");
					try {
						new InternetAddress(tmpEmail);
						emailFrom = tmpEmail;
					} catch (Exception e) {
						logger.warning("Strange from email = " + tmpEmail);
						logger.warning(e.getMessage());
					}
				}
				
				emailFrom = MailService.getDefaultSenderEmail(ctx, emailFrom);

				String emailTo = null;
				String emailToField = comp.getLocalConfig(false).getProperty("mail.to.field", null);
				if (emailToField != null && rs.getParameter(emailToField, "") != null) {
					String tmpEmail = rs.getParameter(emailToField, "");
					try {
						new InternetAddress(tmpEmail);
						emailTo = tmpEmail;
					} catch (Exception e) {
						emailTo = null;
						logger.warning("Strange to email = " + tmpEmail);
						logger.warning(e.getMessage());
					}
				}

				if (emailTo == null) {
					emailTo = comp.getLocalConfig(false).getProperty("mail.to", globalContext.getAdministratorEmail());
				}
				String emailCC = comp.getLocalConfig(false).getProperty("mail.cc", null);
				String emailBCC = comp.getLocalConfig(false).getProperty("mail.bcc", null);

				String pageMailing = comp.getLocalConfig(false).getProperty("page.confirmation", null);
				MenuElement pageConfirmation = content.getNavigation(ctx).searchChildFromName(pageMailing);

				try {
					MailService mailService = MailService.getInstance(new MailConfig(globalContext, StaticConfig.getInstance(request.getSession()), null));

					String adminEmailTo = comp.getLocalConfig(false).getProperty("mail.admin.to", globalContext.getAdministratorEmail());

					InternetAddress fromEmail = new InternetAddress(emailFrom);
					InternetAddress toEmail = new InternetAddress(emailTo);
					InternetAddress adminEmail = new InternetAddress(adminEmailTo);
					InternetAddress ccEmail = null;
					if (emailCC != null && emailCC.trim().length() > 0) {
						ccEmail = new InternetAddress(emailCC);
					}
					InternetAddress bccEmail = null;
					if (emailBCC != null && emailBCC.trim().length() > 0) {
						bccEmail = new InternetAddress(emailBCC);
					}

					List<InternetAddress> ccList = null;
					if (ccEmail != null) {
						ccList = Arrays.asList(ccEmail);
					}
					List<InternetAddress> bccList = null;
					if (bccEmail != null) {
						bccList = Arrays.asList(bccEmail);
					}
					
					email.setSubject(subject);
					email.setSender(fromEmail);
					email.addRecipients(adminEmail);
					email.setCcRecipients(ccList);
					email.setBccRecipients(bccList);
					email.setContent(mailContent);
					email.setHtml(true);	
					email.setDkim(globalContext.getDKIMBean());
					mailService.sendMail(null, email);					

					if (pageConfirmation != null) {
						logger.info("send mailing from:" + emailFrom + " to:" + emailTo);
						NetHelper.sendPageByMailing(ctx, pageConfirmation, emailFrom, emailTo, params);
					}

					if (formEmail != null) {
						toEmail = new InternetAddress(formEmail);
					}
					comp.sendConfirmationEmail(ctx, comp, params, toEmail);

				} catch (Exception e) {					
					String errorID = "E"+StringHelper.getRandomId();
					logger.severe("error id:"+errorID+" = "+e.getMessage());
					e.printStackTrace();
					GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("message.error", "technical error.")+ " ("+errorID+")", GenericMessage.ERROR);
					request.setAttribute("msg", msg);
					request.setAttribute("valid", "false");
					return null;
				}
			}

			GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("message.thanks"), GenericMessage.INFO);
			request.setAttribute("msg", msg);
			request.setAttribute("valid", "true");
		} else {
			request.setAttribute("errorFields", new CollectionAsMap<String>(errorFields));
		}

		return null;
	}

	protected void sendConfirmationEmail(ContentContext ctx, GenericForm comp, Map<String, Object> params, InternetAddress to) throws Exception {
		if (to == null) {
			return;
		}
		/** send email **/
		String subject = comp.getLocalConfig(false).getProperty("mail.subject", null);
		if (subject == null) {
			subject = "Transaction confirmed : " + ctx.getGlobalContext().getContextKey();
		}

		String email = null;
		String mailingPage = getConfig(ctx).getProperty("mail.page", null);
		String pageURL = "error:no link.";
		if (mailingPage != null) {
			MenuElement page = ContentService.getInstance(ctx.getGlobalContext()).getNavigation(ctx).searchChildFromName(mailingPage);
			if (page == null) {
				logger.warning("page not found : " + mailingPage);
			}
			pageURL = URLHelper.createURL(ctx.getContextForAbsoluteURL().getContextWithOtherRenderMode(ContentContext.PAGE_MODE), page.getPath(), params);
			try {
				email = NetHelper.readPageForMailing(new URL(pageURL));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		InternetAddress bcc = null;
		String bccString = getConfig(ctx).getProperty("mail.bcc", null);
		if (bccString != null && StringHelper.isMail(bccString)) {
			bcc = new InternetAddress(getConfig(ctx).getProperty("mail.bcc", null));
		}
		InternetAddress from;
		String fromString = getConfig(ctx).getProperty("mail.from", null);
		if (fromString != null && StringHelper.isMail(fromString)) {
			from = new InternetAddress(fromString);
		} else {
			from = new InternetAddress(ctx.getGlobalContext().getAdministratorEmail());
		}
		if (email == null) {
			email = getConfig(ctx).getProperty("mail.confirmation.body", null);
			if (email != null) {
				NetHelper.sendMail(ctx.getGlobalContext(), from, to, null, bcc, subject, email, null, false);
			}
		} else {
			NetHelper.sendMail(ctx.getGlobalContext(), from, to, null, bcc, subject, email, getConfig(ctx).getProperty("mail.confirmation.body", null), true);
		}

	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}
	
	@Override
	public String getFontAwesome() {	
		return "address-card";
	}

}
