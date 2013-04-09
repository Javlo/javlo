package org.javlo.component.form;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.javlo.context.ContentContext;
import org.javlo.helper.LangHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

public class GenericQuiz extends SmartGenericForm {

	public static final String TYPE = "generic-quiz";

	@Override
	public String getType() {
		return TYPE;
	}

	public static class Status {

		private final List<GenericQuiz.Response> responses = new LinkedList<GenericQuiz.Response>();

		private int question = 1;

		public static Status getInstance(HttpSession session, GenericQuiz comp) {
			String KEY = "status-" + comp.getId();
			Status status = (Status) session.getAttribute(KEY);
			if (status == null) {
				status = new Status();
				session.setAttribute(KEY, status);
			}
			for (Question question : comp.getQuestions()) {
				status.responses.add(new GenericQuiz.Response(question, null));
			}
			return status;
		}

		public int getQuestion() {
			return question;
		}

		public void setQuestion(int question) {
			this.question = question;
		}

		public List<GenericQuiz.Response> getResponses() {
			return responses;
		}
	}

	public static class Response {
		private Question question;
		private String response;

		public Response(Question question, String response) {
			super();
			this.question = question;
			this.response = response;
		}

		public Question getQuestion() {
			return question;
		}

		public void setQuestion(Question question) {
			this.question = question;
		}

		public String getResponse() {
			return response;
		}

		public void setResponse(String response) {
			this.response = response;
		}
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		ctx.getRequest().setAttribute("quiz", true);
		ctx.getRequest().setAttribute("status", Status.getInstance(ctx.getRequest().getSession(), this));
		ctx.getRequest().setAttribute("response", new Response(getQuestions().iterator().next(), ""));
	}

	public static class Question extends SmartGenericForm.Field {

		private static String PREFIX = "question";

		protected static Collection<? extends Object> FIELD_TYPES = Arrays.asList(new String[] { "text", "large-text", "yes-no", "list" });

		private String response = "";

		public Question(String name, String label, String type, String value, String list) {
			super(name, label, type, value, list);
		}

		public Question(String label, String type, String value, String list, int order, String response) {
			super(null, label, type, value, list);
			setResponse(response);
			setOrder(order);
		}

		public String getResponse() {
			return response;
		}

		public void setResponse(String response) {
			this.response = response;
		}

		@Override
		public String toString() {
			return super.toString() + SEP + response;
		}

		@Override
		public String getName() {
			return getPrefix() + "-" + StringHelper.renderNumber(getOrder(), 4);
		}

		@Override
		public String getPrefix() {
			return PREFIX;
		}

		@Override
		public Collection<? extends Object> getFieldTypes() {
			return FIELD_TYPES;
		}

	}

	public synchronized List<Question> getQuestions() {
		List<Question> fields = new LinkedList<Question>();
		Properties p = getLocalConfig(false);
		for (Object objKey : p.keySet()) {
			String key = objKey.toString();
			if (key.startsWith(Question.PREFIX + '.')) {
				String name = key.replaceFirst(Question.PREFIX + '.', "").trim();
				if (name.trim().length() > 0) {
					String propertyValue = p.getProperty(key);
					String[] data = StringUtils.splitPreserveAllTokens(propertyValue, Field.SEP);
					String label = (String) LangHelper.arrays(data, 0, "");
					String type = (String) LangHelper.arrays(data, 1, "");
					String value = (String) LangHelper.arrays(data, 2, "");
					String list = (String) LangHelper.arrays(data, 3, "");
					int order = Integer.parseInt((String) LangHelper.arrays(data, 4, "0"));
					String response = (String) LangHelper.arrays(data, 5, "");
					Question field = new Question(label, type, value, list, order, response);
					fields.add(field);
				}
			}
		}
		Collections.sort(fields, new Field.FieldComparator());
		return fields;
	}

	public boolean isQuizList() {
		for (Field field : getQuestions()) {
			if (field.getType().equals("list")) {
				return true;
			}
		}
		return false;
	}

