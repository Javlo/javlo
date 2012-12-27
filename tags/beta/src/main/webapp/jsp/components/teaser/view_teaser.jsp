<%@ page import="org.javlo.Content" %>
<%@page contentType="text/html"
        import="
        org.javlo.ContentContext,
        org.javlo.helper.URLHelper,
        org.javlo.helper.XHTMLHelper,
        org.javlo.config.GlobalContext,
        org.javlo.config.StaticConfig,
        org.javlo.component.AbstractVisualComponent,
        org.javlo.component.complex.Teaser,
        org.javlo.EditContext"
%><%
Teaser teaser = (Teaser)AbstractVisualComponent.getRequestComponent ( request );
ContentContext ctx = ContentContext.getContentContext ( request, response );
Content content = Content.createContent(request);
String eventPath = "";
if (teaser.getLinkInfo().length() > 0) {
	eventPath = content.getNavigation(ctx).searchChildFromId(teaser.getLinkInfo()).getPath();
}
GlobalContext globalContext = GlobalContext.getInstance(request.getSession());
GlobalContext globalContext = GlobalContext.getInstance(request); EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
String imageURL = URLHelper.createRessourceURL(ctx, staticConfig.getImageFolder()+'/'+teaser.getDirSelected()+'/'+teaser.getFileName()).replace('\\', '/');

boolean isLink = teaser.getLinkInfoTitle().trim().length() > 0;
%>
<div class="<%=teaser.getStyle()%>">
	<div class="<%=teaser.getType()%>">
		<div class="title"><%=teaser.getTitle()%></div>
		<div class="subtitle"><%=teaser.getSubtitle()%></div>
		<div class="text">
			<div class="image"><img src="<%=imageURL%>" alt="<%=teaser.getLabel()%>" /></div>			
			<div class="description"><%=teaser.getDescription()%></div><%
			if(isLink) {%>
				<div class="url"><a href="<%=URLHelper.createURL( ctx, eventPath )%>"><%=teaser.getLinkInfoTitle()%></a></div><%
			}%>
		</div>
	</div>
</div>
