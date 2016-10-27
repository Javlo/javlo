<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%><%@ taglib uri="/WEB-INF/javlo.tld" prefix="jv"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<c:if test="${contentContext.closePopup}">
<head>
<script type="text/javascript">
    var url = top.location.href; // close iframe and refresh parent frame
    
    <c:if test="${not empty contentContext.parentURL}">
    	url = "${contentContext.parentURL}";
    </c:if>
    <c:if test="${not empty messages.rawGlobalMessage}">
    	if (url.indexOf("?")>=0) {
    		url = url + "&${messages.parameterName}=${messages.rawGlobalMessage}";
    	} else {
    		url = url + "?${messages.parameterName}=${messages.rawGlobalMessage}";
    	}
    </c:if>
    if (url != null) {
        var doc = top.document.documentElement, body = top.document.body;    
        var topScroll = (doc && doc.scrollTop  || body && body.scrollTop  || 0);
        if (topScroll>0) {
        	var sep="?";
        	if (url.indexOf("?")>=0) {
        		sep="&";
        	}
        	url = url + sep + "_scrollTo=" + topScroll;
        }
		top.location.href=url; // close iframe and refresh parent frame
    }    
</script>
</head><body></body>
</c:if>
<c:if test="${!contentContext.closePopup}">

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Javlo : ${currentModule.title}</title>
<link rel="stylesheet" media="screen" href="${info.editTemplateURL}/css/bootstrap/bootstrap.css?ts=${info.ts}" />
<link rel="stylesheet" href="${info.editTemplateURL}/css/style.css?ts=${info.ts}" /><link rel="stylesheet" media="screen" href="${info.editTemplateURL}/css/style.css?ts=${info.ts}" />
<link rel="stylesheet" media="screen" href="${info.editTemplateURL}/css/javlo.css?ts=${info.ts}" />
<link rel="stylesheet" media="screen" href="${info.editTemplateURL}/css/plugins/colorbox.css" />
<link rel="stylesheet" media="print" href="${info.editTemplateURL}/css/print.css" />

<style type="text/css">
@font-face {
    font-family: "javloFont";
    src: url('${info.staticRootURL}fonts/Javlo-Italic.ttf') format("truetype");
}
</style>

<c:if test="${not empty globalContext.editTemplateMode}"><link rel="stylesheet" media="screen" href="${info.editTemplateURL}/css/edit_${globalContext.editTemplateMode}.css" /></c:if>
<c:if test="${not empty specificCSS}">
<link rel="stylesheet" media="screen" href="${specificCSS}" />
</c:if>
<!--[if IE 9]>
    <link rel="stylesheet" media="screen" href="${info.editTemplateURL}/css/ie9.css"/>
<![endif]-->

<!--[if IE 8]>
    <link rel="stylesheet" media="screen" href="${info.editTemplateURL}/css/ie8.css"/>
<![endif]-->

<!--[if IE 7]>
    <link rel="stylesheet" media="screen" href="${info.editTemplateURL}/css/ie7.css"/>
<![endif]-->

<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery-ui-1.8.20.custom.min.js"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery.alerts.js"></script>
<script type="text/javascript"><jsp:include page="/jsp/edit/global/dynamic_js.jsp" /></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery.validate.min.js"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery.colorbox-min.js"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery.jgrowl.js"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery.dataTables.min.js"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery.form.js"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery.elastic.source.js"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/custom/gallery.js"></script>
<script type="text/javascript" src="<jv:url value='/js/edit/ajax.js?ts=${info.ts}' />"></script>
<script type="text/javascript" src="<jv:url value='/js/edit/core.js?ts=${info.ts}' />"></script>
<script type="text/javascript" src="<jv:url value='/js/lib/colorpicker/js/colorpicker.js' />"></script>
<link rel="stylesheet" media="screen" type="text/css" href="<jv:url value='/js/lib/colorpicker/css/colorpicker.css' />" />
<script type="text/javascript" src="${info.editTemplateURL}/js/javlo/core.js?ts=${info.ts}"></script>
<c:if test="${not info.editLanguage eq 'en'}"><script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery.ui.datepicker-${info.editLanguage}.js"></script></c:if>
<script type="text/javascript" src="${info.editTemplateURL}/js/custom/general.js?ts=${info.ts}"></script>
<script type="text/javascript" src="${info.editTemplateURL}/js/plugins/jquery.autosize-min.js"></script>


