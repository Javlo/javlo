package org.javlo.component.image;

import java.util.Comparator;

import org.javlo.context.ContentContext;

public class SortImageTitleByPriority implements Comparator<IImageTitle> {

	private ContentContext ctx = null;
	private boolean mobile = false;
	
	public SortImageTitleByPriority(ContentContext ctx) {
		 	if (ctx.getDevice() != null) {
		 		this.mobile = ctx.getDevice().isMobileDevice();
		 	}
			this.ctx = ctx;
		}

	@Override
	public int compare(IImageTitle o1, IImageTitle o2) {
		
		if (mobile) {
			if (o1.isMobileOnly(ctx)) {
				return -Integer.MIN_VALUE;
			}
			if (o2.isMobileOnly(ctx)) {
				return Integer.MAX_VALUE;
			}
		}
		
		return o2.getPriority(ctx) - o1.getPriority(ctx);
	}

}
