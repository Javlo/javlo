package org.javlo.component.links;

public class SmartHTMLPageImportation extends SmartExternalLinkImportation {
	
	@Override
	public String getType() {
		return "smart-html-importation";
	}
	
	@Override
	protected boolean isHTMLPage() {
		return true;
	}

}
