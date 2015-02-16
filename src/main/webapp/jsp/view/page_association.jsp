<%@page import="org.javlo.message.MessageRepository"
%><%@page import="org.javlo.helper.URLHelper"
%><%@page import="org.javlo.template.TemplateFactory"
%><%@page import="org.javlo.template.Template"
%><%@page import="org.javlo.context.ContentContext"
%><%@page import="org.javlo.config.StaticConfig"
%><%@page import="org.javlo.navigation.MenuElement"
%><%
ContentContext ctx = ContentContext.getContentContext(request, response);
StaticConfig staticConfig = ctx.getGlobalContext().getStaticConfig();
if (ctx.isInteractiveMode() && ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {%>
<jsp:include page="<%=staticConfig.getPreviewCommandFilePath()%>" /><%
} else if (ctx.isInteractiveMode() && ctx.getRenderMode() == ContentContext.TIME_MODE) {%>
	<jsp:include page="<%=staticConfig.getTimeTravelerFilePath()%>" />
	<%MessageRepository messageRepository = MessageRepository.getInstance(ctx);
    %><div id="message-container" class="standard"><%
	if (messageRepository.getGlobalMessage().getMessage().trim().length() > 0) {%>
		<div class="notification <%=messageRepository.getGlobalMessage().getTypeLabel()%>"><%=messageRepository.getGlobalMessage().getMessage()%></div>
	<%}%></div><%
}
%><center><table class="association-wrapper"><tbody><tr><td><%
MenuElement currentPage = ctx.getCurrentPage();
boolean savePageAssocitation = ctx.isPageAssociation();

ctx.setPageAssociation(true);

/** remove preview command **/
boolean interactiveMode = ctx.isInteractiveMode();
int pageNumber = 1;
int lastPage = currentPage.getChildMenuElements().size();
String positionStr = " first-page";
for (MenuElement child : currentPage.getChildMenuElements()) {	
	Template childTemplate = TemplateFactory.getTemplate(ctx, child);
	ctx.setCurrentPageCached(child);
	ctx.setCurrentTemplate(childTemplate);
	String jspURI = childTemplate.getRendererFullName(ctx);
	jspURI = URLHelper.addParam(jspURI, "pageAssociation", "true");
	request.setAttribute("pageClass", "page-"+pageNumber+positionStr);
	if (pageNumber<lastPage) {
		positionStr="";
	} else {
		positionStr=" last-page";
	}
	pageNumber++;
	%><jsp:include page="<%=jspURI%>" /><%
}
ctx.setPageAssociation(savePageAssocitation);
%></td></tr></tbody></table></center>
