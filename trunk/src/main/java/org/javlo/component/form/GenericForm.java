package org.javlo.component.form;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.javlo.actions.IAction;
import org.javlo.component.config.ComponentConfig;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.Comparator.StringComparator;
import org.javlo.mailing.MailService;
import org.javlo.message.GenericMessage;
import org.javlo.service.CaptchaService;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.utils.CSVFactory;
import org.javlo.utils.CollectionAsMap;
import org.javlo.ztatic.StaticInfo;

/**
 * store html form in csv file and send email with parameters. For use this component you need to create a renderer with a html form. this form need at least two field : <code>
 * &lt;input type=&quot;hidden&quot; name=&quot;webaction&quot; value=&quot;gform-registering.submit&quot; /&gt;<br/>&lt;input type=&quot;hidden&quot; name=&quot;comp_id&quot; value=&quot;${comp.id}&quot; /&gt; </code>. You can define required field with uppercase letter : "Firstname" > requierd, "firstname" > not requiered. for use captacha you need to tag : <code>&lt;img src=&quot;${info.captchaURL}&quot; alt=&quot;captcha&quot; /&gt;&lt;/label&gt;<br/>&lt;input type=&quot;text&quot; id=&quot;captcha&quot; name=&quot;captcha&quot; value=&quot;&quot; /&gt;</code> <h4>JSTL variable :</h4>
 * <ul>
 * <li>inherited from {@link AbstractVisualComponent}</li>
 * <li>{@link String} msg : message to display.</li>
 * <li>{@link Map} errorFields: field with error.</li>
 * <li>{@link String} valid: contains true if form is valid.</li>
 * </ul>
 * <h4>keys for message and config can be use in content</h4>
 * <ul>
 * <li>captcha : true for use captacha</li>
 * <li>error.required : error message if requiered field is'nt filled.</li>
 * <li>error.captcha : message if catacha value is'nt correct.</li>
 * <li>message.thanks : confirmation message.</li>
 * <li>field.fake : name of fake field.</li>
 * <li>
 * 
 * @author Patrick Vandermaesen
 * 
 */
