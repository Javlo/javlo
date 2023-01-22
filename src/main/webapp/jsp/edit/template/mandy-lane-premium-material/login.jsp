<%@page import="org.javlo.helper.URLHelper"%><%@page import="org.javlo.service.social.ISocialNetwork
"%><%@page import="org.javlo.context.ContentContext"%><%@page import="org.javlo.service.social.SocialService"%><%@page import="org.javlo.servlet.IVersion"%><%@page import="org.javlo.message.MessageRepository"%><%@page import="org.javlo.message.GenericMessage"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%><!doctype html>
<%
MessageRepository msgRepo = MessageRepository.getInstance(request); // load request message
GenericMessage msg = msgRepo.getGlobalMessage();
String bootstrapType = "success";
switch (msg.getType()) {
	case GenericMessage.ERROR :
		bootstrapType = "danger";
		break;
	case GenericMessage.INFO :
		bootstrapType = "info";
		break;
	case GenericMessage.HELP :
		bootstrapType = "info";
		break;
	case GenericMessage.ALERT :
		bootstrapType = "warning";
		break;
	case GenericMessage.SUCCESS :
		bootstrapType = "success";
		break;
}
ContentContext ctx = ContentContext.getContentContext(request, response);
SocialService socialService = SocialService.getInstance(ctx);
ISocialNetwork googleOauth = socialService.getGoogle();
//googleOauth.setRedirectURL(URLHelper.createStaticURL(ctx.getContextForAbsoluteURL(), "/oauth2callbackadmin"));
//googleOauth.setClientId(ctx.getGlobalContext().getStaticConfig().getOauthGoogleIdClient());
//googleOauth.setClientSecret(ctx.getGlobalContext().getStaticConfig().getOauthGoogleSecret());
%><html>
<head>
<meta name="GOOGLEBOT" content="NOSNIPPET" />
<meta name="robots" content="noindex, nofollow" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<meta name="viewport" content="width=device-width, initial-scale=1" />

<title>Javlo (Login)</title>
<link rel="shortcut icon" href="img/assets/favicon.ico" />
<link href="${info.staticRootURL}css/main_edit_and_preview.css" rel="stylesheet" type="text/css" />
<link href="${info.editTemplateURL}/css/login.css" rel="stylesheet" type="text/css" />
<style type="text/css">
body {
	background: linear-gradient(140deg, #1B2D49 0%, rgba(255, 255, 255, 1) 100%);
  background-repeat: repeat;
  }
h1 .logo svg {
	width: 180px;
	margin-bottom: .5rem;
	fill: #1B2D49;
}
</style>
<c:if test="${not empty globalContext.staticConfig.oauthGoogleIdClient && not nogoogle}">
	<meta name="google-signin-client_id" content="${globalContext.staticConfig.oauthGoogleIdClient}" />
</c:if>
</head>

<body class="login-page bg${info.random10+1} ${not empty param.err || not empty messages.globalMessage && messages.globalMessage.type > 0 && not empty messages.globalMessage.message?'wmsg':'nomsg'}">
	<div class="main">
		<c:if test="${not empty messages.globalMessage && messages.globalMessage.type > 0 && not empty messages.globalMessage.message}">
			<div class="alert-wrapper slow-hide">
				<div class="alert alert-<%=bootstrapType%>">${messages.globalMessage.message}</div>
			</div>
		</c:if>
		<c:if test="${not empty param.err}">
			<div class="alert-wrapper slow-hide">
				<div class="alert alert-danger">${fn:escapeXml(param.err)}</div>
			</div>
		</c:if>
		<h1>
			<span class="logo">${info.javloLogoHtml}</span>
			<span class="version"><%=IVersion.VERSION%></span>
		</h1>

		<c:if test="${empty param.pwtoken}">

			<form id="flogin" action="${empty param.backPreview?info.currentEditURL:info.currentPreviewURL}" method="post" role="form">
				<c:if test="${not empty param.previewEdit}">
					<input type="hidden" name="previewEdit" value="${param.previewEdit}" />
				</c:if>
				<input type="hidden" value="adminlogin" name="login-type" />
				<input type="hidden" value="edit-login" name="edit-login" />
				<input type="hidden" id="tokenid" value="" name="tokenid" />
				<c:if test="${param.editPreview}">
					<input type="hidden" value="editPreview" name="true" />
				</c:if>
				<div class="form-group">
					<i class="bi bi-person-fill"></i>
					<input id="j_username" class="form-control" type="text" name="j_username" placeholder="Username or Email" />
				</div>
				<div class="form-group">
					<i class="bi bi-lock-fill"></i>
					<input id="j_password" class="form-control" type="password" name="j_password" placeholder="Password" />
				</div>
				<div class="checkbox pull-left check-remember">
					<label> <input type="checkbox" id="autologin" name="autologin" />Remember me
					</label>
				</div>
				<button class="btn btn-default pull-right">Login</button>

			</form>
			<div class="login-reset-password">
				<label for="resetpwd-input"><a href="#resetpwd" onclick="showHideResetPwd(); return false;">${i18n.edit['login.reset-password']}</a></label>
				<script>
					function showHideResetPwd() {
						let item = document.getElementById("resetpwd");
						if (item.classList.contains("hidden")) {
							item.classList.remove("hidden");
						} else {
							item.classList.add("hidden");
						}
					}
				</script>
			</div>
			<div id="resetpwd" class="hidden">
				<form id="fresetpwd" action="${info.currentEditURL}" method="post" role="form">
					<input type="hidden" name="webaction" value="unsecure.askChangePassword" />
					<div class="form-group">
						<i class="bi bi-person-fill"></i>
						<input type="email" id="resetpwd-input" class="form-control mb-3" name="email" placeholder="${i18n.edit['global.email']}" />
					</div>
					<button type="submit" class="btn-reset btn btn-default pull-right">${i18n.edit['global.reset']}</button>
				</form>
			</div>

		</c:if>
		<c:if test="${not empty param.pwtoken}">
			<form name="change_password" method="post" action="${info.currentURL}">
				<input type="hidden" name="webaction" value="unsecure.changePasswordWithToken" />
				<input type="hidden" name="token" value="${param.pwtoken}" />
				<div class="form-group">
					<div class="input">
						<input class="form-control" id="password" type="password" name="password" value="" placeholder="${i18n.edit['admin.form.password']}" />
					</div>
				</div>
				<div class="form-group">
					<div class="input">
						<input class="form-control" id="password2" type="password" name="password2" value="" placeholder="${i18n.edit['admin.form.password-confirm']}" />
					</div>
				</div>
				<div class="pull-right">
					<a class="btn btn-default" href="${info.currentURL}">${i18n.edit["global.cancel"]}</a>
					<input class="btn btn-primary" type="submit" value="${i18n.edit['global.submit']}" />
				</div>
			</form>
		</c:if>
	</div>
	<!-- End-main -->



</body>
</html>
