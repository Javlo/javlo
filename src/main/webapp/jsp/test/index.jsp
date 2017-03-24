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
		org.javlo.message.MessageRepository,
		org.javlo.user.AdminUserSecurity,
		org.javlo.navigation.MenuElement,
		org.javlo.helper.XHTMLNavigationHelper,
		org.javlo.context.GlobalContext"
%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
ContentContext ctx = ContentContext.getContentContext(request, response);
ContentService content = ContentService.getInstance(request);
GlobalContext globalContext = GlobalContext.getInstance(request); 
MenuElement currentPage = ctx.getCurrentPage();
InfoBean infoBean = InfoBean.getCurrentInfoBean(request);
String currentTitle = currentPage.getContent(ctx).getPageTitle();
String pageName = currentPage.getName();

String globalTitle = currentPage.getGlobalTitle(ctx);if (globalTitle == null) {	globalTitle = globalContext.getGlobalTitle();}
I18nAccess i18nAccess = I18nAccess.getInstance(request);
AdminUserSecurity security = AdminUserSecurity.getInstance();
%><!doctype html>
<html>
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
<meta charset="utf-8"/>
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
<meta name="viewport" content="width=device-width, initial-scale=1"/><!-- Adding "maximum-scale=1" fixes the Mobile Safari auto-zoom bug: http://filamentgroup.com/examples/iosScaleBug/ -->
<title>Europarl Galaxy - <%=currentTitle%></title>


<!--[if lt IE 9]><script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script><![endif]-->
<%if (!XHTMLHelper.allReadyInsered(ctx, "js/epbox/epbox.css")) {%><link rel="stylesheet" type="text/css" href="<%=URLHelper.createStaticTemplateURL(ctx,"/js/epbox/epbox.css", "135090227768448074493")%>" /><%}%>
<%if (!XHTMLHelper.allReadyInsered(ctx, "css/default.css")) {%><link rel="stylesheet" type="text/css" href="<%=URLHelper.createStaticTemplateURL(ctx,"/css/default.css", "135090227768448074493")%>" /><%}%><%if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE || ctx.getRenderMode() == ContentContext.TIME_MODE) {
EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());%>
<%=(ctx.isInteractiveMode() ? "<link rel=\"stylesheet\" type=\"text/css\" href=\""+URLHelper.createStaticURL(ctx,"/css/preview/edit_preview.css")+"\"></link>" : "")  %>
<%=(ctx.isInteractiveMode() ? "<link rel=\"stylesheet\" type=\"text/css\" href=\""+editCtx.getEditTemplateFolder()+"/css/edit_preview.css"+"\"></link>" : "")  %>
<%=(ctx.isInteractiveMode() ? "<script type=\"text/javascript\" src=\""+URLHelper.createStaticURL(ctx,"/js/preview/edit_preview.js")+"\"></script>" : "")  %>
<%=(ctx.isInteractiveMode() ? "<script type=\"text/javascript\" src=\""+URLHelper.createStaticURL(ctx,"/js/edit/ajax.js")+"\"></script>" : "")  %>
<%=(ctx.isInteractiveMode() ? "<script type=\"text/javascript\" src=\""+URLHelper.createStaticURL(ctx,"/js/edit/core.js")+"\"></script>" : "")  %>
<%if ((ctx.isInteractiveMode())&&(security.haveRight((User)editCtx.getUserPrincipal(), "update"))) {%><script type="text/javascript">
var ajaxURL = "<%=URLHelper.createAjaxURL(ctx)%>";
var currentURL = "<%=URLHelper.createURL(ctx)%>";
<%
String url  = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE));
if (url.contains("?")) {
	url = url + "&";
} else {
	url = url + "?";
}
%>
var editPreviewURL = <%=url%>"module=content&webaction=editPreview&previewEdit=true";
</script><%}%><%}%>
<%if (currentPage.getHeaderContent(ctx) != null) {%><%=currentPage.getHeaderContent(ctx)%><%}%>
<!-- template plugins -->
<%if (!XHTMLHelper.allReadyInsered(ctx,"css/javlo_basic.css")) { %><link charset="utf-8" rel="stylesheet" type="text/css" media="screen" href="<%=URLHelper.createStaticTemplatePluginURL(ctx, "css/javlo_basic.css", "plugins/javlo_basic_style__1_1")%>" /><%} else {%><!-- resource allready insered: css/javlo_basic.css --><%}%>
<!-- end template plugins -->
</head>
	