public class GenericForm extends AbstractVisualComponent implements IAction {

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
		return outRenderers;
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
		ctx.getRequest().setAttribute("ci18n", getLocalConfig(false));
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
	public boolean isEmpty(ContentContext ctx) {
		return false;
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
		if (getLocalConfig(false).get("filename") != null) {
			fileName = getLocalConfig(false).getProperty("filename");
		}
		File file = new File(URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getStaticFolder(), "dynamic-form-result", fileName));
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
		File dir = new File(URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getStaticFolder(), "dynamic-form-result", fileName));
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
				CSVFactory.storeContentAsMap(file, newData);
			} else {
				CSVFactory.appendContentAsMap(file, data);
			}
		}
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		super.performEdit(ctx);
		getLocalConfig(true);
	}

	protected boolean isCaptcha() {
		return true;
	}

	protected boolean isSendEmail() {
		return true;
	}

	protected boolean isStorage() {
		return true;
	}

	public static String performSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception {

		RequestService requestService = RequestService.getInstance(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		ContentService content = ContentService.getInstance(request);
		GenericForm comp = (GenericForm) content.getComponent(ctx, requestService.getParameter("comp_id", null));

		/** check captcha **/
		String captcha = requestService.getParameter("captcha", null);

		if (comp.isCaptcha(ctx)) {
			if (captcha == null || CaptchaService.getInstance(request.getSession()).getCurrentCaptchaCode() == null || !CaptchaService.getInstance(request.getSession()).getCurrentCaptchaCode().equals(captcha)) {
				GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("error.captcha", "bad captcha."), GenericMessage.ERROR);

				request.setAttribute("msg", msg);
				request.setAttribute("error_captcha", "true");
				return null;
			} else {
				CaptchaService.getInstance(request.getSession()).setCurrentCaptchaCode("");
			}
		}

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		String subject = "GenericForm submit : '" + globalContext.getGlobalTitle() + "' ";

		String subjectField = comp.getLocalConfig(false).getProperty("mail.subject.field", null);
		if (subjectField != null && requestService.getParameter(subjectField, null) != null) {
			subject = comp.getLocalConfig(false).getProperty("mail.subject", "") + requestService.getParameter(subjectField, null);
		} else {
			subject = comp.getLocalConfig(false).getProperty("mail.subject", subject);
		}

		Map<String, Object> params = requestService.getParameterMap();
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
		boolean fakeFilled = false;

		List<String> keys = new LinkedList(params.keySet());
		Collections.sort(keys, new StringComparator());

		/** store attach files **/

		Map<String, String> specialValues = new HashMap<String, String>();

		String badFileFormatRAW = comp.getLocalConfig(false).getProperty("file.bad-file", "exe,bat,scr,bin,obj,lib,dll,bat,sh,com,cmd,msi");
		List<String> badFileFormat = StringHelper.stringToCollection(badFileFormatRAW, ",");
		String maxFileSizeRAW = comp.getLocalConfig(false).getProperty("file.max-size", "" + (10 * 1024 * 1024));
		long maxFileSize = Long.parseLong(maxFileSizeRAW);

		for (FileItem file : requestService.getAllFileItem()) {
			String ext = StringHelper.getFileExtension(file.getName()).toLowerCase();
			if (badFileFormat.contains(ext)) {
				GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("message.bad-file", "bad file format."), GenericMessage.ERROR);
				request.setAttribute("msg", msg);
				return null;
			}
			InputStream in = file.getInputStream();
			if (in != null) {
				try {
					String fileName = URLHelper.mergePath(comp.getAttachFolder(ctx).getAbsolutePath(), file.getName());
					File freeFile = ResourceHelper.getFreeFileName(new File(fileName));
					if (ResourceHelper.writeStreamToFile(in, freeFile, maxFileSize) < 0) {
						GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("message.tobig-file", "file to big."), GenericMessage.ERROR);
						request.setAttribute("msg", msg);
						return null;
					}
					StaticInfo staticInfo = StaticInfo.getInstance(ctx, freeFile);
					String fileURL = URLHelper.createResourceURL(ctx.getContextForAbsoluteURL(), URLHelper.mergePath(globalContext.getStaticConfig().getStaticFolder(), staticInfo.getStaticURL()));
					result.put(file.getFieldName(), fileURL);
					specialValues.put(file.getFieldName(), fileURL);
				} finally {
					ResourceHelper.closeResource(in);
				}
			}
		}

		for (String key : keys) {
			if (!key.equals("webaction") && !key.equals("comp_id") && !key.equals("captcha")) {
				Object value = params.get(key);
				if (specialValues.get(key) != null) {
					value = specialValues.get(key);
				}
				String finalValue = requestService.getParameter(key, "");
				if (specialValues.get(key) != null) {
					finalValue = specialValues.get(key);
				}

				if (key.equals(fakeField) && finalValue.trim().length() > 0) {
					fakeFilled = true;
				} else if (!withXHTML && (finalValue.toLowerCase().contains("</a>") || finalValue.toLowerCase().contains("</div>"))) {
					fakeFilled = true;
				}

				if (finalValue.trim().length() == 0 && StringHelper.containsUppercase(key)) { // needed field
					errorFields.add(key);
					GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("error.required", "please could you fill all required fields."), GenericMessage.ERROR);
					request.setAttribute("msg", msg);
				}

				if (value instanceof Object[]) {
					finalValue = StringHelper.arrayToString((Object[]) params.get(key), ",");
					out.println(key + ':');
					out.println(finalValue);
				} else {
					out.println(key + ':');
					out.println(finalValue);
				}
				out.println("");
				result.put(key, finalValue);
			}
		}
		out.println("");
		out.close();

		if (fakeFilled) {
			logger.warning("spam detected fake field filled : " + comp.getPage().getPath());
		}

		if (errorFields.size() == 0) {
			String mailContent = new String(outStream.toByteArray());

			logger.info("mail content : " + mailContent);

			if (comp.isStorage()) {
				comp.storeResult(ctx, result);
			}

			if (comp.isSendEmail() && !fakeFilled) {

				String emailFrom = comp.getLocalConfig(false).getProperty("mail.from", StaticConfig.getInstance(request.getSession()).getSiteEmail());
				String emailFromField = comp.getLocalConfig(false).getProperty("mail.from.field", null);
				if (emailFromField != null && requestService.getParameter(emailFromField, "") != null) {
					String tmpEmail = requestService.getParameter(emailFromField, "");
					try {
						new InternetAddress(tmpEmail);
						emailFrom = tmpEmail;
					} catch (Exception e) {
						logger.warning(e.getMessage());
					}
				}
				String emailTo = comp.getLocalConfig(false).getProperty("mail.to", globalContext.getAdministratorEmail());
				String emailCC = comp.getLocalConfig(false).getProperty("mail.cc", null);
				String emailBCC = comp.getLocalConfig(false).getProperty("mail.bcc", null);

				MailService mailService = MailService.getInstance(globalContext.getStaticConfig());
				InternetAddress fromEmail = new InternetAddress(emailFrom);
				InternetAddress toEmail = new InternetAddress(emailTo);
				InternetAddress ccEmail = null;
				if (emailCC != null) {
					ccEmail = new InternetAddress(emailCC);
				}
				InternetAddress bccEmail = null;
				if (emailBCC != null) {
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

				try {
					mailService.sendMail(null, fromEmail, toEmail, ccList, bccList, subject, mailContent, false);
				} catch (Exception e) {
					e.printStackTrace();
					GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("message.error", "technical error."), GenericMessage.ERROR);
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

	@Override
	public int getComplexityLevel() {
		return AbstractVisualComponent.COMPLEXITY_STANDARD;
	}

}
