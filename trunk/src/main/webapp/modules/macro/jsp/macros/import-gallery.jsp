<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<form method="post" action="${info.currentURL}" class="standard-form">
<fieldset>
<legend>Import gallery</legend>
<c:if test="${empty browse}">
<input type="hidden" name="webaction" value="macro-import-gallery.import" />

<div class="line">
	<label for="url">gallery url :</label>
	<input type="text" name="url" id="url" />
</div>

<div class="line">
	<label for="dir">local directory</label>
	<input type="text" name="dir" id="dir" />
</div>

<div class="action">
	<input type="submit" value="import">
</div>
</c:if>
<c:if test="${not empty browse}">
	<div class="line">
		<a href="${browse}" class="action-button">add meta data...</a>
	</div>
</c:if>
</fieldset>
</form>


