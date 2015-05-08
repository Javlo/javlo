package org.javlo.component.form;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.LangHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.Comparator.StringComparator;
import org.javlo.mailing.MailService;
import org.javlo.message.GenericMessage;
import org.javlo.service.CaptchaService;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.utils.CSVFactory;
import org.javlo.utils.CollectionAsMap;
import org.javlo.ztatic.StaticInfo;

public class SmartGenericForm extends AbstractVisualComponent implements IAction{
	
	private Properties bundle;
	
	private static Logger logger = Logger.getLogger(SmartGenericForm.class.getName());
	
	protected static final Object LOCK = new Object();

	public static class Field {

		public static final class FieldComparator implements Comparator<Field> {

			@Override
			public int compare(Field o1, Field o2) {
				if (o1.getOrder() > 0 && o2.getOrder() > 0) {
					return o1.getOrder().compareTo(o2.getOrder());
				} else {
					return StringComparator.compareText(o1.getName(), o2.getName());
				}
			}
		}

		protected static final char SEP = '|';

		private String name;
		private String label;
		private String type = "text";
		private String value;
		private String list = "";
		private String registeredList = "";
		private int order = 0;
		private int width = 12;
		private boolean last = false;
		private boolean first  = false;

		protected static Collection<? extends Object> FIELD_TYPES = Arrays.asList(new String[] { "text", "large-text", "yes-no", "email", "radio", "list", "registered-list", "file", "validation" });

		public Field(String name, String label, String type, String value, String list, String registeredList, int order, int width) {
			this.name = name;
			this.label = label;
			this.type = type;
			this.value = value;
			this.list = list;
			this.registeredList = registeredList;
			this.order = order;
			this.setWidth(width);
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = StringHelper.createASCIIString(name).replace(' ', '_');
		}
		
		@Override
		public String toString() {
			return getLabel() + SEP + getType() + SEP + getValue() + SEP + list + SEP + getOrder() + SEP + getRegisteredList() + SEP + getOrder() + SEP + getWidth();
		}

		public boolean isRequire() {
			if (getName().length() > 0) {
				return Character.isUpperCase(getName().charAt(0));
			} else {
				return false;
			}
		}

		public void setRequire(boolean require) {
			if (getName().length() > 0) {
				if (require) {
					setName(getName().substring(0, 1).toUpperCase() + getName().substring(1));
				} else {
					setName(getName().substring(0, 1).toLowerCase() + getName().substring(1));
				}
			}

		}

		public List<String> getList() {
			List<String> outList = StringHelper.stringToCollection(list);
			return outList;
		}

		public void setList(String list) {
			this.list = StringHelper.replaceCR(list, StringHelper.DEFAULT_LIST_SEPARATOR);
		}

		public Integer getOrder() {
			return order;
		}

		public void setOrder(int ordre) {
			this.order = ordre;
		}

		public String getPrefix() {
			return "field";
		}

		public Collection<? extends Object> getFieldTypes() {
			return FIELD_TYPES;
		}

		public String getRegisteredList() {
			return registeredList;
		}

		public void setRegisteredList(String registeredList) {
			this.registeredList = registeredList;
		}

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		/**
		 * is the last element of cols sequence.  That mean width with next field is greater than 12.
		 * @return
		 */
		public boolean isLast() {
			return last;
		}

		public void setLast(boolean last) {
			this.last = last;
		}

		/**
		 * is the first element of cols sequence.
		 * @return
		 */
		public boolean isFirst() {
			return first;
		}

		public void setFirst(boolean first) {
			this.first = first;
		}
		
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
	
	protected boolean isCaptcha(ContentContext ctx) {
		return StringHelper.isTrue(getLocalConfig(false).getProperty("captcha", "" + isCaptcha()));
	}
	
	public boolean isCaptcha() {
		return StringHelper.isTrue(getLocalConfig(false).getProperty("captcha", "true"));
	}