	public String getEditXHTML(Question question) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<tr class=\"field-line\">");
		out.println("<td><input type=\"text\" name=\"" + getInputName("order-" + question.getName()) + "\" value=\"" + question.getOrder() + "\"/></td>");
		out.println("<td><input type=\"text\" name=\"" + getInputName("label-" + question.getName()) + "\" value=\"" + question.getLabel() + "\"/></td>");
		if (isQuizList()) {
			if (question.getType().equals("list")) {
				out.println("<td><textarea name=\"" + getInputName("list-" + question.getName()) + "\">" + StringHelper.collectionToText(question.getList()) + "</textarea></td>");
			} else {
				out.println("<td>&nbsp;</td>");
			}
		}
		out.println("<td>" + XHTMLHelper.getInputOneSelect(getInputName("type-" + question.getName()), question.getFieldTypes(), question.getType()) + "</td>");
		out.println("<td><input type=\"text\" name=\"" + getInputName("response-" + question.getName()) + "\" value=\"" + question.getResponse() + "\"/></td>");
		out.println("<td><input class=\"needconfirm\" type=\"submit\" name=\"" + getInputName("del-" + question.getName()) + "\" value=\"del\" /></td>");
		out.println("</tr>");
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println(super.getEditXHTMLCode(ctx));

		out.println("<h3>Quiz</h3>");
		out.println("<div class=\"action-add\"><input type=\"submit\" name=\"" + getInputName("addq") + "\" value=\"add question\" /></div>");
		if (getQuestions().size() > 0) {
			out.println("<table class=\"sTable2\">");
			String listTitle = "";
			if (isQuizList()) {
				listTitle = "<td>list</td>";
			}
			out.println("<thead><tr><td>order</td><td>label</td>" + listTitle + "<td>type</td><td>response</td><td>action</td></tr></thead>");
			out.println("<tbody>");
			List<Question> fields = getQuestions();
			for (Question field : fields) {
				out.println(getEditXHTML(field));
			}
			out.println("</tbody>");
			out.println("</table>");
		}

		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	protected synchronized void delField(String name) {
		getLocalConfig(false).remove(Question.PREFIX + '.' + name);
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		super.performEdit(ctx);
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		for (Question question : getQuestions()) {
			String oldName = question.getName();
			if (rs.getParameter(getInputName("del-" + question.getName()), null) != null) {
				delField(question.getName());
				setModify();
				setNeedRefresh(true);
			} else {
				question.setName(rs.getParameter(getInputName("name-" + oldName), ""));
				try {
					question.setOrder(Integer.parseInt(rs.getParameter(getInputName("order-" + oldName), "0")));
				} catch (Throwable e) {
					e.printStackTrace();
				}
				question.setRequire(rs.getParameter(getInputName("require-" + oldName), null) != null);
				question.setLabel(rs.getParameter(getInputName("label-" + oldName), ""));
				question.setType(rs.getParameter(getInputName("type-" + oldName), ""));
				question.setResponse(rs.getParameter(getInputName("response-" + oldName), ""));
				if (!oldName.equals(question.getName())) {
					delField(oldName);
				}
				String listValue = rs.getParameter(getInputName("list-" + oldName), null);
				if (listValue != null) {
					question.setList(listValue);
				}
				store(question);
				setModify();
				setNeedRefresh(true);
			}
		}
		if (rs.getParameter(getInputName("addq"), null) != null) {
			List<Question> questions = getQuestions();
			int order = 1;
			if (questions.size() > 0) {
				order = questions.get(questions.size() - 1).getOrder() + 1;
			}
			Question question = new Question("text", "", "", "", order, "");
			store(question);
			setModify();
			setNeedRefresh(true);
		}
	}

	@Override
	public String getActionGroupName() {
		return "quiz";
	}

	public static String performResponse(RequestService rs, ContentService content, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String compId = rs.getParameter("comp-id", null);
		if (compId == null) {
			return "compId not found.";
		} else {
			GenericQuiz quiz = (GenericQuiz) content.getComponent(ctx, compId);
			Status status = Status.getInstance(ctx.getRequest().getSession(), quiz);
			Response response = status.getResponses().get(status.getQuestion() - 1);
			response.setResponse(rs.getParameter(response.getQuestion().getName(), null));
		}
		return null;
	}
}