<body lang="${info.language}"><%if (ctx.isInteractiveMode() && ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {%>
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
<ul><li><a href="#content"><%=i18nAccess.getViewText("wai.to_content")%></a></li></ul>
</div>
<%if (currentPage.getImage(ctx) != null && globalContext.isFirstImage()) {
%><img id="_first-image" src="<%=URLHelper.createTransformURL(ctx, currentPage.getImage(ctx).getResourceURL(ctx), "standard")%>" alt="<%=currentPage.getImage(ctx).getImageDescription(ctx)%>" /><%}%>

<div class="container">
	<header>
		<div id="languages" class="languages"><jsp:include page="/jsp/view/content_view.jsp?area=languages" /></div>
		<nav>
			<div class="quicklinks">
				<ul>
				<li><a href="http://www.europarl.europa.eu/news/en/">News</a></li>
				<li><a href="http://www.europarl.europa.eu/aboutparliament/en/">About Parliament</a></li>
				<li><a href="http://www.europarl.europa.eu/meps/en/search.html">MEPs</a></li>
				<li><a href="http://www.europarl.europa.eu/plenary/en/home.html">Plenary</a></li>
				<li><a href="http://www.europarl.europa.eu/committees/en/home.html">Committees</a></li>
				<li><a href="http://www.europarl.europa.eu/delegations/en/home.html">Delegations</a></li>
				<li><a href="http://europarltv.europa.eu/en">EPTV</a></li>
				<li class="revealer">
					<a href="#quicklinks-more">More</a>
					<ul class="hidden">
					<li>European Parliament
						<ul>
						<li><a href="http://www.europarl.europa.eu/meps/en/search.html">MEPs</a></li>
						<li><a href="http://www.europarl.europa.eu/aboutparliament/en/007f2537e0/Political-groups.html">Political groups</a></li>
						<li><a href="http://www.europarl.europa.eu/sed/plenary.do?language=en">S&eacute;ance en direct</a></li>
						<li><a href="http://audiovisual.europarl.europa.eu/" rel="external">Audiovisual website</a></li>
						<li><a href="http://europa.eu/lisbon_treaty/index_en.htm" rel="external">EUROPA - Treaty of Lisbon</a></li>
						<li><a href="http://www.europarl.europa.eu/aboutparliament/en/002398d833/Sakharov-Prize-for-Freedom-of-Thought.html">Sakharov Prize</a></li>
						</ul>
					</li>
					<li>Other links
						<ul>
						<li><a href="http://europa.eu/ey2012/ey2012.jsp?langId=en" target="_blank">European Year for Active Ageing</a></li>
						<li><a href="http://europa.eu/index_en.htm" target="_blank">EUROPA</a></li>
						<li><a href="http://www.eu2012.dk/" target="_blank">eu2012.dk</a></li>
						</ul>
					</li>
					<li>A-Z
						<ul>
						<li><a href="http://www.europarl.europa.eu/portal/en/a-z">Complete website list</a></li>
						</ul>
					</li>
					</ul>
				</li>
				</ul>
			</div>
		</nav>
		<div class="logo">
			<a lang="en" href="http://www.europarl.europa.eu/portal/en" title="Go back to the Europarl portal">European Parliament</a>
               <span>/</span>
			<a title="Return to homepage" href="<%=URLHelper.createURLCheckLg(ctx,"/the-president/en/index.html")%>" lang="en">The President</a>
		</div>
		
		<form id="search-form" action="<%=URLHelper.createURL(ctx,"")%>" method="get">
		<div>
			<input name="keywords" value="<%=i18nAccess.getViewText("search.title")%>" class="keywords" onfocus="if (this.value == '<%=i18nAccess.getViewText("search.title")%>'){this.value='';}" type="text" accesskey="4" /><input type="hidden" name="webaction" value="search.search" />
			<input type="hidden" value="search.search" name="webaction" />
			<input type="submit" lang="en" value="go" class="submit" />
		</div>
        </form>
		<nav>
			<%if ( XHTMLNavigationHelper.menuExist(ctx,1) ) {%><div id="nav" class="nav"><jsp:include page="/jsp/view/content_view.jsp?area=nav" /></div>
			<%if ( XHTMLNavigationHelper.menuExist(ctx,2) ) {%><div id="subnav" class="subnav"><jsp:include page="/jsp/view/content_view.jsp?area=subnav" /></div>
		</nav>
	</header>
	<div class="highlight">
	</div>
	<div class="main">
		<div id="content" class="content"><jsp:include page="/jsp/view/content_view.jsp?area=content" /></div>
		<aside>
			<div class="side" id="contextzone"><jsp:include page="/jsp/view/content_view.jsp?area=contextzone" /></div>
		</aside>
	</div>
	<footer>
		<div class="wrapper">
			<div class="logo">
				<a lang="en" href="http://www.europarl.europa.eu/portal/en" title="Go back to the Europarl portal">European Parliament</a>
	               <span>/</span>
				<a title="Return to homepage" href="<%=URLHelper.createURLCheckLg(ctx,"/the-president/en/index.html")%>" lang="en">The President</a>
			</div>
			<div class="tools">
				<ul>
	            <li><a rel="external" href="<%=URLHelper.createURLCheckLg(ctx,"/the-president/xml/en/president_model/rss/all.xml")%>">RSS</a></li>
	            <li><a href="#print">Print</a></li>
	            <li><a href="http://www.europarl.europa.eu/portal/en/legal-notice">Legal notice</a></li>
	            <li><a href="http://www.europarl.europa.eu/portal/en/accessibility">Wai AA- WCAG 2.0</a></li>
	            <li class="mobile"><a href="http://m.europarl.europa.eu/EPMobile/home.htm?language=en" title="Open in a new window" rel="external">European Parliament <span>mobile</span></a></li>
	            </ul>
	        </div>
        </div>
        <nav>
           	<div class="quicklinks" id="quicklinks-more">
            	<ul>
            	<li>European Parliament
                    <ul>                                
                    <li><a href="http://www.europarl.europa.eu/meps/en/search.html">MEPs</a></li>
                    <li><a href="http://www.europarl.europa.eu/aboutparliament/en/007f2537e0/Political-groups.html">Political groups</a></li>                                                                        
                    <li><a href="http://www.europarl.europa.eu/sed/plenary.do?language=en">S&eacute;ance en direct</a></li>
                    <li><a href="http://audiovisual.europarl.europa.eu/" target="_blank">Audiovisual website</a></li>
                    <li><a href="http://europa.eu/lisbon_treaty/index_en.htm" target="_blank">EUROPA - Treaty of Lisbon</a></li>
                    <li><a href="http://www.europarl.europa.eu/aboutparliament/en/002398d833/Sakharov-Prize-for-Freedom-of-Thought.html">Sakharov Prize</a></li>
                    </ul>
                   </li>
                   <li>Other links
                   	<ul>
                       <li><a href="http://europa.eu/ey2012/ey2012.jsp?langId=en" target="_blank">European Year for Active Ageing</a></li>
                       <li><a href="http://europa.eu/index_en.htm" target="_blank">EUROPA</a></li>                                    
                       <li><a href="http://www.eu2012.dk/" target="_blank">eu2012.dk</a></li>                                    
           	        </ul>
           	    </li>
           	    </ul>
               </div>
            <div class="galaxynav">
            	<ul>
            	<li>News
            		<ul>
				    <li><a href="http://www.europarl.europa.eu/en/headlines/">Headlines</a></li>
				    <li><a href="http://www.europarl.europa.eu/en/pressroom/">Press service</a></li>
				    <li><a href="http://www.europarl.europa.eu/news/archive/search.do?language=en">Press archives</a></li>
					</ul>
				</li>
				<li>About Parliament
					<ul>
				    <li><a href="http://www.europarl.europa.eu/aboutparliament/en/at-your-service/">At your service</a></li>
				    <li><a href="http://www.europarl.europa.eu/aboutparliament/en/powers-and-functions/">Power and functions</a></li>
				    <li><a href="http://www.europarl.europa.eu/aboutparliament/en/organisation-and-work/">Organisation and work</a></li>
				    <li><a href="http://www.europarl.europa.eu/aboutparliament/en/in-the-past/">In the past</a></li>                                                
					</ul>
				</li>
				</ul>
				
				<ul>
				<li>MEPs
					<ul>
				    <li><a href="http://www.europarl.europa.eu/meps/en/search.html">Search</a></li>
				    <li><a href="http://www.europarl.europa.eu/meps/en/full-list.html">Full list</a></li>
				    <li><a href="http://www.europarl.europa.eu/meps/en/incoming-outgoing.html">Incoming/outgoing</a></li>
				    <li><a href="http://www.europarl.europa.eu/meps/en/assistants.html">Assistants</a></li>
					</ul>
				</li>
				<li>Plenary
					<ul>                                    
				    <li><a href="http://www.europarl.europa.eu/plenary/en/home.html">Plenary sitting</a></li>
				    <li><a href="http://www.europarl.europa.eu/plenary/en/parliament-positions.html">Ordinary legislative procedure</a></li>
				    <li><a href="http://www.europarl.europa.eu/plenary/en/introduction-budgetary-procedure.html">Budgetary procedure</a></li>
				    <li><a href="http://www.europarl.europa.eu/plenary/en/parliamentary-questions.html">Questions and declarations</a></li>
				    <li><a href="http://www.europarl.europa.eu/plenary/en/meetings-search.html">Calendar</a></li>
					</ul>
				</li>
				</ul>
				
				<ul>
				<li>Committees
					<ul>
                    <li><a href="http://www.europarl.europa.eu/committees/en/home.html">Overview</a></li>
                    <li><a href="http://www.europarl.europa.eu/committees/en/full-list.html">Committee webpages</a></li>
                    <li><a href="http://www.europarl.europa.eu/parlArchives/comArch.do?language=en">Archives</a></li>
                	</ul>
                </li>
                <li>Delegations
                	<ul>
                    <li><a href="http://www.europarl.europa.eu/delegations/en/home.html">Delegations</a></li>
                    <li><a href="http://www.europarl.europa.eu/delegations/delegations/en/meetings-search.html">Calendar</a></li>
                    <li><a href="http://www.europarl.europa.eu/parlArchives/delArch.do?language=en">Archives</a></li>
                	</ul>
                </li>
                </ul>
                
                <ul>
                <li>EP TV
                    <ul>
                    <li><a href="http://europarltv.europa.eu/en/home">Home</a></li>
                    <li><a href="http://europarltv.europa.eu/en/channels">Channels</a></li>
                    <li><a href="http://europarltv.europa.eu/en/themes">Themes</a></li>
                    <li><a href="http://europarltv.europa.eu/en/about-europarltv">About EuroparlTV</a></li>
                    </ul>
                </li>
                <li>A-Z
                    <ul>
                    <li><a href="http://www.europarl.europa.eu/portal/en/a-z">Complete website list</a></li>
                    </ul> 
                </li>
                </ul>
            </div>
    	</nav>
	</footer>
</div>

<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
<%if (!XHTMLHelper.allReadyInsered(ctx, "/js/default.js")) {%><script src="<%=URLHelper.createStaticTemplateURL(ctx,"//js/default.js")%>"><%}%></script>
<%if (!XHTMLHelper.allReadyInsered(ctx, "/js/epbox/epbox.js")) {%><script src="<%=URLHelper.createStaticTemplateURL(ctx,"//js/epbox/epbox.js")%>"><%}%></script>
<script>new EPBoxController('${info.language}');</script>
<script src="https://graph.facebook.com/martinschulz.ep/feed?limit=10&amp;access_token=349993518364549|mZox06-j8i322t2dHQSrm7_rLe0&amp;callback=putFacebookContent" type="text/javascript"></script>
<script src="http://twitter.com/status/user_timeline/martinschulz.json?count=10&amp;callback=putTwitterContent" type="text/javascript"></script>
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
