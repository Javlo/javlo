<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"
%><c:forEach var="row" items="${info.template.rows}"><c:forEach var="area" items="${row.areas}">
#${area.name} h1, #${area.name} h2, #${area.name} h3, #${area.name} h4, #${area.name} h5, #${area.name} h6 {	
	color: ${area.finalTitleColor};
	<c:if test="${not empty area.finalFont}">font-family: ${area.finalFont};</c:if>
}</c:forEach></c:forEach>

p,ul,ol,.internal-link,.unsubscribe-link,.date,.external-link,.pdf-link,.file,.simple-internal-link,.global-image,.gs,.qrcode td,.pdf-head td {
	<c:if test="${not empty area.finalTextColor}">color: ${info.template.style.finalTextColor};</c:if>
	<c:if test="${not empty area.finalTextSize}">font-size: ${info.template.style.finalTextSize};</c:if>	
	<c:if test="${not empty area.finalFont}">font-family: ${info.template.style.finalFont};</c:if>
}

<c:forEach var="row" items="${info.template.rows}"><c:forEach var="area" items="${row.areas}">
#${area.name} p, #${area.name} ul, #${area.name} ol, #${area.name} .internal-link, #${area.name} .unsubscribe-link, #${area.name} .date, #${area.name} .external-link,#${area.name} .pdf-link,#${area.name} .file, #${area.name} .simple-internal-link, #${area.name} .image-left,#${area.name}  .image-right, #${area.name} .global-image,#${area.name} .global-image,#${area.name} .gs,#${area.name} .qrcode td,#${area.name} .pdf-head td {	
	<c:if test="${not empty area.finalTextColor}">color: ${area.finalTextColor};</c:if>
	<c:if test="${not empty area.finalTextSize}">font-size: ${area.finalTextSize};</c:if>
	<c:if test="${not empty area.finalFont}">font-family: ${area.finalFont};</c:if>
}</c:forEach></c:forEach>

<c:forEach var="row" items="${info.template.rows}"><c:forEach var="area" items="${row.areas}">
#${area.name} {	<c:set var="template" value="${area}" scope="request" />
<jsp:include page="styles.jsp" />
}</c:forEach></c:forEach>

<c:forEach var="row" items="${info.template.rows}">	
#${row.name} {	<c:set var="template" value="${row}" scope="request" />
<jsp:include page="styles.jsp" />
}</c:forEach>

