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
<script type="text/javascript">
<!--
animSlide.periodical(100);
//-->
</script>
<div class="thumbnails-slide">
<div class="images">
<%
	for (int i = 0; i < selection.length; i++) {
			String imgClass="";
			String img = URLHelper.createTransformURL(ctx, (staticConfig.getGalleryFolder()+comp.getSelectDir() + '/' + selection[i]), configThumb.getViewSlide());

			String fullRessourceURL = staticConfig.getGalleryFolder()+comp.getSelectDir() + '/' + selection[i];
			String fileURL = URLHelper.createRessourceURL( ctx, fullRessourceURL );
			
			StaticInfo info = StaticInfo.getInstance(ctx, fullRessourceURL);
			
			String imgLink;
			if (configThumb.isPreview()) {
				imgLink=URLHelper.createURL(ctx, ctx.getPath()+"?"+comp.getImageIdInputName()+'='+i+'&'+comp.getPageInputName()+'='+comp.getPage());
			} else {					
				if (configThumb.getViewFilter() == null) {
					imgLink=fileURL;
				} else {
					imgLink=URLHelper.createTransformURL(ctx, (fullRessourceURL), configThumb.getViewFilter());
				}
			}					
			String title = info.getDescription(ctx).trim();
			if (title.startsWith("/")) title = "";

			%><div class="slide-image">
				<a href="<%=imgLink%>" rel="lightbox[<%=comp.getSelectDir()%>]" title="<%= title %>">				
					<img src="<%=img%>" alt="<%= title %>" title="<%= title %>"/>				
				</a>
			</div>
			<%
		}%>
<div class="content_clear"><span></span></div>
</div>
</div>
