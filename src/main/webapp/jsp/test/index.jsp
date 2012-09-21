<%@page contentType="text/html" pageEncoding="UTF-8"
	import="java.util.Date,
		org.javlo.helper.URLHelper,
		org.javlo.context.ContentContext,
		org.javlo.service.ContentService,
		org.javlo.data.InfoBean,
		org.javlo.i18n.I18nAccess,
		org.javlo.helper.StringHelper,
		org.javlo.user.User,
		org.javlo.context.EditContext,
		org.javlo.helper.XHTMLHelper,
		org.javlo.navigation.PageConfiguration,
		org.javlo.message.MessageRepository,
		org.javlo.user.AdminUserSecurity,
		org.javlo.navigation.MenuElement,
		org.javlo.helper.XHTMLNavigationHelper,
		org.javlo.context.GlobalContext"
%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
ContentContext ctx = ContentContext.getContentContext(request, response);
ContentService content = ContentService.createContent(request);
GlobalContext globalContext = GlobalContext.getInstance(request); 
MenuElement currentPage = ctx.getCurrentPage();
InfoBean infoBean = InfoBean.getCurrentInfoBean(request);
String currentTitle = currentPage.getContent(ctx).getPageTitle();
String pageName = currentPage.getName();

String globalTitle = currentPage.getGlobalTitle(ctx);if (globalTitle == null) {	globalTitle = globalContext.getGlobalTitle();}
I18nAccess i18nAccess = I18nAccess.getInstance(request);
AdminUserSecurity security = AdminUserSecurity.getInstance();
%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %><!doctype html>
<html lang="en">

