package org.javlo.component.web2.survey;

import org.javlo.component.form.Field;
import org.javlo.component.form.SmartGenericForm;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.RequestService;
import org.javlo.service.visitors.UserDataService;
import org.javlo.utils.Cell;
import org.javlo.utils.XLSTools;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class SurveyGenericForm extends SmartGenericForm {

	private static Logger logger = Logger.getLogger(SurveyGenericForm.class.getName());

	public static final String TYPE = "survey-generic-form";

	public static final String SESSION_EXCEL_KEY = "_excel_key";

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

	protected String getDataKeyLine() {
		return getType() + getId() + "-line";
	}

	private Integer getExcelLine(ContentContext ctx) throws Exception {

		UserDataService userDataService = UserDataService.getInstance(ctx);
		Integer line = null;
		String lineStr = userDataService.getUserData(ctx, getDataKeyLine());
		if (StringHelper.isDigit(lineStr)) {
			line = Integer.parseInt(lineStr);
		}
		if (line == null) {
			line = (Integer) ctx.getRequest().getSession().getAttribute(SESSION_EXCEL_KEY + getId());
		}
		return line;
	}

	private void setExcelLine(ContentContext ctx, int excelLine) throws Exception {
		UserDataService userDataService = UserDataService.getInstance(ctx);
		userDataService.addUserData(ctx, getDataKeyLine(), "" + excelLine);
		ctx.getRequest().getSession().setAttribute(SESSION_EXCEL_KEY + getId(), excelLine);
	}

	@Override
	protected boolean isFilledFromCookies() {
		return true;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		super.prepareView(ctx);
		Integer excelLine = getExcelLine(ctx);
		// if file rested
		if (!getExcelFile(ctx).exists()) {
			excelLine = null;
		}
		if (excelLine != null) {
			Cell[][] cells = XLSTools.getArray(ctx, getExcelFile(ctx), getPage().getTitle(ctx));
			int n = 0;
			if (cells != null) {
				for (Cell title : cells[0]) {
					// Cell value = cells[excelLine][n];
					Field field = getField(ctx, title.getValue());
					if (field != null) {
						rs.putParameter(field.getName(), cells[excelLine][n].getValue());
					}
					n++;
				}
			} else {
				logger.warning("error on load : " + getExcelFile(ctx));
			}
		}
	}

	private File getExcelFile(ContentContext ctx) throws Exception {
		File storeFolder = new File(URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), AbstractSurvey.STORE_FOLDER_NAME));
		return new File(URLHelper.mergePath(storeFolder.getAbsolutePath(), StringHelper.stringToFileName(AbstractSurvey.getDefaultSessionName(ctx)) + ".xlsx"));
	}

	@Override
	protected int storeResult(ContentContext ctx, Map<String, String> data, boolean sort) throws Exception {
		List<Question> questions = new LinkedList<Question>();
		logger.info("session : " + ctx.getSession().getId());
		for (Map.Entry<String, String> e : data.entrySet()) {
			Question question = new Question();
			question.setLabel(e.getKey());
			question.setResponse(e.getValue());
			questions.add(question);
			logger.info("question:" + question);
		}

		// Question sessionQ = new Question();
		// sessionQ.setLabel(SecurityHelper.USER_CODE_KEY);
		// sessionQ.setResponse(SecurityHelper.getUserCode(ctx));
		// questions.add(sessionQ);

		File excelFile = getExcelFile(ctx);
		synchronized (SESSION_EXCEL_KEY) {
			int excelLine = AbstractSurvey.storeExcel(ctx, excelFile, questions, getPage().getTitle(ctx), getExcelLine(ctx));
			setExcelLine(ctx, excelLine);
		}
		return 1;
	}

	protected boolean isFormExpire() {
		return false;
	}

}
