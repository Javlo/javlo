package org.javlo.component.multimedia;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Properties;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;

public class MultimediaFilter extends AbstractVisualComponent {
	
	public static final String TYPE = "multimedia-filter";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		
		Properties prop = new Properties();
		prop.load(new ByteArrayInputStream(getValue(ctx).getBytes()));
		ctx.getRequest().setAttribute("prop", prop);
		ctx.getRequest().setAttribute("lastYear", Calendar.getInstance().get(Calendar.YEAR));
		
		
		MultimediaResourceFilter.getInstance(ctx);
	}
	
	@Override
	public String getHexColor() {
		return GRAPHIC_COLOR;
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_ADMIN);
	}

}
