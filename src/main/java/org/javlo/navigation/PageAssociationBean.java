package org.javlo.navigation;

import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;

public class PageAssociationBean {

	private ContentContext ctx;
	private MenuElement root;
	private MenuElement associationPage = null;
	private MenuElement articleRoot = null;
	private List<PageBean> pages = null;
	private List<PageBean> articles = null;

	public PageAssociationBean(ContentContext ctx, MenuElement rootChildrenAgregation) {
		this.ctx = ctx;
		this.root = rootChildrenAgregation;
	}

	public List<PageBean> getPages() throws Exception {
		if (pages == null) {
			pages = new LinkedList<PageBean>();
			MenuElement assPage = getAssociationPageInternal();
			for (MenuElement child : assPage.getChildMenuElements()) {
				pages.add(child.getPageBean(ctx));
			}
		}
		return pages;
	}
	
	public List<PageBean> getArticles() throws Exception {
		if (articles == null) {
			articles = new LinkedList<PageBean>();
			MenuElement articlesPage = getArticlesRootInternal();
			for (MenuElement child : articlesPage.getChildMenuElements()) {
				articles.add(child.getPageBean(ctx));
			}
		}
		return articles;
	}

	private MenuElement getAssociationPageInternal() {
		if (associationPage == null) {
			for (MenuElement child : root.getChildMenuElements()) {
				if (child.isChildrenAssociation()) {
					associationPage = child;
					return child;
				}
			}
		}
		return associationPage;
	}

	public PageBean getAssociationPage() throws Exception {
		return getAssociationPageInternal().getPageBean(ctx);
	}

	private MenuElement getArticlesRootInternal() {
		if (articleRoot == null) {
			for (MenuElement child : root.getChildMenuElements()) {
				if (!child.isChildrenAssociation()) {
					articleRoot = child;
					return child;
				}
			}
		}
		return articleRoot;
	}

	public PageBean getArticleRoot() throws Exception {
		return getArticlesRootInternal().getPageBean(ctx);
	}

}
