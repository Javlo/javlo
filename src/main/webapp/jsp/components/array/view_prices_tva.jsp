<%@page contentType="text/html"
        import="
        org.javlo.ContentManager,
        org.javlo.ContentContext,
        org.javlo.helper.URLHelper,
        org.javlo.helper.XHTMLHelper,
        org.javlo.component.AbstractVisualComponent,
        org.javlo.component.PriceListComponent,
        org.javlo.component.array.Array"
%><%
PriceListComponent priceListComp = (PriceListComponent)AbstractVisualComponent.getRequestComponent ( request );
Array priceList = priceListComp.getArray();
ContentContext ctx = ContentContext.getContentContext ( request, response );
%><div class="array">
<table>
	<tr>
		<th class="array-first-title"><%=priceList.getColTitle(0)%></th><%
		for ( int i=1; i<priceList.getWidth(); i++ ) {
		%>		
		<th class="array-title"><%=priceList.getColTitle(i)%></th><%
		}%>
		<th class="array-title">TVAC</th>
	</tr>
<%
for ( int r=1; r<priceList.getSize(); r++ ) {
%><tr>
	<th class="array-row-title"><%=priceList.getRowTitle(r)%></th><%
	for ( int c=1; c<priceList.getWidth()-1; c++ ) {
	%>	
	<td class="array-content"><%=priceList.getCellValue(r,c)%></td><%
	}%>
	<td class="array-content"><%=priceList.getCellValueConvertFormated(r,3,1,2)%></td>
	<td class="array-content"><%=priceList.getCellValueConvertFormated(r,3,1.21,2)%></td>
</tr>
<%}
%></table></div>

