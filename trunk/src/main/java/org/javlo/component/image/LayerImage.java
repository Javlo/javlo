package org.javlo.component.image;

import org.javlo.context.ContentContext;

public class LayerImage extends FilterImage {

	@Override
	protected String getFilter(ContentContext ctx) {
		return "layer";
	}
	
	@Override
	public String getType() {
		return "layer-image";
	}

}
