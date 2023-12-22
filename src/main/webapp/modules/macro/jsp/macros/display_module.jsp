<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:forEach var="js" items="${currentModule.JS}"><script type="text/javascript" src="<jv:url value='${js}?ts=${info.ts}' />"></script></c:forEach>

<h2>[display_module.jsp] include : ${module.currentRenderer}${params}</h2>

<div class="maincontent ${currentModule.name}">
<!-- [display_module.jsp] include : ${module.currentRenderer}${params} -->
<jsp:include page="${module.currentRenderer}${params}" />
</div>

<c:forEach var="css" items="${currentModule.CSS}">
	<link rel="stylesheet" href="<jv:url value='${css}?ts=${info.ts}' />" /></c:forEach>