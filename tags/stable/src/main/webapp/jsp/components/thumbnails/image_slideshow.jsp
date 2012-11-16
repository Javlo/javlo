<%@page contentType="text/html"
        import="
        java.util.Arrays,
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

Arrays.sort(selection);

final int imageCount = configThumb.getThumbnailsCount();

boolean withThumb = request.getParameter("thumb") != null;

%>
<script type="text/javascript">
var images<%=comp.getId()%>;
var pos<%=comp.getId()%>=0;
var latestPos<%=comp.getId()%>=0;
var pause<%=comp.getId()%> = false;
var blockRefresh = false;
/*init side bar*/
var slideSelectable;
function init<%=comp.getId()%>() {
	slideSelectable = $ES('.slideshow .slide-image');
	pictureInView = slideSelectable[0];
	for (var i = 0; i < slideSelectable.length; i++) {
		slideSelectable[i].num = i;		
		slideSelectable[i].addEvent('click', function(){		 		    
			pos<%=comp.getId()%> = this.num;
			blockRefresh=false;
			refreshImage<%=comp.getId()%>();
			blockRefresh=true; /* block next refresh */		
		});
	}
	images<%=comp.getId()%> = $ES('.slideshow img');	
	/*for (var i = 0; i < images<%=comp.getId()%>.length; i++) {
		images<%=comp.getId()%>[i].loaded = false;
		images<%=comp.getId()%>.addEvent('load', function(){		 		    
			this.loaded=true;
		});
	}*/

}
function setPause<%=comp.getId()%>(pause) {
	var images = $$(".slideshow<%=comp.getId()%> .command img");	
	if (pause) {
		pause<%=comp.getId()%> = true;
		images[1].setStyles('');
		images[2].setStyles('display: none;');
	} else {
		pause<%=comp.getId()%> = false;
		images[2].setStyles('');
		images[1].setStyles('display: none;');
	}		
}
function setNavigate<%=comp.getId()%>(next) {
	var images = $$(".slideshow<%=comp.getId()%> .command img");	
	if (next) {
		pos<%=comp.getId()%>++;
	} else {
		pos<%=comp.getId()%>--;		
	}
	blockRefresh=false;
	refreshImage<%=comp.getId()%>();
	blockRefresh=true; /* block next refresh */
}
function refreshImage<%=comp.getId()%>() {
    if (!blockRefresh) {
		pictureInView = slideSelectable[ pos<%=comp.getId()%> ];   		
		var images = $$(".slideshow<%=comp.getId()%> .slideshow-image");
		if (pos<%=comp.getId()%> >= images.length) {
		   pos<%=comp.getId()%>=0;
		} else if (pos<%=comp.getId()%> < 0) {
		   pos<%=comp.getId()%>=images.length-1;
		}
		var itemToView;
		var itemToHide;
		
		for (var i=0; i<images.length; i++) {
			if (i==pos<%=comp.getId()%>) {			
				//images[i].setStyles("");
				itemToHide = images[i];
			} else if (i==latestPos<%=comp.getId()%>) {
				//images[i].setStyles('display: none;');
				itemToView = images[i];
			}
		}
		switchVisibility ( itemToView, itemToHide);	
		latestPos<%=comp.getId()%>=pos<%=comp.getId()%>;
	}
	blockRefresh=false;
}
function changeImage<%=comp.getId()%>() {
	if (!pause<%=comp.getId()%>) {
	    if (pos<%=comp.getId()%> < images<%=comp.getId()%>.length) {	    
			if (images<%=comp.getId()%>[pos<%=comp.getId()%>+1].getCoordinates().width > 0) {
				pos<%=comp.getId()%>++;
				refreshImage<%=comp.getId()%>();
			}
		} else {		
			pos<%=comp.getId()%>++;
			refreshImage<%=comp.getId()%>();			
		}
	}
}
var timer<%=comp.getId()%> = changeImage<%=comp.getId()%>.periodical(5000);
<!--
<%if (withThumb) {%>animSlideToCurrentImage.periodical(100);<%}%>
init<%=comp.getId()%>.delay(1000);
//-->
</script>
<div class="slideshow slideshow<%=comp.getId()%>">
<%if (withThumb) {%><div class="thumbnails-slide">
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
			
			%><div class="slide-image">
					<img src="<%=img%>" alt="<%=info.getDescription(ctx).trim()%>" title="<%=info.getDescription(ctx).trim()%>"/>				
			</div>
			<%
		}%>
<div class="content_clear"><span></span></div>
</div>
</div><%}

String style="";
for (int i=0; i<selection.length; i++) {		
		String img = URLHelper.createTransformURL(ctx, (staticConfig.getGalleryFolder()+comp.getSelectDir() + '/' + selection[i]), "slideshow");

		ImageConfig imageConfig = ImageConfig.getInstance(globalContext, request.getSession(), template);
		int width = imageConfig.getWidth(ctx.getDevice(), configThumb.getThumbnailsFilter());
		
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
		}%>
		<div <%=style%> class="slideshow-image">
			<img src="<%=img%>" alt="<%=info.getDescription(ctx).trim()%>" title="<%=info.getDescription(ctx).trim()%>" />
			<span class="label"><%=XHTMLHelper.autoLink(info.getDescription(ctx).trim())%></span>
		</div>
		<%style="style=\"display: none;\"";
}%>
<div class="command">
<%=XHTMLHelper.getIconesCode(ctx, "actions/previous.png", "previous", "setNavigate"+comp.getId()+"(false)")%>
<%=XHTMLHelper.getIconesCode(ctx, "actions/play.png", "play", "setPause"+comp.getId()+"(false)", "display: none")%>
<%=XHTMLHelper.getIconesCode(ctx, "actions/pause.png", "pause", "setPause"+comp.getId()+"(true)")%>
<%=XHTMLHelper.getIconesCode(ctx, "actions/next.png", "next", "setNavigate"+comp.getId()+"(true)")%>
</div>
</div>
<div class="content_clear">&nbsp;</div>
