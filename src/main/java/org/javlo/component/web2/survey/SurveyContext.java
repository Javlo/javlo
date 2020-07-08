package org.javlo.component.web2.survey;

import java.util.Collections;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;

public class SurveyContext {
	
	private static final String KEY  = SurveyContext.class.getName();
	
	private List<Question> allQuestions = Collections.emptyList();
	private List<Question> selectedQuestions = Collections.emptyList();
	
	public static final SurveyContext getInstance(ContentContext ctx) throws Exception {
		MenuElement parent = ctx.getCurrentPage().getParent();
		String prefix = "";
		if (parent != null) {
			prefix = parent.getName()+'-';
		}
		SurveyContext out = (SurveyContext)ctx.getRequest().getSession().getAttribute(prefix+KEY);
		if (out == null)  {
			out = new SurveyContext();
			ctx.getRequest().getSession().setAttribute(prefix+KEY, out);			
		}
		return out;
	}
	
	public List<Question> getAllQuestions() {
		return allQuestions;
	}

	public void setAllQuestions(List<Question> allQuestions) {
		this.allQuestions = allQuestions;
	}

	public List<Question> getSelectedQuestions() {
		return selectedQuestions;
	}

	public void setSelectedQuestions(List<Question> selectedQuestions) {
		this.selectedQuestions = selectedQuestions;
	}	

}
