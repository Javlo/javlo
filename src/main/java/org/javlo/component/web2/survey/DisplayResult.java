package org.javlo.component.web2.survey;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
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
	protected static final String FILE_TITLE = "file-title";
	protected static final String FILE_TITLE_2 = "file-title-2";
	protected static final String FILE_TITLE_3 = "file--title-3";
	protected static final String MAX_VALUE = "max";
	protected static final String MIN_VALUE = "min";
	protected static final String LENCIONI = "lencioni";
	protected static final String AVERAGE = "average";

	public static final String TYPE = "display-result";

	@Override
	public String getType() {
		return TYPE;
	}

	private static final List<String> FIELDS = Arrays.asList(new String[] { TITLE_FIELD, FILE_NAME, FILE_NAME_2, FILE_NAME_3, FILE_TITLE, FILE_TITLE_2, FILE_TITLE_3, MIN_VALUE, MAX_VALUE });

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
		
		File file1 = new File("C:/Users/user/data/javlo/data-ctx/data-humind/static/dynamic-form-result/resa/confiance/resa_promo_035_2022_autoeval_fin.csv");
		File file2 = new File("C:/Users/user/data/javlo/data-ctx/data-humind/static/dynamic-form-result/resa/confiance/resa_promo_035_2022_autoeval_debut.csv");
		
		int i=0;
		Map<String, Double> ref = null;
		try {
			for (Cell[][] cells : new Cell[][][] { loadCells(null,file1.getAbsolutePath()), loadCells(null, file2.getAbsolutePath()) }) {
				if (cells != null) {
					
					
						Map<String, Double> average = SurveyAverage.average(cells, false, false ? 999 : 80, "#");
						System.out.println("----------------------------------------------");
						System.out.println("# "+average.size());
						for (Map.Entry<String, Double> e : average.entrySet()) {
							System.out.println("> "+e.getValue()+" - "+e.getKey());
						}
						if (ref != null) {
							average = MapHelper.sameSortingNormilized(average, ref);
						}
						
						ref=average;
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
//		try {
//			String filePath = "c:/trans/resa_demo_2022_p1.csv";
//			Cell[][] cells = loadCells(null, filePath);
//			System.out.println(">>>>>>>>> DisplayResult.main : #cells = " + cells.length); // TODO: remove debug trace
//			Map<String, Double> average = SurveyAverage.average(cells, false, 200, "#");
//			average = MapHelper.sameSorting(average, average);
//			System.out.println(">>>>>>>>> DisplayResult.main : #average = " + average.size()); // TODO: remove debug trace
//			for (Cell[] cells2 : cells) {
//				for (int i = 0; i < cells2.length; i++) {
//					System.out.println(cells2[i]);
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
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
					Map<String, Double> averageNotSort = SurveyAverage.average(cells, lencioni, lencioni ? 999 : 80, "#");
					Map<String, Double> average = new LinkedHashMap<String, Double>(averageNotSort);
					
					average = MapHelper.sortByValue(average, false);
//					for (Map.Entry<String, Double> e : average.entrySet()) {
//						System.out.println("> "+e.getValue()+" - "+e.getKey());
//					}
					if (i==0) {
						ref = average;
						ctx.getRequest().setAttribute("average", average);
						ctx.getRequest().setAttribute("averageNotSort", averageNotSort);
						
						for (Map.Entry<String, Double> key : averageNotSort.entrySet()) {
							System.out.println("4.key = "+key.getKey());
						}
						
					} else {
						if (ref != null) {
							ctx.getRequest().setAttribute("average"+(i+1), MapHelper.sameSortingNormilized(average, ref));
							ctx.getRequest().setAttribute("averageNotSort"+(i+1), averageNotSort);
							average = MapHelper.sameSortingNormilized(average, ref);
						} else {
							logger.severe("ref not found.");
						}
					}
					
					if (i==0) {
						double globalAverage = 0;
						for (Double value : average.values()) {
							globalAverage += value;
						}
						ctx.getRequest().setAttribute("participants", cells.length-1);
						ctx.getRequest().setAttribute("globalAverage", StringHelper.renderDouble(globalAverage / average.size(), 1));
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
