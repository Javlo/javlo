/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.core;

import org.javlo.context.ContentContext;

/**
 * @author pvandermaesen
 */
public interface IContentComponentsList {	
	
	public IContentVisualComponent next(ContentContext ctx);
	
	public boolean hasNext(ContentContext ctx);
	
	public String getPrefixXHTMLCode(ContentContext ctx);
	
	public String getSufixXHTMLCode(ContentContext ctx);
	
	public String getLanguage();
	
	public int size(ContentContext ctx);
	
	public void setAllArea(boolean allArea);
    
    public void initialize(ContentContext ctx);

}
