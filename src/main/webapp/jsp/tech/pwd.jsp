<%@page import="org.javlo.helper.SecurityHelper"%>
<%@page import="org.javlo.context.ContentContext"%>
<%@page import="org.javlo.cache.ICache"%>
<%@page import="org.javlo.context.GlobalContext"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" 
%><div class="container"><br />
<%
ContentContext ctx = ContentContext.getContentContext(request, response);

if (ctx.getCurrentEditUser() == null) {
	%>Security error.<%
	return;	
}
GlobalContext gc = GlobalContext.getInstance(request);

String pwd = request.getParameter("password");
if (pwd != null) {
	String encPass = SecurityHelper.passwordEncrypt.encrypt(pwd);
	%>
	<div class="alert alert-danger" role="alert"><%=encPass%></div>
	<%
}

%>
<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.2/css/bootstrap.min.css" integrity="sha384-Smlep5jCw/wG7hdkwQ/Z5nLIefveQRIY9nfy6xoR1uRYBtpZgI6339F5dgvm/e9B" crossorigin="anonymous">
<html>





<div class="alert alert-secondary">Password encprytion = <%=SecurityHelper.passwordEncrypt.getClass()%></div>

<form id="createPassword">
	<div class="form-group">
	<input class="form-control" type="text" name="password" />
	</div>
	<input class="btn btn-primary" type="submit" />
</form>

</div>


</html>