package org.javlo.component.image;

import org.javlo.context.ContentContext;

public class BorderedImage extends FilterImage {

	@Override
	protected String getFilter(ContentContext ctx) {
		return "middle";
	}
	
	@Override
	public String getType() {
		return "bordered-image";
	}

}
