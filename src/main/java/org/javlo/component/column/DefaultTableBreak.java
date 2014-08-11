package org.javlo.component.column;

import org.javlo.context.ContentContext;

public class DefaultTableBreak extends TableBreak {
	
	public static final DefaultTableBreak instance = new DefaultTableBreak();

	public String getOpenTableStyle(ContentContext ctx) {
		StringBuffer style = new StringBuffer();		
		return style.toString();
	}
	
	@Override
	public boolean isGrid(ContentContext ctx) {
		return false;
	}
	
	@Override
	public boolean isBorder(ContentContext ctx) {
		return false;
	}

}
