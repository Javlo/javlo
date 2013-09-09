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
<c:if test="${not empty tags}">

<fieldset>
<legend>tags</legend>
<c:forEach var="tag" items="${tags}">
<div class="inline">		
	<input type="checkbox" class="tag" id="tag-${tag}" name="tag-${tag}" />
	<label class="suffix" for="tag-${tag}">${tag}</label>
</div>
</c:forEach>
</fieldset>

</c:if>
<div class="line">
	<label for="create">create structure</label>
	<input type="checkbox" id="create" name="create" checked="checked" />
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
	<input type="submit" value="create" />
</div>

</fieldset>
</form>


