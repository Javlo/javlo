<%@page contentType="text/html"
        import="
        org.javlo.ContentManager,
        org.javlo.ContentContext,
        org.javlo.helper.URLHelper,
        org.javlo.helper.XHTMLHelper,
        org.javlo.component.AbstractVisualComponent,
        org.javlo.component.FormComponent,
        org.javlo.component.form.Form"
%><%
FormComponent formComponent = (FormComponent)AbstractVisualComponent.getRequestComponent ( request );
Form form = Form.getCurrentSessionForm(session);
ContentContext ctx = ContentContext.getContentContext ( request, response );
String[][] fieldPattern = form.getFieldPattern();
%>
<div class="form">
<form name="generic-form" class="generic-form">
	<%=formComponent.getSpecialTag()%>
	<table>
<%for ( int i=0; i<fieldPattern.length; i++ ) {
	String name=fieldPattern[i][0];
	String label=formComponent.getViewText("form."+fieldPattern[i][0]);
%>
		<tr>
			<td class="label"><%=label%> : </td>
			<td class="in"><input type="text" name="<%=name%>" value="<%=formComponent.getValue (name, "" )%>"/></td>
			<td class="message"><%=formComponent.getErrorMessage(name)%></td>
		</tr><%
}%><tr>
			<td style="text-align: right;" colspan="2">
				<input type="submit" name="submit" value="<%=formComponent.getViewText("form.submit")%>"/>
			</td>
			<td>&nbsp;</td>
		</tr></table>
</form>
</div>
