package org.javlo.component.web2.survey;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.MapHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.utils.CSVFactory;
import org.javlo.utils.Cell;
import org.javlo.utils.XLSTools;

public class DisplayResult extends AbstractSurvey implements IAction {

	protected static final String TITLE_FIELD = "title";
	protected static final String FILE_NAME = "file";
	protected static final String FILE_NAME_2 = "file2";
	protected static final String FILE_NAME_3 = "file3";
	protected static final String MAX_VALUE = "max";
	protected static final String MIN_VALUE = "min";
	protected static final String LENCIONI = "lencioni";
	protected static final String AVERAGE = "average";

	public static final String TYPE = "display-result";

	@Override
	public String getType() {
		return TYPE;
	}

	private static final List<String> FIELDS = Arrays.asList(new String[] { TITLE_FIELD, FILE_NAME, FILE_NAME_2, FILE_NAME_3, MAX_VALUE, MIN_VALUE });

	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		boolean out = super.initContent(ctx);
		return out;
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}

	private static Cell[][] loadCells(ContentContext ctx, String filePath) throws MalformedURLException, Exception {
		
		if (StringHelper.isEmpty(filePath)) {
			return null;
		}
		
		String sheet = null;
		if (filePath.contains("#")) {
			sheet = filePath.split("\\#")[1];
			filePath = filePath.split("\\#")[0];
		}
		Cell[][] cells;
		if (filePath.toLowerCase().startsWith("http")) {
			if (StringHelper.getFileExtension(filePath).equalsIgnoreCase("xlsx") || StringHelper.getFileExtension(filePath).equalsIgnoreCase("xls ")) {
				cells = XLSTools.getArray(null, new URL(filePath), sheet);
			} else {
				cells = CSVFactory.loadContentAsCell(new File(filePath));
			}
		} else {
			File file;
			if (ctx != null) {
				file = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), filePath));
			} else {
				file = new File(filePath);
			}
			if (!file.exists()) {
				logger.warning("file not found : " + file);
				MessageRepository messageRepository = MessageRepository.getInstance(ctx);
				messageRepository.setGlobalMessage(new GenericMessage("file not found : " + file, GenericMessage.ERROR));
				return null;
			} else {
				logger.info("load:" + file + "  sheet:" + sheet);
				if (StringHelper.getFileExtension(filePath).equalsIgnoreCase("xlsx") || StringHelper.getFileExtension(filePath).equalsIgnoreCase("xls ")) {
					cells = XLSTools.getArray(ctx, file, sheet);
				} else {
					cells = CSVFactory.loadContentAsCell(file);
				}
			}
		}
		return cells;
	}

	public static void main(String[] args) {
		try {
			String filePath = "c:/trans/resa_demo_2022_p1.csv";
			Cell[][] cells = loadCells(null, filePath);
			System.out.println(">>>>>>>>> DisplayResult.main : #cells = " + cells.length); // TODO: remove debug trace
			Map<String, Double> average = SurveyAverage.average(cells, false, 50, "#");
			average = MapHelper.sameSorting(average, average);
			System.out.println(">>>>>>>>> DisplayResult.main : #average = " + average.size()); // TODO: remove debug trace
			for (Cell[] cells2 : cells) {
				for (int i = 0; i < cells2.length; i++) {
					System.out.println(cells2[i]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		ctx.getRequest().setAttribute("title", getFieldValue(TITLE_FIELD));
		int i=0;
		Map<String, Double> ref = null;
		
		if (StringHelper.isDigit(getFieldValue(MAX_VALUE))) {
			ctx.getRequest().setAttribute("max", getFieldValue(MAX_VALUE));
		}
		
		if (StringHelper.isDigit(getFieldValue(MIN_VALUE))) {
			ctx.getRequest().setAttribute("min", getFieldValue(MIN_VALUE));
		}
		
		for (Cell[][] cells : new Cell[][][] { loadCells(ctx, getFieldValue(FILE_NAME)), loadCells(ctx, getFieldValue(FILE_NAME_2)), loadCells(ctx, getFieldValue(FILE_NAME_3)) }) {
			if (cells != null) {
				boolean lencioni = getCurrentRenderer(ctx).contains(LENCIONI);
				if (lencioni || getCurrentRenderer(ctx).contains(AVERAGE)) {
					Map<String, Double> average = SurveyAverage.average(cells, lencioni, lencioni ? 999 : 50, "#");
					if (i==0) {
						ref = average;
						ctx.getRequest().setAttribute("average", MapHelper.sortByValue(average, false));
					} else {
						ctx.getRequest().setAttribute("average"+(i+1), MapHelper.sameSorting(average, ref));
					}
					double globalAverage = 0;
					for (Double value : average.values()) {
						globalAverage += value;
					}
					if (i==0) {
						ctx.getRequest().setAttribute("participants", cells.length);
						ctx.getRequest().setAttribute("globalAverage", globalAverage / cells.length);
					}
				}
			}
			i++;
		}
	}

	@Override
	public String getActionGroupName() {
		return TYPE;
	}

}
