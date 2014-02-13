<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<form method="post" action="${info.currentURL}" class="standard-form">
<fieldset>
<legend>Create article</legend>
<input type="hidden" name="webaction" value="macro-create-article-composition.create" />
<input type="hidden" name="module" value="macro" />

<c:if test="${fn:length(pages)>1}">
<div class="line">
<label for="root">group</label>
<select id="root" name="root">
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

<div class="line">
	<label for="title">name</label>
	<input type="text" id="title" name="title" value="${param.title}"/>
</div>

<div class="line">
	<label for="date">date</label>
	<input type="text" class="datepicker" id="date" name="date" value="${param.date}"/>
</div>

<c:if test="${not empty info.page.rootOfChildrenAssociation}">
<div class="line">
	<label for="duplicate">Duplicate current structure</label>
	<input type="checkbox" id="duplicate" name="duplicate" />
</div>
</c:if>

<div class="action">
	<input type="submit" value="create" />
</div>

</fieldset>
</form>


