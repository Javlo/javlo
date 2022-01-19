package org.javlo.component.web2.survey;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.SecurityHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.RequestService;
import org.javlo.service.visitors.UserDataService;

public class ListSurvey extends AbstractSurvey implements IAction {

	protected static final String SESSION_NAME = "session_file";
	protected static final String TITLE_FIELD = "title";
	protected static final String QUESTION_FIELD = "questions";
	protected static final String RESPONSE_FIELD = "responses";
	protected static final String FIELD_LABEL_SEND = "sendlabel";

	public static final String TYPE = "list-survey";

	private static final String RANDOM_STYLE = "random";

	private static final String RESULT_STYLE = "result";

	private static String[] STYLES = new String[] { "stay order", RANDOM_STYLE, RESULT_STYLE };

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return STYLES;
	}

	private static final List<String> FIELDS = Arrays.asList(new String[] { SESSION_NAME, TITLE_FIELD, QUESTION_FIELD, RESPONSE_FIELD, FIELD_LABEL_SEND });

	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		boolean out = super.initContent(ctx);
		if (getPage().getParent() != null) {
			setFieldValue(SESSION_NAME, StringHelper.createFileName(getPage().getParent().getTitle(ctx)));
		}
		return out;
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}

	@Override
	protected String getSessionName(ContentContext ctx) throws Exception {
		String sessionName = getFieldValue(SESSION_NAME);
		if (StringHelper.isEmpty(sessionName)) {
			return super.getSessionName(ctx);
		}
		return sessionName;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);

		UserDataService userDataService = UserDataService.getInstance(ctx);
		String dataRaw = userDataService.getUserData(ctx, getDataKey());
		Map<String, String> data = null;
		if (!StringHelper.isEmpty(dataRaw)) {
			data = StringHelper.stringToMap(dataRaw);
		}

		ctx.getRequest().setAttribute("title", getFieldValue(TITLE_FIELD));

		List<Question> questions = getQuestions(ctx, data);
		if (getStyle().equals(RESULT_STYLE)) {
			String userCode = ctx.getRequest().getParameter("user-code");
			if (StringHelper.isEmpty(userCode)) {
				userCode = SecurityHelper.getUserCode(ctx);
			}
			
			if (!StringHelper.isEmpty(userCode)) {
				
				if (getPage().getNextBrother() != null) {
					String pdfLink = URLHelper.createURL(ctx.getContextWithOtherFormat("pdf"), getPage().getNextBrother());
					pdfLink = URLHelper.addParam(pdfLink, "user-code", userCode);
					ctx.getRequest().setAttribute("pdfLink", pdfLink);
				}
				
				if (loadExcel(ctx, getExcelFile(ctx), questions, getFieldValue(TITLE_FIELD), userCode)) {
					ctx.getRequest().setAttribute("questions", questions);
				} else {
					logger.info("info code user not found userCode:"+userCode+ " worksheet:"+getFieldValue(TITLE_FIELD));
				}
			} else {
				logger.severe("userCode not found.");
			}
		} else {
			ctx.getRequest().setAttribute("questions", questions);
			ctx.getRequest().setAttribute("sendLabel", getFieldValue(FIELD_LABEL_SEND));
		}
	}

	public List<Question> getQuestions(ContentContext ctx, Map<String, String> data) throws Exception {

		int num = 1;
		List<Response> responses = new LinkedList<Response>();
		for (String resp : StringHelper.textToList(getFieldValue(RESPONSE_FIELD))) {
			responses.add(new Response(resp, num));
			num++;
		}

		List<Question> outQuestion = new LinkedList<Question>();
		if (StringHelper.isEmpty(getFieldValue(QUESTION_FIELD))) {
			for (Question ctxQuestion : SurveyContext.getInstance(ctx).getSelectedQuestions()) {
				Question q = new Question(ctxQuestion);
				q.setResponses(responses);
				outQuestion.add(q);
				if (data != null && data.get("" + q.getNumber()) != null) {
					q.setResponse(data.get("" + q.getNumber()));
				}
				num++;
			}
		} else {
			num = 1;
			for (String qstr : StringHelper.textToList(getFieldValue(QUESTION_FIELD))) {
				Question q = new Question();
				q.setNumber(num);
				q.setLabel(qstr);
				q.setResponses(responses);
				outQuestion.add(q);
				if (data != null && data.get("" + q.getNumber()) != null) {
					q.setResponse(data.get("" + q.getNumber()));
				}
				num++;
			}
		}

		if (getStyle().equals(RANDOM_STYLE)) {
			Collections.shuffle(outQuestion);
		}

		return outQuestion;
	}

	public List<Question> getAllQuestions(ContentContext ctx) throws Exception {

		int num = 1;
		List<Response> responses = new LinkedList<Response>();
		for (String resp : StringHelper.textToList(getFieldValue(RESPONSE_FIELD))) {
			responses.add(new Response(resp, num));
			num++;
		}

		List<Question> outQuestion = new LinkedList<Question>();
		if (StringHelper.isEmpty(getFieldValue(QUESTION_FIELD))) {
			for (Question ctxQuestion : SurveyContext.getInstance(ctx).getAllQuestions()) {
				Question q = new Question(ctxQuestion);
				q.setResponses(responses);
				outQuestion.add(q);
				num++;
			}
		} else {
			num = 1;
			for (String qstr : StringHelper.textToList(getFieldValue(QUESTION_FIELD))) {
				Question q = new Question();
				q.setNumber(num);
				q.setLabel(qstr);
				q.setResponses(responses);
				outQuestion.add(q);
				num++;
			}
		}

		return outQuestion;
	}

	@Override
	public String getEditRenderer(ContentContext ctx) {
		return "/jsp/edit/component/survey/edit_list_survery.jsp";
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		return super.performEdit(ctx);
	}

	@Override
	public String getActionGroupName() {
		return TYPE;
	}

	public static String performSend(ContentContext ctx, RequestService rs) throws Exception {
		ListSurvey comp = (ListSurvey) ComponentHelper.getComponentFromRequest(ctx);
		List<Question> questions = comp.getAllQuestions(ctx);
		logger.info("session : " + ctx.getSession().getId());
		SurveyContext surveyContext = SurveyContext.getInstance(ctx);

		Map<String, String> dataMap = new HashMap<>();
		for (Question q : questions) {
			String rep = rs.getParameter(q.getInputName());
			if (rep != null) {
				q.setResponse(StringHelper.neverEmpty(rep, ""));
				surveyContext.updateQuestion(q);
				dataMap.put("" + q.getNumber(), "" + q.getResponse().getNumber());
				logger.info("" + q);
			}
		}

		UserDataService userDataService = UserDataService.getInstance(ctx);
		Integer line = null;
		String lineStr = userDataService.getUserData(ctx, comp.getDataKeyLine());
		if (StringHelper.isDigit(lineStr)) {
			line = Integer.parseInt(lineStr);
		}
		line = comp.store(ctx, questions, comp.getFieldValue(TITLE_FIELD), line);
		userDataService.addUserData(ctx, comp.getDataKey(), StringHelper.mapToString(dataMap));
		userDataService.addUserData(ctx, comp.getDataKeyLine(), "" + line);

		MenuElement nextPage = comp.getPage().getNextBrother();
		if (nextPage == null) {
			logger.severe("next page not found.");
		} else {
			ctx.setPath(nextPage.getPath());
		}
		return null;
	}
}
