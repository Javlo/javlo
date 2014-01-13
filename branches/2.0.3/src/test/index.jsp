<%@page contentType="text/html" pageEncoding="UTF-8"
	import="java.util.Date,
		org.javlo.helper.URLHelper,
		org.javlo.ContentContext,
		org.javlo.Content,
		org.javlo.I18nAccess,
		org.javlo.helper.StringHelper,
		org.javlo.user.User,
		org.javlo.EditContext,
		org.javlo.helper.XHTMLHelper,
		org.javlo.navigation.PageConfiguration,
		org.javlo.message.MessageRepository,
		org.javlo.user.AdminUserSecurity,
		org.javlo.MenuElement,
		org.javlo.helper.XHTMLNavigationHelper,
		org.javlo.config.GlobalContext"
%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
	ContentContext ctx = ContentContext.getContentContext(request, response);
Content content = Content.createContent(request);
GlobalContext globalContext = GlobalContext.getInstance(request.getSession());
MenuElement currentPage = ctx.getCurrentPage();
String currentTitle = currentPage.getCopiedPageContent(ctx).getPageTitle();

String globalTitle = currentPage.getGlobalTitle(ctx);if (globalTitle == null) {	globalTitle = globalContext.getGlobalTitle();}
I18nAccess i18nAccess = I18nAccess.getInstance(request);
AdminUserSecurity security = AdminUserSecurity.getInstance(request.getSession().getServletContext());
%>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="${info.language}" lang="${info.language}">
    <head><script type="text/javascript">
<!--
var sLanguage = '<%=ctx.getRequestContentLanguage()%>';
var server = '<%=URLHelper.createStaticURL(ctx, "/")%>';
-->
</script><%if (currentPage.getKeywords(ctx).length()>0){%>
<meta name="keywords" content="<%=currentPage.getKeywords(ctx)%>" />
<%}%><%if (currentPage.getMetaDescription(ctx).length()>0){%><meta name="description" content="<%=currentPage.getMetaDescription(ctx)%>" />
<%}
%><link rel="stylesheet" type="text/css" href="<%=URLHelper.createStaticURL(ctx,"/css/_basic_style.css")%>" />
<%=XHTMLNavigationHelper.getRSSHeader(ctx, currentPage)%>
<link rel="stylesheet" type="text/css" href="<%=URLHelper.createStaticURL(ctx,"/jsp/components_css.jsp")%>" />
<link rel="shortcut icon" type="image/ico" href="<%=URLHelper.createStaticURL(ctx,"/favicon.ico")%>" />
<%if (ctx.isInteractiveMode()) {%>
<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, "/js/mootools.js")%>
<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, "/js/global.js")%>
<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, "/js/calendar/js/HtmlManager.js")%>
<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, "/js/calendar/js/calendarFunctions.js")%>
<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, "/js/calendar/js/calendarOptions.js")%>
<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, "/js/calendar/js/calendarTranslate_"+ctx.getRequestContentLanguage()+".js")%>
<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, "/js/calendar/css/style_calendar.css")%>
<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, "/js/calendar/css/style_calendarcolor.css")%>
<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, "/js/shadowbox/src/adapter/shadowbox-base.js")%>
<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, "/js/shadowbox/src/shadowbox.js")%>
<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, "/js/shadowboxOptions.js")%>
<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, "/js/onLoadFunctions.js")%>
<%}%><%for (String uri : currentPage.getExternalResources(ctx)) {%>
<%=XHTMLHelper.renderHeaderResourceInsertion(ctx, uri)%><%}%>
        <meta http-equiv="X-UA-Compatible" content="IE=edge" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />        
        <title>${info.globalTitle} : ${info.pageTitle}</title>
        
	<meta name="robots" content="index, follow, noodp, noydir, notranslate, noarchive"/>
        <meta name="language" content="${info.language}" />        
        <!-- Design du generateur -->
        <link rel="stylesheet" type="text/css" href="<%=URLHelper.createStaticTemplateURL(ctx,"/website/visiting/css/style_page.css", "131590916176872503505")%>" />
        <link rel="stylesheet" type="text/css" href="<%=URLHelper.createStaticTemplateURL(ctx,"//css/integration.css", "131590916176872503505")%>" />
        <link rel="stylesheet" type="text/css" href="<%=URLHelper.createStaticTemplateURL(ctx,"//css/integration_cronos.css", "131590916176872503505")%>" />
        <!-- Importation des scripts -->
            <!-- GENERAL - Outils necessaires aux widgets javascript -->            
            <script src="<%=URLHelper.createStaticTemplateURL(ctx,"/website/common/js/htmanager.js")%>" type="text/javascript"></script>
            <script src="<%=URLHelper.createStaticTemplateURL(ctx,"/website/common/js/managetag.js")%>" type="text/javascript"></script> 
            <!-- GENERAL - Gestion des widgets javascript communs a tous les sites -->

            <script src="<%=URLHelper.createStaticTemplateURL(ctx,"/website/common/js/widget.js")%>" type="text/javascript"></script>
            <!-- GENERAL - Gestion commune de l'affichage des calendriers de tous les sites -->
            <script src="<%=URLHelper.createStaticTemplateURL(ctx,"/website/common/js/calendarfunction.js")%>" type="text/javascript"></script>
            <script src="<%=URLHelper.createStaticTemplateURL(ctx,"/website/common/js/calendarcontrol.js")%>" type="text/javascript"></script>
            <!-- GENERAL - Gestion commune de l'affichage des formulaires de tous les sites -->
            <script src="<%=URLHelper.createStaticTemplateURL(ctx,"/website/common/js/form.js")%>" type="text/javascript"></script>
            <!-- SPECIFIQUE - Lancement des widgets a la fin du chargement complet de la page -->
            
             <!-- jQuery -->
              <script src="<%=URLHelper.createStaticTemplateURL(ctx,"/website/common/js/jquery.min.js")%>" type="text/javascript"></script>
    
              <!-- Lightbox -->                                                       
            <script src="<%=URLHelper.createStaticTemplateURL(ctx,"/website/common/js/epbox.js")%>" type="text/javascript"></script>

            <script src="<%=URLHelper.createStaticTemplateURL(ctx,"/website/visiting/js/onload.js")%>" type="text/javascript"></script> 
   <%=(ctx.isInteractiveMode() ? "<link rel=\"stylesheet\" type=\"text/css\" href=\""+URLHelper.createStaticURL(ctx,"/css/edit_preview.css")+"\"></link>" : "")  %>
