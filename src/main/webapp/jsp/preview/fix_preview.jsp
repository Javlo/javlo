<%@page import="org.javlo.context.EditContext"
%><%@page import="org.javlo.context.ContentContext"
%><%@page import="org.javlo.helper.URLHelper"%><%
ContentContext ctx = ContentContext.getContentContext(request, response);
EditContext editCtx = EditContext.getInstance(ctx.getGlobalContext(), request.getSession());
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="/WEB-INF/javlo.tld" prefix="jv"
%><script type="text/javascript">if (window!=window.top) {top.location.href = document.location;}</script><html>
<head>
	<title>${info.pageTitle}</title>
	
	<link href="<%=URLHelper.createStaticURL(ctx,"/css/preview/edit_preview.css")%>" type="text/css" rel="stylesheet" />
	<link href="<%=URLHelper.mergePath(URLHelper.createStaticURL(ctx,"/"), editCtx.getEditTemplateFolder(), "/css/edit_preview.css")%>" type="text/css" rel="stylesheet" />
	
	<link href="<%=URLHelper.createStaticURL(ctx,"/css/lib/colorbox/colorbox.css")%>" type="text/css" rel="stylesheet" />
	
	<script type="text/javascript" src="<%=URLHelper.createStaticURL(ctx,"/js/lib/jquery-1.7.2.min.js")%>"></script>
	<script type="text/javascript" src="<%=URLHelper.createStaticURL(ctx,"/js/lib/jquery-ui-1.9.2.custom.min.js")%>"></script>
	<script type="text/javascript" src="<%=URLHelper.createStaticURL(ctx,"/js/lib/jquery.colorbox-min.js")%>"></script>
	<script type="text/javascript" src="<%=URLHelper.createStaticURL(ctx,"/js/lib/jquery.cookie.js")%>"></script>
	
	<script type="text/javascript" src="<%=URLHelper.createStaticURL(ctx,"/js/preview/edit_preview.js")%>"></script>
	<script type="text/javascript" src="<%=URLHelper.createStaticURL(ctx,"/js/edit/ajax.js")%>"></script>
	<script type="text/javascript" src="<%=URLHelper.createStaticURL(ctx,"/js/edit/core.js")%>"></script>
	
	<style type="text/css">
	    body {
	    	margin: 0;
	    	padding: 0;
	    }
	    
	    #main {
	    	overflow: hidden;
	    }
	    
		#fix-commands {
			width: 350px;			
			min-height: 100%;
			height: auto !important;
			position: absolute;
			background-color: #EEEEEE;
    		box-shadow: 0 0 7px #999999;
		}
		
		#page {		    			
			float: right;
			width: 100%;
		}
		
		#iframe {
			margin-left: 350px;
		}
		
		#page iframe {		   
		   margin: 0;
		   border: 0px none;
		   padding: 0;
		}		
		
		#preview_command {
			position: relative;
			width: auto;
			right: auto;
			top: auto;
			box-shadow: none;
		}
		
		.component-list {
			overflow: hidden;
		}
		
		.component-list .one_half {
		    width: 46%;
		    margin-right: 3px;
		    float: left;
		}
		
		#preview_command .component-list .component {
		    padding-left: 12px;
		}	
		
		fieldset.closable {
			position: relative;
		}		

		fieldset.closable .closable_action {
			position: absolute;
			display: block;			
			right: 12px;
			top: -2px;
		}
			
	</style>
</head>
<body>
<div id="main">
<div id="page">
<div id="iframe">
	<iframe src="${currentPage}" width="100%" height="100%"></iframe>
</div>
</div>
<div id="fix-commands">
	<jsp:include page="command.jsp"></jsp:include>
</div>
</div>
</body>
</html>