<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><form method="post" action="${info.currentURL}" class="standard-form js-change-submit">
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
	<legend>login</legend>
	<div class="line">
		<label for="login">login :</label>
		<input type="text" name="login" id="login" value="${not empty login?login:''}"/>
	</div>
	<div class="line">
		<label for="password">password :</label>
		<input type="password" name="password" id="password" value="${not empty password?password:''}"/>
	</div>
</fieldset>


<fieldset class="mt-3">
<legend>CSS Selector</legend>
<div class="line">
	<label for="title">title :</label>
	<c:set var="titleSelector" value="${not empty param.title?title:'h1'}" />
	<input type="text" name="title" id="title" value="${not empty titleSelector?titleSelector:'h1'}" />
</div>

<div class="line">
	<label for="image">image (tag img) :</label>
	<input type="text" name="image" id="image" value="${not empty imageSelector?imageSelector:'img'}"/>
</div>

<div class="line">
	<label for="file">file :</label>
	<input type="text" name="file" id="file" value="${not empty fileSelector?fileSelector:'img'}"/>
</div>

<div class="line">
	<label for="content">content :</label>
	<input type="text" name="content" id="content" value="${not empty contentSelector?contentSelector:'.content'}" />
</div>

<div class="line">
	<label for="content">date :</label>
	<input type="text" name="date" id="date" value="${not empty dateSelector?dateSelector:'.date'}" />
</div>

</fieldset>

<div class="action">
	<button type="submit" name="import">import</button>
</div>
</c:if>
</fieldset>
</form>


