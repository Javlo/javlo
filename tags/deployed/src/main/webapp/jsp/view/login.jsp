<%@page contentType="text/html"
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
<div class="login">
	<p><%=i18nAccess.getViewText ( "login.intro" )%></p>
	<%
	if ( request.getParameter("login") != null ) {%>
		<div class="message"><div class="error"><%=i18nAccess.getViewText ( "login.error" )%></div></div>
	<%}%>
	<form name="login" method="post">
	<div class="line">
		<label for="login"><%=i18nAccess.getViewText ( "form.login" )%></label>
	    <div class="input"><input id="login" type="text" name="j_username" value="" /></div>
	</div>
	<div class="line">
		<label for="password"><%=i18nAccess.getViewText ( "form.password" )%>:</label>
		<div class="input"><input id="password" type="password" name="j_password" value="" /></div>
	</div>		
	<input type="submit" value="<%=i18nAccess.getViewText ( "form.submit" )%>" />
	</form>
</div>
<%} else {%>
<div class="login">
	<form name="logout"  method="post">
		<input type="submit" name="__logout" value="<%=i18nAccess.getViewText ( "form.logout" )%>"/>
	</form>
</div>
<%}%>
