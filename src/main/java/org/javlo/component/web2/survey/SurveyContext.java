package org.javlo.component.web2.survey;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;

public class SurveyContext {

	private static final String KEY = SurveyContext.class.getName();

	private List<Question> allQuestions = Collections.emptyList();
	private List<Question> selectedQuestions = Collections.emptyList();

	public static final SurveyContext getInstance(ContentContext ctx) throws Exception {
		MenuElement parent = ctx.getCurrentPage().getParent();
		String prefix = "";
		if (parent != null) {
			prefix = parent.getName() + '-';
		}
		SurveyContext out = (SurveyContext) ctx.getRequest().getSession().getAttribute(prefix + KEY);
		if (out == null) {
			out = new SurveyContext();
			ctx.getRequest().getSession().setAttribute(prefix + KEY, out);
		}
		return out;
	}

	public List<Question> getAllQuestions() {
		return allQuestions;
	}

	public void setAllQuestions(List<Question> allQuestions) {
		Collections.sort(allQuestions, new Comparator<Question>() {
			@Override
			public int compare(Question o1, Question o2) {
				return o1.getOrder() - o2.getOrder();
			}
		});
		this.allQuestions = allQuestions;
	}

	public List<Question> getSelectedQuestions() {
		Collections.sort(selectedQuestions, new Comparator<Question>() {
			@Override
			public int compare(Question o1, Question o2) {
				return o1.getOrder() - o2.getOrder();
			}
		});
		return selectedQuestions;
	}

	public void setSelectedQuestions(List<Question> selectedQuestions) {
		this.selectedQuestions = selectedQuestions;
	}

	/**
	 * update values of question in internal list.
	 * 
	 * @param number
	 * @param inQuestion
	 * @return false if question not found.
	 */
	public boolean updateQuestion(Question inQuestion) {
		int number = inQuestion.getNumber();
		boolean out = false;
		for (Question question : allQuestions) {
			if (question.getNumber() == number) {
				out = true;
				question.setResponse(inQuestion.getResponse().getLabel());
				question.setOrder(inQuestion.getOrder());
			}
		}
		for (Question question : selectedQuestions) {
			if (question.getNumber() == number) {
				out = true;
				question.setResponse(inQuestion.getResponse().getLabel());
				question.setOrder(inQuestion.getOrder());
			}
		}
		return out;
	}

}
