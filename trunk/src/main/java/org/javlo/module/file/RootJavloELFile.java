package org.javlo.module.file;

import java.io.File;

import org.javlo.context.ContentContext;

public class RootJavloELFile extends JavloELFile {
	
	ContentContext ctx;
	
	public RootJavloELFile(ContentContext ctx, ELVolume volume, File file) {
		super(volume, file, null);
		System.out.println("***** RootJavloELFile.RootJavloELFile : class : "+getClass().getCanonicalName()); //TODO: remove debug trace
		this.ctx = ctx;		
	}
	
	@Override
	public ContentContext getContentContext() {
		System.out.println("***** RootJavloELFile.getContentContext : "); //TODO: remove debug trace
		return ctx;
	}

	public void setContentContext(ContentContext contentContext) {
		ctx = contentContext;
	}
	
	@Override
	public boolean isRoot() {
		return true;
	}
}
