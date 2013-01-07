<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<form method="post" action="${info.currentURL}" class="standard-form">
<fieldset>
<legend>Create article</legend>
<input type="hidden" name="webaction" value="macro-create-article.create" />
<input type="hidden" name="module" value="content" />
<div class="line">
<label for="root">group</label>
<select id="root" name="root">
<c:forEach var="page" items="${pages}">
<option value="${page.key}">${page.value}</option>
</c:forEach>
</select>
</div>
<div class="line">
	<label for="date">date</label>
	<input type="text" class="datepicker" id="date" name="date" />
</div>
<div class="action">
	<input type="submit" value="create" />
</div>

</fieldset>
</form>


