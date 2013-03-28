package org.javlo.utils;


public class UploadXHTML {
	
	/**
	 * create a static logger.
	 */
	/*protected static Logger logger = Logger.getLogger(UploadXHTML.class.getName());
	
	ContentContext ctx;
	MenuElement basePage;
	MenuElement currentPage;
	Content content;
	String lastContentId = null;
	int level;
	int countPage=1;
	
	public UploadXHTML (ContentContext inCtx) throws DAOException {
		ctx=inCtx;
		content = Content.createContent(ctx.getRequest());
		currentPage = content.getNavigation(ctx).getCurrentPage(ctx);
		basePage = currentPage;
	}
	
	private String createName (String label) {
		return "page-"+countPage;
	}
	
	public void createNewPage(String label) throws DAOException {	
		lastContentId = null;
		NavigationDAO dao = NavigationDAO.createDAO(ctx.getRequest());
		String name = createName(label);
		logger.info("create page : "+name);
		dao.addNavigation(basePage.getPath(), name, ctx.getContext(), countPage*10, null );
		content.releasePreviewNavigation();
		currentPage = content.getNavigation(ctx).searchChildFromName(name);
		insertComponent("title", label);
		countPage++;
	}
	
	public void insertComponent(String type, String content) throws DAOException {
		if (content != null) {
			if (content.trim().length() > 0) {
				ContentDAO dao = ContentDAO.createDAO(ctx.getRequest().getSession().getServletContext());
				lastContentId = dao.createNewContent(lastContentId, type, ctx, currentPage.getPath(), content);
			}
		}
	}
	
	public void uploadXHTML ( InputStream xhtmlFile, HttpServletRequest request, HttpServletResponse response) throws DAOException, Exception {
		ContentContext ctx = ContentContext.getContentContext ( request, response );
		Content content = Content.createContent(request);
		MenuElement currentPage = content.getNavigation(ctx).getCurrentPage(ctx);
		
		SAXBuilder sb = new SAXBuilder();
		Document doc = sb.build(xhtmlFile);
		
		insertCurrentLevel ( doc.getRootElement(), currentPage, ctx, 0 );
	}
	
	private String extractContentFromList ( List content ) {
		Iterator it = content.iterator();
		StringBuffer outString = new StringBuffer();
		while(it.hasNext()) {
			Object item = it.next();
			if (item instanceof Text) {
				outString.append(((Text)item).getText());
			}
		}
		return outString.toString();
	}
	
	private void insertCurrentLevel ( Element currentElement, MenuElement currentPage, ContentContext ctx, int depth ) throws DAOException {
		
		Iterator children = currentElement.getChildren().iterator();
		while(children.hasNext()) {
			
			Element currentChild = (Element)children.next();
		
		if (currentChild.getName().equalsIgnoreCase("h1")) {
			createNewPage(extractContentFromList(currentChild.getContent()));
		} else if (currentChild.getName().equalsIgnoreCase("h2")) {
			insertComponent("subtitle", extractContentFromList(currentChild.getContent()));
		} if (currentChild.getName().equalsIgnoreCase("p")) {
			insertComponent("paragraph", extractContentFromList(currentChild.getContent()));
		} 
		insertCurrentLevel ( currentChild, currentPage, ctx, depth++ );
					
		}
		
	}*/

}
