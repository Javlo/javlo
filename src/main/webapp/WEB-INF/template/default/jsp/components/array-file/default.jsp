<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:set var="colTag" value="${colHead}" /><c:set var="rowTag" value="${rowHead}" /><c:set var="blocTag" value="${colHead eq 'td'?'tbody':'thead'}" /><c:set var="titlebloc" value="${not (colHead eq 'td')}" />
<table class="table" summary="${summary}"><${blocTag}><c:set var="closeLastBloc" value="</${colHead eq 'td'?'tbody':'thead'}>" />
<c:forEach var="row" items="${array}" varStatus="status"><c:set var="rowHTML" value="<tr>" /><c:set var="rowEmpty" value="true" /><c:set var="colHTML" value="" />
<c:forEach var="cell" items="${row}" varStatus="status"><c:if test="${not empty cell}">	
		<c:set var="tag" value="td" /><c:if test="${cell != null && cell.firstCol}"><c:set var="tag" value="${rowTag}" /></c:if>
		<c:if test="${cell != null && cell.firstRow && tag eq 'td'}"><c:set var="tag" value="${colTag}" /></c:if>
		 
		<c:set var="line" value="${status.count % 2 == 0?'odd':'even'}" />
		<c:set var="cssClass" value=" class='${cell.type} ${line}'" />
		
		<c:set var="colHTML" value="${colHTML}<${tag}${cell.spanAttributes}${cssClass}>${cell.value}</${tag}>" />
		<c:set var="rowEmpty" value="false" /><c:set var="rowHTML" value="${rowHTML}${colHTML}" /><c:set var="colHTML" value="" />				
	</c:if></c:forEach>
<c:if test="${status.index > row[0].colTitleHeight && titlebloc}"><c:set var="titlebloc" value="false" /></${blocTag}><tbody><c:set var="closeLastBloc" value="</tbody>" /></c:if>
<c:if test="${not rowEmpty}">${rowHTML}</tr></c:if>
</c:forEach>${closeLastBloc}</table>