	public static final String TYPE = "smart-generic-form";

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println(XHTMLHelper.renderLine("title", getInputName("title"), getLocalConfig(false).getProperty("title", "")));
		out.println(XHTMLHelper.renderLine("filename", getInputName("filename"), getLocalConfig(false).getProperty("filename", "")));
		out.println(XHTMLHelper.renderLine("captcha", getInputName("captcha"), StringHelper.isTrue(getLocalConfig(false).getProperty("captcha", null))));
		if (isFile()) {
			out.println(XHTMLHelper.renderLine("max file size (Kb)", getInputName("filesize"), ""+getMaxFileSize() ));
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
		out.println("<div class=\"one_half\"><fieldset><legend>message</legend>");
		out.println(XHTMLHelper.renderLine("field required :", getInputName("message-required"), getLocalConfig(false).getProperty("message.required", "")));
		out.println(XHTMLHelper.renderLine("error required :", getInputName("error-required"), getLocalConfig(false).getProperty("error.required", "")));
		out.println(XHTMLHelper.renderLine("thanks :", getInputName("message-thanks"), getLocalConfig(false).getProperty("message.thanks", "")));
		out.println(XHTMLHelper.renderLine("error :", getInputName("message-error"), getLocalConfig(false).getProperty("message.error", "")));
		out.println(XHTMLHelper.renderLine("reset :", getInputName("message-reset"), getLocalConfig(false).getProperty("message.reset", "")));
		if (isCaptcha()) {
			out.println(XHTMLHelper.renderLine("captcha :", getInputName("label-captcha"), getLocalConfig(false).getProperty("label.captcha", "")));
		}
		if (isFile()) {
			out.println(XHTMLHelper.renderLine("bad file format :", getInputName("message-bad-file"), getLocalConfig(false).getProperty("message.bad-file", "")));
			out.println(XHTMLHelper.renderLine("file to big :", getInputName("message-tobig-file"), getLocalConfig(false).getProperty("message.tobig-file", "")));
		}
		out.println("</fieldset></div></div>");
		out.println("<div class=\"action-add\"><input type=\"text\" name=\"" + getInputName("new-name") + "\" placeholder=\"field name\" /> <input type=\"submit\" name=\"" + getInputName("add") + "\" value=\"add field\" /></div>");
		if (getFields().size() > 0) {
			out.println("<table class=\"sTable2\">");
			String listTitle = "";
			if (isList()) {
				listTitle = "<td>list</td>";
			}
			out.println("<thead><tr><td>name</td><td>label</td>" + listTitle + "<td>type</td><td>width</td><td>required</td><td>action</td></tr></thead>");
			out.println("<tbody>");
			List<Field> fields = getFields();
			for (Field field : fields) {
				out.println(getEditXHTML(field));
			}
			out.println("</tbody>");
			out.println("</table>");
		}

		out.close();
		return new String(outStream.toByteArray());
	}

