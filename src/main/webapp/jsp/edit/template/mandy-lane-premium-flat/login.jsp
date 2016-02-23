<%@page import="org.javlo.servlet.IVersion"
%><%@page import="org.javlo.message.MessageRepository"
%><%@page import="org.javlo.message.GenericMessage"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><!doctype html><%
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
}%><html>
    <head>
    	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <meta http-equiv="X-UA-Compatible" content="IE=edge" />
        <title>Javlo (Login)</title>
        <link rel="shortcut icon" href="img/assets/favicon.ico" />
        <link href="${info.editTemplateURL}/css/login.css" rel="stylesheet" type="text/css" />
        <style type="text/css">
		@font-face {
    		font-family: "javloFont";
    		src: url('${info.staticRootURL}fonts/Javlo-Italic.ttf') format("truetype");
		}
		</style>
    </head>
    
    <body class="login-page bg${info.random10+1}">    	
        <div class="main">
           <c:if test="${not empty messages.globalMessage && messages.globalMessage.type > 0 && not empty messages.globalMessage.message}">
				<div class="alert-wrapper slow-hide"><div class="alert alert-<%=bootstrapType%>">${messages.globalMessage.message}</div></div>
			</c:if>
            <h1>Javlo<span class="version"><%=IVersion.VERSION%></span></h1>
            <form action="${empty param.backPreview?info.currentEditURL:info.currentPreviewURL}" method="post" role="form">
            	<c:if test="${not empty param.previewEdit}">
					<input type="hidden" name="previewEdit" value="${param.previewEdit}" />
				</c:if>
				<input type="hidden" value="adminlogin" name="login-type" />
				<input type="hidden" value="edit-login" name="edit-login" />
				<c:if test="${param.editPreview}"><input type="hidden" value="editPreview" name="true" /></c:if>
				<div class="form-group">    							
                <input id="j_username" class="form-control" type="text" name="j_username" placeholder="User name" />
                </div><div class="form-group">
				<input id="j_password" class="form-control" type="password" name="j_password" placeholder="Password" />
				</div>
				<div class="checkbox pull-left">
				<label>
				<input type="checkbox" id="autologin" name="autologin" /> Remember me on this computer.
				</label>
				</div>
				<button class="btn btn-default pull-right" name="submit">Login</button>
			</form>
         </div><!-- End-main -->
    </body>
</html>
