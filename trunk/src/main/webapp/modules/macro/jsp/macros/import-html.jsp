<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<form method="post" action="${info.currentURL}" class="standard-form js-change-submit">
<c:if test="${not empty contexts}">
<div class="line">
	<label for="context">choose default context :</label>
	<select id="context" name="context">
		<option value="">choose a context...</option>
	<c:forEach var="context" items="${contexts}">
		<option ${currentContext eq context?'selected="selected"':''}>${context}</option>
	</c:forEach>
	</select>
</div>
</c:if>
<fieldset>
<legend>Import gallery</legend>
<c:if test="${empty browse}">
<input type="hidden" name="webaction" value="macro-import-html.import" />



<div class="line">
	<label for="url">url :</label>
	<input type="text" name="url" id="url" value="${param.url}" />
</div>

<fieldset>
<legend>CSS Selector</legend>
<div class="line">
	<label for="title">title :</label>
	<input type="text" name="title" id="title" value="${not empty titleSelector?titleSelector:'h1'}" />
</div>

<div class="line">
	<label for="image">image (tag img) :</label>
	<input type="text" name="image" id="image" value="${not empty imageSelector?imageSelector:'img'}"/>
</div>

<div class="line">
	<label for="content">content :</label>
	<input type="text" name="content" id="content" value="${not empty contentSelector?contentSelector:'.content'}" />
</div>

</fieldset>

<div class="action">
	<input type="submit" name="import" value="import" />
</div>
</c:if>
</fieldset>
</form>


