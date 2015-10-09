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
if ( user != null ) {
%>
<div class="login center-block">
	<c:set var="notFound" value="<%=I18nAccess.KEY_NOT_FOUND%>" />
    <c:if test="${not (not empty messages.globalMessage && messages.globalMessage.type > 0 && !empty messages.globalMessage.message)}"><c:if test="${!fn:contains(i18n.view['login.password.change'],notFound) && not empty i18n.view['login.password.change']}"><div class="alert alert-info login-welcome" role="alert">${i18n.view['login.password.change']}</div></c:if></c:if>	
	<c:if test="${not empty messages.globalMessage && messages.globalMessage.type > 0 && !empty messages.globalMessage.message}"><div class="error alert alert-${messages.globalMessage.bootstrapType}">${messages.globalMessage.message}</div></c:if>	
	<c:if test="${empty passwordChanged}">
	<form name="login" method="post" action="${info.currentURL}">		
		<div class="panel panel-default">
		  	<div class="panel-body">
		  	<input type="hidden" name="webaction" value="user.changePassword2Check" />
			<div class="line form-group">
				<label for="password"><%=i18nAccess.getViewText ( "form.new-password" )%>:</label>
				<div class="input"><input class="form-control" id="password" type="password" name="newpassword" value="" /></div>
			</div>
			<div class="line form-group">
				<label for="password"><%=i18nAccess.getViewText ( "form.new-password2" )%>:</label>
				<div class="input"><input class="form-control" id="password" type="password" name="newpassword2" value="" /></div>
			</div>		
			<input class="btn btn-default pull-right" type="submit" value="<%=i18nAccess.getViewText ( "form.submit" )%>" />
			</div>
		</div>		
	</form>
	</c:if><c:if test="${not empty passwordChanged}">
		<a class="btn btn-default pull-right" href="${info.currentURL}"><%=i18nAccess.getViewText ( "global.ok" )%></a>
	</c:if>
</div>
<%} else {%>
<div class="message"><div class="error alert alert-danger">Error, no user found.</div></div>
<%}%>
