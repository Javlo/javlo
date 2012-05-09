/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.files;

import org.javlo.context.ContentContext;

/**
 * @author pvandermaesen
 */
public class GenericFileList extends GenericFile {

	@Override
	public boolean isInline() {
		return false;
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return "<li class=\"list\">";
	}

	@Override
	public String getSufixViewXHTMLCode(ContentContext ctx) {
		return "</li>";
	}

	@Override
	public String getType() {
		return "file-list";
	}
	
	@Override
	public String getFirstPrefix(ContentContext ctx) {
		return "<ul>";
	}
	
	@Override
	public String getLastSufix(ContentContext ctx) {
		return "</ul>";
	}
	
}
