package org.javlo.component.form;

import static j2html.TagCreator.a;
import static j2html.TagCreator.div;
import static j2html.TagCreator.input;
import static j2html.TagCreator.label;
import static j2html.TagCreator.span;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.javlo.actions.IAction;
import org.javlo.actions.IEventRegistration;
import org.javlo.bean.Company;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.InfoBean;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.LangHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.StringSecurityUtil;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.XHTMLNavigationHelper;
import org.javlo.helper.Comparator.StringComparator;
import org.javlo.i18n.I18nAccess;
import org.javlo.mailing.MailConfig;
import org.javlo.mailing.MailService;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.CaptchaService;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.service.event.Event;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserFactory;
import org.javlo.utils.CSVFactory;
import org.javlo.utils.CollectionAsMap;
import org.javlo.utils.JSONMap;
import org.javlo.utils.StructuredProperties;
import org.javlo.utils.TimeMap;
import org.javlo.ztatic.StaticInfo;

public class SmartGenericForm extends AbstractVisualComponent implements IAction, IEventRegistration {

	private static final String RECAPTCHASECRETKEY = "recaptchasecretkey";

	private static final String RECAPTCHAKEY = "recaptchakey";
	
	public static final String FOLDER = "dynamic-form-result";
	
	private static final String EDIT_LINE_PARAM = "_el";
	
	private static final String VALID_LINE_PARAM = "_vl";
	
	private static final String VALIDED = "__valided";

	private Properties bundle;

	private Integer countCache = null;

	private static final Map<String, String> cacheForm = Collections.synchronizedMap(new TimeMap<String, String>(60 * 60)); // 1 hours validity

	private static Logger logger = Logger.getLogger(SmartGenericForm.class.getName());

	protected static final Object LOCK_ACCESS_FILE = new Object();

