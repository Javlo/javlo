<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><form method="post" action="${info.currentURL}" class="standard-form">
<fieldset>
<legend>Create article</legend>
<input type="hidden" name="webaction" value="macro-create-article.create" />
<input type="hidden" name="module" value="content" />
<input type="hidden" name="page" value="${info.pageName}" />
<c:if test="${fn:length(pages)>1}">
<div class="form-group">
<label for="root">group</label>
<select class="form-control" id="root" name="root">
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
<c:if test="${fn:length(info.languages)>1}">
<div class="form-group">
<label for="lang">Language</label>
<select class="form-control" id="lang" name="lang">
<c:forEach var="lg" items="${info.contentLanguages}">
<option value="${lg}" ${info.contentLanguage == lg?'selected="selected"':''}>${lg}</option>
</c:forEach>
</select>
</div>
</c:if>
<div class="form-group">
	<label for="date">date</label>
	<input type="text" class="datepicker form-control" id="date" name="date" />
</div>
<c:if test="${not empty tags}">

<!-- <fieldset>
<legend>tags</legend>
<c:forEach var="tag" items="${tags}">
<div class="inline">		
	<input type="checkbox" class="tag" id="tag-${tag}" name="tag-${tag}" />
	<label class="suffix" for="tag-${tag}">${tag}</label>
</div>
</c:forEach>
</fieldset>  -->

</c:if>
<div class="line">
	<label for="create">create structure</label>
	<input type="checkbox" id="create" name="create" checked="checked" onclick="if (document.getElementById('create').checked) {document.getElementById('duplicate').checked = false; document.getElementById('children').checked = false;}" />
</div>
<div class="line">
	<label for="duplicate">duplicate page</label>
	<input type="checkbox" id="duplicate" name="duplicate" onclick="if (document.getElementById('duplicate').checked) {document.getElementById('create').checked = false;} if (!document.getElementById('duplicate').checked) {document.getElementById('children').checked = false}" />
</div>
<div class="line">
	<label for="children">duplicate page and children</label>
	<input type="checkbox" id="children" name="children" onclick="document.getElementById('create').checked = false; document.getElementById('duplicate').checked = true;" />
</div>

<c:if test="${globalContext.collaborativeMode}">
	<div class="line">
		<label for="email">send email</label>
		<input type="checkbox" id="email" name="email" />
	</div>
	<div class="roles">	
	<fieldset>
		<legend>choose group (no selection = everybody)</legend>		
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
	<input class="btn btn-primary pull-right" type="submit" value="create" />
</div>

</fieldset>
</form>