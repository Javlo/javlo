<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:if test="${globalContext.master}">
	<div class="alert alert-warning" role="alert">You are in master context, modifications will be apply to all site.</div>
</c:if>
<form method="post" action="${info.currentURL}">

<div class="form-group">
	<label for="login">login : </label>
	<select id="login" name="login" class="form-control" data-placeholder="${i18n.edit['global.select']}">
		<option></option>
		<c:forEach var="user" items="${users}">
			<c:if test="${not empty user.login}">
			<option value="${user.login}" ${user.login==contentContext.currentUser.login?'selected="selected"':''}>${user.login} - ${user.firstName} ${user.lastName}</option>
			</c:if>
		</c:forEach>
	</select>
</div>


<div class="action">	
	<button class="btn btn-primary pull-right btn-sm" type="submit" name="webaction" value="log-as-user.login">${i18n.edit['global.login']}</button>
</div>
</form>


<script>
document.addEventListener("DOMContentLoaded", function(event) { 
	jQuery('#login').chosen();
});
</script>

<c:if test="${not empty contentContext.currentUser && !contentContext.currentUser.editor}">
<form method="post" action="${info.currentURL}">
<button class="btn btn-secondary pull-right btn-sm" type="submit" name="webaction" value="log-as-user.logout">${i18n.edit['global.logout']}</button>
</form>
</c:if>
