<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<div class="${box.action?'widgetbox2':'widgetbox'} ${not empty box.title?'width-title':'no-title'}">
	<c:if test="${not empty box.title}"><h3><span>${box.title}</span></h3></c:if>
	<jsp:include page="${box.renderer}" />
</div>