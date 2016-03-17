package org.javlo.module.dashboard;

public class ReportBean {
	
	public int pageDescriptionRight = 0;
	public int pageDescriptionBad = 0;
	public int pageTitleRight = 0;
	public int pageTitleBad = 0;
	public int pageTitleStructureRight = 0;
	public int pageTitleStructureBad = 0;
	public int pageImageAltRight = 0;
	public int pageImageAltBad = 0;
	public int pageWithoutContent = 0;
	public int pageWithContent = 0;
	public int badExternalLink = 0;
	public int rightExternalLink = 0;
	public int badInternalLink = 0;
	public int rightInternalLink = 0;
	
	public int getGlobalComponentScore() {
		return (getRightInternalLink()+getPageTitle()+getPageDescription()+getPageTitleStructure()+getPageImageAlt())/4;
	}
	
	public int getGlobalCriteriaScore() {
		return (getExternalLinkTotal()+getPageContentTotal())/2;
	}
	
	public int getPageTitle() {
		return Math.round((pageTitleRight*100)/(pageTitleBad+pageTitleRight));
	}
	
	public int getPageDescription() {
		return Math.round((pageDescriptionRight*100)/(pageDescriptionBad+pageDescriptionRight));
	}

	public int getPageTitleStructure() {
		return Math.round((pageTitleStructureRight*100)/(pageTitleStructureBad+pageTitleStructureRight));
	}
	
	public int getPageImageAlt() {
		return Math.round((pageImageAltRight*100)/(pageImageAltBad+pageImageAltRight));
	}
	
	public int getPageWithoutContent() {
		return pageWithoutContent;
	}
	
	public int getPageWithContent() {
		return pageWithContent;
	}
	
	public int getPageContentTotal() {
		return Math.round((pageWithContent*100)/(pageWithoutContent+pageWithContent));
	}
	
	public int getBadExternalLink() {
		return badExternalLink;
	}
	
	public int getRightExternalLink() {
		return rightExternalLink;
	}
	
	public int getExternalLinkTotal() {
		return Math.round((rightExternalLink*100)/(badExternalLink+rightExternalLink));
	}
	
	public int getBadInternalLink() {
		return badInternalLink;
	}
	
	public int getRightInternalLink() {
		return rightInternalLink;
	}
	
	public int getInternalLinkTotal() {
		return Math.round((rightInternalLink*100)/(badInternalLink+rightInternalLink));
	}
	
}
