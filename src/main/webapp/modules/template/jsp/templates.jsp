<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div id="gallery" class="gallery main-template full-height">
<div id="gridview" class="thumbview display-list">
<c:set var="mailingFound" value="${false}" />
<c:set var="webFound" value="${false}" />
<c:forEach var="tpl" items="${templates}">
<c:if test="${tpl.mailing}">
<c:set var="mailingFound" value="${true}" />
</c:if>
<c:if test="${!tpl.mailing}">
<c:set var="webFound" value="${true}" />
</c:if>
</c:forEach>
<c:if test="${webFound}">
<div class="widgetbox">
<h3><span>web</span></h3>
<div class="content">
<ul>
<c:forEach var="tpl" items="${templates}">
<c:if test="${!tpl.mailing}">
<c:set var="template" value="${tpl}" scope="request" />
<jsp:include page="template.jsp" />
</c:if>
</c:forEach>
</ul>
</div>
</div>
</c:if>
<c:if test="${mailingFound}">
<div class="widgetbox">
<h3><span>mail</span></h3>
<div class="content">
<ul>
<c:forEach var="tpl" items="${templates}">
<c:if test="${tpl.mailing}">
<c:set var="template" value="${tpl}" scope="request" />
<jsp:include page="template.jsp" />
</c:if>
</c:forEach>
</ul>
</div>
</div>
</c:if>
</div>
</div>

