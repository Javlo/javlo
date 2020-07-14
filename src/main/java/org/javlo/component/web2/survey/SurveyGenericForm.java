package org.javlo.component.web2.survey;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.component.form.SmartGenericForm;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.MenuElement;

public class SurveyGenericForm extends SmartGenericForm {	

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
		for (Map.Entry<String, String> e : data.entrySet()) {
			Question question = new Question();
			question.setLabel(e.getKey());
			question.setResponse(e.getValue());
			questions.add(question);
		}
		File storeFolder = new File(URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), AsbtractSurvey.STORE_FOLDER_NAME));
		File excelFile = new File(URLHelper.mergePath(storeFolder.getAbsolutePath(), StringHelper.stringToFileName(AsbtractSurvey.getDefaultSessionName(ctx))+".xlsx"));
		AsbtractSurvey.storeExcel(ctx, excelFile, questions, getPage().getTitle(ctx));
		return 1;
	}

}
