<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<form method="post" action="${info.currentURL}" class="standard-form">
<fieldset>
<legend>Create article</legend>
<input type="hidden" name="webaction" value="macro-create-article-composition.create" />
<input type="hidden" name="module" value="macro" />

<c:if test="${fn:length(pages)>1}">
<div class="form-group">
<label for="root">group</label>
<select id="root" name="root" class="form-control">
<c:forEach var="page" items="${pages}">
<option value="${page.key}">${page.value}</option>
</c:forEach>
</select>
</div>
</c:if>

<c:if test="${fn:length(pages)==1}">
<c:forEach var="page" items="${pages}">
<input type="hidden" name="root" value="${page.key}" />
</c:forEach>
</c:if>

<div class="form-group">
	<label for="title">name</label>
	<input type="text" id="title" name="title" value="${param.title}" class="form-control" />
</div>

<div><input type="hidden" id="date" name="date" value="${info.date}"/></div>

<c:if test="${not empty info.page.rootOfChildrenAssociation}">
<div class="checkbox">
	<label>
	<input type="checkbox" id="duplicate" name="duplicate" /> Duplicate current structure
	</label>
</div>
</c:if>

<div class="action">
	<input type="submit" value="create"  class="btn btn-default" />
</div>

</fieldset>
</form>


