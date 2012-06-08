<%@page contentType="text/html" pageEncoding="UTF-8"
        import="java.lang.StringBuffer,
        java.util.Iterator,
        java.util.List,
        java.util.Collection,
        org.javlo.helper.PaginationContext,
        org.javlo.helper.URLHelper,
        org.javlo.component.core.ComponentBean,
        org.javlo.search.SearchResult,        
        org.javlo.search.SearchResult.SearchElement,
        org.javlo.component.title.GroupTitle,
        org.javlo.context.GlobalContext,
        org.javlo.component.core.ComponentFactory,
        org.javlo.component.core.IContentVisualComponent,
        org.javlo.helper.XHTMLHelper,
        org.javlo.i18n.I18nAccess,
        org.javlo.context.ContentContext"
%><%@ taglib uri="http://displaytag.sf.net" prefix="display" %><%
ContentContext ctx = new ContentContext ( ContentContext.getContentContext ( request, response ) ); /* local context */
GlobalContext globalContext = GlobalContext.getInstance(request);
SearchResult status = SearchResult.getInstance( request.getSession() );
I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());

Collection<SearchResult.SearchElement> result = status.getSearchResultCollection();
PaginationContext paginationContext = PaginationContext.getInstance(request.getSession(), result.size(), 10);
paginationContext.pageAction(request);

List<ComponentBean> comps = ComponentFactory.getContentByType(ctx, GroupTitle.TYPE );

String searchGroupId = request.getParameter("search-group");
if (searchGroupId == null) {
	searchGroupId = "";
}

request.setAttribute("search-list", status.getSearchResultCollection() );
%>

<div id="search-result">
<h1 class="title"><%=i18nAccess.getViewText("search.result-title")%></h1>
<h2 class="subtitle"><%=result.size()%> <%=i18nAccess.getViewText("search.element-found")%> : <%=XHTMLHelper.escapeXHTML(status.getSearchText())%></h2>
<ul class="search-group">
<%
for (ComponentBean comp : comps) {
	String selected = "";
	if (comp.getId().equals(searchGroupId)) {
		selected = " class=\"selected\"";
	}
	%><li<%=selected%>><a href="<%=URLHelper.createURL(ctx, new String[] {"keywords","webaction"}, null)%>&search-group=<%=comp.getId()%>"><%=comp.getValue()%></a></li><%
}
%>
</ul>
<%
if (result.size() > 0) {%>
<%=paginationContext.renderCommand(ctx, URLHelper.createURL(ctx)+"?webaction=search.search&keywords="+status.getSearchText()+"&search-group="+searchGroupId)%>
<ul>
	<%
	int i=0;
	for (SearchResult.SearchElement elem : result) {
		i++;
		String classTR="even";
		if (i%2==0) {
			classTR="odd";
		}
	if (paginationContext.isPageVisible(i)) {
		%><li class="<%=classTR%>">
			<div class="title"><a href="<%=elem.getUrl()%>"><%=elem.getTitle()%></a></div>
			<div class="description"><%=elem.getDescription()%></div>
			<div class="url"><a href="<%=elem.getUrl()%>"><%=elem.getUrl()%></a></div>
		</li><%
	}
	}
	%>
</ul>
<%=paginationContext.renderCommand(ctx, URLHelper.createURL(ctx)+"?webaction=search.search&keywords="+status.getSearchText()+"&search-group="+searchGroupId)%><%
}%>

</div>