	public Properties getLocalConfig(boolean reload) {
		if (bundle == null || reload) {
			bundle = new StructuredProperties();
			try {
				bundle.load(new StringReader(getValue()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bundle;
	}
	
	protected File getAttachFolder(ContentContext ctx) throws Exception {
		File file = getFile(ctx);		
		File dir = new File(URLHelper.mergePath(getFile(ctx).getParentFile().getAbsolutePath(), StringHelper.getFileNameWithoutExtension(file.getName())));
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}
	
	protected String getInputEditLineName(ContentContext ctx) throws Exception {
		return EDIT_LINE_PARAM+'_'+StringSecurityUtil.encode(getId(), ctx.getGlobalContext().getStaticConfig().getSecretKey());		
	}
	
	protected String getInputValidLineName(ContentContext ctx) throws Exception {
		return VALID_LINE_PARAM+'_'+StringSecurityUtil.encode(getId(), ctx.getGlobalContext().getStaticConfig().getSecretKey());		
	}
	
	protected String encodeEditNumber (ContentContext ctx, int number) throws Exception {
		return StringSecurityUtil.encode(""+number, ctx.getGlobalContext().getStaticConfig().getSecretKey());
	}
	
	protected int decodeEditNumber (ContentContext ctx, String number) throws NumberFormatException, Exception {
		try {
			if (!StringHelper.isEmpty(number)) {
				return Integer.parseInt(StringSecurityUtil.decode(""+number, ctx.getGlobalContext().getStaticConfig().getSecretKey()));
			}
		} catch (Throwable t) {					
		}
		return -1;		
	}

	protected boolean isCaptcha(ContentContext ctx) {
		return StringHelper.isTrue(getLocalConfig(false).getProperty("captcha", "" + isCaptcha()));
	}

	public boolean isCaptcha() {
		return StringHelper.isTrue(getLocalConfig(false).getProperty("captcha", "true"));
	}

	public String getRecaptchaKey() {
		return getLocalConfig(false).getProperty(RECAPTCHAKEY, null);
	}

	public String getRecaptchaSecretKey() {
		return getLocalConfig(false).getProperty(RECAPTCHASECRETKEY, null);
	}

	public int getCountSubscription(ContentContext ctx) throws Exception {
		if (countCache == null) {
			File file = getFile(ctx);
			if (!file.exists()) {
				countCache = 0;
			} else {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				try {
					int countLine = 0;
					String read = reader.readLine();
					while (read != null) {
						countLine++;
						read = reader.readLine();
					}
					countCache = countLine - 1;
				} finally {
					ResourceHelper.closeResource(reader);
				}
			}
		}
		return countCache;
	}

	public List<Map<String, String>> getData(ContentContext ctx) throws Exception {
		File file = getFile(ctx);
		if (!file.exists()) {
			return Collections.EMPTY_LIST;
		} else {
			return CSVFactory.loadContentAsMap(file);
		}
	}

	@Override
	public void setValue(String inContent) {
		super.setValue(inContent);
		bundle = null;
	}

	public boolean isEvent() {
		return true;
	}

	public static final String TYPE = "smart-generic-form";

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		StaticConfig staticConfig = ctx.getGlobalContext().getStaticConfig();
		
		//out.println(XHTMLHelper.renderLine("title", getInputName("title"), getLocalConfig(false).getProperty("title", "")));
		//out.println(XHTMLHelper.renderLine("filename", getInputName("filename"), getLocalConfig(false).getProperty("filename", "")));
		
		out.println("<div class=\"row\">");
		out.println(div(label("title").attr("for", getInputName("title"))).withClass("col-sm-2").render());
		out.println(div(div(input().withClass("form-control").attr("id", getInputName("title")).attr("name", getInputName("title")).attr("value",getLocalConfig(false).getProperty("title", ""))).withClass("form-group")).withClass("col-sm-10").render());
		out.println("</div>");

		String inputName = getInputName("filename");
		out.println("<div class=\"row\">");
		out.println(div(label("filename").attr("for", inputName)).withClass("col-sm-2").render());
		out.println(div(div(input().withClass("form-control").attr("id", inputName).attr("name", inputName).attr("value",getLocalConfig(false).getProperty("filename", ""))).withClass("form-group")).withClass("col-sm-8").render());
		String csvLink = URLHelper.createResourceURL(ctx, URLHelper.mergePath("/",staticConfig.getStaticFolder(),FOLDER, getLocalConfig(false).getProperty("filename", "")));
		String xlsxLink = FilenameUtils.removeExtension(csvLink)+".xlsx";
		out.println(div(a("[XSLX]").attr("href", xlsxLink),span(" - "),a("[CSV]").attr("href", csvLink)).withClass("col-sm-2").render());
		out.println("</div>");

		out.println("<div class=\"row\"><div class=\"col-sm-2\">");
		out.println(XHTMLHelper.renderLine("captcha", getInputName("captcha"), StringHelper.isTrue(getLocalConfig(false).getProperty("captcha", null))));
		out.println("</div><div class=\"col-sm-5\">");
		out.println(XHTMLHelper.renderLine("google recaptcha site key", getInputName(RECAPTCHAKEY), getLocalConfig(false).getProperty(RECAPTCHAKEY, "")));
		out.println("</div><div class=\"col-sm-5\">");
		out.println(XHTMLHelper.renderLine("google recaptcha secret key", getInputName(RECAPTCHASECRETKEY), getLocalConfig(false).getProperty(RECAPTCHASECRETKEY, "")));
		out.println("</div></div>");
		if (isFile()) {
			out.println(XHTMLHelper.renderLine("max file size (Kb)", getInputName("filesize"), "" + getMaxFileSize()));
		}
		out.println("<div class=\"col-group\"><div class=\"one_half\"><fieldset><legend>e-mail</legend>");
		out.println(XHTMLHelper.renderLine("mail to :", getInputName("to"), getLocalConfig(false).getProperty("mail.to", "")));
		out.println(XHTMLHelper.renderLine("mail cc :", getInputName("cc"), getLocalConfig(false).getProperty("mail.cc", "")));
		out.println(XHTMLHelper.renderLine("mail bcc :", getInputName("bcc"), getLocalConfig(false).getProperty("mail.bcc", "")));
		out.println(XHTMLHelper.renderLine("mail subject :", getInputName("subject"), getLocalConfig(false).getProperty("mail.subject", "")));
		out.println(XHTMLHelper.renderLine("mail subject field :", getInputName("subject-field"), getLocalConfig(false).getProperty("mail.subject.field", "")));
		out.println(XHTMLHelper.renderLine("mail from :", getInputName("from"), getLocalConfig(false).getProperty("mail.from", "")));
		out.println(XHTMLHelper.renderLine("mail from field :", getInputName("from-field"), getLocalConfig(false).getProperty("mail.from.field", "")));
		out.println("</fieldset></div>");

		/** MESSAGE **/
		out.println("<div class=\"one_half\"><fieldset><legend>message</legend>");
		out.println(XHTMLHelper.renderLine("field required :", getInputName("message-required"), getLocalConfig(false).getProperty("message.required", "")));
		out.println(XHTMLHelper.renderLine("error generic :", getInputName("error-generic"), getLocalConfig(false).getProperty("error.generic", "")));
		out.println(XHTMLHelper.renderLine("error required :", getInputName("error-required"), getLocalConfig(false).getProperty("error.required", "")));
		out.println(XHTMLHelper.renderLine("thanks :", getInputName("message-thanks"), getLocalConfig(false).getProperty("message.thanks", "")));
		out.println(XHTMLHelper.renderLine("error :", getInputName("message-error"), getLocalConfig(false).getProperty("message.error", "")));
		out.println(XHTMLHelper.renderLine("reset :", getInputName("message-reset"), getLocalConfig(false).getProperty("message.reset", "")));
		if (isCaptcha()) {
			out.println(XHTMLHelper.renderLine("captcha :", getInputName("label-captcha"), getLocalConfig(false).getProperty("label.captcha", "")));
			out.println(XHTMLHelper.renderLine("captcha error :", getInputName("error.captcha"), getLocalConfig(false).getProperty("error.captcha", "")));
		}
		if (isFile()) {
			out.println(XHTMLHelper.renderLine("bad file format :", getInputName("message-bad-file"), getLocalConfig(false).getProperty("message.bad-file", "")));
			out.println(XHTMLHelper.renderLine("file to big :", getInputName("message-tobig-file"), getLocalConfig(false).getProperty("message.tobig-file", "")));
		}
		out.println("</fieldset></div></div>");

		/** EVENT **/
		if (isEvent()) {
			out.println("<fieldset><legend>Event</legend><div class=\"row\"><div class=\"col-sm-6\"><div class=\"row\"><div class=\"col-xs-6\">");
			out.println(XHTMLHelper.renderLine("subscribe limit :", getInputName("event-limit"), getLocalConfig(false).getProperty("event.limit", "")));
			out.println("</div><div class=\"col-xs-6\">");
			out.println(XHTMLHelper.renderLine("subscribe alert :", getInputName("event.alert-limit"), getLocalConfig(false).getProperty("event.alert-limit", "")));
			out.println("</div></div></div><div class=\"col-sm-6\">");
			out.println(XHTMLHelper.renderLine("current subscription :", "" + getCountSubscription(ctx)));
			out.println("</div></div><div class=\"row\"><div class=\"col-xs-6\">");
			out.println(XHTMLHelper.renderLine("confirm subject :", getInputName("mail-confirm-subject"), getLocalConfig(false).getProperty("mail.confirm.subject", "")));
			out.println("<div class=\"line validation-email\"><label>Confirm Email page : </label>");
			out.println(XHTMLNavigationHelper.renderComboNavigation(ctx, getPage().getRoot(), getInputName("mail-confirm-link"), getLocalConfig(false).getProperty("mail.confirm.link", ""), true) + "</div>");
			out.println(XHTMLHelper.renderLine("open message :", getInputName("event-open-message"), getLocalConfig(false).getProperty("event.open.message", "")));
			out.println("</div><div class=\"col-xs-6\">");
			out.println(XHTMLHelper.renderLine("closed subject :", getInputName("mail-closed-subject"), getLocalConfig(false).getProperty("mail.closed.subject", "")));
			out.println("<div class=\"line validation-email\"><label>Closed Email page : </label>");
			out.println(XHTMLNavigationHelper.renderComboNavigation(ctx, getPage().getRoot(), getInputName("mail-closed-link"), getLocalConfig(false).getProperty("mail.closed.link", ""), true) + "</div>");
			out.println(XHTMLHelper.renderLine("close message :", getInputName("event-close-message"), getLocalConfig(false).getProperty("event.close.message", "")));
			out.println("</div></fieldset>");
		}

		out.println("<div class=\"action-add\"><input type=\"text\" name=\"" + getInputName("new-name") + "\" placeholder=\"field name\" /> <input type=\"submit\" name=\"" + getInputName("add") + "\" value=\"add field\" /></div>");
		if (getFields().size() > 0) {
			out.println("<table class=\"sTable2\">");
			String listTitle = "";
			if (isList()) {
				listTitle = "<td>list</td>";
			}
			out.println("<thead><tr><td>name</td><td>label</td><td>condition</td>" + listTitle + "<td>type</td><td>width</td><td>required</td><td>action</td></tr></thead>");
			out.println("<tbody>");
			List<Field> fields = getFields();
			for (Field field : fields) {
				out.println(getEditXHTML(ctx, field));
			}
			out.println("</tbody>");
			out.println("</table>");
		}

		out.close();
		return new String(outStream.toByteArray());
	}

	private String getEditXHTML(ContentContext ctx, Field field) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		String scrollToMe = "";
		if (StringHelper.neverNull(ctx.getRequest().getAttribute(getNewFieldKey())).equals(field.getName())) {
			scrollToMe = " scroll-to-me";
		}
		out.println("<tr class=\"field-line" + scrollToMe + "\">");
		out.println("<td class=\"input name\"><input class=\"form-control\" type=\"text\" name=\"" + getInputName("name-" + field.getName()) + "\" value=\"" + field.getName() + "\"/></td>");
		out.println("<td class=\"input label\"><input class=\"form-control\" type=\"text\" name=\"" + getInputName("label-" + field.getName()) + "\" value=\"" + field.getLabel() + "\"/></td>");
		out.println("<td class=\"input condition\"><input class=\"form-control\" type=\"text\" name=\"" + getInputName("condition-" + field.getName()) + "\" value=\"" + field.getCondition() + "\"/></td>");
		if (isList()) {
			if (field.getType().equals("radio") || field.getType().equals("list")) {
				out.println("<td class=\"list\"><textarea class=\"form-control\" name=\"" + getInputName("list-" + field.getName()) + "\">" + StringHelper.collectionToText(field.getList()) + "</textarea></td>");
			} else if (field.getType().equals("registered-list")) {
				out.println("<td class=\"list\"><input class=\"form-control\" name=\"" + getInputName("registered-list-" + field.getName()) + "\" placeholder=\"list name\" value=\"" + field.getRegisteredList() + "\"/></td>");
			} else {
				out.println("<td class=\"list\">&nbsp;</td>");
			}
		}
		out.println("<td class=\"type\">" + XHTMLHelper.getInputOneSelect(getInputName("type-" + field.getName()), field.getFieldTypes(), field.getType(), "form-control", (String) null, true) + "</td>");
		out.println("<td class=\"width\"><select class=\"form-control\" name=\"" + getInputName("width-" + field.getName()) + "\" >");
		for (int i = 1; i <= 12; i++) {
			String selected = "";
			if (i == field.getWidth()) {
				selected = " selected=\"selected\"";
			}
			out.println("<option" + selected + ">" + i + "</option>");
		}
		out.println("</select></td>");
		String required = "";
		if (field.isRequire()) {
			required = " checked=\"checked\"";
		}
		out.println("<td class=\"required\"><input type=\"checkbox\" name=\"" + getInputName("require-" + field.getName()) + "\"" + required + " /></td>");
		out.println("<td class=\"buttons\"><div  class=\"btn-group btn-group-sm\">");
		out.println("  <button class=\"up btn btn-default btn-sm ajax\" type=\"submit\" name=\"" + getInputName("up-" + field.getName()) + "\" ><span class=\"glyphicon glyphicon-menu-up\" aria-hidden=\"true\"></span></button>");
		out.println("  <button class=\"down btn btn-default btn-sm ajax\" type=\"submit\" name=\"" + getInputName("down-" + field.getName()) + "\"><span class=\"glyphicon glyphicon-menu-down\" aria-hidden=\"true\"></span></button>");
		out.println("  <button class=\"needconfirm btn btn-default btn-sm ajax\" type=\"submit\" name=\"" + getInputName("del-" + field.getName()) + "\" ><span class=\"glyphicon glyphicon-trash\" aria-hidden=\"true\"></span></button>");
		out.println("</div></td>");
		out.println("</tr>");
		out.close();
		return new String(outStream.toByteArray());
	}
	
	protected Field getField(String fieldName) {
		for (Field field : getFields()) {
			if (field.getName().equalsIgnoreCase(fieldName)) {
				return field;
			}
		}
		return null;
	}

	public synchronized List<Field> getFields() {
		List<Field> fields = new LinkedList<Field>();
		Properties p = getLocalConfig(false);

		for (Object objKey : p.keySet()) {
			String key = objKey.toString();
			if (key.startsWith("field.")) {
				String name = key.replaceFirst("field.", "").trim();
				if (name.trim().length() > 0) {
					String value = p.getProperty(key);
					String[] data = StringUtils.splitPreserveAllTokens(value, Field.SEP);
					Field field = new Field(name, (String) LangHelper.arrays(data, 0, ""), (String) LangHelper.arrays(data, 1, ""), (String) LangHelper.arrays(data, 8, ""), (String) LangHelper.arrays(data, 2, ""), (String) LangHelper.arrays(data, 3, ""), (String) LangHelper.arrays(data, 5, ""), Integer.parseInt("" + LangHelper.arrays(data, 6, "0")), Integer.parseInt("" + LangHelper.arrays(data, 7, "6")));
					fields.add(field);
				}
			}
		}
		Collections.sort(fields, new Field.FieldComparator());
		int currentWidth = 0;
		Field lastField = null;
		for (Field field : fields) {
			if (currentWidth == 0) {
				field.setFirst(true);
			}
			currentWidth = currentWidth + field.getWidth();
			if (currentWidth >= 12) {
				field.setLast(true);
				currentWidth = 0;
			}
			lastField = field;
		}
		if (lastField != null) {
			lastField.setLast(true);
		}
		return fields;
	}

	public boolean isFile() {
		for (Field field : getFields()) {
			if (field.getType().equals("file")) {
				return true;
			}
		}
		return false;
	}

	public boolean isList() {
		for (Field field : getFields()) {
			if (field.getType().contains("list") || field.getType().contains("radio")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getRenderer(ContentContext ctx) {
		return super.getRenderer(ctx);
	}

	public String getTitle() {
		return getLocalConfig(false).getProperty("title");
	}

	protected synchronized void store(Field field) {
		String key = field.getPrefix() + '.' + field.getName();
		Properties prop = getLocalConfig(false);
		if (prop.contains(key)) {
			prop.remove(prop);
		}
		getLocalConfig(false).put(key, field.toString());
	}

	protected synchronized void delField(String name) {
		getLocalConfig(false).remove("field." + name);
	}

	protected void store(ContentContext ctx) throws IOException {
		Writer writer = new StringWriter();
		getLocalConfig(false).store(writer, "comp:" + getId());
		if (!getValue().equals(writer.toString())) {
			setValue(writer.toString());
			setModify();
			setNeedRefresh(true);
		}
	}

	public boolean isClose(ContentContext ctx) throws Exception {
		Properties localConfig = getLocalConfig(false);
		String eventLimistStr = localConfig.getProperty("event.limit");
		if (StringHelper.isDigit(eventLimistStr)) {
			int maxSubscription = Integer.parseInt(eventLimistStr);
			if (maxSubscription > 0) {
				int countSubscription = getCountSubscription(ctx);
				if (countSubscription >= maxSubscription) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		CaptchaService.getInstance(ctx.getRequest().getSession());
		Properties localConfig = getLocalConfig(false);
		ctx.getRequest().setAttribute("ci18n", localConfig);
		String code = StringHelper.getRandomId();
		cacheForm.put(code, "");
		ctx.getRequest().setAttribute("formCode", code);
		String eventLimistStr = localConfig.getProperty("event.limit");
		ctx.getRequest().setAttribute("openEvent", true);
		if (StringHelper.isDigit(eventLimistStr)) {
			int maxSubscription = Integer.parseInt(eventLimistStr);
			if (maxSubscription > 0) {
				int countSubscription = getCountSubscription(ctx);
				ctx.getRequest().setAttribute("countSubscription", countSubscription);
				GenericMessage msg;
				if (countSubscription < maxSubscription) {
					msg = new GenericMessage((String) localConfig.get("event.open.message"), GenericMessage.INFO);
				} else {
					ctx.getRequest().setAttribute("openEvent", false);
					msg = new GenericMessage((String) localConfig.get("event.close.message"), GenericMessage.ALERT);
				}
				if (ctx.getRequest().getAttribute("msg") == null) {
					ctx.getRequest().setAttribute("msg", msg);
				}
			}
		}
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		String editLineStr = rs.getParameter(getInputEditLineName(ctx));		
		int editLine = decodeEditNumber(ctx, editLineStr);	
		if (editLine>0) {			
			synchronized(LOCK_ACCESS_FILE) {
				ctx.getRequest().setAttribute("editForm", "true");
				ctx.getRequest().setAttribute("editLine", editLineStr);
				ctx.getRequest().setAttribute("inputLine", getInputEditLineName(ctx));
				File csvFile = getFile(ctx);
				List<Map<String, String>> data = CSVFactory.loadContentAsMap(csvFile);				
				if (data.size()>=editLine) {
					Map<String,String> line = data.get(editLine);					
					for (Map.Entry<String, String> entry : line.entrySet()) {						
						rs.setParameter(entry.getKey(), entry.getValue());
					}
				}
			}		
		}
		
		String validLineStr = rs.getParameter(getInputValidLineName(ctx));		
		int validLine = decodeEditNumber(ctx, validLineStr);	
		if (validLine>0) {
			logger.info("valid line : "+validLine);
			synchronized(LOCK_ACCESS_FILE) {
				ctx.getRequest().setAttribute("validForm", true);				
				ctx.getRequest().setAttribute("editLine", editLineStr);
				ctx.getRequest().setAttribute("inputLine", getInputValidLineName(ctx));
				
				File csvFile = getFile(ctx);
				List<Map<String, String>> data = CSVFactory.loadContentAsMap(csvFile);				
				if (data.size()>=validLine) {
					Map<String,String> line = data.get(validLine);					
					for (Map.Entry<String, String> entry : line.entrySet()) {						
						rs.setParameter(entry.getKey(), entry.getValue());
					}
				}
				
				if (data.size()>=validLine) {
					Map<String,String> line = data.get(validLine);					
					line.put(VALIDED, "true");
				}
				CSVFactory.storeContentAsMap(csvFile, data);
				
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				String msg = I18nAccess.getInstance(ctx).getViewText("form.confirm", "data is confirmed.");
				System.out.println(">>>>>>>>> SmartGenericForm.prepareView : msg = "+msg); //TODO: remove debug trace
				messageRepository.setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
				System.out.println(">>>>>>>>> SmartGenericForm.prepareView : messageRepository.msg = "+messageRepository.getGlobalMessage().getMessage()); //TODO: remove debug trace
			}		
		}
	}
	
	protected boolean isUpdate(ContentContext ctx) throws Exception {
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		int num = decodeEditNumber(ctx, rs.getParameter(getInputEditLineName(ctx)));
		return num>0;
		
	}

	protected long getMaxFileSize() {
		String fileSize = getLocalConfig(false).getProperty("file.max-size");
		if (StringHelper.isDigit(fileSize)) {
			return Long.parseLong(fileSize);
		} else {
			return 0;
		}
	}

	protected String getNewFieldKey() {
		return "_new_field_" + getId();
	}

	protected boolean isWarningEventSite(ContentContext ctx) throws Exception {
		Properties localConfig = getLocalConfig(false);
		String eventLimistStr = localConfig.getProperty("event.alert-limit");
		int countSubscription = getCountSubscription(ctx);
		if (StringHelper.isDigit(eventLimistStr)) {
			return Integer.parseInt(eventLimistStr) == countSubscription;
		} else {
			return false;
		}
	}

	protected boolean isClosedEventSite(ContentContext ctx) throws Exception {
		Properties localConfig = getLocalConfig(false);
		String eventLimistStr = localConfig.getProperty("event.limit");
		int countSubscription = getCountSubscription(ctx);
		if (StringHelper.isDigit(eventLimistStr)) {
			return Integer.parseInt(eventLimistStr) == countSubscription;
		} else {
			return false;
		}
	}

	protected boolean isFullEventSite(ContentContext ctx) throws Exception {
		Properties localConfig = getLocalConfig(false);
		String eventLimistStr = localConfig.getProperty("event.limit");
		int countSubscription = getCountSubscription(ctx);
		if (StringHelper.isDigit(eventLimistStr)) {
			return Integer.parseInt(eventLimistStr) < countSubscription;
		} else {
			return false;
		}
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		getLocalConfig(false).setProperty("title", rs.getParameter(getInputName("title"), ""));
		getLocalConfig(false).setProperty("filename", rs.getParameter(getInputName("filename"), ""));
		boolean oldCaptcha = isCaptcha();
		getLocalConfig(false).setProperty("captcha", rs.getParameter(getInputName("captcha"), ""));
		getLocalConfig(false).setProperty(RECAPTCHAKEY, rs.getParameter(getInputName(RECAPTCHAKEY), ""));
		getLocalConfig(false).setProperty(RECAPTCHASECRETKEY, rs.getParameter(getInputName(RECAPTCHASECRETKEY), ""));
		if (oldCaptcha != isCaptcha()) {
			ctx.setClosePopup(false);
		}
		getLocalConfig(false).setProperty("file.max-size", rs.getParameter(getInputName("filesize"), ""));
		getLocalConfig(false).setProperty("mail.to", rs.getParameter(getInputName("to"), ""));
		getLocalConfig(false).setProperty("mail.cc", rs.getParameter(getInputName("cc"), ""));
		getLocalConfig(false).setProperty("mail.bcc", rs.getParameter(getInputName("bcc"), ""));
		getLocalConfig(false).setProperty("mail.subject", rs.getParameter(getInputName("subject"), ""));
		getLocalConfig(false).setProperty("mail.subject.field", rs.getParameter(getInputName("subject-field"), ""));
		getLocalConfig(false).setProperty("mail.from", rs.getParameter(getInputName("from"), ""));
		getLocalConfig(false).setProperty("mail.from.field", rs.getParameter(getInputName("from-field"), ""));
		getLocalConfig(false).setProperty("message.generic", rs.getParameter(getInputName("message-generic"), ""));
		getLocalConfig(false).setProperty("message.required", rs.getParameter(getInputName("message-required"), ""));
		getLocalConfig(false).setProperty("error.required", rs.getParameter(getInputName("error-required"), ""));
		getLocalConfig(false).setProperty("message.thanks", rs.getParameter(getInputName("message-thanks"), ""));
		getLocalConfig(false).setProperty("message.error", rs.getParameter(getInputName("message-error"), ""));
		getLocalConfig(false).setProperty("message.reset", rs.getParameter(getInputName("message-reset"), ""));

		getLocalConfig(false).setProperty("event.limit", rs.getParameter(getInputName("event-limit"), ""));
		getLocalConfig(false).setProperty("event.alert-limit", rs.getParameter(getInputName("event.alert-limit"), ""));
		getLocalConfig(false).setProperty("mail.confirm.subject", rs.getParameter(getInputName("mail-confirm-subject"), ""));
		getLocalConfig(false).setProperty("mail.confirm.link", rs.getParameter(getInputName("mail-confirm-link"), ""));
		getLocalConfig(false).setProperty("mail.closed.subject", rs.getParameter(getInputName("mail-closed-subject"), ""));
		getLocalConfig(false).setProperty("mail.closed.link", rs.getParameter(getInputName("mail-closed-link"), ""));
		getLocalConfig(false).setProperty("event.open.message", rs.getParameter(getInputName("event-open-message"), ""));
		getLocalConfig(false).setProperty("event.close.message", rs.getParameter(getInputName("event-close-message"), ""));

		if (isCaptcha()) {
			getLocalConfig(false).setProperty("label.captcha", rs.getParameter(getInputName("label-captcha"), ""));
			getLocalConfig(false).setProperty("error.captcha", rs.getParameter(getInputName("error.captcha"), ""));
		}
		// getLocalConfig(false).setProperty("",
		// rs.getParameter(getInputName(""), ""));

		if (isFile()) {
			getLocalConfig(false).setProperty("message.bad-file", rs.getParameter(getInputName("message-bad-file"), ""));
			getLocalConfig(false).setProperty("message.tobig-file", rs.getParameter(getInputName("message-tobig-file"), ""));
		}

		int pos = 10;
		for (Field field : getFields()) {
			field.setOrder(pos);
			pos = pos + 10;
			String oldName = field.getName();

			String name = getInputName("del-" + oldName);
			if (rs.getParameter(name, null) != null) {
				delField(oldName);
			} else {
				field.setName(rs.getParameter(getInputName("name-" + oldName), ""));
				field.setRequire(rs.getParameter(getInputName("require-" + oldName), null) != null);
				field.setLabel(rs.getParameter(getInputName("label-" + oldName), ""));
				String cond = rs.getParameter(getInputName("condition-" + oldName), "");
				field.setCondition(cond);
				field.setType(rs.getParameter(getInputName("type-" + oldName), ""));
				field.setWidth(Integer.parseInt(rs.getParameter(getInputName("width-" + oldName), "6")));
				if (!oldName.equals(field.getName())) {
					delField(oldName);
				}
				String listValue = rs.getParameter(getInputName("list-" + oldName), null);
				if (listValue != null) {
					field.setList(listValue);
				}
				String registeredListValue = rs.getParameter(getInputName("registered-list-" + oldName), null);
				if (registeredListValue != null) {
					field.setRegisteredList(registeredListValue);
				}

				String up = getInputName("up-" + oldName);
				if (rs.getParameter(up, null) != null) {
					field.setOrder(field.getOrder() - 15);
				}
				String down = getInputName("down-" + oldName);
				if (rs.getParameter(down, null) != null) {
					field.setOrder(field.getOrder() + 15);
				}
				store(field);
			}
		}

		if (rs.getParameter(getInputName("new-name"), "").trim().length() > 0) {
			String fieldName = StringHelper.createFileName(rs.getParameter(getInputName("new-name"), null));
			store(new Field(fieldName, "", "text", "", "text", "", "", pos + 20, 6));
			ctx.getRequest().setAttribute(getNewFieldKey(), fieldName);
		}

		store(ctx);

		countCache = null;

		return null;
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

	protected boolean isStorage() {
		return true;
	}

	protected File getFile(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String loc = ctx.getCurrentPage().getLocation(ctx);
		if (!StringHelper.isEmpty(loc)) {
			loc = "_"+StringHelper.createFileName(loc);
		} else {
			loc = "";
		}
		String fileName = StringHelper.createFileName(InfoBean.getCurrentInfoBean(ctx).getSection())+"/df-" + StringHelper.createFileName(getPage().getTitle(ctx)+"_"+StringHelper.renderDate(getPage().getContentDateNeverNull(ctx))) +loc+".csv";		
		if (getLocalConfig(false).get("filename") != null && getLocalConfig(false).get("filename").toString().trim().length() > 0) {
			fileName = getLocalConfig(false).getProperty("filename");
		} else {
			getLocalConfig(false).setProperty("filename", fileName);	
			store(ctx);
		}
		File file = new File(URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getStaticFolder(), "dynamic-form-result", fileName));
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		return file;
	}
	
	private int lineNumber = -1;

	protected int storeResult(ContentContext ctx, Map<String, String> data) throws Exception {
		synchronized (LOCK_ACCESS_FILE) {
			File file = getFile(ctx);
			if (lineNumber < 0) {
				if (!file.exists()) {
					lineNumber = -1;
				} else {
					lineNumber = ResourceHelper.countLines(file)-2;
					if (lineNumber<0) {
						lineNumber = 0;
					}
				}
			}
			Collection<String> titles = CSVFactory.loadTitle(file);
			boolean newTitleFound = false;
			for (String newTitle : data.keySet()) {
				if (!titles.contains(newTitle)) {
					newTitleFound = true;
				}
			}			
			RequestService rs = RequestService.getInstance(ctx.getRequest());
			int editLineNumber = decodeEditNumber(ctx, rs.getParameter(getInputEditLineName(ctx)));
			if (editLineNumber > 0) {
				lineNumber = editLineNumber;
				File csvFile = getFile(ctx);
				List<Map<String, String>> allData = CSVFactory.loadContentAsMap(csvFile);
				if (allData.size() >= editLineNumber) {
					Map<String, String> oldData = allData.get(editLineNumber);
					for (Map.Entry<String, String> entry : data.entrySet()) {
						Field field = getField(entry.getKey());
						if (field != null && field.getType().equals("file") && StringHelper.isEmpty(entry.getValue())) {
							data.put(entry.getKey(), oldData.get(entry.getKey()));
						}
					}
					allData.set(editLineNumber, data);
				}
				CSVFactory.storeContentAsMap(getFile(ctx), allData);
			} else {		
				if (newTitleFound) {
					List<Map<String, String>> newData = CSVFactory.loadContentAsMap(file);
					newData.add(data);
					CSVFactory.storeContentAsMap(file, newData);
				} else {
					CSVFactory.appendContentAsMap(file, data);
				}
				lineNumber++;
			}
		}
		return lineNumber;
	}

	protected boolean isSendEmail() {
		return true;
	}

	protected InternetAddress getConfirmToEmail(ContentContext ctx) {
		String emailConformField = getLocalConfig(false).getProperty("mail.confirm.field", null);
		if (emailConformField == null) {
			for (Field field : getFields()) {
				if (field.getType().equals("email") && emailConformField == null) {
					emailConformField = field.getName();
				}
			}
		}
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		if (emailConformField != null && requestService.getParameter(emailConformField, "") != null) {
			String tmpEmail = requestService.getParameter(emailConformField, "");
			try {
				return new InternetAddress(tmpEmail);
			} catch (Exception e) {
				logger.warning(e.getMessage());
			}
		}
		return null;
	}

	public static String performSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception {

		RequestService rs = RequestService.getInstance(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		ContentService content = ContentService.getInstance(request);
		SmartGenericForm comp = (SmartGenericForm) content.getComponent(ctx, rs.getParameter("comp_id", null));
		boolean eventClose = comp.isClose(ctx);

		String code = rs.getParameter("_form-code", "");
		if (!comp.cacheForm.containsKey(code) && !comp.isCaptcha(ctx)) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			GenericMessage msg = new GenericMessage(i18nAccess.getViewText("error.bad-from-version", "This form has experied, try again."), GenericMessage.ERROR);
			request.setAttribute("msg", msg);
			return "This form has experied, try again.";
		}

		/** check captcha **/
		String captcha = rs.getParameter("captcha", null);

		String userIP = request.getHeader("x-real-ip");
		if (StringHelper.isEmpty(userIP)) {
			userIP = request.getRemoteAddr();
		}

		if (comp.isCaptcha(ctx)) {
			if (StringHelper.isOneEmpty(comp.getRecaptchaKey(), comp.getRecaptchaSecretKey())) {
				if (captcha == null || CaptchaService.getInstance(request.getSession()).getCurrentCaptchaCode() == null || !CaptchaService.getInstance(request.getSession()).getCurrentCaptchaCode().equals(captcha)) {
					GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("error.captcha", "bad captcha."), GenericMessage.ERROR);
					request.setAttribute("msg", msg);
					request.setAttribute("error_captcha", "true");
					CaptchaService.getInstance(request.getSession()).reset();
					return null;
				} else {
					CaptchaService.getInstance(request.getSession()).reset();
				}
			} else {
				Map<String, String> params = new HashMap<String, String>();
				params.put("secret", comp.getRecaptchaKey());
				params.put("response", rs.getParameter("g-recaptcha-response", ""));
				params.put("remoteip", request.getRemoteAddr());
				String url = URLHelper.addAllParams("https://www.google.com/recaptcha/api/siteverify", "secret=" + comp.getRecaptchaSecretKey(), "response=" + rs.getParameter("g-recaptcha-response", ""), "remoteip=" + userIP);
				String captchaResponse = NetHelper.readPage(new URL(url));
				JSONMap map = JSONMap.parseMap(captchaResponse);
				if (map == null || !StringHelper.isTrue(map.get("success"))) {
					GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("error.captcha", "bad captcha."), GenericMessage.ERROR);
					request.setAttribute("msg", msg);
					request.setAttribute("error_captcha", "true");
					return null;
				}
			}
		}

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		String subject = "GenericForm submit : '" + globalContext.getGlobalTitle() + "' ";
		String subjectField = comp.getLocalConfig(false).getProperty("mail.subject.field", null);
		if (subjectField != null && rs.getParameter(subjectField, null) != null) {
			subject = comp.getLocalConfig(false).getProperty("mail.subject", "") + rs.getParameter(subjectField, null);
		} else {
			subject = comp.getLocalConfig(false).getProperty("mail.subject", subject);
		}
		InfoBean.getCurrentInfoBean(ctx); // create info bean if not exist
		subject = XHTMLHelper.replaceJSTLData(ctx, subject);		
		Map<String, Object> params = rs.getParameterMap();
		Map<String, String> result = new HashMap<String, String>();
		List<String> errorFields = new LinkedList<String>();
		result.put("__registration time", StringHelper.renderSortableTime(new Date()));
		result.put("__local addr", request.getLocalAddr());
		result.put("__remote addr", request.getRemoteAddr());
		result.put("__X-Forwarded-For", request.getHeader("x-forwarded-for"));
		result.put("__X-Real-IP", request.getHeader("x-real-ip"));
		result.put("__referer", request.getHeader("referer"));
		String registrationID = StringHelper.getShortRandomId();
		result.put("_registrationID", registrationID);
		result.put("_event-close", "" + comp.isClose(ctx));
		result.put(VALIDED, "false");
		String fakeField = comp.getLocalConfig(false).getProperty("field.fake", "fake");
		boolean withXHTML = StringHelper.isTrue(comp.getLocalConfig(false).getProperty("field.xhtml", null));
		boolean fakeFilled = false;

		List<String> keys = new LinkedList<String>(params.keySet());
		Collections.sort(keys, new StringComparator());

		/** store attach files **/

		Map<String, String> specialValues = new HashMap<String, String>();

		String badFileFormatRAW = comp.getLocalConfig(false).getProperty("file.bad-file", "exe,bat,scr,bin,obj,lib,dll,bat,sh,com,cmd,msi,jsp,xml,html,htm,vbe,wsf,wsc,asp,php");
		List<String> badFileFormat = StringHelper.stringToCollection(badFileFormatRAW, ",");
		long maxFileSize = comp.getMaxFileSize();

		for (FileItem file : rs.getAllFileItem()) {
			String ext = StringHelper.getFileExtension(file.getName()).toLowerCase();
			if (badFileFormat.contains(ext)) {
				logger.warning("file blocked because bad extention : " + file.getName());
				GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("message.bad-file", "bad file format."), GenericMessage.ERROR);
				request.setAttribute("msg", msg);
				return null;
			}
			InputStream in = file.getInputStream();
			if (in != null) {
				try {
					if (file.getName().trim().length() > 0) {
						String fileName = URLHelper.mergePath(comp.getAttachFolder(ctx).getAbsolutePath(), StringHelper.createFileName(file.getName()));
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
					}
				} finally {
					ResourceHelper.closeResource(in);
				}
			}
		}

		Map<String, String> adminMailData = new LinkedHashMap<String, String>();
		Map<String, String> userMailData = new LinkedHashMap<String, String>();
		String errorFieldList = " (";
		String errorFieldSep = "";
		Collection<String> errorKeyFound = new HashSet<String>();
		boolean badFormatFound = false;
		boolean update = comp.isUpdate(ctx);
		
		String loc = ctx.getCurrentPage().getLocation(ctx);
		if (!StringHelper.isEmpty(loc)) {
			adminMailData.put("location", loc);
		}
		
		for (Field field : comp.getFields()) {
			String key = field.getName();			

			Object value = params.get(key);
			if (specialValues.get(key) != null) {
				value = specialValues.get(key);
			}
			String finalValue = rs.getParameter(key, "");
			if (specialValues.get(key) != null) {
				finalValue = specialValues.get(key);
			}
			
			if (key.equals(fakeField) && finalValue.trim().length() > 0) {
				fakeFilled = true;
			} else if (!withXHTML && (finalValue.toLowerCase().contains("</a>") || finalValue.toLowerCase().contains("</div>"))) {
				fakeFilled = true;
			}

			if (!update || !field.getType().equals("file")) {
				if (!field.isFilledWidth(finalValue) && StringHelper.containsUppercase(key.substring(0, 1))) {
					errorKeyFound.add(key);
					errorFields.add(key);
					errorFieldList = errorFieldList + errorFieldSep + field.getLabel();
					errorFieldSep = ",";
					if (badFormatFound) {
						GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("error.generic", "please check all fields.") + errorFieldList + ')', GenericMessage.ERROR);
						request.setAttribute("msg", msg);
					} else {
						GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("error.required", "please could you fill all required fields.") + errorFieldList + ')', GenericMessage.ERROR);
						request.setAttribute("msg", msg);
					}
				}
			}
			if (!StringHelper.isEmpty(finalValue)) {
				if (field.getType().equals(Field.TYPE_VAT) && !StringHelper.isEmpty(finalValue)) {
					Boolean errorVAT = false;
					Company company = NetHelper.validVATEuroparlEU(ctx, finalValue);
					if (ctx.getGlobalContext().getStaticConfig().isInternetAccess()) {
						if (company == null) {
							errorVAT = true;
						} else {
							for (Field f : comp.getFields()) {
								if (f.getName().equalsIgnoreCase("adresse") || f.getName().equalsIgnoreCase("address") && StringHelper.isEmpty(f.getValue())) {
									f.setValue(company.getAddress());
								}
							}
						}
					} else {
						errorVAT = StringHelper.isVAT(field.getValue());
					}
					if (errorVAT) {
						errorKeyFound.add(key);
						errorFields.add(key);
						errorFieldList = errorFieldList + errorFieldSep + field.getLabel();
						errorFieldSep = ",";
						I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
						GenericMessage msg = new GenericMessage(i18nAccess.getViewText("error.vat", "VAT number not idenfied. (sample:BE0824.985.592)") + ')', GenericMessage.ERROR);
						request.setAttribute("msg", msg);
						badFormatFound = true;
					}
				} else if (!field.isValueValid(finalValue) && !errorKeyFound.contains(key)) {
					errorKeyFound.add(key);
					errorFields.add(key);
					errorFieldList = errorFieldList + errorFieldSep + field.getLabel();
					errorFieldSep = ",";
					GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("error.generic", "please check all fields.") + errorFieldList + ')', GenericMessage.ERROR);
					request.setAttribute("msg", msg);
					badFormatFound = true;
				}
			}
			
//			if (!StringHelper.isEmpty(field.getRegisteredList())) {
//				List<IListItem> list = ListService.getInstance(ctx).getList(ctx, field.getRegisteredList());
//				if (list != null) {
//					for (IListItem item : list) {
//						if (item.getKey().equals(finalValue)) {
//							finalValue = finalValue+" ("+item.getValue()+')';
//						}
//					}
//				}
//			}
			 
			if (ctx.getCurrentUser() != null) {
				adminMailData.put("user", ctx.getCurrentUser().getLogin());
			}		

			if (value instanceof Object[]) {				
				finalValue = StringHelper.arrayToString((Object[]) params.get(key), ",");
				adminMailData.put(field.getLabel() + " (" + key + ") ", finalValue);
				userMailData.put(field.getLabel() + MailService.HIDDEN_DIV + key + "</div>", finalValue);
			} else {
				adminMailData.put(field.getLabel() + " (" + key + ") ", finalValue);
				userMailData.put(field.getLabel() + MailService.HIDDEN_DIV + key + "</div>", finalValue);
			}
			result.put(key, finalValue);

		}

		if (fakeFilled) {
			logger.warning("spam detected fake field filled : " + comp.getPage().getPath());
		}

		if (errorFields.size() == 0) {

			logger.info(adminMailData.toString());

			int lineNumber = -1;
			if (comp.isStorage()) {
				lineNumber = comp.storeResult(ctx, result);
			}

			if (comp.isSendEmail() && !fakeFilled) {

				String emailFrom = comp.getLocalConfig(false).getProperty("mail.from", StaticConfig.getInstance(request.getSession()).getSiteEmail());
				String emailFromField = comp.getLocalConfig(false).getProperty("mail.from.field", null);
				if (emailFromField != null && rs.getParameter(emailFromField, "") != null) {
					String tmpEmail = rs.getParameter(emailFromField, "");
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
				MailService mailService = null;
				
				Map<String, String> paramsEdit = new HashMap<String, String>();
				paramsEdit.put(comp.getInputEditLineName(ctx), comp.encodeEditNumber(ctx, lineNumber));
				String editURL = URLHelper.createURL(ctx.getContextForAbsoluteURL(), ctx.getPath(), paramsEdit);
				paramsEdit = new HashMap<String, String>();
				paramsEdit.put(comp.getInputValidLineName(ctx), comp.encodeEditNumber(ctx, lineNumber));
				String validURL = URLHelper.createURL(ctx.getContextForAbsoluteURL(), ctx.getPath(), paramsEdit);				
				try {
					mailService = MailService.getInstance(new MailConfig(globalContext, globalContext.getStaticConfig(), null));
					InternetAddress fromEmail = new InternetAddress(emailFrom);
					InternetAddress toEmail = new InternetAddress(emailTo);
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

					comp.countCache = null;
					ContentContext absCtx = ctx.getContextForAbsoluteURL();

					String mailAdminContent;
					String prefix = "Event registration :";
					if (comp.isFullEventSite(ctx)) {
						prefix = "Waiting list : ";
						subject = prefix + subject;
					}
					
					if (!StringHelper.isEmpty(comp.getLocalConfig(false).getProperty("event.limit"))) {
						mailAdminContent = XHTMLHelper.createAdminMail(ctx.getCurrentPage().getTitle(ctx), prefix + comp.getCountSubscription(ctx) + "/" + comp.getLocalConfig(false).getProperty("event.limit"), adminMailData, editURL, "edit >>", null);
					} else {
						mailAdminContent = XHTMLHelper.createAdminMail(ctx.getCurrentPage().getTitle(ctx), "Form submit - " + comp.getCountSubscription(ctx), adminMailData, editURL, "edit >>", null);
					}
					mailService.sendMail(null, fromEmail, toEmail, ccList, bccList, subject, mailAdminContent, true, null, globalContext.getDKIMBean());
					
					if (comp.isWarningEventSite(ctx)) {
						subject = globalContext.getContextKey() + " - WARNING Event almost full : " + ctx.getCurrentPage().getTitle(ctx) + " ["+StringHelper.renderDate(comp.getPage().getContentDateNeverNull(absCtx))+']';							
						Map data = new HashMap();
						String eventLimistStr = comp.getLocalConfig(false).getProperty("event.alert-limit");
						int countSubscription = comp.getCountSubscription(ctx);						
						data.put("event", ctx.getCurrentPage().getTitle(ctx));
						data.put("subscription", countSubscription);
						data.put("limit", comp.getLocalConfig(false).getProperty("event.limit", ""));
						data.put("alert limit", eventLimistStr);
						if (loc != null) {
							data.put("location", loc);
						}
						absCtx.setRenderMode(ContentContext.PREVIEW_MODE);
						String adminMailContent = XHTMLHelper.createAdminMail(ctx.getCurrentPage().getTitle(ctx), "Please check you event, it will be automaticly closed soon.", data, editURL, "edit >>", null);
						mailService.sendMail(null, new InternetAddress(globalContext.getAdministratorEmail()), toEmail, ccList, bccList, subject, adminMailContent, true, null, globalContext.getDKIMBean());
					}

					if (comp.isClosedEventSite(ctx)) {
						subject = globalContext.getContextKey() + " - WARNING Event full : " + ctx.getCurrentPage().getTitle(ctx) + " ["+StringHelper.renderDate(comp.getPage().getContentDateNeverNull(absCtx))+']';							
						Map data = new HashMap();
						String eventLimistStr = comp.getLocalConfig(false).getProperty("event.alert-limit");
						int countSubscription = comp.getCountSubscription(ctx);
						data.put("event", ctx.getCurrentPage().getTitle(ctx));
						data.put("subscription", countSubscription);
						data.put("limit", comp.getLocalConfig(false).getProperty("event.limit", ""));
						data.put("alert limit", eventLimistStr);
						if (loc != null) {
							data.put("location", loc);
						}
						absCtx.setRenderMode(ContentContext.PREVIEW_MODE);
						String adminMailContent = XHTMLHelper.createAdminMail(ctx.getCurrentPage().getTitle(ctx), "Please check you event, it has been closed.", data, editURL, "edit >>", null);
						mailService.sendMail(null, new InternetAddress(globalContext.getAdministratorEmail()), toEmail, ccList, bccList, subject, adminMailContent, true, null, globalContext.getDKIMBean());
					}

					if (eventClose) {
						subject = globalContext.getContextKey() + " - WARNING registration on full event : " + ctx.getCurrentPage().getTitle(ctx) + " ["+StringHelper.renderDate(comp.getPage().getContentDateNeverNull(absCtx))+']';	
						Map data = new HashMap();
						String eventLimistStr = comp.getLocalConfig(false).getProperty("event.alert-limit");
						int countSubscription = comp.getCountSubscription(ctx);
						data.put("event", ctx.getCurrentPage().getTitle(ctx));
						data.put("subscription", countSubscription);
						data.put("limit", comp.getLocalConfig(false).getProperty("event.limit", ""));
						data.put("alert limit", eventLimistStr);
						if (loc != null) {
							data.put("location", loc);
						}
						absCtx.setRenderMode(ContentContext.PREVIEW_MODE);
						String adminMailContent = XHTMLHelper.createAdminMail(ctx.getCurrentPage().getTitle(ctx), "Please check you event, it is full.", data, editURL, "edit >>", null);
						mailService.sendMail(null, new InternetAddress(globalContext.getAdministratorEmail()), toEmail, ccList, bccList, subject, adminMailContent, true, null, globalContext.getDKIMBean());
					}

					String mailPath;
					String mailSubject;
					if (!eventClose) {
						mailPath = comp.getLocalConfig(false).getProperty("mail.confirm.link", null);
						mailSubject = comp.getLocalConfig(false).getProperty("mail.confirm.subject", null);
					} else {
						mailPath = comp.getLocalConfig(false).getProperty("mail.closed.link", null);
						mailSubject = comp.getLocalConfig(false).getProperty("mail.closed.subject", null);
					}
					ContentContext pageCtx = ctx.getContextForAbsoluteURL();
					pageCtx.setRenderMode(ContentContext.PAGE_MODE);
					if (!StringHelper.isEmpty(mailSubject)) {
						mailSubject = XHTMLHelper.replaceJSTLData(pageCtx, mailSubject);
						logger.info("read mail from : "+mailPath);
						String email = NetHelper.readPageForMailing(new URL(URLHelper.createURL(pageCtx, mailPath)));
						if (email != null && email.length() > 0) {
							InternetAddress to = comp.getConfirmToEmail(ctx);
							if (to != null) {
								for (Field field : comp.getFields()) {
									email = email.replace("${field." + field.getName() + "}", rs.getParameter(field.getName(), ""));
								}
								email = email.replace("${registrationID}", registrationID);								
								email = email.replace("${communication}", StringHelper.encodeAsStructuredCommunicationMod97(registrationID));
								email = email.replace("${htmlFields}", XHTMLHelper.createHTMLTable(userMailData));
								
								email = email.replace("${url.edit}", editURL);
								email = email.replace("${url.valid}", validURL);
								email = email.replace("${url.page}", URLHelper.createURL(ctx.getContextForAbsoluteURL()));
								email = email.replace("${url.root}", URLHelper.createURL(ctx.getContextForAbsoluteURL(), "/"));

								email = email.replace("${event.title}", comp.getPage().getTitle(pageCtx));
								email = email.replace("${event.location}", comp.getPage().getLocation(pageCtx));
								email = email.replace("${event.description}", XHTMLHelper.textToXHTML(comp.getPage().getDescriptionAsText(pageCtx), globalContext));
								Event event = comp.getPage().getEvent(pageCtx);
								if (event != null) {
									email = email.replace("${event.start}", StringHelper.renderDate(event.getStart()));
									email = email.replace("${event.end}", StringHelper.renderDate(event.getEnd()));
								}

								InternetAddress registrationFrom = new InternetAddress(comp.getLocalConfig(false).getProperty("mail.from", StaticConfig.getInstance(request.getSession()).getSiteEmail()));
								NetHelper.sendMail(ctx.getGlobalContext(), registrationFrom, to, null, bccEmail, mailSubject, email, null, true);
							} else {
								return "warning : no recipient found.";
							}
						}
					}
				} catch (Exception e) {
					if (mailService != null && mailService.getMailConfig() != null) {
						System.out.println("SMTP host  = " + mailService.getMailConfig().getSMTPHost());
						System.out.println("SMTP port  = " + mailService.getMailConfig().getSMTPPort());
						System.out.println("SMTP login = " + mailService.getMailConfig().getLogin());
						System.out.println("SMTP pwd?  = " + !StringHelper.isEmpty(mailService.getMailConfig().getPassword()));
					}
					String errorID = "E" + StringHelper.getRandomId();
					logger.severe("error id:" + errorID + " = " + e.getMessage());
					e.printStackTrace();					
					request.setAttribute("valid", "false");
					return comp.getLocalConfig(false).getProperty("message.error", "technical error.") + " (" + errorID + ')';
				}
			}

			GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("message.thanks"), GenericMessage.INFO);
			request.setAttribute("msg", msg);
			request.setAttribute("valid", "true");
			comp.cacheForm.remove(code);
		} else {
			request.setAttribute("errorFields", new CollectionAsMap<String>(errorFields));
		}

		return null;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getActionGroupName() {
		return "smart-form";
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean isContentCachableByQuery(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		boolean outB = super.initContent(ctx);
		getLocalConfig(false).setProperty("title", getType());
		store(ctx);
		return outB;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}

	@Override
	public List<IUserInfo> getParticipants(ContentContext ctx) throws Exception {
		List<Map<String, String>> data = getData(ctx);
		if (data.size() == 0) {
			return Collections.EMPTY_LIST;
		} else {
			IUserFactory userFactory = UserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
			List<IUserInfo> users = new LinkedList<IUserInfo>();
			for (Map<String, String> line : data) {
				IUserInfo userInfo = userFactory.createUserInfos();
				for (Object key : BeanHelper.beanSetList(userInfo)) {
					String value = line.get(key.toString());
					if (value == null) {
						value = line.get(StringUtils.capitalize(key.toString()));
					}
					if (value != null) {
						BeanHelper.setProperty(userInfo, key.toString(), value);
					}
				}
				if (StringHelper.isEmpty(userInfo.getLogin())) {
					userInfo.setLogin(userInfo.getEmail());
					if (StringHelper.isEmpty(userInfo.getLogin())) {
						userInfo.setLogin("???");
					}
				}
				users.add(userInfo);
			}
			return users;
		}
	}

	@Override
	public String getUserLink(ContentContext ctx) throws Exception {
		return null;
	}
	
	@Override
	public String getFontAwesome() {	
		return "address-card";
	}
}
