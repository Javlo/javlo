package org.javlo.component.multimedia;

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
		MultimediaResourceFilter.getInstance(ctx);
	}
	
	@Override
	public String getHexColor() {
		return GRAPHIC_COLOR;
	}

}
