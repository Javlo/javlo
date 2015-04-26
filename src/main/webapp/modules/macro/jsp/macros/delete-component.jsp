<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<h2>Choose component type</h2>
<form method="post" action="${info.currentURL}" class="standard-form">
<fieldset>
<input type="hidden" name="webaction" value="macro-delete-component.delete" />
<c:forEach var="type" items="${types}">
	<div class="checkbox">
		<label><input name="types" value="${type}" type="checkbox" /> ${type}</label>
	</div>	
</c:forEach>
<button type="submit" class="btn btn-primary">Delete</button>
</fieldset>
</form>




