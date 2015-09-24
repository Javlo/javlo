<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:set var="canCreate" value="${not empty param.canCreate}" />

<form method="post" action="${info.currentURL}">
<fieldset>
<legend>Create newsletter ${canCreate}</legend>
<input type="hidden" name="webaction" value="macro-create-article-composition.create" />
<input type="hidden" name="module" value="macro" />
<c:if test="${fn:length(pages)>1 || canCreate}">
<div class="form-group">
<label for="root">group</label>
<select id="root" name="root" class="form-control" onchange="if (this.value == 'new') {document.getElementById('new-group').className = 'form-group';} else {document.getElementById('new-group').className = 'form-group hidden';}">
<c:forEach var="page" items="${pages}">
<option value="${page.key}">${page.value}</option>
</c:forEach>
<c:if test="${canCreate}"><option value="new">create group...</option></c:if>
</select>
</div>
</c:if>

<c:if test="${canCreate}"><div id="new-group" class="form-group hidden">
	<label for="newgroup">New group</label>
	<input type="text" id="newgroup" name="newgroup" value="" class="form-control" />
</div></c:if>

<c:if test="${fn:length(pages)==1}">
<c:forEach var="page" items="${pages}">
<input type="hidden" name="root" value="${page.key}" />
</c:forEach>
</c:if>

<div class="form-group">
	<label for="title">name</label>
	<input type="text" id="title" name="title" value="${param.title}" class="form-control" />
</div>

<div class="checkbox">
	<label>
	<input type="checkbox" id="hidden" name="hidden" checked="checked" /> Hidden newsletter for other users.
	</label>
</div>

<div><input type="hidden" id="date" name="date" value="${info.date}"/></div>

<c:if test="${not empty info.page.rootOfChildrenAssociation}">
<div class="checkbox">
	<label>
	<input type="checkbox" id="duplicate" name="duplicate" /> Duplicate current structure
	</label>
</div>
</c:if>

<c:if test="${not empty sourcePage}">
<div class="checkbox">
	<label>
	<input type="checkbox" id="duplicate" name="duplicate" checked="checked" /> Duplicate : ${sourcePage.info.title}
	<input type="hidden" name="page" value="${sourcePage.id}" />
	</label>
</div>
</c:if>

<div class="action">
	<input type="submit" value="create"  class="btn btn-default" />
</div>

</fieldset>
</form>


