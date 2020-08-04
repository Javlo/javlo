package org.javlo.component.web2.survey;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.component.form.SmartGenericForm;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.MenuElement;

public class SurveyGenericForm extends SmartGenericForm {	
	
	private static Logger logger = Logger.getLogger(SurveyGenericForm.class.getName());

	public static final String TYPE = "survey-generic-form";
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	public boolean isEvent() {
		return false;
	}
	
	protected boolean isSendEmail() {
		return false;
	}
	
	@Override
	protected MenuElement getNextPage(ContentContext ctx) {
		return getPage().getNextBrother();
	}
	
	@Override
	protected int storeResult(ContentContext ctx, Map<String, String> data) throws Exception {
		List<Question> questions = new LinkedList<Question>();
		logger.info("session : "+ctx.getSession().getId());
		for (Map.Entry<String, String> e : data.entrySet()) {
			Question question = new Question();
			question.setLabel(e.getKey());
			question.setResponse(e.getValue());
			questions.add(question);
			logger.info("question:"+question);
		}
		File storeFolder = new File(URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), AbstractSurvey.STORE_FOLDER_NAME));
		File excelFile = new File(URLHelper.mergePath(storeFolder.getAbsolutePath(), StringHelper.stringToFileName(AbstractSurvey.getDefaultSessionName(ctx))+".xlsx"));
		AbstractSurvey.storeExcel(ctx, excelFile, questions, getPage().getTitle(ctx));
		return 1;
	}

}