<%=(ctx.isInteractiveMode() ? "<link rel=\"stylesheet\" type=\"text/css\" href=\""+URLHelper.createStaticURL(ctx,"/css/edit_preview_specific.css")+"\"></link>" : "")  %>
<%=(ctx.isInteractiveMode() ? "<script type=\"text/javascript\" src=\""+URLHelper.createStaticURL(ctx,"/js/preview.js")+"\"></script>" : "")  %>
<%=(ctx.isInteractiveMode() ? "<script type=\"text/javascript\" src=\""+URLHelper.createStaticURL(ctx,"/js/content_ajax.js")+"\"></script>" : "")  %>
<%=(ctx.isInteractiveMode() ? "<script type=\"text/javascript\" src=\""+URLHelper.createStaticURL(ctx,"/js/content_preview.js")+"\"></script>" : "")  %>
<%if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
GlobalContext globalContext = GlobalContext.getInstance(request); EditContext editCtx = EditContext.getInstance(globalContext, request.getSession());
%><%=XHTMLHelper.renderHeaderResourceInsertion(ctx, "/js/calendar/css/reset.css")%>
<%if ((ctx.isInteractiveMode())&&(security.haveRight((User)editCtx.getUserPrincipal(), "update"))) {%><script type="text/javascript">
var ajaxURL = "<%=URLHelper.createAjaxURL(ctx)%>";
window.addEvent("domready", function (){
var previewEdit = new PreviewEdit();
});
</script><%} }%>
<%if (currentPage.getHeaderContent(ctx) != null) {%><%=currentPage.getHeaderContent(ctx)%><%}%></head>
    <body xml:lang="${info.language}" lang="${info.language}"><%if (ctx.isInteractiveMode() && ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {%>
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
<div style="position: absolute; top: -100px;" id="_escape_menu">
<ul><li><a href="#mainzone"><%=i18nAccess.getViewText("wai.to_content")%></a></li></ul>
</div>
<%if (currentPage.getImage(ctx) != null && globalContext.isFirstImage()) {
%><img id="_first-image" src="<%=URLHelper.createTransformURL(ctx, currentPage.getImage(ctx).getImageURL(ctx), "standard")%>" alt="<%=currentPage.getImage(ctx).getImageDescription(ctx)%>" /><%}%>

        <!-- Zone "Entete" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
        <div id="header">
            <div id="headerbox">
                <!-- Entete - Bloc titre et logo -->
                <div id="headerwrapper_title">
                    <div id="headertitle">
                        <span class="ep_title">
                            <a  href="http://www.europarl.europa.eu/${info.language}" target="_blank">
                                <img title="" alt="${i18n.view['header.european-parliament']}" class="ep_logo" src="<%=URLHelper.createStaticTemplateURL(ctx,"/website/common/img/icon/header_icon_eplogo.png")%>" />
                            </a>
                            <img title="" alt="${i18n.view['header.european-parliament']}" src="<%=URLHelper.createStaticTemplateURL(ctx,"/website/common/img/icon/header_icon_eplogo_print.png")%>" />
                            <a class="ep_galaxy" target="_blank" href="http://www.europarl.europa.eu/${info.language}" title="Return to the European portal">${i18n.view['header.european-parliament']}</a>
                            <span>/</span>
                            <a title="Return to the home page" class="ep_site" href="<%=URLHelper.createURLCheckLg(ctx,"/")%>">${i18n.view['header.visiting']}</a>
                        </span>
                        <span class="ep_align"> </span>
                    </div>
                </div>
                <!-- Entete - Barre de menu superieure -->
                <div id="headerwrapper_menu">
                    <div id="headermenu">
                        <div class="ep_wrapper">
                            <!-- Entete - Barre de menu superieure (menu linguistique) -->
                            <div class="ep_lang">
                                <%if (globalContext.getVisibleLanguages().size() > 1) {%><form id="langbox" class="ep_menu" method="post" action=""><%=XHTMLHelper.renderSelectLanguage(ctx, false, "langbox_select",  "langbox_btn", false)%></form><%}%>
                                <span class="ep_endbox"> </span>
                            </div>
                            <!-- Entete - Barre de menu superieure (menu raccourcis) -->
                            <div class="ep_menu">
                                <span class="ep_hidden">Other available websites: </span>
                                <div id="xep_header_links"><jsp:include page="/jsp/content_view.jsp?area=planet-links" /></div>
                                <ul>
                                    <li id="menumore"><a href="#footermenusite" title="View list of the others available websites">${i18n.view['header.more']}</a></li>
                                </ul>
                                <span class="ep_endbox"> </span>
                            </div>
                            <!-- Entete - Barre de menu superieure (cloture) -->
                            <span class="ep_endbox"> </span>
                        </div>
                    </div>
                </div>
                <!-- Entete - Bloc de promotion -->
                <div id="headerwrapper_promo">
                    <!-- <div id="headerpromotion">Zone promotionnelle ÃÂ  dÃÂ©finir</div> -->
                    <span class="ep_endbox"> </span>
                </div>
                <!-- Entete - Cloture de l'entete -->
                <span class="ep_endbox"> </span>
            </div>
        </div>
        <!-- Zone "Entete" : fin - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
        <!-- Zone "Corps de la page" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
        <div id="body" class="ep_2headmenu">
            <!-- Corps de la page - Menu principal (recherche et navigation) -->
            <div id="mainmenu">
                <div id="mainmenubox">
                    <div class="ep_wrapper">
                        <!-- Corps de la page - Menu principal (recherche) -->
                            <!-- Pas de menu de recherche -->
                        <!-- Corps de la page - Menu principal (menu de navigation) -->
                        <div id="menunavigation">
                            <div class="ep_hidden">Main navigation</div>
                            <%if ( XHTMLNavigationHelper.menuExist(ctx,1) ) {%><div id="menu" class="ep_menu ep_3columns"><%=XHTMLNavigationHelper.renderMenu(ctx, 0, 0, globalContext.isExtendMenu())%></div><%}%>
                        </div>
                        <!-- Corps de la page - Menu principal (cloture) -->
                        <span class="ep_endbox"> </span>
                    </div>
                </div>
            </div>
            <!-- Corps de la page - Menu secondaire  - - - - - - - - - - - - - - - - - - - - - - - - - - -->
            <div id="submenu">
                <div id="submenubox">
                    <div  class="ep_wrapper">
                        <div class="ep_hidden">Secondary navigation</div>
                        <%if ( XHTMLNavigationHelper.menuExist(ctx,2) ) {%><div id="menu_2" class="ep_menu"><%=XHTMLNavigationHelper.renderMenu(ctx, 1, 1, globalContext.isExtendMenu())%></div><%}%>
                        <span class="ep_endbox"> </span>
                    </div>
                </div>
            </div>
            <!-- Corps de la page - Zone de contenu  - - - - - - - - - - - - - - - - - - - - - - - - - - -->            
            <div id="maincontent">
                <div id="maincontentbox">
                    <div class="ep_wrapper ep_2columns">
                        <div id="bannerzone"><jsp:include page="/jsp/content_view.jsp?area=bannerzone" /></div>
                        <!-- Corps de la page - Zone de contenu principal (colonne gauche) - - - - - - - -->
                        <div id="mainzone"><jsp:include page="/jsp/content_view.jsp?area=content" /></div>
                        <!-- Corps de la page - Zone d'information complementaire (colonne droite) - - - -->
                        <div id="contextzone"><jsp:include page="/jsp/content_view.jsp?area=contextzone" /></div>
                        <!-- Corps de la page - Cloture de la zone de contenu -->
                        <span class="ep_endbox"> </span>
                    </div>
                </div>
            </div>
        </div>
        <!-- Zone "Corps de la page" : fin - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
        <!-- Zone "Pied de page" - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
        <div id="footer">
            <div id="footerwrapper">
                <!-- Pied de page - Bloc titre et logo -->
                <div id="footertitle">
                    <span class="ep_title">
                        <a class="ep_galaxy" target="_blank"  href="http://www.europarl.europa.eu/${info.language}" title="Return to the European portal">${i18n.view['header.european-parliament']}</a>
                        <span>/</span>
                        <a title="Return to the home page" class="ep_site" href="<%=URLHelper.createURLCheckLg(ctx,"/")%>">${i18n.view['header.visiting']}</a>
                    </span>
                    <span class="ep_align"> </span>
                </div>
                <!-- Pied de page - Bloc liens utiles -->
                <div id="footermenutools">
                    <ul>
                        <li><a target="_blank" href="<%=URLHelper.createRSSURL(ctx, "")%>">RSS</a></li>
                        <li><a target="_blank" href="http://www.europarl.europa.eu/toolbox/contact.do?language=${info.language}">${i18n.view['footer.contact']}</a></li>
                        <li><a target="_blank" href="http://www.europarl.europa.eu/tools/disclaimer/default_${info.language}.htm">${i18n.view['footer.legal-notice']}</a></li>
                        <li><a target="_blank" href="http://www.europarl.europa.eu/tools/accessibility/default_${info.language}.htm">Wai AA- WCAG 2.0</a></li>
                    </ul>
                    <span class="ep_align"> </span>
                </div>
                <!-- Pied de page - Bloc liens specifiques au site -->
                <div id="footermenusite">
                    <div class="ep_menu ep_3columns">
                        <div class="ep_block">
                            <div class="ep_column">
                                <div class="ep_title">${i18n.view['footer.tourist-info']}</div>
                                <ul>
                                    <li><a target="_blank" href="http://www.europarl.be/view/${info.language}/le_bi_a_bruxelles/Infopoint.html">${i18n.view['footer.brussels']}</a></li>
                                    <li><a target="_blank" href="http://www.otstrasbourg.fr/?lang=${info.language}">${i18n.view['footer.strasbourg']}</a></li>
                                    <li><a target="_blank" href="http://www.europarl.lu/view/${info.language}/homepage.html">${i18n.view['footer.luxembourg']}</a></li>
                                </ul>
                                <span class="ep_endbox"> </span>
                            </div>
                            <div class="ep_column" id="information-offices"><jsp:include page="/jsp/content_view.jsp?area=information-offices" /></div>
                            <div class="ep_column">
                                <div class="ep_title">${i18n.view['footer.visit-institutions']}</div>
                                <ul>
                                    <li><a target="_blank" href="http://europa.eu/take-part/visit/index_${info.language}.htm">${i18n.view['footer.full-list']}</a></li>
                                </ul>
                                <span class="ep_endbox"> </span>
                            </div>
                            <span class="ep_endbox"> </span>
                        </div>
                    </div>
                </div>
                <!-- Pied de page - Bloc liens des principaux sites existants -->
                <div id="footermenugalaxy">
                    <div class="ep_menu ep_5columns">
                        <div class="ep_block">
                            <div class="ep_column">
                                <div class="ep_title">${i18n.view['footer.news']}</div>
                                <ul>
                                    <li><a target="_blank" href="http://www.europarl.europa.eu/${info.language}/headlines/">${i18n.view['footer.headlines']}</a></li>
                                    <li><a target="_blank" href="http://www.europarl.europa.eu/${info.language}/pressroom/">${i18n.view['footer.press-service']}</a></li>
                                    <li><a target="_blank" href="http://www.europarl.europa.eu/news/archive/search.do?language=${info.language}">${i18n.view['footer.press-archive']}</a></li>
                                </ul>
                                <span class="ep_endbox"> </span>
                            </div>
                            <div class="ep_column">
                                <div class="ep_title">${i18n.view['footer.parliament']}</div>
                                <ul>
                                    <li><a target="_blank" href="http://www.europarl.europa.eu/parliament/public/staticDisplay.do?id=146&amp;language=${info.language}">${i18n.view['footer.parliament-intro']}</a></li>
                                    <li><a target="_blank" href="http://www.europarl.europa.eu/parliament/expert/staticDisplay.do?id=52&amp;language=${info.language}">${i18n.view['footer.details']}</a></li>
                                    <li><a target="_blank" href="http://www.europarl.europa.eu/parliament/archive/staticDisplay.do?id=191&amp;language=${info.language}">${i18n.view['footer.parliament-archive']}</a></li>
                                </ul>
                                <span class="ep_endbox"> </span>
                            </div>
                            <div class="ep_column">
                                <div class="ep_title">${i18n.view['footer.meps']}</div>
                                <ul>
                                    <li><a target="_blank" href="http://www.europarl.europa.eu/members/public/geoSearch.do?language=${info.language}">${i18n.view['footer.mep-intro']}</a></li>
                                    <li><a target="_blank" href="http://www.europarl.europa.eu/members/expert/groupAndCountry.do?language=${info.language}">${i18n.view['footer.directory']}</a></li>
                                    <li><a target="_blank" href="http://www.europarl.europa.eu/members/archive/alphaOrder.do?language=${info.language}">${i18n.view['footer.mep-archive']}</a></li>
                                </ul>
                                <span class="ep_endbox"> </span>
                            </div>
                            <div class="ep_column">
                                <div class="ep_title">${i18n.view['footer.activities']}</div>
                                <ul>
                                    <li><a target="_blank" href="http://www.europarl.europa.eu/activities/introduction/home.do?language=${info.language}">${i18n.view['footer.activities-intro']}</a></li>
                                    <li><a target="_blank" href="http://www.europarl.europa.eu/activities/plenary/home.do?language=${info.language}">${i18n.view['footer.plenary']}</a></li>
                                    <li><a target="_blank" href="http://www.europarl.europa.eu/activities/committees/home.do?language=${info.language}">${i18n.view['footer.committees']}</a></li>
                                    <li><a target="_blank" href="http://www.europarl.europa.eu/activities/delegations/home.do?language=${info.language}">${i18n.view['footer.delegations']}</a></li>
                                    <li><a target="_blank" href="http://www.europarl.europa.eu/activities/archives/staticDisplay.do?id=120&amp;language=${info.language}">${i18n.view['footer.activities-archive']}</a></li>
                                </ul>
                                <span class="ep_endbox"> </span>
                            </div>
                            <div class="ep_column">
                                <div class="ep_title">${i18n.view['footer.eplive']}</div>
                                <ul>
                                    <li><a target="_blank" href="http://www.europarl.europa.eu/${info.language}/see-and-hear">${i18n.view['footer.see-hear']}</a></li>
                                    <li><a target="_blank" href="http://www.europarl.europa.eu/${info.language}/media-professionals/">${i18n.view['footer.media']}</a></li>
                                    <li><a target="_blank" href="http://www.europarl.europa.eu/${info.language}/multimedia-library/">${i18n.view['footer.multimedia']}</a></li>
                                </ul>
                                <span class="ep_endbox"> </span>
                            </div>
                            <span class="ep_endbox"> </span>
                        </div>
                    </div>
                </div>
                <!-- Pied de page - Cloture de l'entete -->
                <span class="ep_endbox"> </span>
            </div>
        </div>
        <!-- Zone "Pied de page" : fin - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
    <script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script>
<script type="text/javascript">
try {
var pageTracker = _gat._getTracker("<%=globalContext.getGoogleAnalyticsUACCT()%>");
pageTracker._trackPageview();
} catch(err) {}</script>
</body>
</html>
