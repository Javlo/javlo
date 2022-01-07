package org.javlo.component.web2.survey;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.SecurityHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.PageBean;
import org.javlo.service.visitors.UserDataService;
import org.javlo.utils.Cell;
import org.javlo.utils.XLSTools;

public abstract class AbstractSurvey extends AbstractPropertiesComponent {

	static final String STORE_FOLDER_NAME = "survey";

	private File storeFolder;

	@Override
	public void init(ComponentBean bean, ContentContext ctx) throws Exception {
		super.init(bean, ctx);
		storeFolder = new File(URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), STORE_FOLDER_NAME));
		storeFolder.mkdirs();
	}

	public PageBean getPreviousPage(ContentContext ctx) throws Exception {
		PageBean page = getPage().getPageBean(ctx);
		return page.getPreviousPage();
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		ctx.getRequest().setAttribute("previousLink", URLHelper.createURL(ctx, getPreviousPage(ctx).getPath(), (Map) null));
	}

	protected int storeExcel(ContentContext ctx, List<Question> questions, String sessionName, String stepName, Integer line) throws Exception {
		File excelFile = new File(URLHelper.mergePath(storeFolder.getAbsolutePath(), StringHelper.stringToFileName(sessionName) + ".xlsx"));
		return storeExcel(ctx, excelFile, questions, stepName, line);
	}

	protected String getDataKey() {
		String prefix = "survey-" + getPage().getParent().getId() + '-';
		return prefix + getType() + getId();
	}

	protected String getDataKeyLine() {
		return getType() + getId() + "-line";
	}

	public static String getUserCode(ContentContext ctx) throws Exception {
		String userCode = null;
		final String KEY = "_userCode";
		if (ctx.getRequest().getSession().getAttribute(KEY) != null) {
			userCode = ctx.getRequest().getSession().getAttribute(KEY).toString();
		}
		UserDataService userDataService = UserDataService.getInstance(ctx);
		if (userDataService.getUserData(ctx, KEY) != null) {
			userCode = userDataService.getUserData(ctx, KEY);
		} else {
			if (userCode == null) {
				userCode = StringHelper.getLargeRandomIdBase64();
				userDataService.addUserData(ctx, KEY, userCode);
			}
		}
		ctx.getRequest().getSession().setAttribute(KEY, userCode);
		return userCode;
	}

	synchronized static void loadExcel(ContentContext ctx, File excelFile, List<Question> inQuestions, String stepName, int line) throws Exception {
		Cell[][] cells = null;
		if (excelFile.exists()) {
			cells = XLSTools.getArray(ctx, excelFile, stepName);
		}
		int i = 0;
		for (Question question : inQuestions) {
			question.setResponse(cells[line][i].getValue());
			i++;
		}
	}

	synchronized static int storeExcel(ContentContext ctx, File excelFile, List<Question> inQuestions, String stepName, Integer inLine) throws Exception {

		logger.info("store : " + excelFile + " line:" + inLine);

		List<Question> questions = new LinkedList<Question>(inQuestions);
		Question sessionQ = new Question();
		sessionQ.setLabel(SecurityHelper.USER_CODE_KEY);
		sessionQ.setResponse(SecurityHelper.getUserCode(ctx));
		questions.add(sessionQ);

		Question timeQ = new Question();
		timeQ.setLabel("_time");
		timeQ.setResponse(StringHelper.renderSortableTime(new Date()));
		questions.add(timeQ);

		Collections.sort(questions, new java.util.Comparator<Question>() {
			@Override
			public int compare(Question o1, Question o2) {
				return o1.getLabel().compareTo(o2.getLabel());
			}
		});

		Cell[][] cells = null;

		File sourceFile = null;
		if (excelFile.exists()) {
			sourceFile = excelFile;
			cells = XLSTools.getArray(ctx, excelFile, stepName);
		}
		if (cells == null) {
			cells = new Cell[1][];
			cells[0] = new Cell[questions.size()];
			int i = 0;
			for (Question q : questions) {
				if (q.getNumber() > 0) {
					cells[0][i] = new Cell(q.getNumber() + "." + q.getLabel(), null, cells, i, 0);
				} else {
					cells[0][i] = new Cell(q.getLabel(), null, cells, i, 0);
				}
				i++;
			}
		}
		Cell[][] newCells;

		// if excel file was reseted
		if (inLine != null && inLine >= cells.length) {
			inLine = null;
		}

		Integer line = inLine;
		if (line != null) {
			newCells = new Cell[cells.length][];
		} else {
			newCells = new Cell[cells.length + 1][];
		}

		for (int x = 0; x < cells.length; x++) {
			newCells[x] = new Cell[cells[x].length];
			for (int y = 0; y < newCells[x].length; y++) {
				newCells[x][y] = cells[x][y];
			}
		}
		if (line == null) {
			line = newCells.length - 1;
			newCells[newCells.length - 1] = new Cell[questions.size()];
		}
		int i = 0;
		for (Question question : questions) {
			if (question.getResponse() != null) {
				String val = question.getResponse().getLabel();
				newCells[line][i] = new Cell(val, null, newCells, line, i);
			} else {
				newCells[line][i] = new Cell("", null, newCells, line, i);
			}
			i++;
		}
		File tempFile = new File(excelFile.getAbsolutePath() + ".temp");
		OutputStream out = new FileOutputStream(tempFile);
		try {
			XLSTools.writeXLSX(newCells, out, sourceFile, stepName);
			if (tempFile.exists()) {
				// check if tempFile is a valid XLSXFile, if not throw exception
				XLSTools.getArray(ctx, tempFile);
				excelFile.delete();
				tempFile.renameTo(excelFile);
			}
		} finally {
			out.close();
		}
		if (inLine == null) {
			return cells.length;
		} else {
			return line;
		}
	}
	
	public static String getDefaultSessionName(ContentContext ctx) throws Exception {
		String sessionName;
		MenuElement parentPage = ctx.getCurrentPage().getParent();
		if (parentPage != null) {
			sessionName = parentPage.getName();
		} else {
			sessionName = ctx.getCurrentPage().getName();
		}
		return sessionName + '_' + ctx.getRequestContentLanguage();
	}

	protected String getSessionName(ContentContext ctx) throws Exception {
		return getDefaultSessionName(ctx);
	}

	protected int store(ContentContext ctx, List<Question> questions, String stepName, Integer line) throws Exception {
		return storeExcel(ctx, questions, getSessionName(ctx), stepName, line);
	}

	public static void main(String[] args) {
		File excel = new File("c:/trans/europenclimatefoundation2021_en.xlsx");

		List<Question> questions = new LinkedList<>();
		Question q = new Question();
		q.setLabel("Seniority");
		q.setNumber(0);
		questions.add(q);
		q = new Question();
		q.setLabel("Role");
		q.setNumber(1);
		questions.add(q);

		try {
			loadExcel(null, excel, questions, "Demographics", 4);
			for (Question question : questions) {
				System.out.println(question);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// File excel = new File("c:/trans/questions.xlsx");

		// {
		// List<Response> responses = new LinkedList<Response>();
		// responses.add(new Response("oui", 1));
		// responses.add(new Response("non", 2));
		//
		// Question q1 = new Question();
		// q1.setNumber(1);
		// q1.setLabel("comment vas tu ?");
		// q1.setResponses(responses);
		// q1.setResponse("2");
		//
		// Question q2 = new Question();
		// q2.setNumber(2);
		// q2.setLabel("comment veux tu faire ?");
		// q2.setResponses(responses);
		// q2.setResponse("1");
		//
		// Question q3 = new Question();
		// q3.setNumber(3);
		// q3.setLabel("et moi comment puis je faires ?");
		// q3.setResponses(responses);
		// q3.setResponse("2");
		//
		// List<Question> questions = new LinkedList<Question>();
		// questions.add(q1);
		// questions.add(q2);
		// questions.add(q3);
		// try {
		// storeExcel(null, excel, questions, "step 1");
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		//
		// {
		// List<Response> responses = new LinkedList<Response>();
		// responses.add(new Response("oui", 1));
		// responses.add(new Response("non", 2));
		//
		// Question q1 = new Question();
		// q1.setNumber(1);
		// q1.setLabel("comment vas tu ?");
		// q1.setResponses(responses);
		// q1.setResponse("1");
		//
		// Question q2 = new Question();
		// q2.setNumber(2);
		// q2.setLabel("comment veux tu faire ?");
		// q2.setResponses(responses);
		// q2.setResponse("1");
		//
		// Question q3 = new Question();
		// q3.setNumber(3);
		// q3.setLabel("et moi comment puis je faires ?");
		// q3.setResponses(responses);
		// q3.setResponse("1");
		//
		// List<Question> questions = new LinkedList<Question>();
		// questions.add(q1);
		// questions.add(q2);
		// questions.add(q3);
		// try {
		// storeExcel(null, excel, questions, "step 1");
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		//
		// {
		// List<Response> responses = new LinkedList<Response>();
		// responses.add(new Response("oui", 1));
		// responses.add(new Response("non", 2));
		//
		// Question q1 = new Question();
		// q1.setNumber(1);
		// q1.setLabel("comment vas tu ?");
		// q1.setResponses(responses);
		// q1.setResponse("2");
		//
		// Question q2 = new Question();
		// q2.setNumber(2);
		// q2.setLabel("comment veux tu faire ?");
		// q2.setResponses(responses);
		// q2.setResponse("1");
		//
		// List<Question> questions = new LinkedList<Question>();
		// questions.add(q1);
		// questions.add(q2);
		// try {
		// storeExcel(null, excel, questions, "step 2");
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		//
		// {
		// List<Response> responses = new LinkedList<Response>();
		// responses.add(new Response("oui", 1));
		// responses.add(new Response("non", 2));
		//
		// Question q1 = new Question();
		// q1.setNumber(1);
		// q1.setLabel("comment vas tu ?");
		// q1.setResponses(responses);
		// q1.setResponse("1");
		//
		// Question q2 = new Question();
		// q2.setNumber(2);
		// q2.setLabel("comment veux tu faire ?");
		// q2.setResponses(responses);
		// q2.setResponse("1");
		//
		// List<Question> questions = new LinkedList<Question>();
		// questions.add(q1);
		// questions.add(q2);
		// try {
		// storeExcel(null, excel, questions, "step 2");
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }

	}

	@Override
	public String getSpecificClass(ContentContext ctx) {
		return "survey";
	}

}
