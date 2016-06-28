package org.javlo.fields;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;

public class SortContainer implements Comparator<IFieldContainer> {

	private List<String> fields = new LinkedList<String>();
	private ContentContext ctx;

	public SortContainer(ContentContext ctx, String field) {
		if (field.contains(",")) {
			fields.addAll(Arrays.asList(field.split(",")));
		} else {
			fields.add(field);
		}

		this.ctx = ctx;
	}
	
	private int compareTo(int number, Comparable obj1, Comparable obj2) {
		if (obj1.compareTo(obj2) > 0) {
			return number;
		} else if (obj1.compareTo(obj2) < 0) {
			return -number;
		} else {
			return 0;
		}
	}

	@Override
	public int compare(IFieldContainer f1, IFieldContainer f2) {
		try {
			Iterator<String> ite = fields.iterator();
			String field = ite.next();			
			int multi = 1;
			int number = 500;
			if (field.startsWith("!")) {
				multi = -1;
				field = field.substring(1);
			}
			while (f1.getField(ctx, field).compareTo(f2.getField(ctx, field)) == 0 && ite.hasNext()) {
				field = ite.next();				
				multi = 1;
				if (field.startsWith("!")) {
					multi = -1;
					field = field.substring(1);
				}
				number--;
			}			
			return compareTo(number, f1.getField(ctx, field), f2.getField(ctx, field))*multi;
			/*return f1.getField(ctx, field).compareTo(f2.getField(ctx, field))*multi;
			 * field1 = f1.getField(ctx, field); Field field2 = f2.getField(ctx,
			 * field); if (field1 == null || field2 == null) { return 0; } else
			 * { return field1.compareTo(field2); }
			 */
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

}
