<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:if test="${globalContext.master}">
	<div class="alert alert-warning" role="alert">You are in master context, modifications will be apply to all site.</div>
</c:if>
<form method="post" action="${info.currentURL}" class="standard-form">
<fieldset>
<legend>Roles</legend>
<input type="hidden" name="webaction" value="update-user-role.update" />
<div class="roles">	
	<c:forEach var="role" items="${roles}">
		<div class="inline">			
			<input type="checkbox" id="role-${role}" name="role" value="${role}" />
			<label class="suffix" for="role-${role}">${role}</label>
		</div>
	</c:forEach>
</div>

</fieldset>

<div class="action">
	<input class="btn btn-primary pull-right btn-sm" type="submit" name="remove" value="remove on everybody" />
	<input class="btn btn-primary pull-right btn-sm" type="submit" name="add" value="add on everybody" />
</div>


</form>


