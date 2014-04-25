<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"
%><c:forEach var="row" items="${info.template.rows}"><c:forEach var="area" items="${row.areas}">
#${area.name} h1, #${area.name} h2, #${area.name} h3, #${area.name} h4, #${area.name} h5, #${area.name} h6 {	
	color: ${area.finalTitleColor};
}</c:forEach></c:forEach>

p,ul,.internal-link,.unsubscribe-link,.date,.external-link,.pdf-link,.file,.simple-internal-link,.image-left,.image-right

<c:forEach var="row" items="${info.template.rows}"><c:forEach var="area" items="${row.areas}">
#${area.name} p, #${area.name} ul, #${area.name} .internal-link, #${area.name} .unsubscribe-link, #${area.name} .date, #${area.name} .external-link,#${area.name} .pdf-link,#${area.name} .file, #${area.name} .simple-internal-link, #${area.name} .image-left,#${area.name}  .image-right, #${area.name} .global-image {	
	color: ${area.finalTextColor};
}</c:forEach></c:forEach>

<c:forEach var="row" items="${info.template.rows}"><c:forEach var="area" items="${row.areas}">
#${area.name} {	<c:set var="template" value="${area}" scope="request" />
<jsp:include page="styles.jsp" />
}</c:forEach></c:forEach>

<c:forEach var="row" items="${info.template.rows}">	
#${row.name} {	<c:set var="template" value="${row}" scope="request" />
<jsp:include page="styles.jsp" />
}</c:forEach>

