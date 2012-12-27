<%@page contentType="text/html"
        import="
        org.javlo.ContentManager,
        org.javlo.ContentContext,
        org.javlo.EditContext,
        org.javlo.helper.URLHelper,
        org.javlo.helper.XHTMLHelper,
        org.javlo.component.AbstractVisualComponent,
        org.javlo.component.ThumbnailsComponent,
        org.javlo.image.ThumbnailsConfig,org.javlo.template.Template, 
        org.javlo.image.ImageConfig,
        org.javlo.config.GlobalContext,
        org.javlo.config.StaticConfig,
        org.javlo.ztatic.StaticInfo,
        org.javlo.Content,
        org.javlo.navigation.PageConfiguration,
        org.javlo.MenuElement"
%><%
ThumbnailsComponent comp = (ThumbnailsComponent)AbstractVisualComponent.getRequestComponent ( request );
ContentContext ctx = ContentContext.getContentContext ( request, response );
MenuElement currentPage = ctx.getCurrentPage();
StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
ThumbnailsConfig configThumb = ThumbnailsConfig.getInstance(request.getSession().getServletContext());
GlobalContext globalContext = GlobalContext.getInstance(request);
Template template = PageConfiguration.getInstance(globalContext).getCurrentTemplate(ctx, currentPage);
String[] selection = comp.getSelectionArray(ctx);

final int thumbnailsCount = comp.getThumbnailsCount(ctx);

int i = thumbnailsCount*comp.getCurrentPage(ctx);
String sep="";
if ( comp.getSelectDir().length() > 1 ) {
	sep="/";
}
%>

<div <%=comp.getSpecialPreviewCssClass(ctx, "thumbnails")+comp.getSpecialPreviewCssId(ctx)%>>
	<div class="command"><%
		String pgdnURL = URLHelper.createViewURL(ctx.getPath()+"?"+comp.getImageIdInputName()+'='+i,ctx);
		if ( comp.getCurrentPage(ctx) > 0 ) {
		%><div class="element">
			<a href="<%=URLHelper.createURL(ctx, ctx.getPath()+'?'+comp.getPageInputName()+'='+(comp.getCurrentPage(ctx)-1))%>">
			&lt;&lt;
			</a>
		</div><%
		} else {%><div class="element">&nbsp;</div><%}%>
		<div class="element"><%=comp.getCurrentPage(ctx)+1%>/<%=comp.getNumberOfPage(ctx)%></div><%
		if ( (comp.getCurrentPage(ctx)+1)*thumbnailsCount < selection.length ) {%>
		<div class="element">
			<a href="<%=URLHelper.createURL(ctx, ctx.getPath()+'?'+comp.getPageInputName()+'='+(comp.getCurrentPage(ctx)+1))%>">
			&gt;&gt;
			</a>		
		</div><%
		} else {%><div class="element">&nbsp;</div><%}%>

	</div><div class="images">
<%
	boolean descriptionFound = false;
	for (int y = 0; y < thumbnailsCount; y++) {
	if (i < selection.length) {
			String img = URLHelper.createTransformURL(ctx, comp.getPage(), (staticConfig.getGalleryFolder()+comp.getSelectDir() + '/' + selection[i]), configThumb.getThumbnailsFilter());
		
				ImageConfig imageConfig = ImageConfig.getInstance(globalContext, request.getSession(), template);
				int width = imageConfig.getWidth(ctx.getDevice(), configThumb.getThumbnailsFilter());
				
				String fullRessourceURL = staticConfig.getGalleryFolder()+comp.getSelectDir() + '/' + selection[i];
				String fileURL = URLHelper.createRessourceURL( ctx, comp.getPage(), fullRessourceURL );
				
				StaticInfo info = StaticInfo.getInstance(ctx, fullRessourceURL);
				
				String imgLink;
				if (configThumb.isPreview()) {
					imgLink=URLHelper.createURL(ctx, ctx.getPath()+"?"+comp.getImageIdInputName()+'='+i+'&'+comp.getPageInputName()+'='+comp.getCurrentPage(ctx));
				} else {					
					if (configThumb.getViewFilter() == null) {
						imgLink=fileURL;
					} else {
						imgLink=URLHelper.createTransformURL(ctx, comp.getPage(), (fullRessourceURL), configThumb.getViewFilter());
					}
				}
%>
				<div class="thumb-image">
					<div class="visual" style="width: <%=width+5%>px; height:<%=width+5%>px;">
					<a href="<%=imgLink%>" rel="shadowbox[<%=comp.getId()%>]" title="<%=info.getFullDescription(ctx).trim()%>" lang="<%=ctx.getRequestContentLanguage()%>">
					<img src="<%=img%>" alt="<%=info.getFullDescription(ctx).trim()%>" title="<%=info.getFullDescription(ctx).trim()%>"/>
					</a>
					</div><%
					if (descriptionFound||info.getDescription(ctx).trim().length() > 0) {
						descriptionFound=true;%>
					<div class="description" style="width: <%=width%>px;"><%=info.getDescription(ctx)%></div><%
					} else {%>
					<div class="no-description" style="width: <%=width%>px; margin-left: auto; margin-right: auto;">&nbsp;</div><%
					}%>
				</div><%
			} else {%>
				&nbsp;
			<%
			}
			i++;
		}%></div>
<div class="content_clear">&nbsp;</div>&nbsp;</div>
