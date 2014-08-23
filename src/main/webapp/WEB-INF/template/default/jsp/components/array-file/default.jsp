<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:set var="colTag" value="${colHead}" /><c:set var="rowTag" value="${rowHead}" /><c:set var="tag" value="${rowHead eq 'td'?colHead:rowHead}" />
<table class="table" summary="${summary}">
<c:forEach var="row" items="${array}" varStatus="status">
<c:if test="${status.index==0 && tableHead}"><thead></c:if>
<c:if test="${status.index==0 && !tableHead}"><tbody></c:if>
<c:if test="${status.index==1 && tableHead}"><tbody></c:if>
<tr>
<c:forEach var="cell" items="${row}"><${tag}>${cell}</${tag}><c:set var="tag" value="${colTag}" /></c:forEach>
<c:set var="tag" value="${rowTag}" />
<c:set var="colTag" value="td" />
</tr>
<c:if test="${status.index==0 && tableHead}"></thead></c:if>
<c:if test="${status.index==0 && !tableHead}"></tbody></c:if>
<c:if test="${status.index==1 && tableHead}"></tbody></c:if>
</c:forEach>
</table>