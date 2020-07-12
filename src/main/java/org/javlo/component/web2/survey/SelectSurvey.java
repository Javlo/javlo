package org.javlo.component.web2.survey;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.StringHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.RequestService;

public class SelectSurvey extends AsbtractSurvey implements IAction {
	
	protected static final String SESSION_NAME = "session_file";
	protected static final String TITLE_FIELD = "title";
	protected static final String QUESTION_FIELD = "questions";
	protected static final String SELECT = "select";
	protected static final String FIELD_LABEL_SEND = "sendlabel";

	public static final String TYPE = "select-survey";

	@Override
	public String getType() {
		return TYPE;
	}
	
	private static final List<String> FIELDS = Arrays.asList(new String[] {SESSION_NAME, TITLE_FIELD, QUESTION_FIELD, SELECT, FIELD_LABEL_SEND});
	private static final String SELECT_VALUE = "yes";
	private static final String UNSELECT_VALUE = "no";
	
	private static final String RANDOM_STYLE = "random";
	
	private static final String[] styles = new String[] {"standard", RANDOM_STYLE};
	
	@Override
	public String[] getStyleList(ContentContext ctx) {
		return styles;
	}
	
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
		ctx.getRequest().setAttribute("title", getFieldValue(TITLE_FIELD));
		if (getStyle().equals(RANDOM_STYLE)) {
			List<Question> questions = getQuestions(ctx);
			Collections.shuffle(questions, new Random());
			ctx.getRequest().setAttribute("questions", questions);
		} else {
			ctx.getRequest().setAttribute("questions", getQuestions(ctx));
		}
		ctx.getRequest().setAttribute("sendLabel", getFieldValue(FIELD_LABEL_SEND));
	}
	
	public List<Question> getQuestions(ContentContext ctx) {		
		List<Response> responses = new LinkedList<Response>();
		responses.add(new Response(UNSELECT_VALUE, 1));
		responses.add(new Response(SELECT_VALUE, 2));
		int num=1;
		List<Question> outQuestion = new LinkedList<Question>();
		for (String qstr : StringHelper.textToList(getFieldValue(QUESTION_FIELD))) {			
			Question q = new Question();
			q.setNumber(num);
			q.setLabel(qstr);
			q.setResponses(responses);
			outQuestion.add(q);
			num++;
		}
		return outQuestion;
	}

	@Override
	public String getEditRenderer(ContentContext ctx) {
		return "/jsp/edit/component/survey/edit_select_survery.jsp";
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
		SelectSurvey comp = (SelectSurvey)ComponentHelper.getComponentFromRequest(ctx);
		List<Question> questions = comp.getQuestions(ctx);
		String selected = rs.getParameter("selected");		
		if (selected == null) {
			logger.severe("param 'selected' not found.");
			return "param 'selected' not found.";
		}
		Collection<String> selectedList = StringHelper.stringToCollection(selected, ",");
		List<Question> selectedQuestion = new LinkedList<Question>();
		for (Question q : questions) {
			if (selectedList.contains(""+q.getNumber())) {
				q.setResponse(SELECT_VALUE);
				selectedQuestion.add(q);
			} else {				
				q.setResponse(UNSELECT_VALUE);
			}
			logger.info(""+q);
		}
		
		SurveyContext surveyContext = SurveyContext.getInstance(ctx);
		surveyContext.setAllQuestions(comp.getQuestions(ctx));
		surveyContext.setSelectedQuestions(selectedQuestion);		
		
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
