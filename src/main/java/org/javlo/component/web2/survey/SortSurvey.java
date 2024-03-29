package org.javlo.component.web2.survey;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.StringHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.RequestService;
import org.javlo.service.visitors.UserDataService;

public class SortSurvey extends AbstractSurvey implements IAction {

	protected static final String SESSION_NAME = "session_file";
	protected static final String TITLE_FIELD = "title";
	protected static final String QUESTION_FIELD = "questions";
	protected static final String FIELD_LABEL_SEND = "sendlabel";

	public static final String TYPE = "sort-survey";

	public class BothQuestion {
		public Question q1;
		public Question q2;
		public Integer selectQuestion = null;

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
		
		public Integer getSelectQuestion() {
			return selectQuestion;
		}
		
		public void setSelectQuestion(Integer selectQuestion) {
			this.selectQuestion = selectQuestion;
		}
		
		public String getMapKey() {
			return ""+this.q1.getNumber()+'-'+this.q2.getNumber();
		}

		@Override
		public int hashCode() {
			int select = 0;
			if (selectQuestion != null) {
				select = selectQuestion;
			}
			if (q1.getNumber() > q2.getNumber()) {
				return q1.hashCode() + q2.hashCode() + select;
			} else {
				return q2.hashCode() + q1.hashCode() + select;
			}
		}
	}

	@Override
	public String getType() {
		return TYPE;
	}

	private static final List<String> FIELDS = Arrays.asList(new String[] { SESSION_NAME, TITLE_FIELD, QUESTION_FIELD, FIELD_LABEL_SEND });

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
		
		Map<String,String> data = null;
		if (!StringHelper.isEmpty(dataRaw)) {
			data = StringHelper.stringToMap(dataRaw);
		}
		
		List<BothQuestion> boths = new LinkedList<SortSurvey.BothQuestion>();
		List<Question> questions = getQuestions(ctx);
		
		for (Question q1 : questions) {
			for (Question q2 : questions) {
				if (q1.getNumber() > q2.getNumber()) {
					BothQuestion newBoth = new BothQuestion(q1, q2);
					if (data != null && StringHelper.isDigit(data.get(newBoth.getMapKey()))) {
						newBoth.setSelectQuestion(Integer.parseInt(data.get(newBoth.getMapKey())));
					}
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
		int num = 1;
		List<Question> outQuestion = new LinkedList<Question>();
		if (StringHelper.isEmpty(getFieldValue(QUESTION_FIELD))) {
			for (Question ctxQuestion : SurveyContext.getInstance(ctx).getSelectedQuestions()) {
				Question q = new Question(ctxQuestion);
				outQuestion.add(q);
				num++;
			}
		} else {
			num = 1;
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
		int num = 1;

		List<Question> outQuestion = new LinkedList<Question>();
		if (StringHelper.isEmpty(getFieldValue(QUESTION_FIELD))) {
			for (Question ctxQuestion : SurveyContext.getInstance(ctx).getAllQuestions()) {
				Question q = new Question(ctxQuestion);
				q.setResponses(Collections.emptyList());
				outQuestion.add(q);
				num++;
			}
		} else {
			num = 1;
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
		List<Question> finalQuestions = new LinkedList<Question>();
		logger.info("session : "+ctx.getSession().getId());
		
		Map<String,String> data = new HashMap<String,String>();
		for (Question q1 : questions) {
			for (Question q2 : questions) {
				String key = q1.getNumber()+"-"+q2.getNumber();
				String rep = rs.getParameter("b"+key);
				if (!StringHelper.isEmpty(rep)) {
					data.put(key, rep);
				}
			}
		}
		
		for (Question q : questions) {
			String rep = rs.getParameter("q"+q.getNumber());
			if (rep != null) {
				finalQuestions.add(q);
				q.setResponse(StringHelper.neverEmpty(rep, ""));
				logger.info(""+q);
			}
		}
		Collections.sort(finalQuestions, new Comparator<Question>() {
			@Override
			public int compare(Question o1, Question o2) {				
				if (StringHelper.isDigit(o1.getResponse().getLabel())&&StringHelper.isDigit(o2.getResponse().getLabel())) {
					return Integer.parseInt(o2.getResponse().getLabel())-Integer.parseInt(o1.getResponse().getLabel());
				} else  {
					return 0;
				}
			}
		});
		int order = 1;
		for (Question question : finalQuestions) {
			question.setOrder(order);
			SurveyContext.getInstance(ctx).updateQuestion(question);
			order++;
		}
		SurveyContext.getInstance(ctx).setSelectedQuestions(finalQuestions);
		
		UserDataService userDataService = UserDataService.getInstance(ctx);
		Integer line = null;
		String lineStr = userDataService.getUserData(ctx, comp.getDataKeyLine());
		if (StringHelper.isDigit(lineStr)) {
			line = Integer.parseInt(lineStr);
		}		
		line = comp.store(ctx, questions, comp.getFieldValue(TITLE_FIELD), line);
		userDataService.addUserData(ctx, comp.getDataKey(), StringHelper.mapToString(data));
		userDataService.addUserData(ctx, comp.getDataKeyLine(), ""+line);
		
		MenuElement nextPage = comp.getPage().getNextBrother();
		if (nextPage == null) {
			logger.severe("next page not found.");			
		} else {
			ctx.setPath(nextPage.getPath());
		}
		return null;
	}
}