	public String getEditXHTML(Field field) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<tr class=\"field-line\">");
		out.println("<td><input type=\"text\" name=\"" + getInputName("name-" + field.getName()) + "\" value=\"" + field.getName() + "\"/></td>");
		out.println("<td><input type=\"text\" name=\"" + getInputName("label-" + field.getName()) + "\" value=\"" + field.getLabel() + "\"/></td>");
		if (isList()) {
			if (field.getType().equals("radio") || field.getType().equals("list")) {
				out.println("<td><textarea name=\"" + getInputName("list-" + field.getName()) + "\">" + StringHelper.collectionToText(field.getList()) + "</textarea></td>");
			} else if (field.getType().equals("registered-list")) {
				out.println("<td><input name=\"" + getInputName("registered-list-" + field.getName()) + "\" placeholder=\"list name\" value=\""+field.getRegisteredList()+"\"/></td>");
			} else {
				out.println("<td>&nbsp;</td>");
			}
		}
		out.println("<td>" + XHTMLHelper.getInputOneSelect(getInputName("type-" + field.getName()), field.getFieldTypes(), field.getType()) + "</td>");
		out.println("<td><select name=\"" + getInputName("width-" + field.getName()) + "\" >");
		for (int i=1; i<=12; i++) {
			String selected = "";
			if (i==field.getWidth()) {
				selected = " selected=\"selected\"";
			}
			out.println("<option"+selected+">"+i+"</option>");
		}
		out.println("</select></td>");
		String required = "";
		if (field.isRequire()) {
			required = " checked=\"checked\"";
		}		
		out.println("<td><input type=\"checkbox\" name=\"" + getInputName("require-" + field.getName()) + "\"" + required + " /></td>");
		out.println("<td><div  class=\"action\">");
		out.println("  <input class=\"up\" type=\"submit\" name=\"" + getInputName("up-" + field.getName()) + "\" value=\"up\" />");
		out.println("  <input class=\"down\" type=\"submit\" name=\"" + getInputName("down-" + field.getName()) + "\" value=\"down\" />");
		out.println("  <input class=\"needconfirm\" type=\"submit\" name=\"" + getInputName("del-" + field.getName()) + "\" value=\"del\" />");
		out.println("</div></td>");
		out.println("</tr>");
		out.close();
		return new String(outStream.toByteArray());
	}

	public synchronized List<Field> getFields() {
		List<Field> fields = new LinkedList<SmartGenericForm.Field>();
		Properties p = getLocalConfig(false);		
		
		for (Object objKey : p.keySet()) {
			String key = objKey.toString();
			if (key.startsWith("field.")) {
				String name = key.replaceFirst("field.", "").trim();
				if (name.trim().length() > 0) {
					String value = p.getProperty(key);
					String[] data = StringUtils.splitPreserveAllTokens(value, Field.SEP);					
					Field field = new Field(name, (String) LangHelper.arrays(data, 0, ""), (String) LangHelper.arrays(data, 1, ""), (String) LangHelper.arrays(data, 2, ""), (String) LangHelper.arrays(data, 3, ""), (String) LangHelper.arrays(data, 5, ""), Integer.parseInt(""+LangHelper.arrays(data, 6, "0")), Integer.parseInt(""+LangHelper.arrays(data, 7, "6")));
					fields.add(field);									
				}
			}
		}
		Collections.sort(fields, new Field.FieldComparator());
		int currentWidth = 0;
		Field lastField = null;
		for (Field field : fields) {
			if (currentWidth==0) {
				field.setFirst(true);
			}
			currentWidth = currentWidth+field.getWidth();			
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
			if (field.getType().contains("list")) {
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
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		ctx.getRequest().setAttribute("ci18n", getLocalConfig(false));
	}
	
	protected long getMaxFileSize() {
		String fileSize = getLocalConfig(false).getProperty("file.max-size");
		if (StringHelper.isDigit(fileSize)) {
			return Long.parseLong(fileSize);
		} else {
			return 0;
		}
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		getLocalConfig(false).setProperty("title", rs.getParameter(getInputName("title"), ""));
		getLocalConfig(false).setProperty("filename", rs.getParameter(getInputName("filename"), ""));
		getLocalConfig(false).setProperty("captcha", rs.getParameter(getInputName("captcha"), ""));
		getLocalConfig(false).setProperty("file.max-size", rs.getParameter(getInputName("filesize"), ""));
		getLocalConfig(false).setProperty("mail.to", rs.getParameter(getInputName("to"), ""));
		getLocalConfig(false).setProperty("mail.cc", rs.getParameter(getInputName("cc"), ""));
		getLocalConfig(false).setProperty("mail.bcc", rs.getParameter(getInputName("bcc"), ""));
		getLocalConfig(false).setProperty("mail.subject", rs.getParameter(getInputName("subject"), ""));
		getLocalConfig(false).setProperty("mail.subject.field", rs.getParameter(getInputName("subject-field"), ""));
		getLocalConfig(false).setProperty("mail.from", rs.getParameter(getInputName("from"), ""));
		getLocalConfig(false).setProperty("mail.from.field", rs.getParameter(getInputName("from-field"), ""));

		getLocalConfig(false).setProperty("message.required", rs.getParameter(getInputName("message-required"), ""));
		getLocalConfig(false).setProperty("error.required", rs.getParameter(getInputName("error-required"), ""));
		getLocalConfig(false).setProperty("message.thanks", rs.getParameter(getInputName("message-thanks"), ""));
		getLocalConfig(false).setProperty("message.error", rs.getParameter(getInputName("message-error"), ""));
		getLocalConfig(false).setProperty("message.reset", rs.getParameter(getInputName("message-reset"), ""));		
		
		if (isCaptcha()) {
			getLocalConfig(false).setProperty("label.captcha", rs.getParameter(getInputName("label-captcha"), ""));
		}
		// getLocalConfig(false).setProperty("", rs.getParameter(getInputName(""), ""));

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
					field.setOrder (field.getOrder()-15);
				} 
				String down = getInputName("down-" + oldName);
				if (rs.getParameter(down, null) != null) {
					field.setOrder (field.getOrder()+15);
				} 
				
				store(field);
			}
		}

		if (rs.getParameter(getInputName("new-name"), "").trim().length() > 0) {
			String fieldName = StringHelper.createFileName(rs.getParameter(getInputName("new-name"),null));
			store(new Field(fieldName, "", "text", "", "", "", pos+20, 6));
		}

		store(ctx);
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
	
	protected File getFile(ContentContext ctx) throws IOException {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String fileName = "df-" + getId() + ".csv";
		if (getLocalConfig(false).get("filename") != null && getLocalConfig(false).get("filename").toString().trim().length() > 0) {
			fileName = getLocalConfig(false).getProperty("filename");
		}
		File file = new File(URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getStaticFolder(), "dynamic-form-result", fileName));
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		return file;
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
	
	protected boolean isSendEmail() {
		return true;
	}

	
	public static String performSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception {

		RequestService requestService = RequestService.getInstance(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		ContentService content = ContentService.getInstance(request);
		SmartGenericForm comp = (SmartGenericForm) content.getComponent(ctx, requestService.getParameter("comp_id", null));

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

		List<String> keys = new LinkedList<String>(params.keySet());
		Collections.sort(keys, new StringComparator());

		/** store attach files **/

		Map<String, String> specialValues = new HashMap<String, String>();

		String badFileFormatRAW = comp.getLocalConfig(false).getProperty("file.bad-file", "exe,bat,scr,bin,obj,lib,dll,bat,sh,com,cmd,msi,jsp,xml,html,htm,vbe,wsf,wsc,asp");
		List<String> badFileFormat = StringHelper.stringToCollection(badFileFormatRAW, ",");	
		long maxFileSize = comp.getMaxFileSize();
		
		for (FileItem file : requestService.getAllFileItem()) {
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

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		for (Field field : comp.getFields()) {
			String key = field.getName();
			
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

			if (finalValue.trim().length() == 0 && key.length() > 0 && StringHelper.containsUppercase(key.substring(0,1))) { // needed field
				errorFields.add(key);
				GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("error.required", "please could you fill all required fields."), GenericMessage.ERROR);
				request.setAttribute("msg", msg);
			}

			if (value instanceof Object[]) {
				finalValue = StringHelper.arrayToString((Object[]) params.get(key), ",");
				out.println(field.getLabel()+ " ("+key+") " + ':');
				out.println(finalValue);
			} else {
				out.println(field.getLabel()+ " ("+key+") " + ':');
				out.println(finalValue);
			}
			out.println("");
			result.put(key, finalValue);
		
		}
		out.println("");
		out.close();

		if (fakeFilled) {
			logger.warning("spam detected fake field filled : " + comp.getPage().getPath());
		}
		
		if (errorFields.size() == 0) {
			String mailContent = new String(outStream.toByteArray());
			if (comp.isHTMLMail()) {
				mailContent = XHTMLHelper.textToXHTML(mailContent);
			}
			mailContent = comp.getMailHeader(ctx) + mailContent + comp.getMailFooter(ctx);

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

				try {

					MailService mailService = MailService.getInstance(globalContext.getStaticConfig());
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

					mailService.sendMail(null, fromEmail, toEmail, ccList, bccList, subject, mailContent, comp.isHTMLMail());
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
	public boolean isEmpty(ContentContext ctx) {
		return false;
	}
}
