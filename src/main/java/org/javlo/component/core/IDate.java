package org.javlo.component.core;

import java.util.Date;

import org.javlo.context.ContentContext;

public interface IDate {

	public Date getDate(ContentContext ctx);
	
	public boolean isValidDate(ContentContext ctx);

}
