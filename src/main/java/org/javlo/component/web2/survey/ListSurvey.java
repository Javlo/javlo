package org.javlo.component.web2.survey;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.StringHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.RequestService;

public class ListSurvey extends AbstractSurvey implements IAction {
	
	protected static final String SESSION_NAME = "session_file";
	protected static final String TITLE_FIELD = "title";
	protected static final String QUESTION_FIELD = "questions";
	protected static final String RESPONSE_FIELD = "responses";
	protected static final String FIELD_LABEL_SEND = "sendlabel";

	public static final String TYPE = "list-survey";

	@Override
	public String getType() {
		return TYPE;
	}
	
	private static final List<String> FIELDS = Arrays.asList(new String[] {SESSION_NAME, TITLE_FIELD, QUESTION_FIELD, RESPONSE_FIELD, FIELD_LABEL_SEND});
	
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
		ctx.getRequest().setAttribute("title", getFieldValue(TITLE_FIELD));
		ctx.getRequest().setAttribute("questions", getQuestions(ctx));
		ctx.getRequest().setAttribute("sendLabel", getFieldValue(FIELD_LABEL_SEND));
	}
	
	public List<Question> getQuestions(ContentContext ctx) throws Exception {
		
		int num=1;
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
				num++;
			}	
		} else {
			num=1;		
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
	
	public List<Question> getAllQuestions(ContentContext ctx) throws Exception {
		
		int num=1;
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
			num=1;		
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
		ListSurvey comp = (ListSurvey)ComponentHelper.getComponentFromRequest(ctx);
		List<Question> questions = comp.getAllQuestions(ctx); 
		logger.info("session : "+ctx.getSession().getId());
		SurveyContext surveyContext = SurveyContext.getInstance(ctx);
		for (Question q : questions) {
			String rep = rs.getParameter(q.getInputName());
			if (rep != null) {
				q.setResponse(StringHelper.neverEmpty(rep, ""));
				surveyContext.updateQuestion(q);							
				logger.info(""+q);
			}			
		}
		comp.store(ctx, questions, comp.getFieldValue(TITLE_FIELD));
		MenuElement nextPage = comp.getPage().getNextBrother();
		if (nextPage == null) {
			logger.severe("next page not found.");			
		} else {
			ctx.setPath(nextPage.getPath());
		}
		return null;
	}
}