<link rel="stylesheet" href="<jv:url value='/js/lib/tooltipster-master/css/tooltipster.css' />" />
<script type="text/javascript" src="<jv:url value='/js/lib/tooltipster-master/js/jquery.tooltipster.min.js' />"></script>

<!-- module '${currentModule.name}' CSS --><c:forEach var="css" items="${currentModule.CSS}">
<link rel="stylesheet" href="<jv:url value='${css}?ts=${info.ts}' />"/>
</c:forEach>

<!-- module '${currentModule.name}' JS --><c:forEach var="js" items="${currentModule.JS}">
<script type="text/javascript" src="<jv:url value='${js}?ts=${info.ts}' />"></script>
</c:forEach>

<script type="text/javascript">var i18nURL = "${info.i18nAjaxURL}";</script>

</head>

<body class="bodygrey ${info.admin?'right-admin ':'noright-admin '}${not empty param.previewEdit?'previewEdit':''}${requestService.parameterMap.lightEdit?' light-edit':''}">

<c:if test="${empty param.previewEdit}">
<div class="header">
	
    <c:if test="${currentModule.search}">
    <form id="search" action="${info.currentURL}" method="post">
    	<input type="hidden" name="webaction" value="search" />
    	<input type="text" name="query" /> <button class="searchbutton"></button>
    </form>
    </c:if>    
    
    <div class="topheader">
        <ul class="notebutton">
            <li class="note">
                <div class="im-wizz-message" style="display: none;" title="${i18n.edit['im.title']}">
                    ${i18n.edit['im.message.wizz']}
                </div>
                <a href="${info.editTemplateURL}/im.jsp" class="messagenotify">
                    <span class="wrap">
                        <span class="thicon msgicon"></span>
                        <span class="count" style="display: none;">0</span>
                    </span>
                </a>
            </li>
            <li class="note">
            	<a href="${info.editTemplateURL}/notifications.jsp" class="alertnotify">
                	<span class="wrap">
                    	<span class="thicon commenticon"></span>
                        <c:if test="${not empty notificationSize}"><span id="notification-count" class="count">${notificationSize}</span></c:if>
                    </span>
                </a>
            </li>
            <c:if test="${not empty info.privateHelpURL}">
            <li class="note">
                <a href="${info.privateHelpURL}" target="_blanck">
                    <span class="wrap">
                        <span class="thicon helpicon"></span>
                        <span class="count" style="display: none;">0</span>
                    </span>
                </a>
            </li>
            </c:if>
            <c:if test="${not empty integrities}"><li class="note">
            	<a href="${info.editTemplateURL}/integrity.jsp?path=${info.path}" class="alertintegrity">
                	<span class="wrap">
                    	<span class="thicon infoicon"></span>
                        <span id="integrity-count" class="count ${integrities.errorCount == 0?'clear':'not-clear'}">${integrities.errorCount}</span>
                    </span>
                </a>
            </li></c:if>
        </ul>
        <a href="${info.currentViewURL}"><h1>${info.globalTitle}</h1></a>
    </div><!-- topheader -->
    
    <!-- logo -->
    <div class="logo">Javlo</div>
    
    <div class="tabmenu">
    	<ul>
    	    <c:forEach var="module" items="${modules}">
    	    <c:if test="${empty module.parent}">
	        	<li class="module ${module.name} ${module.name == currentModule.name || module.name == currentModule.parent?'current':''} ${module.name == fromModule.name?'from':''}">        		
	        		<a href="${info.currentURL}?module=${module.name}"><span class="title">${module.title}</span>        		
	        		${module.name == currentModule.name || module.name == currentModule.parent?'<div id="ajax-loader"></div>':''}
	        		<c:if test="${currentModule.name != module.name && module.name == currentModule.parent}">
	        			<span class="subname">${currentModule.title}</span>
	        		</c:if></a>	
	        		<c:if test="${fn:length(module.children) > 0}">
	        		<ul class="subnav">
	        			<c:forEach var="submodule" items="${module.children}">
							<li><a href="${info.currentURL}?module=${submodule.name}"><span>${submodule.title}</span></a></li>
						</c:forEach>
					</ul>
	        		</c:if>
	        	</li>
        	</c:if>
        	</c:forEach>            	
        </ul>
    </div><!-- tabmenu -->
    
    <div class="accountinfo">    
    	<c:if test="${not empty info.currentUserAvatarUrl}">
    	<img src="${info.currentUserAvatarUrl}" alt="social avatar" lang="en" />
    	</c:if>
    	<c:if test="${empty info.currentUserAvatarUrl}">
    	<img src="${info.editTemplateURL}/images/avatar.png" alt="default avatar" lang="en" />
    	</c:if>
        <div class="info">
        	<h3>${currentUser.name}</h3>
            <small>${currentUser.userInfo.email}&nbsp;</small>
            <p>
            	<c:if test="${info.accountSettings}">
            	<a class="account" href="${info.currentURL}?module=users&webaction=user.ChangeMode&mode=myself">${i18n.edit["global.account-setting"]}</a>
            	</c:if>
            	<a href="${info.currentURL}?edit-logout=logout">logout</a>
            </p>
        </div><!-- info -->
    </div><!-- accountinfo -->
