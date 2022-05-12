<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:forEach var="js" items="${currentModule.JS}">
<script type="text/javascript" src="<jv:url value='${js}?ts=${info.ts}' />"></script></c:forEach>

<div class="maincontent ${currentModule.name}"><jsp:include page="${module.currentRenderer}" /></div>

<c:forEach var="css" items="${currentModule.CSS}">
	<link rel="stylesheet" href="<jv:url value='${css}?ts=${info.ts}' />" /></c:forEach>