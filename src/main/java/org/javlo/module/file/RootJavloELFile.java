package org.javlo.module.file;

import java.io.File;
import java.lang.ref.WeakReference;

import org.javlo.context.ContentContext;

public class RootJavloELFile extends JavloELFile {
	
	private WeakReference<ContentContext> refCtx;
	
	public RootJavloELFile(ContentContext ctx, ELVolume volume, File file) {
		super(volume, file, null);		
		this.refCtx = new WeakReference<ContentContext>(ctx);		
	}
	
	@Override
	public ContentContext getContentContext() {
		if (refCtx != null) {
			return refCtx.get();
		}
		return null;
	}

	public void setContentContext(ContentContext ctx) {
		this.refCtx = new WeakReference<ContentContext>(ctx);
	}
	
	@Override
	public boolean isRoot() {
		return true;
	}
}
