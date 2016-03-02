package org.javlo.fields;

import java.util.Comparator;

import org.javlo.context.ContentContext;

public class SortContainer implements Comparator<IFieldContainer> {
	
	private String field;
	private ContentContext ctx;

	public SortContainer(ContentContext ctx, String field) {
		this.field = field;
		this.ctx = ctx;
	}

	@Override
	public int compare(IFieldContainer f1, IFieldContainer f2) {		
		Field field1;
		try {
			field1 = f1.getField(ctx, field);
			Field field2 = f2.getField(ctx, field);			
			if (field1 == null || field2 == null) {
				return 0;
			} else {
				return field1.compareTo(field2);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}		
	}

}
