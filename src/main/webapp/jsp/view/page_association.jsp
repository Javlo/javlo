<%@page import="org.javlo.message.MessageRepository"
%><%@page import="org.javlo.helper.URLHelper"
%><%@page import="org.javlo.template.TemplateFactory"
%><%@page import="org.javlo.template.Template"
%><%@page import="org.javlo.context.ContentContext"
%><%@page import="org.javlo.navigation.MenuElement"
%><%
ContentContext ctx = ContentContext.getContentContext(request, response);
if (ctx.isInteractiveMode() && ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {%>
<jsp:include page="/jsp/preview/command.jsp" /><%
} else if (ctx.isInteractiveMode() && ctx.getRenderMode() == ContentContext.TIME_MODE) {%>
	<jsp:include page="/jsp/time-traveler/command.jsp" />
	<%MessageRepository messageRepository = MessageRepository.getInstance(ctx);
    %><div id="message-container" class="standard"><%
	if (messageRepository.getGlobalMessage().getMessage().trim().length() > 0) {%>
		<div class="notification <%=messageRepository.getGlobalMessage().getTypeLabel()%>"><%=messageRepository.getGlobalMessage().getMessage()%></div>
	<%}%></div><%
}

MenuElement currentPage = ctx.getCurrentPage();
boolean savePageAssocitation = ctx.isPageAssociation();

ctx.setPageAssociation(true);

/** remove preview command **/
boolean interactiveMode = ctx.isInteractiveMode();

for (MenuElement child : currentPage.getChildMenuElements()) {
	Template childTemplate = TemplateFactory.getTemplate(ctx, child);
	ctx.setCurrentPageCached(child);
	ctx.setCurrentTemplate(childTemplate);
	String jspURI = childTemplate.getRendererFullName(ctx);
	jspURI = URLHelper.addParam(jspURI, "pageAssociation", "true");
	%><jsp:include page="<%=jspURI%>" /><%
}
ctx.setPageAssociation(savePageAssocitation);
%>
