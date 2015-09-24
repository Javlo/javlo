<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"
%><c:if test="${empty rows}"><c:set var="rows" value="${info.template.rows}" scope="request" /></c:if><c:forEach var="row" items="${rows}"><c:forEach var="area" items="${row.areas}">
${param.prefix}#${area.name} h1, ${param.prefix}#${area.name} h2, ${param.prefix}#${area.name} h3, ${param.prefix}#${area.name} h4, ${param.prefix}#${area.name} h5, ${param.prefix}#${area.name} h6, ${param.prefix}#${area.name} h1 div, ${param.prefix}#${area.name} h2 div, ${param.prefix}#${area.name} h3 div, ${param.prefix}#${area.name} h4 div, ${param.prefix}#${area.name} h5 div, ${param.prefix}#${area.name} h6 div {	
	color: ${area.finalTitleColor};
	<c:if test="${not empty area.finalFont}">font-family: ${area.finalFont};</c:if>
}</c:forEach></c:forEach>

${param.prefix}p,${param.prefix}ul,${param.prefix}ol,${param.prefix}.table-li td.internal-link,${param.prefix}.unsubscribe-link,${param.prefix}.date,.external-link,${param.prefix}.pdf-link,${param.prefix}.text,${param.prefix}.file,${param.prefix}.simple-internal-link,${param.prefix}.global-image,${param.prefix}.gs,${param.prefix}.qrcode td,${param.prefix}.pdf-head td, ${param.prefix}.table th, ${param.prefix}.table td {
	<c:if test="${not empty dynamicCSSTemplate.style.finalTextColor}">color: ${dynamicCSSTemplate.style.finalTextColor};</c:if>
	<c:if test="${not empty dynamicCSSTemplate.style.finalTextSize}">font-size: ${dynamicCSSTemplate.style.finalTextSize};</c:if>	
	<c:if test="${not empty dynamicCSSTemplate.style.finalFont}">font-family: ${dynamicCSSTemplate.style.finalFont};</c:if>
}

${param.prefix}.visible-separation {
	margin: 10px 0;
	padding: 0;
	font-size: 0;
	line-height: 0;
	height: 0;
	border-top-style: solid;
	border-top-width: 1px;
	<c:if test="${not empty dynamicCSSTemplate.style.finalTextColor}">border-color: ${dynamicCSSTemplate.style.finalTextColor};</c:if>
	<c:if test="${empty dynamicCSSTemplate.style.finalTextColor}">border-color: #000000;</c:if>
}

<c:forEach var="row" items="${dynamicCSSTemplate.rows}"><c:forEach var="area" items="${row.areas}">
${param.prefix}#${area.name} p, ${param.prefix}#${area.name} ul, ${param.prefix}#${area.name} ol, ${param.prefix}#${area.name} .table-li td,${param.prefix}#${area.name} .internal-link, ${param.prefix}#${area.name} .unsubscribe-link, ${param.prefix}#${area.name} .date, ${param.prefix}#${area.name} .external-link,${param.prefix}#${area.name} .pdf-link,${param.prefix}#${area.name} .file, ${param.prefix}#${area.name} .text,${param.prefix}#${area.name} .simple-internal-link, ${param.prefix}#${area.name} .image-left,${param.prefix}#${area.name}  .image-right, ${param.prefix}#${area.name} .global-image,${param.prefix}#${area.name} .global-image,${param.prefix}#${area.name} .gs,${param.prefix}#${area.name} .qrcode td,${param.prefix}#${area.name} .pdf-head td,  ${param.prefix}#${area.name} .float-image .text,  ${param.prefix}#${area.name} .float-image .zone1,${param.prefix}#${area.name} .float-image .zone2, ${param.prefix}#${area.name} .table td, ${param.prefix}#${area.name} .table th {	
	<c:if test="${not empty area.finalTextColor}">color: ${area.finalTextColor};</c:if>
	<c:if test="${not empty area.finalTextSize}">font-size: ${area.finalTextSize};</c:if>
	<c:if test="${not empty area.finalFont}">font-family: ${area.finalFont};</c:if>
}

<c:if test="${not empty area.finalLinkColor}">
	${param.prefix}#${area.name} a {	
		color: ${area.finalLinkColor};	
	}
</c:if>

${param.prefix}#${area.name} h1 {	
	font-size: ${area.finalH1Size};	
}

${param.prefix}#${area.name} h2 {	
	font-size: ${area.finalH2Size};	
}

${param.prefix}#${area.name} h3 {	
	font-size: ${area.finalH3Size};	
}

${param.prefix}#${area.name} h4 {	
	font-size: ${area.finalH4Size};	
}

${param.prefix}#${area.name} h5 {	
	font-size: ${area.finalH5Size};	
}

${param.prefix}#${area.name} h6 {	
	font-size: ${area.finalH6Size};	
}

</c:forEach></c:forEach>

<c:forEach var="row" items="${dynamicCSSTemplate.rows}"><c:forEach var="area" items="${row.areas}">
${param.prefix}#${area.name} {	<c:set var="template" value="${area}" scope="request" />
<jsp:include page="styles.jsp" />
}</c:forEach></c:forEach>

<c:forEach var="row" items="${dynamicCSSTemplate.rows}">	
${param.prefix}#${row.name} {	<c:set var="template" value="${row}" scope="request" />
<jsp:include page="styles.jsp" />
}</c:forEach>

