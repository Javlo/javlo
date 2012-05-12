<%@ page contentType="text/html"
	import="org.javlo.i18n.I18nAccess,
	    	org.javlo.context.GlobalContext,
	    	org.javlo.config.StaticConfig,
	    	org.javlo.context.ContentContext,
	    	org.javlo.helper.XHTMLHelper,
	    	org.javlo.helper.URLHelper"

%><%
GlobalContext globalContext = GlobalContext.getInstance(request);
StaticConfig staticConfig = StaticConfig.getInstance(application);
ContentContext ctx = ContentContext.getContentContext ( request, response );
I18nAccess i18nAccess = I18nAccess.getInstance ( globalContext,session );
String popup = request.getParameter("popup");
String param = "";
if (popup != null) {
	param = "<input type=\"hidden\" name=\"popup\" value=\"welcome\" />";
}
String actionURL;
if (request.getAttribute("login_jee") != null) {
	actionURL = "j_security_check";
} else {
	actionURL = URLHelper.createURL(ctx);
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="fr-BE" xml:lang="fr-BE">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=<%=ContentContext.CHARACTER_ENCODING%>" />
<title><%=i18nAccess.getText("login.authentification")%> - <%=globalContext.getGlobalTitle()%></title>
<link rel="stylesheet" type="text/css" href="<%=URLHelper.createStaticURL(ctx,"/css/edit.css")%>" /><%
if (ctx.getRenderMode() == ContentContext.MAILING_MODE) {%>
<link rel="stylesheet" type="text/css" href="<%=URLHelper.createStaticURL(ctx,"/css/mailing_"+globalContext.getLook()+".css")%>" /><%
} else {%>
<link rel="stylesheet" type="text/css" href="<%=URLHelper.createStaticURL(ctx,"/css/"+globalContext.getLook()+".css")%>" /><%
}%>
</head>
<body>
<div id="login">
<div id="main">
<div id="header"><%=staticConfig.getHeaderMessage(ctx.getLanguage()).trim().length()>0?"<div class=\"message\">"+staticConfig.getHeaderMessage(ctx.getLanguage())+"</div>":""%></div>
<div id="body">
<%=staticConfig.getWelcomeMessage(ctx.getLanguage()).trim().length()>0?"<div class=\"message\">"+staticConfig.getWelcomeMessage(ctx.getLanguage())+"</div>":""%>
<div class="error-message"><%=i18nAccess.getText("login.error-browser")%></div>
<div class="box">
<div class="corner top-left"><div class="corner top-right"><span></span></div></div>
<div class="title"><span><%=globalContext.getGlobalTitle()%></span></div>
<div class="box-body">
<div class="icone" lang="en"><%=XHTMLHelper.getIconesCode(ctx, "password.png", "password icone")%></div>
<form method="post" action="<%=actionURL%>">
<div><input type="hidden" name="webaction" value="adminlogin"/><%=param%></div>
<div class="label">
<label for="j_username"><%=i18nAccess.getText("login.user")%> &nbsp; </label>
</div>

<div class="input">
<input id="j_username" type="text" name="j_username" />
</div>

<div class="label">
<label for="j_password"><%=i18nAccess.getText("login.password")%> &nbsp; </label>
</div>

<div class="input">
<input id="j_password" type="password" name="j_password"  />
</div>
<div class="line">
<%
if (ctx.getRenderMode() == ContentContext.EDIT_MODE) {%>
		<label for="autologin"><%=i18nAccess.getText ( "login.auto" )%>:</label>
		<div class="input"><input id="autologin" type="checkbox" name="autologin" /></div>
		<%
}%>
	</div>
<div style="clear: both; font-size:0px;">&nbsp;</div>

<div id="send">
<input type="submit" name="edit-login" id="edit-login" value="<%=i18nAccess.getText("login.enter")%>" />
</div>
</form>
</div>
<div class="corner bottom-left"><div class="corner bottom-right"><span></span></div></div>			
</div>
<img class="shadow" src="<%=URLHelper.createStaticURL(ctx, "/images/edit/deco_login.png")%>" alt="deco" />
</div>
<div id="footer"><%=staticConfig.getFooterMessage(ctx.getLanguage()).trim().length()>0?"<div class=\"message\">"+staticConfig.getFooterMessage(ctx.getLanguage())+"</div>":""%></div>
</div>
</div>
</body>
</html>