<head><script type="text/javascript">
<!--
var sLanguage = '<%=ctx.getRequestContentLanguage()%>';
var server = '<%=URLHelper.createStaticURL(ctx, "/")%>';
-->
</script><%if (currentPage.getKeywords(ctx).length()>0){%>
<meta name="keywords" content="<%=currentPage.getKeywords(ctx)%>" />
<%}%><%if (currentPage.getMetaDescription(ctx).length()>0){%><meta name="description" content="<%=currentPage.getMetaDescription(ctx)%>" />
<%}
%><%=XHTMLNavigationHelper.getRSSHeader(ctx, currentPage)%>
<link rel="stylesheet" type="text/css" href="<%=URLHelper.createStaticURL(ctx,"/jsp/view/components_css.jsp")%>" />
<link rel="shortcut icon" type="image/ico" href="<%=URLHelper.createStaticURL(ctx,"/favicon.ico")%>" />
<%if (ctx.isInteractiveMode()) {%>
<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, "/js/lib/jquery-1.7.2.min.js")%>
<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, "/js/lib/jquery-ui-1.8.20.custom.min.js")%>
<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, "/js/lib/jquery.colorbox-min.js")%>
<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, "/css/lib/colorbox/colorbox.css")%>
<%}%><%for (String uri : currentPage.getExternalResources(ctx)) {%>
<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, uri)%><%}%>
	<meta charset="utf-8" />
	<title>The representative organisation of persons with disabilities in Europe - <%=currentTitle%></title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0" />	
	<!-- Grid styles for IE -->
	<!--[if lt IE 9]><%if (!XHTMLHelper.allReadyInsered(ctx, "css/ie.css")) {%><link rel="stylesheet" type="text/css" media="all" href="<%=URLHelper.createStaticTemplateURL(ctx,"/css/ie.css", "134678298573296807310")%>" /><%}%><![endif]-->
	<%if (!XHTMLHelper.allReadyInsered(ctx, "css/reset.css")) {%><link rel="stylesheet" type="text/css" media="all" href="<%=URLHelper.createStaticTemplateURL(ctx,"/css/reset.css", "134678298573296807310")%>" /><%}%>
	<%if (!XHTMLHelper.allReadyInsered(ctx, "style.css")) {%><link rel="stylesheet" type="text/css" media="screen" href="<%=URLHelper.createStaticTemplateURL(ctx,"/style.css", "134678298573296807310")%>" /><%}%>    
    <%if (!XHTMLHelper.allReadyInsered(ctx, "css/prettyPhoto.css")) {%><link rel="stylesheet" type="text/css" media="screen" href="<%=URLHelper.createStaticTemplateURL(ctx,"/css/prettyPhoto.css", "134678298573296807310")%>" /><%}%>
	<%if (!XHTMLHelper.allReadyInsered(ctx, "css/slider.css")) {%><link rel="stylesheet" type="text/css" media="all" href="<%=URLHelper.createStaticTemplateURL(ctx,"/css/slider.css", "134678298573296807310")%>" /><%}%>
	<%if (!XHTMLHelper.allReadyInsered(ctx, "css/flexslider.css")) {%><link rel="stylesheet" type="text/css" media="screen" href="<%=URLHelper.createStaticTemplateURL(ctx,"/css/flexslider.css", "134678298573296807310")%>" /><%}%>    
    <%if (!XHTMLHelper.allReadyInsered(ctx, "css/responsive.css")) {%><link rel="stylesheet" type="text/css" media="all" href="<%=URLHelper.createStaticTemplateURL(ctx,"/css/responsive.css", "134678298573296807310")%>" /><%}%>
    
    <!-- Favicons
	================================================== -->
	<%if (!XHTMLHelper.allReadyInsered(ctx, "images/favicon.ico")) {%><link rel="shortcut icon" href="<%=URLHelper.createStaticTemplateURL(ctx,"/images/favicon.ico", "134678298573296807310")%>" /><%}%>
	<%if (!XHTMLHelper.allReadyInsered(ctx, "images/apple-touch-icon.png")) {%><link rel="apple-touch-icon" href="<%=URLHelper.createStaticTemplateURL(ctx,"/images/apple-touch-icon.png", "134678298573296807310")%>" /><%}%>
	<%if (!XHTMLHelper.allReadyInsered(ctx, "images/apple-touch-icon-72x72.png")) {%><link sizes="72x72" rel="apple-touch-icon" href="<%=URLHelper.createStaticTemplateURL(ctx,"/images/apple-touch-icon-72x72.png", "134678298573296807310")%>" /><%}%> 
	<%if (!XHTMLHelper.allReadyInsered(ctx, "images/apple-touch-icon-114x114.png")) {%><link sizes="114x114" rel="apple-touch-icon" href="<%=URLHelper.createStaticTemplateURL(ctx,"/images/apple-touch-icon-114x114.png", "134678298573296807310")%>" /><%}%>
    
    <!-- script
	================================================== -->
    <%if (!XHTMLHelper.allReadyInsered(ctx, "js/jquery-1.7.1.min.js")) {%><script src="<%=URLHelper.createStaticTemplateURL(ctx,"/js/jquery-1.7.1.min.js")%>" type="text/javascript"><%}%></script>
    <%if (!XHTMLHelper.allReadyInsered(ctx, "js/jquery.prettyPhoto.js")) {%><script src="<%=URLHelper.createStaticTemplateURL(ctx,"/js/jquery.prettyPhoto.js")%>" type="text/javascript"><%}%></script>
    <%if (!XHTMLHelper.allReadyInsered(ctx, "js/jflickrfeed.min.js")) {%><script src="<%=URLHelper.createStaticTemplateURL(ctx,"/js/jflickrfeed.min.js")%>" type="text/javascript"><%}%></script>
    <%if (!XHTMLHelper.allReadyInsered(ctx, "js/jquery.cslider.js")) {%><script src="<%=URLHelper.createStaticTemplateURL(ctx,"/js/jquery.cslider.js")%>" type="text/javascript"><%}%></script>
    <%if (!XHTMLHelper.allReadyInsered(ctx, "js/modernizr.custom.28468.js")) {%><script src="<%=URLHelper.createStaticTemplateURL(ctx,"/js/modernizr.custom.28468.js")%>" type="text/javascript"><%}%></script>	
    <%if (!XHTMLHelper.allReadyInsered(ctx, "js/jquery.easing.1.3.js")) {%><script src="<%=URLHelper.createStaticTemplateURL(ctx,"/js/jquery.easing.1.3.js")%>" type="text/javascript"><%}%></script>
    <%if (!XHTMLHelper.allReadyInsered(ctx, "js/jquery.carouFredSel-5.6.2.js")) {%><script src="<%=URLHelper.createStaticTemplateURL(ctx,"/js/jquery.carouFredSel-5.6.2.js")%>" type="text/javascript"><%}%></script>
	<%if (!XHTMLHelper.allReadyInsered(ctx, "js/jquery.flexslider.js")) {%><script src="<%=URLHelper.createStaticTemplateURL(ctx,"/js/jquery.flexslider.js")%>" type="text/javascript"><%}%></script>
    <%if (!XHTMLHelper.allReadyInsered(ctx, "js/custom.js")) {%><script src="<%=URLHelper.createStaticTemplateURL(ctx,"/js/custom.js")%>" type="text/javascript"><%}%></script>
	<%if (!XHTMLHelper.allReadyInsered(ctx, "js/css3-mediaqueries.js")) {%><script src="<%=URLHelper.createStaticTemplateURL(ctx,"/js/css3-mediaqueries.js")%>" type="text/javascript"><%}%></script>
	<!--[if lt IE 9]>
		<script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
	<![endif]--><%if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());%>
