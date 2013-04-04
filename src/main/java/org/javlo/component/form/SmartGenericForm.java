package org.javlo.component.form;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.Comparator.StringComparator;
import org.javlo.service.RequestService;

public class SmartGenericForm extends GenericForm {

	public static final class Field {

		public static final class FieldComparator implements Comparator<Field> {

			@Override
			public int compare(Field o1, Field o2) {
				return StringComparator.compareText(o1.getName(), o2.getName());
			}

		}

		private static final char SEP = '|';

		private String name;
		private String label;
		private String type = "text";
		private String value;

		public Field(String name, String label, String type, String value) {
			this.name = name;
			this.label = label;
			this.type = type;
			this.value = value;
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
			this.name = StringHelper.createASCIIString(name);
		}

		@Override
		public String toString() {
			return getLabel() + SEP + getType() + SEP + getValue();
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
	}

	public static final String TYPE = "smart-generic-form";
	private static final Collection<? extends Object> FIELD_TYPES = Arrays.asList(new String[] { "text", "yes/no", "email", "file" });

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println(XHTMLHelper.renderLine("title", getInputName("title"), getLocalConfig(false).getProperty("title", "")));
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
		out.println(XHTMLHelper.renderLine("field required :", getInputName("error-required"), getLocalConfig(false).getProperty("error.required", "")));
		out.println(XHTMLHelper.renderLine("thanks :", getInputName("message-thanks"), getLocalConfig(false).getProperty("message.thanks", "")));
		out.println(XHTMLHelper.renderLine("error :", getInputName("message-error"), getLocalConfig(false).getProperty("message.error", "")));
		if (isFile()) {
			out.println(XHTMLHelper.renderLine("bad file format :", getInputName("message-bad-file"), getLocalConfig(false).getProperty("message.bad-file", "")));
			out.println(XHTMLHelper.renderLine("file to big :", getInputName("message-tobig-file"), getLocalConfig(false).getProperty("message.tobig-file", "")));
		}
		out.println("</fieldset></div></div>");
		out.println("<div class=\"action-add\"><input type=\"text\" name=\"" + getInputName("new-name") + "\" placeholder=\"field name\" /> <input type=\"submit\" name=\"" + getInputName("add") + "\" value=\"add\" /></div>");
		out.println("<table class=\"sTable2\">");
		out.println("<thead><tr><td>name</td><td>label</td><td>type</td><td>require</td><td>action</td></tr></thead>");
		out.println("<tbody>");
		List<Field> fields = getFields();
		for (Field field : fields) {
			out.println(getEditXHTML(field));
		}
		out.println("</tbody>");
		out.println("</table>");

		out.close();
		return new String(outStream.toByteArray());
	}

	public String getEditXHTML(Field field) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<tr class=\"field-line\">");
		out.println("<td><input type=\"text\" name=\"" + getInputName("name-" + field.getName()) + "\" value=\"" + field.getName() + "\"/></td>");
		out.println("<td><input type=\"text\" name=\"" + getInputName("label-" + field.getName()) + "\" value=\"" + field.getLabel() + "\"/></td>");
		out.println("<td>" + XHTMLHelper.getInputOneSelect(getInputName("type-" + field.getName()), FIELD_TYPES, field.getType()) + "</td>");
		String required = "";
		if (field.isRequire()) {
			required = " checked=\"checked\"";
		}
		out.println("<td><input type=\"checkbox\" name=\"" + getInputName("require-" + field.getName()) + "\"" + required + " /></td>");
		out.println("<td><input class=\"needconfirm\" type=\"submit\" name=\"" + getInputName("del-" + field.getName()) + "\" value=\"del\" /></td>");
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
					Field field = new Field(name, data[0], data[1], data[2]);
					fields.add(field);
				}
			}
		}
		Collections.sort(fields, new Field.FieldComparator());
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

	@Override
	public String getRenderer(ContentContext ctx) {
		return super.getRenderer(ctx);
	}

	public String getTitle() {
		return getLocalConfig(false).getProperty("title");
	}

	public synchronized void storeField(Field field) {
		String key = "field." + field.getName();
		Properties prop = getLocalConfig(false);
		if (prop.contains(key)) {
			prop.remove(prop);
		}
		prop.setProperty(key, field.toString());
	}

	public synchronized void delField(String name) {
		getLocalConfig(false).remove("field." + name);
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		getLocalConfig(false).setProperty("title", rs.getParameter(getInputName("title"), ""));
		getLocalConfig(false).setProperty("mail.to", rs.getParameter(getInputName("to"), ""));
		getLocalConfig(false).setProperty("mail.cc", rs.getParameter(getInputName("cc"), ""));
		getLocalConfig(false).setProperty("mail.bcc", rs.getParameter(getInputName("bcc"), ""));
		getLocalConfig(false).setProperty("mail.subject", rs.getParameter(getInputName("subject"), ""));
		getLocalConfig(false).setProperty("mail.subject.field", rs.getParameter(getInputName("subject-field"), ""));
		getLocalConfig(false).setProperty("mail.from", rs.getParameter(getInputName("from"), ""));
		getLocalConfig(false).setProperty("mail.from.field", rs.getParameter(getInputName("from-field"), ""));

		getLocalConfig(false).setProperty("error.required", rs.getParameter(getInputName("error-required"), ""));
		getLocalConfig(false).setProperty("message.thanks", rs.getParameter(getInputName("message-thanks"), ""));
		getLocalConfig(false).setProperty("message.error", rs.getParameter(getInputName("message-error"), ""));
		// getLocalConfig(false).setProperty("", rs.getParameter(getInputName(""), ""));

		if (isFile()) {
			getLocalConfig(false).setProperty("message.bad-file", rs.getParameter(getInputName("message-bad-file"), ""));
			getLocalConfig(false).setProperty("message.tobig-file", rs.getParameter(getInputName("message-tobig-file"), ""));
		}

		for (Field field : getFields()) {
			String oldName = field.getName();
			if (rs.getParameter(getInputName("del-" + field.getName()), null) != null) {
				delField(field.getName());
			} else {
				field.setName(rs.getParameter(getInputName("name-" + oldName), ""));
				field.setRequire(rs.getParameter(getInputName("require-" + oldName), null) != null);
				field.setLabel(rs.getParameter(getInputName("label-" + oldName), ""));
				field.setType(rs.getParameter(getInputName("type-" + oldName), ""));
				if (!oldName.equals(field.getName())) {
					delField(oldName);
				}
				storeField(field);
			}
		}

		if (rs.getParameter(getInputName("add"), null) != null && rs.getParameter(getInputName("new-name"), "").trim().length() > 0) {
			storeField(new Field(rs.getParameter(getInputName("new-name"), ""), "", "text", ""));

		}

		Writer writer = new StringWriter();
		getLocalConfig(false).store(writer, "comp:" + getId());
		if (!getValue().equals(writer.toString())) {
			setValue(writer.toString());
			setModify();
			setNeedRefresh(true);
		}
	}

	@Override
	public String getType() {
		return TYPE;
	}
}
