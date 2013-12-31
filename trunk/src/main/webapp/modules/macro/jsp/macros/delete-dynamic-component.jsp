<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<form method="post" action="${info.currentURL}" class="standard-form">
<fieldset>
<input type="hidden" name="webaction" value="macro-delete-dynamic-component.delete" />
<c:if test="${empty foundField || not empty param.confirm}">
<legend>Delete Dynamic Component</legend>
<textarea name="filter"></textarea>
<input type="submit" >
</c:if>
<c:if test="${not empty foundField && empty param.confirm}">
<input type="hidden" name="filter" value="${param.filter}" />
<ul>
	<li>Dynamic Component found           : ${foundField}</li>
	<li>Dynamic Component must be deleted : ${deleteField}</li>
	<li>Dynamic Component after delete    : ${resultField}</li>
</ul>
<input type="submit" name="confirm" value="confirm delete" />
</c:if>
</fieldset>
</form>




