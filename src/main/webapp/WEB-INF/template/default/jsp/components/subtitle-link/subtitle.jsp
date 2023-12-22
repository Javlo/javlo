<%@ taglib uri="jakarta.tags.core" prefix="c"
%><c:if test="${not empty value}"><h3>${value}</h3></c:if>
<ul class="subtitle-link">
<c:forEach items="${links}" var="link"><c:if test="${link.level ==  '2'}"><li><a href="${link.url}">${link.label}</a></li></c:if></c:forEach>
</ul>