<%=(ctx.isInteractiveMode() ? "<link rel=\"stylesheet\" type=\"text/css\" href=\""+URLHelper.createStaticURL(ctx,"/css/preview/edit_preview.css")+"\"></link>" : "")  %>
<%=(ctx.isInteractiveMode() ? "<link rel=\"stylesheet\" type=\"text/css\" href=\""+editCtx.getEditTemplateFolder()+"/css/edit_preview.css"+"\"></link>" : "")  %>
<%=(ctx.isInteractiveMode() ? "<script type=\"text/javascript\" src=\""+URLHelper.createStaticURL(ctx,"/js/preview/edit_preview.js")+"\"></script>" : "")  %>
<%=(ctx.isInteractiveMode() ? "<script type=\"text/javascript\" src=\""+URLHelper.createStaticURL(ctx,"/js/edit/ajax.js")+"\"></script>" : "")  %>
<%=(ctx.isInteractiveMode() ? "<script type=\"text/javascript\" src=\""+URLHelper.createStaticURL(ctx,"/js/edit/core.js")+"\"></script>" : "")  %>
<%if ((ctx.isInteractiveMode())&&(security.haveRight((User)editCtx.getUserPrincipal(), "update"))) {%><script type="text/javascript">
var ajaxURL = "<%=URLHelper.createAjaxURL(ctx)%>";
var currentURL = "<%=URLHelper.createURL(ctx)%>";
var editPreviewURL = "<%=URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE))%>?module=content&webaction=editPreview&previewEdit=true";
</script><%}%><%}%>
<%if (currentPage.getHeaderContent(ctx) != null) {%><%=currentPage.getHeaderContent(ctx)%><%}%>
<!-- template plugins -->
<%if (!XHTMLHelper.allReadyInsered(ctx,"jquery.js")) { %><script src="<%=URLHelper.createStaticTemplatePluginURL(ctx, "http://ajax.googleapis.com/ajax/libs/jquery/1/jquery.js", "plugins/galleria__1_2_7")%>"></script><%} else {%><!-- resource allready insered: jquery.js --><%}%>
<%if (!XHTMLHelper.allReadyInsered(ctx,"galleria-1.2.7.min.js")) { %><script src="<%=URLHelper.createStaticTemplatePluginURL(ctx, "galleria-1.2.7.min.js", "plugins/galleria__1_2_7")%>"></script><%} else {%><!-- resource allready insered: galleria-1.2.7.min.js --><%}%>
<style type="text/css">.thumbnails { height: 450px; }</style>
<script>Galleria.loadTheme('<%=URLHelper.createStaticTemplatePluginURL(ctx, "/", "plugins/galleria__1_2_7")%>themes/classic/galleria.classic.min.js'); Galleria.run('.thumbnails');</script>
<!-- end template plugins -->
</head>
<body id="sub-page"><%if (ctx.isInteractiveMode() && ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {%>
<jsp:include page="/jsp/preview/command.jsp" />
<%MessageRepository messageRepository = MessageRepository.getInstance(ctx);
   if (messageRepository.getGlobalMessage().getMessage().trim().length() > 0) {%>
	    <div id="pc_message" class="standard">
		<%=XHTMLHelper.getIconesCode(ctx, "close.gif", "close", "hidePreviewMessage();")%>
       <div class="<%=messageRepository.getGlobalMessage().getTypeLabel()%>"><%=messageRepository.getGlobalMessage().getMessage()%></div>
</div><%}%>
<%}%>
<%if (ctx.isInteractiveMode() && ctx.getRenderMode() == ContentContext.TIME_MODE) {%>
<jsp:include page="/jsp/time-traveler/command.jsp" />
<%MessageRepository messageRepository = MessageRepository.getInstance(ctx);
   if (messageRepository.getGlobalMessage().getMessage().trim().length() > 0) {%>
	    <div id="pc_message" class="standard">
		<%=XHTMLHelper.getIconesCode(ctx, "close.gif", "close", "hidePreviewMessage();")%>
       <div class="<%=messageRepository.getGlobalMessage().getTypeLabel()%>"><%=messageRepository.getGlobalMessage().getMessage()%></div>
</div><%}%>
<%}%>
<div style="position: absolute; top: -100px;" id="jv_escape_menu">
<ul><li><a href="#main-menu"><%=i18nAccess.getViewText("wai.to_content")%></a></li></ul>
</div>
<%if (currentPage.getImage(ctx) != null && globalContext.isFirstImage()) {
%><img id="_first-image" src="<%=URLHelper.createTransformURL(ctx, currentPage.getImage(ctx).getImageURL(ctx), "standard")%>" alt="<%=currentPage.getImage(ctx).getImageDescription(ctx)%>" /><%}%>

<header id="header">
	<div id="header-outer">
		<div class="wrapper clearfix">
			<div id="logo-image">
				<a href="<%=URLHelper.createURLCheckLg(ctx,"index.html")%>"><img alt="logo" src="<%=URLHelper.createStaticTemplateURL(ctx,"/placeholders/logo-european-disability-fo.gif")%>" /></a>
			</div><!--end:logo-image-->		       
			<%if ( XHTMLNavigationHelper.menuExist(ctx,1) ) {%><nav id="main-menu" class="clearfix"><jsp:include page="/jsp/view/content_view.jsp?area=content" /></nav><!--end:main-menu-->
		</div><!--end:wrapper-->
	</div><!--header-outer-->
</header>
<div id="main-content">
	<div id="bottom-bg">	
		<div class="outter">
			<div class="wrapper">        
				<div class="container">
					<div class="three-forth">
						<section class="clearfix">
							<header id="breadcrumb"><jsp:include page="/jsp/view/content_view.jsp?area=breadcrumb" /></header>
							<div id="content" class="post-item clearfix">
								<div class="post-meta">
									<div class="entry-date">
										<p>21</p>
										<span>Mar, 2010</span>
									</div><!--end:entry-date-->
									<a class="entry-author" href="#">John Smith</a>									
									<div class="social-share clearfix">
										<div class="social-share-button clearfix">
											<!--Begin  twitter-->
											<div class="social-share-twitter">
												<script src="http://platform.twitter.com/widgets.js" type="text/javascript"></script>
												<a href="http://twitter.com/share" class="twitter-share-button">Tweet</a>
											</div><!--twitter-share-->											
											
										</div><!--social-share-button-->
									</div><!--social-share-->
								</div><!--end:post-sidebar-->
								<article class="post-content">
									<h5 class="entry-title"><a href="#">We Appreciate Any Kind of Feedback (Standard Post)</a></h5>
									<span class="entry-category">Tag:&nbsp;</span><a class="entry-category" href="#">Photography,&nbsp;</a><a class="entry-category" href="#">Wordpress,&nbsp;</a><a class="entry-category" href="#">Website</a>
									<img alt="" class="thumbnails" src="<%=URLHelper.createStaticTemplateURL(ctx,"/images/enablemap.jpg")%>" />
									<p>Raw denim you probably haven't heard of them jean shorts Austin. Nesciunt tofu stumptown aliqua, retro synth master cleanse. Mustache cliche tempor, williamsburg carles vegan helvetica. Reprehenderit butcher retro keffiyeh dreamcatcher synth. Cosby sweater eu banh mi, qui irure terry richardson ex squid. Aliquip placeat salvia cillum iphone. Seitan aliquip quis cardigan american apparel, butcher voluptate nisi qui.</p>
									 <br />
									 <blockquote>
										<span>Montes primis. Ultrices netus augue. Ridiculus 	Hac conubia quisque morbi sit suspendisse interdum class curae; magnis vehicula rhoncus consectetuer bibendum nullam ante leo. Nisi consequat vulputate sem mi. Curabitur duis risus nam quisque dictumst mauris purus mi mi blandit arcu odio felis euismod dignissim lectus rhoncus ultrices. Commodo sodales mus.</span>
									</blockquote>
									<br />
									<p>Bibendum proin risus. Feugiat duis tincidunt mus inceptos ante elementum posuere odio et ullamcorper, morbi duis. Torquent risus quisque est. Vulputate nec conubia vel massa.</p>
									<p>Montes primis. Ultrices netus augue. Ridiculus Hac conubia quisque morbi sit suspendisse interdum class curae; magnis vehicula rhoncus consectetuer bibendum nullam ante leo. Nisi consequat vulputate sem mi. Curabitur duis risus nam quisque dictumst mauris purus mi mi blandit arcu odio felis euismod dignissim lectus rhoncus ultrices. Commodo sodales mus.</p>
									<p>Interdum dictumst cras per sit nibh rutrum. Gravida interdum, iaculis luctus rhoncus praesent rutrum platea justo montes blandit porttitor consectetuer libero sociosqu tempus ac. Etiam ornare malesuada donec gravida id torquent Dolor aliquam pede, commodo ligula taciti commodo congue, ultrices. Conubia aliquam euismod leo parturient proin.</p>
								
								</article><!--end:post-content-->
								<div class="clear"></div>
							</div><!--end:post-item-->                  
						</section>
						
						
						
					</div><!--end:three-forth-->
					<div id="right-sidebar" class="one-forth last"><jsp:include page="/jsp/view/content_view.jsp?area=sidebar" /></div><!--end:right-sidebar-->
					<div class="clear"></div>
				</div><!--end:container--> 
			</div><!--end:wrapper-->
		</div><!--end:outter-->
	</div><!--bottom-bg-->
</div><!--end:main-content-->     
<div id="page-botom">
	<div class="outter">
		<div class="wrapper">
			<div id="footer-sidebar">
				<div class="one-forth">
					<aside class="widget">
						<h3 class="widget-title">About</h3>
						<div class="text-widget">
							<p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud</p>
							<ul class="social-links clearfix">
								<li class="follow-us">Follow Us</li>
								<li class="gplus-icon">
									<a target="_blank" title="Google+" href="#">
										<img alt="" src="<%=URLHelper.createStaticTemplateURL(ctx,"/images/icons/gplus-icon.png")%>" />                        
									</a>
								</li>
								<li class="facebook-icon">
									<a target="_blank" title="Facebook" class="facebook" href="#">
										<img alt="" src="<%=URLHelper.createStaticTemplateURL(ctx,"/images/icons/facebook-icon.png")%>" />
									</a>
								</li>
								<li class="twitter-icon">
									<a target="_blank" title="Twitter" class="twitter" href="#">
										<img alt="" src="<%=URLHelper.createStaticTemplateURL(ctx,"/images/icons/twitter-icon.png")%>" />                        
									</a>
								</li>
								<li class="rss-icon">
									<a target="_blank" title="RSS" href="#">
										<img alt="" src="<%=URLHelper.createStaticTemplateURL(ctx,"/images/icons/rss-icon.png")%>" />                        
									</a>
								</li>
							</ul><!--end:social-links-->
						</div><!--end:text-widget-->
					</aside><!--end:widget-->
				</div><!--one-forth-->
				<div class="one-forth">	
					<aside class="widget">
							<h3 class="widget-title">Latest News</h3>
							<ul class="latest-news">
								<li>
									<span class="entry-date">23 July, 2012</span>
									<a href="#">Who made dummy text?</a>
								</li>
								<li>
									<span class="entry-date">23 July, 2012</span>
									<a href="#">Limbaugh: Does 'Dark Knight Rises' have it</a>
								</li>
							</ul>
						</aside><!--end:widget-->
				</div><!--one-forth-->	
				<div class="one-forth">	
					<aside class=" widget">
						<h3 class="widget-title">Newsletter</h3>
						<div class="newsletter">
							<p>Enter your email address below to receive updates each time we publish</p>
							<form>
								<input type="text" value="Your email" name="email" class="email" maxlength="100" onFocus="if(this.value==this.defaultValue)this.value='';" onBlur="if(this.value=='')this.value=this.defaultValue;" />
								<input type="submit" value="Submit" class="submit-newsletter" />
							</form>    
						</div><!--end:newsletter-->
					</aside><!--end:widget-->
				</div><!--one-forth-->
				<div class="one-forth last">	
					<aside class="widget">
						<h3 class="widget-title">Latest Tweets</h3>
						<!-- begin [Tweets List] -->
							<ul id="twitter_update_list" class="twitter-ul"><li></li></ul>
							<script  src="http://twitter.com/javascripts/blogger.js" type="text/javascript"></script>
							<script src="http://twitter.com/statuses/user_timeline/envato.json?callback=twitterCallback2&amp;count=1" type="text/javascript"></script>
						<!-- end [Tweets List] -->
					</aside><!--end:widget-->
				</div><!--one-forth-->
				<div class="clear"></div>        	
			</div><!--end:footer-sidebar-->
		</div><!--end:wrapper-->
	</div><!--end:outter-->
	<footer id="footer" class="wrapper clearfix">
		<p id="copyright">Copyright &copy; 2012 KopaTheme. All Rights Reserved</p>            
		<a id="scroll-to-top" href="#top">Back to top</a>
	</footer>
</div><!--page-bottom-->
<script type="text/javascript">
var _gaq = _gaq || [];
_gaq.push(['_setAccount', '<%=globalContext.getGoogleAnalyticsUACCT()%>']);
_gaq.push(['_trackPageview']);
   (function() {
      var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
      ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
      var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
   })();
</script>
</body>
</html>