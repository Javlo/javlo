package org.javlo.component.text;

import org.javlo.context.ContentContext;

public class FullWysiwygParagraph extends WysiwygParagraph {

	public static final String TYPE = "full-wysiwyg-paragraph";
	
	@Override
	public String getType() {
		return TYPE;		
	}
	
	@Override
	protected String getEditorComplexity(ContentContext ctx) {
		return getConfig(ctx).getProperty("complexity", "high");
	}

}
