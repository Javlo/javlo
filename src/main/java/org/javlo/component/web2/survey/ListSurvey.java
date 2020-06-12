package org.javlo.component.web2.survey;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public class ListSurvey extends AsbtractSurvey implements IAction {
	
	protected static final String TITLE_FIELD = "title";
	protected static final String QUESTION_FIELD = "questions";
	protected static final String RESPONSE_FIELD = "responses";
	protected static final String FIELD_LABEL_SEND = "sendlabel";

	public static final String TYPE = "list-survey";

	@Override
	public String getType() {
		return TYPE;
	}
	
	private static final List<String> FIELDS = Arrays.asList(new String[] {TITLE_FIELD, QUESTION_FIELD, RESPONSE_FIELD, FIELD_LABEL_SEND});
	
	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {	
		return FIELDS;
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {	
		super.prepareView(ctx);
		ctx.getRequest().setAttribute("title", getFieldValue(TITLE_FIELD));
		ctx.getRequest().setAttribute("questions", getQuestions());
		ctx.getRequest().setAttribute("sendLabel", getFieldValue(FIELD_LABEL_SEND));
	}
	
	public List<Question> getQuestions() {
		int num=1;
		List<Response> responses = new LinkedList<Response>();
		for (String resp : StringHelper.textToList(getFieldValue(RESPONSE_FIELD))) {
			responses.add(new Response(resp, num));
			num++;
		}
		num=1;
		List<Question> outQuestion = new LinkedList<Question>();
		for (String qstr : StringHelper.textToList(getFieldValue(QUESTION_FIELD))) {			
			num++;
			Question q = new Question();
			q.setNumber(num);
			q.setLabel(qstr);
			q.setResponses(responses);
			outQuestion.add(q);
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
}
