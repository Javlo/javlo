<%@page contentType="text/html"
        import="
        org.javlo.ContentManager,
        org.javlo.ContentContext,
        org.javlo.helper.URLHelper,
        org.javlo.helper.XHTMLHelper,
        org.javlo.component.AbstractVisualComponent,
        org.javlo.component.ArrayComponent,
        org.javlo.component.array.Array,
        org.javlo.MenuElement,
        org.javlo.Content"
%><%
try {
ArrayComponent arrayComponent = (ArrayComponent)AbstractVisualComponent.getRequestComponent ( request );
Array array = arrayComponent.getArray();

ContentContext ctx = ContentContext.getContentContext ( request, response );
Content content = Content.createContent(request);

String[][] TAParam = { { "rows", "2" }, { "cols", "100" } };

%>
<div class="edit">
<table class="edit-array">
<%
for ( int r=0; r<array.getSize(); r++ ) {
%><tr><%
	for ( int c=0; c<array.getWidth(); c++ ) {%>
	<td>
		<input type="hidden" name="<%=arrayComponent.getCellName(r,c)%>" value="<%=array.getCellValue(r,c)%>" />
		<div class="insert-page">
		<select name="<%=arrayComponent.getCellNameNavigation(r,c)%>" class="select-navigation">
			<option value="">pages...</option><%
			MenuElement pageToInclude = array.getPageToInclude(ctx, r, c);
			String path = null;
			if (pageToInclude != null) {
				path = pageToInclude.getPath();
			}
			MenuElement elem = ctx.getCurrentPage();
			%><%=XHTMLHelper.getHTMLChildList(elem, path, "<option value=\"#name\">", "<option value=\"#name\" selected=\"selected\">", "</option>", false )%><%

		%></select><%
			if (pageToInclude != null) {
				%><a class="page" href="<%=URLHelper.createURL(ctx, pageToInclude.getPath())%>">&gt;&gt;</a><%
			}
			%>
		</div>
	</td><%
	}%>
</tr>
<%}
%>
</table></div><%
} catch ( Throwable e ) {
  e.printStackTrace();
  %><%=e.getMessage()%><%
}%>

