<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><c:set var="canCreate" value="${not empty param.canCreate}" />

<form method="post" action="${info.currentURL}" class="standard-form">
<fieldset>
<legend>Create newsletter</legend>
<input type="hidden" name="webaction" value="macro-create-article-composition.create" />
<input type="hidden" name="module" value="macro" />
<c:if test="${fn:length(pages)>1 || canCreate}">
<div class="form-group">
<label for="root">folder</label>
<select id="root" name="root" class="form-control" onchange="if (this.value == 'new') {document.getElementById('new-group').className = 'form-group';} else {document.getElementById('new-group').className = 'form-group hidden';}">
<c:forEach var="page" items="${pages}">
<option value="${page.key}">${page.value}</option>
</c:forEach>
<c:if test="${canCreate}"><option value="new">create folder...</option></c:if>
</select>
</div>
</c:if>

<c:if test="${canCreate}"><div id="new-group" class="form-group hidden">
	<label for="newgroup">New folder</label>
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
	<input type="checkbox" id="visible" name="visible" /> Newsletter visible for other users.
	</label>
</div>

<div><input type="hidden" id="date" name="date" value="${info.currentDate}"/></div>

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

<c:if test="${globalContext.collaborativeMode}">	
	<div class="roles">	
	<fieldset>
		<legend>choose folder (no selection = everybody)</legend>		
		<c:forEach var="role" items="${adminRoles}">
			<div class="inline">			
				<input type="checkbox" id="role-${role}" name="role-${role}" />
				<label class="suffix" for="role-${role}">${role}</label>
			</div>
		</c:forEach>
	</fieldset>
	</div>
</c:if>

<div class="action">
	<input type="submit" value="create"  class="btn btn-default" />
</div>

</fieldset>
</form>


