<%@page contentType="text/html"
        import="
        org.javlo.ContentManager,
        org.javlo.ContentContext,
        org.javlo.EditContext,
        org.javlo.config.GlobalContext,
        org.javlo.config.StaticConfig,
        org.javlo.helper.URLHelper,
        org.javlo.helper.XHTMLHelper,
        org.javlo.component.AbstractVisualComponent,
        org.javlo.component.ThumbnailsComponent,
        org.javlo.image.ThumbnailsConfig" 
%><%
GlobalContext globalContext = GlobalContext.getInstance(request);
EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
ThumbnailsConfig configThumb = ThumbnailsConfig.getInstance(request.getSession().getServletContext());
StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());

ThumbnailsComponent comp = (ThumbnailsComponent)AbstractVisualComponent.getRequestComponent ( request );
ContentContext ctx = ContentContext.getContentContext ( request, response );
String[] selection = comp.getSelectionArray(ctx);
final int imageCount = configThumb.getThumbnailsCount();
int i = imageCount*comp.getCurrentPage(ctx);
int imageId = comp.getCurrentImageId(ctx);
String previousImage = comp.getPreviousImage(ctx);
String nextImage = comp.getNextImage(ctx);
String imageName = comp.getCurrentImage(ctx);
String img = URLHelper.createTransformURL(ctx, (staticConfig.getGalleryFolder()+comp.getSelectDir() + '/' + imageName).substring(1), configThumb.getPreviewFilter());
String sep="";
if ( comp.getSelectDir().length() > 1 ) {
	sep="/";
}
String bigImgURL = URLHelper.createStaticURL( ctx, staticConfig.getGalleryFolder()+comp.getSelectDir()+sep+comp.getCurrentImage(ctx) );
String backURL = URLHelper.createURL(ctx, ctx.getPath()+"?"+comp.getPageInputName()+'='+comp.getPage());%>
<center class="thumbnails">
	<table class="command"><tr>
		<td style="width: 33%;text-align: center;"><%
			if ( previousImage == null ) {
				%>&nbsp;<%
			} else {%>			
			<a href="<%=URLHelper.createURL(ctx, ctx.getPath()+'?'+comp.getPageInputName()+'='+(comp.getPage())+'&'+comp.getImageIdInputName()+'='+(imageId-1))%>">
			&lt;&lt;
			</a>
			<%}%>
		</td>
		<td style="width: 34%;text-align: center;">
				<a href="<%=URLHelper.createURL(ctx, ctx.getPath()+'?'+comp.getPageInputName()+'='+(comp.getPage()))%>">
				^
				</a>
		<br/></td>
		<td style="width: 33%;text-align: center;"><%
			if ( nextImage == null ) {
				%>&nbsp;<%
			} else {%>			
			<a href="<%=URLHelper.createURL(ctx, ctx.getPath()+'?'+comp.getPageInputName()+'='+(comp.getPage())+'&'+comp.getImageIdInputName()+'='+(imageId+1))%>">
			&gt;&gt;
			</a>
			<%}%>
		</td>
	</tr></table><center>
	<div class="preview" style="width: <%=comp.IMAGE_WIDTH+10%>px;">
	<a href="<%=bigImgURL%>">
		<img src="<%=img%>" alt="<%=imageName%>"/>
	</a>
	</div>
	</center>
</center>