<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib uri="jakarta.tags.functions" prefix="fn"
%><%
/*
 * This page is included inside the inline style block of index.html, not served
 * as a standalone css file, and the first include carries no prefix of its own
 * so the parameter comes straight from the visitor request. Anything but a
 * plain css selector prefix could close the style element and inject markup.
 */
String cssPrefixParam = request.getParameter("prefix");
if (cssPrefixParam == null || !cssPrefixParam.matches("[A-Za-z0-9_.#\\- ]{0,64}")) {
	cssPrefixParam = "";
}
request.setAttribute("safeCssPrefix", cssPrefixParam);
%><c:if test="${empty rows}"><c:set var="rows" value="${info.template.rows}" scope="request" /></c:if><c:forEach var="row" items="${rows}"><c:forEach var="area" items="${row.areas}">
<c:forEach var = "i" begin = "1" end = "6" varStatus="status">${safeCssPrefix}#${area.name} h${i}, ${safeCssPrefix}#${area.name} h${i} div, ${safeCssPrefix}#${area.name} h${i} a${status.last?'':','}</c:forEach> {	
	<c:if test="${not empty area.finalTitleColor}">color: ${area.finalTitleColor};</c:if>
	text-decoration: none;
	<c:if test="${not empty area.finalFont}">font-family: ${area.finalFont};</c:if>
}</c:forEach></c:forEach>

<c:if test="${not empty currentPage}">${safeCssPrefix} {
	<c:if test="${not empty currentPage.color}">background-color: ${currentPage.color};</c:if>
}</c:if>

<c:if test="${not empty currentPage}">.pdf ${safeCssPrefix} {
	<c:if test="${not empty currentPage.imageBackground}">background-image: url('${currentPage.imageBackground.previewURL}'); background-position:left top; background-size: 100%;</c:if>
}</c:if>

${safeCssPrefix}p,${safeCssPrefix}ul,${safeCssPrefix}ol,${safeCssPrefix}.table-li td.internal-link,${safeCssPrefix}.unsubscribe-link,${safeCssPrefix}.date,.external-link,${safeCssPrefix}.pdf-link,${safeCssPrefix}.text,${safeCssPrefix}.file,${safeCssPrefix}.simple-internal-link,${safeCssPrefix}.global-image,${safeCssPrefix}.gs,${safeCssPrefix}.qrcode td,${safeCssPrefix}.pdf-head td, ${safeCssPrefix}.table th, ${safeCssPrefix}.table td {
	<c:if test="${not empty dynamicCSSTemplate.style.finalTextColor}">color: ${dynamicCSSTemplate.style.finalTextColor};</c:if>
	<c:if test="${not empty dynamicCSSTemplate.style.finalTextSize}">font-size: ${dynamicCSSTemplate.style.finalTextSize};</c:if>	
	<c:if test="${not empty dynamicCSSTemplate.style.finalFont}">font-family: ${dynamicCSSTemplate.style.finalFont};</c:if>
}

${safeCssPrefix}.visible-separation {	
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

${safeCssPrefix}.visible-large {	
	margin: 10px 0;
	padding: 0;
	font-size: 0;
	line-height: 0;
	height: 0;
	border-top-style: solid;
	border-top-width: 3px;
	<c:if test="${not empty dynamicCSSTemplate.style.finalTextColor}">border-color: ${dynamicCSSTemplate.style.finalTextColor};</c:if>
	<c:if test="${empty dynamicCSSTemplate.style.finalTextColor}">border-color: #000000;</c:if>
}

<c:forEach var="row" items="${dynamicCSSTemplate.rows}"><c:forEach var="area" items="${row.areas}">
${safeCssPrefix}#${area.name} p, ${safeCssPrefix}#${area.name} ul, ${safeCssPrefix}#${area.name} ol, ${safeCssPrefix}#${area.name} .table-li td,${safeCssPrefix}#${area.name} .internal-link, ${safeCssPrefix}#${area.name} .unsubscribe-link, ${safeCssPrefix}#${area.name} .date, ${safeCssPrefix}#${area.name} .external-link,${safeCssPrefix}#${area.name} .pdf-link,${safeCssPrefix}#${area.name} .file, ${safeCssPrefix}#${area.name} .text,${safeCssPrefix}#${area.name} .simple-internal-link, ${safeCssPrefix}#${area.name} .image-left,${safeCssPrefix}#${area.name}  .image-right, ${safeCssPrefix}#${area.name} .global-image,${safeCssPrefix}#${area.name} .global-image,${safeCssPrefix}#${area.name} .gs,${safeCssPrefix}#${area.name} .qrcode td,${safeCssPrefix}#${area.name} .pdf-head td,  ${safeCssPrefix}#${area.name} .float-image .text,  ${safeCssPrefix}#${area.name} .float-image .zone1,${safeCssPrefix}#${area.name} .float-image .zone2, ${safeCssPrefix}#${area.name} .table td, ${safeCssPrefix}#${area.name} .table th {	
	<c:if test="${not empty area.finalTextColor}">color: ${area.finalTextColor};</c:if>
	<c:if test="${not empty area.finalTextSize}">font-size: ${area.finalTextSize};</c:if>
	<c:if test="${not empty area.finalFont}">font-family: ${area.finalFont};</c:if>
}

<c:if test="${not empty area.finalLinkColor}">
	${safeCssPrefix}#${area.name} a {	
		color: ${area.finalLinkColor};	
	}
</c:if>

${safeCssPrefix}#${area.name} h1 {	
	font-size: ${area.finalH1Size};	
}

${safeCssPrefix}#${area.name} h2 {	
	font-size: ${area.finalH2Size};	
}

${safeCssPrefix}#${area.name} h3 {	
	font-size: ${area.finalH3Size};	
}

${safeCssPrefix}#${area.name} h4 {	
	font-size: ${area.finalH4Size};	
}

${safeCssPrefix}#${area.name} h5 {	
	font-size: ${area.finalH5Size};	
}

${safeCssPrefix}#${area.name} h6 {	
	font-size: ${area.finalH6Size};	
}

</c:forEach></c:forEach>

<c:forEach var="row" items="${dynamicCSSTemplate.rows}"><c:forEach var="area" items="${row.areas}">
${safeCssPrefix}#${area.name} {	<c:set var="template" value="${area}" scope="request" />
<jsp:include page="styles.jsp" />
}</c:forEach></c:forEach>

<c:forEach var="row" items="${dynamicCSSTemplate.rows}">	
${safeCssPrefix}#${row.name} {	<c:set var="template" value="${row}" scope="request" />
<jsp:include page="styles.jsp" />
}</c:forEach>

