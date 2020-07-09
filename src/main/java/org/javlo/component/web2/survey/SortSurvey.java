package org.javlo.component.web2.survey;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.StringHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.RequestService;

public class SortSurvey extends AsbtractSurvey implements IAction {
	
	protected static final String SESSION_NAME = "session_file";
	protected static final String TITLE_FIELD = "title";
	protected static final String QUESTION_FIELD = "questions";	
	protected static final String FIELD_LABEL_SEND = "sendlabel";

	public static final String TYPE = "sort-survey";
	
	public class BothQuestion {
		public Question q1;
		public Question q2;
		
		public BothQuestion(Question q1, Question q2) {
			super();
			this.q1 = q1;
			this.q2 = q2;
		}
		public Question getQ1() {
			return q1;
		}
		public void setQ1(Question q1) {
			this.q1 = q1;
		}
		public Question getQ2() {
			return q2;
		}
		public void setQ2(Question q2) {
			this.q2 = q2;
		}		
		@Override
		public int hashCode() {
			if (q1.getNumber()>q2.getNumber()) {
				return q1.hashCode()+q2.hashCode();
			} else {
				return q2.hashCode()+q1.hashCode();
			}
		}
	}

	@Override
	public String getType() {
		return TYPE;
	}
	
	private static final List<String> FIELDS = Arrays.asList(new String[] {SESSION_NAME, TITLE_FIELD, QUESTION_FIELD, FIELD_LABEL_SEND});
	
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
	protected String getSessionName(ContentContext ctx) {
		String sessionName = getFieldValue(SESSION_NAME);
		if (StringHelper.isEmpty(sessionName)) {
			return super.getSessionName(ctx);
		}
		return sessionName;
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {	
		super.prepareView(ctx);
		List<BothQuestion> boths = new LinkedList<SortSurvey.BothQuestion>(); 
		List<Question> questions = getQuestions(ctx);
		for (Question q1 : questions) {
			for (Question q2 : questions) {
				if (q1.getNumber() > q2.getNumber()) {
					BothQuestion newBoth = new BothQuestion(q1,q2);
					boths.add(newBoth);					
				}
			}
		}
		ctx.getRequest().setAttribute("title", getFieldValue(TITLE_FIELD));
		ctx.getRequest().setAttribute("boths", boths);
		ctx.getRequest().setAttribute("questions", questions);
		ctx.getRequest().setAttribute("sendLabel", getFieldValue(FIELD_LABEL_SEND));
	}
	
	public List<Question> getQuestions(ContentContext ctx) throws Exception {		
		int num=1;
		List<Question> outQuestion = new LinkedList<Question>();
		if (StringHelper.isEmpty(getFieldValue(QUESTION_FIELD))) {			
			for (Question ctxQuestion : SurveyContext.getInstance(ctx).getSelectedQuestions()) {			
				Question q = new Question(ctxQuestion);
				outQuestion.add(q);
				num++;
			}	
		} else {
			num=1;		
			for (String qstr : StringHelper.textToList(getFieldValue(QUESTION_FIELD))) {			
				Question q = new Question();
				q.setNumber(num);
				q.setLabel(qstr);
				outQuestion.add(q);
				num++;
			}	
		}
		return outQuestion;
	}
	
	public List<Question> getAllQuestions(ContentContext ctx) throws Exception {
		int num=1;
		
		List<Question> outQuestion = new LinkedList<Question>();
		if (StringHelper.isEmpty(getFieldValue(QUESTION_FIELD))) {
			for (Question ctxQuestion : SurveyContext.getInstance(ctx).getAllQuestions()) {			
				Question q = new Question(ctxQuestion);		
				q.setResponses(Collections.emptyList());
				outQuestion.add(q);
				num++;
			}	
		} else {
			num=1;		
			for (String qstr : StringHelper.textToList(getFieldValue(QUESTION_FIELD))) {			
				Question q = new Question();
				q.setNumber(num);
				q.setLabel(qstr);				
				outQuestion.add(q);
				num++;
			}	
		}		
		
		return outQuestion;
	}

	@Override
	public String getEditRenderer(ContentContext ctx) {
		return "/jsp/edit/component/survey/edit_list_survery.jsp?noresponse=true";
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
		SortSurvey comp = (SortSurvey)ComponentHelper.getComponentFromRequest(ctx);
		List<Question> questions = comp.getAllQuestions(ctx); 
		for (Question q : questions) {
			String rep = rs.getParameter("q"+q.getNumber());
			q.setResponse(StringHelper.neverEmpty(rep, ""));
			logger.info(""+q);
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
