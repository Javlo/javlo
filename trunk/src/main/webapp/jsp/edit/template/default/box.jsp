<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="${box.action?'widgetbox2':'widgetbox'}">
	<c:if test="${box.title != null}">
	<h3 class="">
	<span>${box.title}</span>
	</h3>
	</c:if>			
	<jsp:include page="${box.renderer}" />			
</div>

