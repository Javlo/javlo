package org.javlo.fields;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.context.ContentContext;

public class SortContainer implements Comparator<IFieldContainer> {

	private List<String> fields = new LinkedList<String>();
	private ContentContext ctx;
	private Map<String, Integer> fieldsAscendant = new HashMap<String, Integer>();

	public SortContainer(ContentContext ctx, String inField) {
		try {
			List<String> localField = new LinkedList<String>();
			if (inField.contains(",")) {
				localField.addAll(Arrays.asList(inField.split(",")));
			} else {
				localField.add(inField);
			}

			for (String field : localField) {
				if (field.startsWith(">")) {										
					field = field.substring(1).trim();
					fieldsAscendant.put(field, 1);					
				} else if (field.startsWith("<")) {					
					field = field.substring(1).trim();
					fieldsAscendant.put(field, -1);										
				}
				fields.add(field);	
			}

			this.ctx = ctx;
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private int ascendant(String field) {
		Integer factor = fieldsAscendant.get(field);
		if (factor == null) {
			return 1;
		} else {
			return factor;
		}
	}

	private int compareTo(int number, Comparable obj1, Comparable obj2, int ascendant) {		
		try {
			if (obj1 == null && obj2 == null) {
				return 0;
			}
			if (obj1 == null) {
				return 1;
			}
			if (obj2 == null) {
				return -1;
			}
			if (obj1.compareTo(obj2) > 0) {
				return number*ascendant;
			} else if (obj1.compareTo(obj2) < 0) {
				return -number*ascendant;
			} else {
				return 0;
			}
		} catch (Throwable e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public int compare(IFieldContainer f1, IFieldContainer f2) {
		if (f1 == null && f2 == null) {
			return 0;
		}
		if (f1 == null) {
			return 1;
		}
		if (f2 == null) {
			return -1;
		}
		try {
			Iterator<String> ite = fields.iterator();
			String field = ite.next();
			int multi = 1;
			int number = 500;
			if (field.startsWith("!")) {
				multi = -1;
				field = field.substring(1);
			}
			while (f1.getField(ctx, field) != null && f1.getField(ctx, field).compareTo(f2.getField(ctx, field)) == 0 && ite.hasNext()) {
				field = ite.next();
				multi = 1;
				if (field.startsWith("!")) {
					multi = -1;
					field = field.substring(1);
				}
				number--;
			}
			return compareTo(number, f1.getField(ctx, field), f2.getField(ctx, field), ascendant(field)) * multi;
			/*
			 * return f1.getField(ctx, field).compareTo(f2.getField(ctx,
			 * field))*multi; field1 = f1.getField(ctx, field); Field field2 =
			 * f2.getField(ctx, field); if (field1 == null || field2 == null) {
			 * return 0; } else { return field1.compareTo(field2); }
			 */
		} catch (Throwable e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public static void main(String[] args) {
		String field = "< name";
		field = field.substring(1).trim();
		System.out.println("field = "+field);
	}

}
