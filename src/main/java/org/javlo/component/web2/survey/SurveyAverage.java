package org.javlo.component.web2.survey;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.helper.StringHelper;
import org.javlo.utils.Cell;
import org.javlo.utils.NeverEmptyMap;
import org.javlo.utils.XLSTools;

public class SurveyAverage {

	private static Logger logger = Logger.getLogger(SurveyAverage.class.getName());

	public static String extractKey(String title) {
		title = title.substring(title.indexOf('_') + 1);
		return title.trim();
	}

	public static Map<String, Double> average(Cell[][] cells, boolean underscore, int maxSizeLabel, String labelSeparator) {

		if (cells.length == 0 || cells[0].length == 0) {
			logger.warning("empty array.");
			return Collections.EMPTY_MAP;
		}

		Map<String, Integer> total = new NeverEmptyMap<>(String.class, Integer.class);
		Map<String, Integer> count = new NeverEmptyMap<>(String.class, Integer.class);
		for (int i = 0; i < cells[0].length; i++) {
			if (cells[0][i] != null && cells[0][i].getValue() != null && !cells[0][i].getValue().startsWith("_") && (!underscore || cells[0][i].getValue().contains("_"))) {
				String key = extractKey(cells[0][i].getValue());
				for (int j = 1; j < cells.length; j++) {
					if (StringHelper.isDigit(cells[j][i].getValue())) {
						System.out.println(">>>>>>>>> SurveyAverage.average 1.key = "+key); //TODO: remove debug trace
						total.put(key, total.get(key).intValue() + Integer.parseInt(cells[j][i].getValue()));
						count.put(key, count.get(key).intValue() + 1);
					}
				}
			}
		}
		Map<String, Double> out = new LinkedHashMap<>();
		for (String key : total.keySet()) {
			System.out.println(">>>>>>>>> SurveyAverage.average 2.key = "+key); //TODO: remove debug trace
			//out.put(StringHelper.setLineSeparator(key.trim().toLowerCase(), maxSizeLabel, labelSeparator), (double) total.get(key) / (double) count.get(key));
			out.put(StringHelper.setLineSeparator(key.trim(), maxSizeLabel, labelSeparator), (double) total.get(key) / (double) count.get(key));
		}
		
		for (String key : out.keySet()) {
			System.out.println(">>>>>>>>> SurveyAverage.average 3.key = "+key); //TODO: remove debug trace
		}

		return out;
	}

	public static void main(String[] args) throws Exception {
		// Cell[][] cells = XLSTools.getArray(null, new File("c:/trans/a_test.xlsx"));
		Cell[][] cells = XLSTools.getArray(null, new File("c:/trans/gouvernance_bcf_2022_fr_2.xlsx"), "list survey");
		Map<String, Double> average = average(cells, false, 25, "#");
		for (Map.Entry<String, Double> a : average.entrySet()) {
			System.out.println(a.getKey() + " = " + a.getValue());
		}
	}

}
