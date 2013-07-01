<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="content">

<form class="standard-form" id="create-site" method="post" action="${info.currentURL}">
	<input type="hidden" name="webaction" value="remote.update" />
	<c:if test="${not empty remote}">
	<input type="hidden" name="id" value="${remote.id}" />
	</c:if>
	<div class="line">
		<label for="priority">priority</label>
		<select id="priority" name="priority">
			<option value="1">low</option>
			<option value="2">middle</option>
			<option value="3">high</option>
		</select>
	</div>
	<div class="line">
		<label for="url">url</label>
		<input type="text" id="url" name="url" />
	</div>
	<div class="line">
		<label for="text">text</label>
		<input type="text" id="text" name="text" />
	</div>	
	<div class="action">
		<input type="submit" value="${i18n.view['global.ok']}" />
	</div>	
</form>

</div>