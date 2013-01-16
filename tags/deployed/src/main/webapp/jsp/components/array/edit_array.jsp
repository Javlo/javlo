<%@page contentType="text/html"
        import="
        org.javlo.ContentManager,
        org.javlo.ContentContext,
        org.javlo.helper.URLHelper,
        org.javlo.helper.XHTMLHelper,
        org.javlo.I18nAccess,
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
I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
Content content = Content.createContent(request);

String[][] TAParam = { { "rows", "2" }, { "cols", "100" } };

%><div class="edit">
<div class="summary">
<div class="row">
	<label for="<%=arrayComponent.getSummaryInputName()%>"><%=i18nAccess.getText(ctx, "component.array.summary")%>: </label>
	<div class="input"><textarea name="<%=arrayComponent.getSummaryInputName()%>" id="<%=arrayComponent.getSummaryInputName()%>" rows="2" cols="150"><%=array.getSummary()%></textarea></div>
</div>
</div>
<table class="edit-array">
<tr><%
	if ( array.getSize() > 0 ) {
		for ( int c=0; c<array.getWidth(); c++ ) {%>
		<th align="center"><input type="radio" name="<%=arrayComponent.getSortName()%>" value="<%=c%>"/></th><%
		}
		%><th align="center"><input type="radio" name="<%=arrayComponent.getSortName()%>" value=""/>&nbsp;<%=i18nAccess.getText("component.array.no-sort")%></th><%
	}%>

</tr><tr><%
	if ( array.getSize() > 0 ) {
		for ( int c=0; c<array.getWidth(); c++ ) {%>
		<th><%=XHTMLHelper.getTextArea ( arrayComponent.getCellName(0,c), array.getCellValue(0,c), TAParam )%></th><%
		}
	}%>
	<th class="delete"><%=i18nAccess.getText(ctx, "component.array.delete")%></th>
</tr><%
for ( int r=1; r<array.getSize(); r++ ) {
%><tr><%
	for ( int c=0; c<array.getWidth(); c++ ) {%>
	<td>
		<%=XHTMLHelper.getTextArea ( arrayComponent.getCellName(r,c), array.getCellValue(r,c), TAParam )%>
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
	<td class="delete"><div><input type="checkbox" name="<%=arrayComponent.getRowName(r)%>"/></div></td>
</tr>
<%}
%><tr>
	<td colspan="<%=array.getWidth()%>" align="center">
		<input type="hidden" name="<%=arrayComponent.getSpecialActionName()%>" value=""/>
		<a href="javascript:document.forms['content_update'].<%=arrayComponent.getSpecialActionName()%>.value='new-line';document.forms['content_update'].submit();"><%=i18nAccess.getText(ctx, "component.array.insert-line")%></a>&nbsp;
		<a href="javascript:document.forms['content_update'].<%=arrayComponent.getSpecialActionName()%>.value='new-line-2';document.forms['content_update'].submit();">x2</a>&nbsp;
		<a href="javascript:document.forms['content_update'].<%=arrayComponent.getSpecialActionName()%>.value='new-line-3';document.forms['content_update'].submit();">x3</a>&nbsp;
		<a href="javascript:document.forms['content_update'].<%=arrayComponent.getSpecialActionName()%>.value='new-line-7';document.forms['content_update'].submit();">x7</a>
	</td>
</tr>
</table></div><%
} catch ( Throwable e ) {
  e.printStackTrace();
  %><%=e.getMessage()%><%
}%>

