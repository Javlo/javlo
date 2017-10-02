<%@page import="org.javlo.message.MessageRepository"
%><%@page import="org.javlo.helper.URLHelper"
%><%@page import="org.javlo.template.TemplateFactory"
%><%@page import="org.javlo.template.Template"
%><%@page import="org.javlo.context.ContentContext"
%><%@page import="org.javlo.config.StaticConfig"
%><%@page import="org.javlo.navigation.MenuElement"
%><%
ContentContext ctx = ContentContext.getContentContext(request, response, false);
ctx.setForceCorrectPath(false);
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
boolean mailing = ctx.getCurrentTemplate().isMailing();
%>
<%if (mailing) {%><center><table class="association-wrapper" cellpadding="0" cellspacing="0"><tbody><tr><td><%}
if (!mailing) {%><div class="association-wrapper"><%}
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
	String jspURI = childTemplate.getRendererFullName(ctx);
	jspURI = URLHelper.addParam(jspURI, "pageAssociation", "true");
	jspURI = URLHelper.addParam(jspURI, Template.FORCE_TEMPLATE_PARAM_NAME, childTemplate.getName());
	request.setAttribute("pageNumber", ""+pageNumber);	
	if (child.getImageBackground(ctx) != null) {
		String backgroundURL = URLHelper.createTransformURL(ctx, ctx.getCurrentPage(), child.getImageBackground(ctx).getResourceURL(ctx), "background");
		request.setAttribute("backgroundImage", backgroundURL);
		request.setAttribute("backgroundImageStyle", " background-image: url('"+backgroundURL+"');");
	}
	request.setAttribute("pageClass", "page-"+pageNumber+positionStr);
	if (pageNumber<lastPage) {
		positionStr="";
	} else {
		positionStr=" last-page";
		request.setAttribute("lastAssociation", true);
	}
	pageNumber++;
	ctx.setCurrentTemplate(null);
	%><jsp:include page="<%=jspURI%>" /><%
	request.removeAttribute("backgroundImage");
	request.removeAttribute("backgroundImageStyle");
}
ctx.setPageAssociation(savePageAssocitation);
%><%if (mailing) {%></td></tr></tbody></table></center><%} if (!mailing) {%></div><%}%>
