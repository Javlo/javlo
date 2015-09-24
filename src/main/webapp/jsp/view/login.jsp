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
	<c:set var="notFound" value="<%=I18nAccess.KEY_NOT_FOUND%>" />
    <c:if test="${!fn:contains(i18n.view['login.message'],notFound) && not empty i18n.view['login.message']}"><div class="alert alert-info login-welcome" role="alert">${i18n.view['login.message']}</div></c:if>
	<%if ( request.getParameter("j_username") == null ) {%><p class="alert alert-info"><%=i18nAccess.getViewText ( "login.intro" )%></p><%}%>
	<%if ( request.getParameter("j_username") != null ) {%><div class="message"><div class="error alert alert-danger"><%=i18nAccess.getViewText ( "login.error" )%></div></div><%}%>	
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
			<input class="btn btn-default pull-right" type="submit" value="<%=i18nAccess.getViewText ( "form.submit" )%>" />
			</div>
		</div>		
	</form>
</div>
<%} else {%>
<div class="login">
	<form name="logout"  method="post">
		<input type="hidden" name="edit-logout" value="logout" />
		<input class="btn btn-default pull-right" type="submit" name="__logout" value="<%=i18nAccess.getViewText ( "form.logout" )%>"/>
	</form>
</div>
<%}%>
