package org.javlo.component.meta;

import java.io.StringReader;
import java.io.StringWriter;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.utils.StructuredProperties;

public class I18nComponent extends AbstractVisualComponent {
	
	public static String TYPE = "i18n";

	public I18nComponent() {
	}

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String performEdit(ContentContext ctx) throws Exception {	
		String msg = super.performEdit(ctx);
		StructuredProperties prop = new StructuredProperties();
		prop.load(new StringReader(getValue()));
		StringWriter strWtr = new StringWriter();
		prop.store(strWtr, null);
		setValue(strWtr.toString());
		return msg;
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return false;		
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}
}
