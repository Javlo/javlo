package org.javlo.module.dashboard;

import java.util.LinkedList;
import java.util.List;

import org.javlo.bean.Link;

public class ReportBean {
	
	public static final int MAX_LINK_CHECK = 5;
	
	public int pageDescriptionRight = 0;
	public int pageDescriptionBad = 0;
	public int pageDescriptionBadSize = 0;
	public int pageDescriptionRightSize = 0;
	public int pageTitleBadSize = 0;
	public int pageTitleRightSize = 0;
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
	public boolean allTitleDifferent = false;
	public Link sameTitlePage1 = null;
	public Link sameTitlePage2 = null;
	
	public List<Link> badInternalLinkPages = new LinkedList<Link>();
	public List<Link> badExternalLinkPages = new LinkedList<Link>();
	public List<Link> emptyLinkPages = new LinkedList<Link>();	
	
	public List<Link> noDescriptionPages = new LinkedList<Link>();
	public List<Link> noTitlePages = new LinkedList<Link>();	
	
	public int getGlobalComponentScore() {
		return ((getPageTitle()+getPageDescription()+getPageTitleStructure()+getPageImageAlt())*2+getPageDescriptionOkSize()+getPageTitleOkSize())/10;
	}
	
	public int getGlobalCriteriaScore() {
		int dec = 0;
		if (!allTitleDifferent) {
			dec++;
		}
		return (getExternalLinkTotal()+getPageContentTotal())/2-dec;
	}
	
	public int getPageTitle() {
		return getPageTitleOkSize()+getPageTitleBadSize();
	}
	
	public int getPageDescription() {		
		return getPageDescriptionOkSize()+getPageDescriptionBadSize();
	}
	
	public int getPageDescriptionOkSize() {
		if (pageDescriptionRight+pageDescriptionBad == 0) {
			return 100;
		}
		return Math.round((pageDescriptionRightSize*100)/(pageDescriptionRight+pageDescriptionBad));
	}
	
	public int getPageTitleOkSize() {
		if (pageTitleRight+pageTitleBad == 0) {
			return 100;
		}
		return Math.round((pageTitleRightSize*100)/(pageTitleRight+pageTitleBad));
	}
	
	public int getPageTitleBadSize() {
		if (pageTitleRight+pageTitleBad == 0) {
			return 100;
		}
		return Math.round((pageTitleBadSize*100)/(pageTitleRight+pageTitleBad));
	}
	
	public int getPageDescriptionBadSize() {
		if (pageDescriptionRight+pageDescriptionBad+pageDescriptionBadSize == 0) {
			return 100;
		}
		return Math.round((pageDescriptionBadSize*100)/(pageDescriptionRight+pageDescriptionBad));
	}

	public int getPageTitleStructure() {
		if (pageTitleStructureBad+pageTitleStructureRight == 0) {
			return 100;
		}
		return Math.round((pageTitleStructureRight*100)/(pageTitleStructureBad+pageTitleStructureRight));
	}
	
	public int getPageImageAlt() {
		if (pageImageAltBad+pageImageAltRight == 0) {
			return 100;
		}
		return Math.round((pageImageAltRight*100)/(pageImageAltBad+pageImageAltRight));
	}
	
	public int getPageWithoutContent() {
		return pageWithoutContent;
	}
	
	public int getPageWithContent() {
		return pageWithContent;
	}
	
	public int getPageContentTotal() {
		if (pageWithoutContent+pageWithContent == 0) {
			return 100;
		}
		return Math.round((pageWithContent*100)/(pageWithoutContent+pageWithContent));
	}
	
	public int getBadExternalLink() {
		return badExternalLink;
	}
	
	public int getRightExternalLink() {
		return rightExternalLink;
	}
	
	public int getExternalLinkTotal() {
		if (badExternalLink+rightExternalLink == 0) {
			return 100;
		}
		return Math.round((rightExternalLink*100)/(badExternalLink+rightExternalLink));
	}
	
	public int getBadInternalLink() {
		return badInternalLink;
	}
	
	public int getRightInternalLink() {
		return rightInternalLink;
	}
	
	public int getInternalLinkTotal() {
		if (rightInternalLink+badInternalLink == 0) {
			return 100;
		}
		return Math.round((rightInternalLink*100)/(badInternalLink+rightInternalLink));
	}
	
	public List<Link> getBadInternalLinkPages() {
		return badInternalLinkPages;
	}
	
	public List<Link> getBadExternalLinkPages() {
		return badExternalLinkPages;
	}
	
	public int getMaxLinkCheck() {
		return MAX_LINK_CHECK;
	}
	
	public boolean isAllTitleDifferent() {
		return allTitleDifferent;
	}
	
	public Link getSameTitlePage1() {
		return sameTitlePage1;
	}
	
	public Link getSameTitlePage2() {
		return sameTitlePage2;
	}

	public List<Link> getNoDescriptionPages() {
		return noDescriptionPages;
	}

	public void setNoDescriptionPages(List<Link> noDescriptionPages) {
		this.noDescriptionPages = noDescriptionPages;
	}

	public List<Link> getNoTitlePages() {
		return noTitlePages;
	}

	public void setNoTitlePages(List<Link> noTitlePages) {
		this.noTitlePages = noTitlePages;
	}
}
