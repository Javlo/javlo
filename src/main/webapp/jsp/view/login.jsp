<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" 
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%><%@page contentType="text/html"
import="org.javlo.i18n.I18nAccess,
        org.javlo.user.User,
        org.javlo.user.IUserFactory,
		org.javlo.user.UserFactory"
%><%
I18nAccess i18nAccess = I18nAccess.getInstance ( request );
IUserFactory userFactory = UserFactory.createUserFactory(request);
User user = userFactory.getCurrentUser(session);
if ( user == null ) {
%>
<div class="login center-block">
	 <c:if test="${messages.globalMessage.needDisplay}">
		<div class="alert alert-${messages.globalMessage.bootstrapType}" role="alert">			
			<span>${messages.globalMessage.message}</span>
		</div>
	</c:if>
	<c:set var="notFound" value="<%=I18nAccess.KEY_NOT_FOUND%>" />
    <c:if test="${!fn:contains(i18n.view['login.message'],notFound) && not empty i18n.view['login.message']}"><div class="alert alert-info login-welcome" role="alert">${i18n.view['login.message']}</div></c:if>
	<%if ( request.getParameter("j_username") == null  && request.getParameter("pwtoken") == null && request.getParameter("resetpwd") == null) {%><div class="message"><p class="alert alert-info"><%=i18nAccess.getViewText ( "login.intro" )%></p></div><%}
	%><%if ( request.getParameter("j_username") != null) {%><div class="message"><p class="error alert alert-danger"><%=i18nAccess.getViewText ( "login.error" )%></p></div><%}
	%><%if ( request.getParameter("pwtoken") != null ) {%><div class="message"><p class="alert alert-info"><%=i18nAccess.getViewText ( "login.change-password" )%></p></div><%}
	%><%if ( request.getParameter("resetpwd") != null ) {%><div class="message"><p class="alert alert-info"><%=i18nAccess.getViewText ( "user.message.email-for-reset" )%></p></div><%}
	%><%if (request.getParameter("pwtoken") == null && request.getParameter("resetpwd") == null) {%>	
	<form name="login" method="post" action="${info.currentURL}">		
		<div class="panel panel-default">
		  	<div class="panel-body">
			<div class="line form-group">
				<label for="login"><%=i18nAccess.getViewText ( "form.login" )%></label>
			    <div class="input"><input class="form-control" id="login" type="text" name="j_username" value="" /></div>
			</div>
			<div class="line form-group">
				<label for="password"><%=i18nAccess.getViewText ( "form.password" )%>:</label>
				<div class="input"><input class="form-control" id="password" type="password" name="j_password" value="" /></div>
			</div>
			<a class="changelink" href="${info.currentURL}?resetpwd=true">${i18n.view['user.reset-password']}</a>			
			<input class="btn btn-primary pull-right" type="submit" value="<%=i18nAccess.getViewText ( "form.submit" )%>" />
			</div>
		</div>		
	</form><%} else if (request.getParameter("resetpwd") != null) {%>
	<form name="change_password_email" method="post" action="${info.currentURL}">		
		<div class="panel panel-default">
			<input type="hidden" name="webaction" value="user.askChangePassword" />
		  	<div class="panel-body">
			<div class="line form-group">
				<label for=""email""><%=i18nAccess.getViewText ( "form.email" )%></label>
			    <div class="input"><input class="form-control" id="email" type="text" name="email" value="" /></div>
			</div>			
			<div class="pull-right">
			<a class="btn btn-default" href="${info.currentURL}"><%=i18nAccess.getViewText ( "global.cancel" )%></a>						
			<input class="btn btn-primary" type="submit" value="<%=i18nAccess.getViewText ( "form.submit" )%>" />			
			</div>
			</div>
		</div>		
	</form><%} else {%>
	<form name="change_password" method="post" action="${info.currentURL}">		
		<div class="panel panel-default">
			<input type="hidden" name="webaction" value="user.changePasswordWithToken" />
			<input type="hidden" name="token" value="<%=request.getParameter("pwtoken")%>" />
		  	<div class="panel-body">
			<div class="line form-group">
				<label for="password"><%=i18nAccess.getViewText ( "form.new-password" )%></label>
			    <div class="input"><input class="form-control" id="password" type="password" name="password" value="" /></div>
			</div>
			<div class="line form-group">
				<label for="password2"><%=i18nAccess.getViewText ( "form.new-password2" )%>:</label>
				<div class="input"><input class="form-control" id="password2" type="password" name="password2" value="" /></div>
			</div>
			<div class="pull-right">
			<a class="btn btn-default" href="${info.currentURL}"><%=i18nAccess.getViewText ( "global.cancel" )%></a>			
			<input class="btn btn-primary" type="submit" value="<%=i18nAccess.getViewText ( "form.submit" )%>" />
			</div>			
			</div>
		</div>		
	</form><%}%>
	<c:if test="${not empty info.accountPageUrl}">
		<div class="account">
			<a class="btn btn-default pull-right" href="${info.accountPageUrl}"><%=i18nAccess.getViewText("form.create-account") %></a>
		</div>
	</c:if>
</div>
<%} else {%>
<div class="login">
	<form name="logout" method="post">
		<input type="hidden" name="edit-logout" value="logout" />
		<input class="btn btn-default pull-right" type="submit" name="__logout" value="<%=i18nAccess.getViewText ( "form.logout" )%>"/>
	</form>
</div>
<%}%>