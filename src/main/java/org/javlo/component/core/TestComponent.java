/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.core;

import org.javlo.context.ContentContext;

/**
 * @author pvandermaesen
 */
public class TestComponent extends AbstractVisualComponent {
	
	public static final String TYPE = "test";
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getEditRenderer(ContentContext ctx) {
		return "/jsp/edit/component/test/edit_test.jsp";
	}
	
	@Override
	public String getFontAwesome() {
		return "fa fa-thumb-tack";
	}
	

}
