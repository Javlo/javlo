<%@page contentType="text/html"
        import="
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
String sep="";
if ( comp.getSelectDir().length() > 1 ) {
	sep="/";
}
%>
<div class="thumbnails-board">
<div class="images">
<%
	for (int i = 0; i < selection.length; i++) {
			String imgClass="";
			String img = URLHelper.createTransformURL(ctx, (staticConfig.getGalleryFolder()+comp.getSelectDir() + '/' + selection[i]), configThumb.getThumbnailsFilter());
			String imgOff = URLHelper.createTransformURL(ctx, (staticConfig.getGalleryFolder()+comp.getSelectDir() + '/' + selection[i]), configThumb.getThumbnailsFilter());
			if (configThumb.getViewOnFilter() != null) {
				imgClass = " class=\"roll_image\"";
				img = URLHelper.createTransformURL(ctx, (staticConfig.getGalleryFolder()+comp.getSelectDir() + '/' + selection[i]), configThumb.getViewOnFilter());
				imgOff = URLHelper.createTransformURL(ctx, (staticConfig.getGalleryFolder()+comp.getSelectDir() + '/' + selection[i]), configThumb.getViewOffFilter());
			}

			ImageConfig imageConfig = ImageConfig.getInstance(globalContext, request.getSession(), template);
			int width = imageConfig.getWidth(ctx.getDevice(), configThumb.getThumbnailsFilter(),null);
			
			String fullRessourceURL = staticConfig.getGalleryFolder()+comp.getSelectDir() + '/' + selection[i];
			
			StaticInfo info = StaticInfo.getInstance(ctx, fullRessourceURL);
			
			String imgLink;
			if (configThumb.isPreview()) {
				imgLink=URLHelper.createURL(ctx, ctx.getPath()+"?"+comp.getImageIdInputName()+'='+i+'&'+comp.getPageInputName()+'='+comp.getPage());
			} else {					
				if (configThumb.getViewFilter() == null) {
					imgLink = URLHelper.createRessourceURL( ctx, fullRessourceURL );
				} else {
					imgLink=URLHelper.createTransformURL(ctx, (fullRessourceURL), configThumb.getViewFilter());
				}
			}					
			String title = info.getFullDescription(ctx).trim();
			if (title.startsWith("/")) title = "";
			
			%><div class="board-thumb-image">
				<div class="background-image" style="background: url('<%=img%>') no-repeat;">
				<a href="<%=imgLink%>" rel="lightbox[<%=comp.getSelectDir()%>]" title="<%= title %>">				
					<img<%=imgClass%> src="<%=imgOff%>" alt="<%= title %>" title="<%= title %>"/>				
				</a>
				</div>
			</div>
			<%
		}%></div>
<div class="content_clear"><span></span></div></div>
