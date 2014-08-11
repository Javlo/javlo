<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<form method="post" action="${info.currentURL}" class="standard-form">
<fieldset>
<legend>Create business component</legend>
<input type="hidden" name="webaction" value="macro-create-business-component.create" />
<input type="hidden" name="module" value="content" />
<div class="line">
<label for="component">component</label>
<select id="component" name="component">
<c:forEach var="component" items="${components}">
<option value="${component.key}">${component.value}</option>
</c:forEach>
</select>
</div>
<div class="line">
	<label for="name">name</label>
	<input type="text" id="name" name="name" />
</div>

<div class="action">
	<input type="submit" value="create" />
</div>

</fieldset>
</form>


