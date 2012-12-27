<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">
<c:if test="${empty macroRenderer}">
	<ul id="macro">
	<c:forEach var="macro" items="${imacros}">
	<li>
		<form id="exec-${macro.name}" action="${info.currentURL}" method="post">
			<div>
				<input type="hidden" name="webaction" value="executeInteractiveMacro" />
				<input type="hidden" name="macro" value="${macro.name}" />
				<input class="action-button" type="submit" name="run" value="${macro.name}" />
			</div>
		</form>
	</li>
	</c:forEach>
	</ul>
</c:if>
<c:if test="${not empty macroRenderer}">
<div class="cancel">
	<form action="${info.currentURL}" method="post">
		<div>
			<input type="hidden" name="webaction" value="closeMacro" />
			<input class="action-button" type="submit" value="${i18n.edit['global.cancel']}" />			 
		</div>
	</form>
</div>
<div class="macro-interface">
<jsp:include page="${macroRenderer}"></jsp:include>
</c:if>
</div>
</div>
 
