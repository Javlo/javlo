<%@page contentType="text/html"
        import="
        org.javlo.ContentManager,
        org.javlo.ContentContext,
        org.javlo.helper.URLHelper,
        org.javlo.helper.XHTMLHelper,
        org.javlo.component.AbstractVisualComponent,
        org.javlo.component.ArrayComponent,
        org.javlo.component.array.Array"
%><%
ArrayComponent arrayComponent = (ArrayComponent)AbstractVisualComponent.getRequestComponent ( request );
Array array = arrayComponent.getArray();
ContentContext ctx = ContentContext.getContentContext ( request, response );

String[][] TAParam = { { "rows", "1" } };

%><div class="edit">
<table class="edit-array">
<tr><%
	if ( array.getSize() > 0 ) {
		for ( int c=0; c<array.getWidth(); c++ ) {%>
		<th align="center"><input type="radio" name="<%=arrayComponent.getSortName()%>" value="<%=c%>"/></th><%
		}
		%><th>&nbsp;</th><%
	}%>

</tr><tr><%
	if ( array.getSize() > 0 ) {
		for ( int c=0; c<array.getWidth(); c++ ) {%>
		<th><%=arrayComponent.getColTitle(c)%></th><%
		}
	}%>
	<th>delete</th>
</tr><%
for ( int r=1; r<array.getSize(); r++ ) {
%><tr><%
	for ( int c=0; c<array.getWidth(); c++ ) {%>
	<td><%=XHTMLHelper.getTextArea ( arrayComponent.getCellName(r,c), array.getCellValue(r,c), TAParam )%></td><%
	}%>
	<td class="delete">
		<div><input type="checkbox" name="<%=arrayComponent.getRowName(r)%>"/></div>
	</td>
</tr>
<%}
%><tr>
	<td colspan="<%=array.getWidth()%>" align="center">
		<input type="hidden" name="<%=arrayComponent.getSpecialActionName()%>" value=""/>
		<a href="javascript:document.content_update.<%=arrayComponent.getSpecialActionName()%>.value='new-line';document.content_update.submit();">
			insert line
		</a>
	</td>
</tr>
</table></div>

