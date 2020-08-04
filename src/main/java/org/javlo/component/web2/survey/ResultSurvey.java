package org.javlo.component.web2.survey;

import java.util.Arrays;
import java.util.List;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public class ResultSurvey extends AbstractSurvey implements IAction {
	
	protected static final String SESSION_NAME = "session_file";
	protected static final String TITLE_FIELD = "title";
	
	public static final String TYPE = "result-survey";

	@Override
	public String getType() {
		return TYPE;
	}
	
	private static final List<String> FIELDS = Arrays.asList(new String[] {SESSION_NAME, TITLE_FIELD});
	
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
		ctx.getRequest().setAttribute("questions", SurveyContext.getInstance(ctx).getSelectedQuestions());
	}	
	
	@Override
	public String getActionGroupName() {
		return TYPE;
	}	

}
