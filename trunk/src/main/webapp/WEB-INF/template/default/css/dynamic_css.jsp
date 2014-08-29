<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"
%><c:forEach var="row" items="${info.template.rows}"><c:forEach var="area" items="${row.areas}">
#${area.name} h1, #${area.name} h2, #${area.name} h3, #${area.name} h4, #${area.name} h5, #${area.name} h6 {	
	color: ${area.finalTitleColor};
	<c:if test="${not empty area.finalFont}">font-family: ${area.finalFont};</c:if>
}</c:forEach></c:forEach>

p,ul,ol,.table-li td.internal-link,.unsubscribe-link,.date,.external-link,.pdf-link,.file,.simple-internal-link,.global-image,.gs,.qrcode td,.pdf-head td, .table th, .table td {
	<c:if test="${not empty info.template.style.finalTextColor}">color: ${info.template.style.finalTextColor};</c:if>
	<c:if test="${not empty info.template.style.finalTextSize}">font-size: ${info.template.style.finalTextSize};</c:if>	
	<c:if test="${not empty info.template.style.finalFont}">font-family: ${info.template.style.finalFont};</c:if>
}

.visible-separation {
	margin: 10px 0;
	padding: 0;
	font-size: 0;
	line-height: 0;
	height: 0;
	border-top-style: solid;
	border-top-width: 1px;
	<c:if test="${not empty info.template.style.finalTextColor}">border-color: ${info.template.style.finalTextColor};</c:if>
	<c:if test="${empty info.template.style.finalTextColor}">border-color: #000000;</c:if>
}

<c:forEach var="row" items="${info.template.rows}"><c:forEach var="area" items="${row.areas}">
#${area.name} p, #${area.name} ul, #${area.name} ol, #${area.name} .table-li td,#${area.name} .internal-link, #${area.name} .unsubscribe-link, #${area.name} .date, #${area.name} .external-link,#${area.name} .pdf-link,#${area.name} .file, #${area.name} .simple-internal-link, #${area.name} .image-left,#${area.name}  .image-right, #${area.name} .global-image,#${area.name} .global-image,#${area.name} .gs,#${area.name} .qrcode td,#${area.name} .pdf-head td, #${area.name} .float-image .zone1,#${area.name} .float-image .zone2, #${area.name} .table td, #${area.name} .table th {	
	<c:if test="${not empty area.finalTextColor}">color: ${area.finalTextColor};</c:if>
	<c:if test="${not empty area.finalTextSize}">font-size: ${area.finalTextSize};</c:if>
	<c:if test="${not empty area.finalFont}">font-family: ${area.finalFont};</c:if>
}

#${area.name} h1 {	
	font-size: ${area.finalH1Size};	
}

#${area.name} h2 {	
	font-size: ${area.finalH2Size};	
}

#${area.name} h3 {	
	font-size: ${area.finalH3Size};	
}

#${area.name} h4 {	
	font-size: ${area.finalH4Size};	
}

#${area.name} h5 {	
	font-size: ${area.finalH5Size};	
}

#${area.name} h6 {	
	font-size: ${area.finalH6Size};	
}

</c:forEach></c:forEach>

<c:forEach var="row" items="${info.template.rows}"><c:forEach var="area" items="${row.areas}">
#${area.name} {	<c:set var="template" value="${area}" scope="request" />
<jsp:include page="styles.jsp" />
}</c:forEach></c:forEach>

<c:forEach var="row" items="${info.template.rows}">	
#${row.name} {	<c:set var="template" value="${row}" scope="request" />
<jsp:include page="styles.jsp" />
}</c:forEach>

