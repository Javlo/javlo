<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">
<ul id="macro">
<c:forEach var="macro" items="${macros}">
<li>
	<form id="exec-${macro.name}" action="${info.currentURL}" method="post">
		<div>
			<input type="hidden" name="webaction" value="executeMacro" />
			<input type="hidden" name="macro" value="${macro.name}" />
			<input class="action-button" type="submit" name="run" value="${macro.name}" />
		</div>
	</form>
</li>
</c:forEach>
</ul>
</div>
 
