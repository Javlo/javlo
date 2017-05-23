<%@page import="org.javlo.helper.URLHelper"
%><%@page import="org.javlo.service.social.ISocialNetwork
"%><%@page import="org.javlo.context.ContentContext"
%><%@page import="org.javlo.service.social.SocialService"
%><%@page import="org.javlo.servlet.IVersion"
%><%@page import="org.javlo.message.MessageRepository"
%><%@page import="org.javlo.message.GenericMessage"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%><!doctype html><%
MessageRepository msgRepo = MessageRepository.getInstance(request); // load request message
GenericMessage msg = msgRepo.getGlobalMessage();
String bootstrapType = "success";
switch (msg.getType()) {
case GenericMessage.ERROR:
	bootstrapType = "danger";
	break;
case GenericMessage.INFO:
	bootstrapType = "info";
	break;
case GenericMessage.HELP:
	bootstrapType = "info";
	break;
case GenericMessage.ALERT:
	bootstrapType = "warning";
	break;
case GenericMessage.SUCCESS:
	bootstrapType = "success";
	break;
}
ContentContext ctx = ContentContext.getContentContext(request, response);
SocialService socialService = SocialService.getInstance(ctx);
ISocialNetwork googleOauth = socialService.getGoogle();
googleOauth.setRedirectURL(URLHelper.createStaticURL(ctx.getContextForAbsoluteURL(), "/oauth2callbackadmin"));
googleOauth.setClientId(ctx.getGlobalContext().getStaticConfig().getOauthGoogleIdClient());
googleOauth.setClientSecret(ctx.getGlobalContext().getStaticConfig().getOauthGoogleSecret());
%><html>
    <head>
    	<meta name="GOOGLEBOT" content="NOSNIPPET" />
    	<meta name="robots" content="noindex, nofollow" />
    	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <meta http-equiv="X-UA-Compatible" content="IE=edge" />
        <title>Javlo (Login)</title>
        <link rel="shortcut icon" href="img/assets/favicon.ico" />
        <link href="${info.editTemplateURL}/css/login.css" rel="stylesheet" type="text/css" />
        <style type="text/css">
		@font-face {
    		font-family: "javloFont";
    		src: url('${info.staticRootURL}fonts/javlo-italic.ttf') format("truetype");
		}
		</style>
		<c:if test="${not empty globalContext.staticConfig.oauthGoogleIdClient && not nogoogle}">
			<meta name="google-signin-client_id" content="${globalContext.staticConfig.oauthGoogleIdClient}" />
		</c:if>
    </head>
    
    <body class="login-page bg${info.random10+1} ${not empty param.err || not empty messages.globalMessage && messages.globalMessage.type > 0 && not empty messages.globalMessage.message?'wmsg':'nomsg'}">    	
        <div class="main">
            <c:if test="${not empty messages.globalMessage && messages.globalMessage.type > 0 && not empty messages.globalMessage.message}">
				<div class="alert-wrapper slow-hide"><div class="alert alert-<%=bootstrapType%>">${messages.globalMessage.message}</div></div>
			</c:if><c:if test="${not empty param.err}">
				<div class="alert-wrapper slow-hide"><div class="alert alert-danger">${fn:escapeXml(param.err)}</div></div>
			</c:if>
            <h1>Javlo<span class="version"><%=IVersion.VERSION%></span></h1>
            <form id="flogin" action="${empty param.backPreview?info.currentEditURL:info.currentPreviewURL}" method="post" role="form">
            	<c:if test="${not empty param.previewEdit}">
					<input type="hidden" name="previewEdit" value="${param.previewEdit}" />
				</c:if>
				<input type="hidden" value="adminlogin" name="login-type" />
				<input type="hidden" value="edit-login" name="edit-login" />
				<input type="hidden" id="tokenid" value="" name="tokenid" />								
				<c:if test="${param.editPreview}"><input type="hidden" value="editPreview" name="true" /></c:if>
				<div class="form-group">    							
                <input id="j_username" class="form-control" type="text" name="j_username" placeholder="User name" />
                </div><div class="form-group">
				<input id="j_password" class="form-control" type="password" name="j_password" placeholder="Password" />
				</div>
				<div class="checkbox pull-left">
				<label>
				<input type="checkbox" id="autologin" name="autologin" /> Remember.
				</label>
				</div>
				<button class="btn btn-default pull-right">Login</button>
				<c:if test="${not empty globalContext.staticConfig.oauthGoogleIdClient && not nogoogle}">
				<script src="https://apis.google.com/js/platform.js" async defer></script>				
				<a title="google login" class="btn-google btn btn-default pull-right" href="<%=googleOauth.getSigninURL(ctx)%>"><svg version="1.1" xmlns="http://www.w3.org/2000/svg" width="16px" height="16px" viewBox="0 0 48 48" class="abcRioButtonSvg"><g><path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"></path><path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"></path><path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"></path><path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"></path><path fill="none" d="M0 0h48v48H0z"></path></g></svg></a>
				<script type="text/javascript">         
					function onSignIn(googleUser) {
					  var userInfo = googleUser.getAuthResponse();	
					  console.log(userInfo);
					  document.getElementById('tokenid').value = userInfo.id_token;
					 /* document.getElementById('gid').value = profile.getId();
					  document.getElementById('gname').value = profile.getName();
					  document.getElementById('gmail').value = profile.getEmail();
					  document.getElementById('gimage').value = profile.getImageUrl();*/					  
					  //document.getElementById('flogi/n').submit();
					}				
				</script>
				</c:if>
			</form>
         </div><!-- End-main -->
         
         
         
    </body>
</html>
