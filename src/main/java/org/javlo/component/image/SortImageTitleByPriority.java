package org.javlo.component.image;

import java.util.Comparator;

import org.javlo.context.ContentContext;

public class SortImageTitleByPriority implements Comparator<IImageTitle> {

	private ContentContext ctx = null;

	public SortImageTitleByPriority(ContentContext ctx) {
			this.ctx = ctx;
		}

	@Override
	public int compare(IImageTitle o1, IImageTitle o2) {
		return o2.getPriority(ctx) - o1.getPriority(ctx);
	}

}
