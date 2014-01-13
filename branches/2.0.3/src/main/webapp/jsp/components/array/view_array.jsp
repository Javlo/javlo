<%@page contentType="text/html"
        import="
        org.javlo.ContentManager,
        org.javlo.ContentContext,
        org.javlo.helper.URLHelper,
        org.javlo.helper.XHTMLHelper,
        org.javlo.component.AbstractVisualComponent,
        org.javlo.component.ArrayComponent,
        org.javlo.component.array.Array,
        org.javlo.MenuElement"
%><%
ArrayComponent arrayComponent = (ArrayComponent)AbstractVisualComponent.getRequestComponent ( request );
Array array = arrayComponent.getArray();
ContentContext ctx = ContentContext.getContentContext ( request, response );

String firstColTag = "th";
if (!arrayComponent.isFirstColTitle()) {
	firstColTag = "td";
}

%>
<div class="<%=arrayComponent.getType()%>"><div class="<%=arrayComponent.getCSSClassName(ctx)%>">
<table <%=array.getSummary().trim().length()<0?"summary=\""+array.getSummary()+'"':""%>><%
int startLine = 0;
if (arrayComponent.isFirstRowTitle()) {
	startLine = 1;%>
	<tr><%
		for ( int i=0; i<array.getWidth(); i++ ) {
			String cssStyle = "array-title";
			if (i==0) {
				cssStyle = "array-first-title";
			}
		if ( !array.colEmpty(i) ) {
			MenuElement pageToInclude = array.getPageToInclude(ctx, 0, i);
			if (pageToInclude != null) {
				String currentPath = ctx.getPath();
				ctx.setPath(pageToInclude.getPath());
				ctx.setArray(true);
				%><th class="<%=cssStyle%>"><jsp:include page="/jsp/content_view.jsp" flush="true" /></th><%
				ctx.setArray(false);
				ctx.setPath(currentPath);
			} else {
  		        %><th class="<%=cssStyle%>"><%=array.getColTitle(i)%></th>
<%
			}
  		}
    }%>		
	</tr>
<%
}
for ( int r=startLine; r<array.getSize(); r++ ) {
	String trClass = "";
	if (r%2==1) {
		trClass = " class=\"odd\"";
	}
	
%><tr<%=trClass%>><%
	for ( int c=0; c<array.getWidth(); c++ ) {
		String cssStyle = "array-content";
		String cellTag = "td";
		if (c==0) {
			cellTag = firstColTag;
			cssStyle = "array-row-title";
		}
	if ( !array.colEmpty(c) ) {
		MenuElement pageToInclude = array.getPageToInclude(ctx, r, c);
		if (pageToInclude != null) {
			String currentPath = ctx.getPath();
			ctx.setPath(pageToInclude.getPath());
			ctx.setArray(true);
			%><<%=cellTag%> class="<%=cssStyle%>"><jsp:include page="/jsp/content_view.jsp" flush="true" /></<%=cellTag%>><%
			ctx.setArray(false);
			ctx.setPath(currentPath);
		} else {
  		%><<%=cellTag%> class="<%=cssStyle%>"><%=array.getCellValueFormated(r,c,2)%></<%=cellTag%>>
<%
  		}
  	}
  }%>
</tr>
<%}
%></table></div></div>

