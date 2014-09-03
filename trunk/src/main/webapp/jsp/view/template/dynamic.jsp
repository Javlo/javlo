<%@page import="org.javlo.helper.StringHelper"
%><%@page import="org.javlo.data.InfoBean"
%><%@page import="org.javlo.helper.URLHelper"
%><%@page import="org.javlo.template.Row"
%><%@page import="java.util.Collection"
%><%@page import="org.javlo.template.Template"
%><%@page import="org.javlo.context.ContentContext"
%><%
ContentContext ctx = new ContentContext(ContentContext.getContentContext(request, response));
ctx.setAbsoluteURL(false);
Template template = ctx.getCurrentTemplate();
Collection<Row> rows = template.getRows();
InfoBean bean = InfoBean.getCurrentInfoBean(ctx);
String rowJsp = URLHelper.createStaticTemplateURLWithoutContext(ctx,ctx.getCurrentTemplate(), "/jsp/row.jsp");

boolean renderLast = true;
boolean renderNotLast = true;
if (request.getParameter("lastRow") != null) {	
	renderLast = StringHelper.isTrue(request.getParameter("lastRow"));
	renderNotLast = !renderLast;
}
int rowNumber = 1;
for (Row row : rows) {
	request.setAttribute("row", row);
	request.setAttribute("rowNumber", rowNumber);
	request.setAttribute("lastRow", rowNumber==rows.size());	
	if ((renderLast || rowNumber<rows.size()) && (renderNotLast || rowNumber==rows.size())) {%><jsp:include page="<%=rowJsp%>" /><%}
	rowNumber++;
}%>