</div><!-- header -->
</c:if>

<c:if test="${empty param.previewEdit}">
<div class="sidebar">
	<div id="navigation">
		<c:forEach var="box" items="${currentModule.navigation}">
			<c:if test="${box.title != null}">		
			<h3 class="open">${box.title}</h3>
			</c:if>
			<div class="content leftmenu" style="display: block;" >			
				<jsp:include page="${box.renderer}" />			
			</div>		
		</c:forEach>		        
        <%--<c:if test="${currentModule.helpTitle != null}">
        <h3 class="open">${currentModule.helpTitle}</h3>
        <div class="content" style="display: block;">${currentModule.helpText}</div>
        </c:if> --%>
	</div>
	
</div><!-- leftmenu -->
</c:if>

<div class="maincontent ${currentModule.name}">

<c:if test="${currentModule.breadcrumb && empty param.nobreadcrumbs}">
	<div id="breadcrumbs" class="breadcrumbs">
		<jsp:include page="breadcrumbs.jsp" />
	</div>
</c:if>

<c:if test="${currentModule.sidebar}">
<div class="two_third maincontent_inner">
</c:if>
	<div class="left">		 
		
		<div id="message-container">
		<jsp:include page="message.jsp" />
		</div>
		
		<c:if test="${currentModule.toolsRenderer != null && info.tools}">
			<div id="tools">
				<h3>${currentModule.toolsTitle}</h3>
				<div class="content"><jsp:include page="${currentModule.toolsRenderer}" /></div>
			</div>
		</c:if>
		<c:if test="${currentModule.renderer != null}">
			<div id="main-renderer">
			<c:if test="${not empty specialEditRenderer}"><jsp:include page="${specialEditRenderer}" /></c:if>
			<c:if test="${empty specialEditRenderer}"><jsp:include page="${currentModule.renderer}" /></c:if>
			</div>
		</c:if>	
		<c:forEach var="currentBox" items="${currentModule.mainBoxes}">
			<c:set var="box" value="${currentBox}" scope="request" />
			<div class="mainBox" id="${box.id}">
			<jsp:include page="box.jsp" />
			</div>
		</c:forEach>
	</div>
<c:if test="${currentModule.sidebar}">
</div> <!-- side bar -->
</c:if>
<c:if test="${empty param.previewEdit}">
<c:if test="${currentModule.sidebar}">
<div class="one_third last">
<div class="right">
	<c:forEach var="currentBox" items="${currentModule.sideBoxes}">
		<c:set var="box" value="${currentBox}" scope="request" />
		<div class="sidebox" id="${box.id}">
		<jsp:include page="box.jsp" />
		</div>
	</c:forEach>
</div>
</div>
</c:if>
</c:if>
</div><!--maincontent-->

<br />
<c:if test="${empty param.previewEdit}">
<div id="footer" class="footer footer_float">
	<div class="footerinner">
    	<a href="http://javlo.org">javlo.org</a>
    	<c:if test="${!userInterface.light}">
    		${info.currentYear} - v ${info.version} - 
    		<span id="preview-version">${info.previewVersion}</span> - 
    		<span id="server-time">${info.serverTime}</span> -   		
    		<span id="server-time">IP:${contentContext.remoteIp}</span>
    		<c:if test="${info.localModule}">
    			<span class="localmodule"><a href="${info.staticRootURL}webstart/localmodule.jnlp.jsp">Local Module</a></span>
    		</c:if>
    	</c:if>
    </div><!-- footerinner -->
</div><!-- footer -->
</c:if>
<div id="layer">&nbsp;</div>
</body>
</c:if>
</html>
