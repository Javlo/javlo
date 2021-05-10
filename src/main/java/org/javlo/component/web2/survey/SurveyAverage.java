package org.javlo.component.web2.survey;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.javlo.helper.StringHelper;
import org.javlo.utils.Cell;
import org.javlo.utils.NeverEmptyMap;
import org.javlo.utils.XLSTools;

public class SurveyAverage {

	public static String extractKey(String title) {
		title = title.substring(title.indexOf('_') + 1);
		return title.trim();
	}

	public static Map<String, Double> average(Cell[][] cells) {
		Map<String, Integer> total = new NeverEmptyMap<>(String.class, Integer.class);
		Map<String, Integer> count = new NeverEmptyMap<>(String.class, Integer.class);
		for (int i = 0; i < cells[0].length; i++) {
			if (cells[0][i] != null && cells[0][i].getValue() != null && !cells[0][i].getValue().startsWith("_") && cells[0][i].getValue().contains("_")) {
				String key = extractKey(cells[0][i].getValue());
				for (int j = 1; j < cells.length; j++) {
					if (StringHelper.isDigit(cells[j][i].getValue())) {
						total.put(key, total.get(key).intValue() + Integer.parseInt(cells[j][i].getValue()));
						count.put(key, count.get(key).intValue() + 1);
					}
				}
			}
		}
		Map<String, Double> out = new HashMap<>();
		for (String key : total.keySet()) {
			out.put(key.trim().toLowerCase(), (double) total.get(key) / (double) count.get(key));
		}
		return out;
	}

	public static void main(String[] args) throws Exception {
		Cell[][] cells = XLSTools.getArray(null, new File("c:/trans/a_test.xlsx"));
		Map<String, Double> average = average(cells);
		for (Map.Entry<String, Double> a : average.entrySet()) {
			System.out.println(a.getKey() + " = " + a.getValue());
		}
	}

}
