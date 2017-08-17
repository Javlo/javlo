<%@page language="java" contentType="text/css; charset=UTF-8"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"
%><%@ taglib uri="/WEB-INF/javlo.tld" prefix="jv" %><jv:infoBean /><c:set var="template" value="${info.template}" /><c:if test="${not empty param.template}"><c:set var="template" value="${info.templatesMap[param.template]}" /></c:if>
<c:set var="rows" value="${template.rows}" scope="request" />
<c:set var="dynamicCSSTemplate" value="${info.template}" scope="request" />
<jsp:include page="dynamic_css.jsp" />
<c:forEach var="page" items="${info.page.children}" varStatus="status">
<c:set var="dynamicCSSTemplate" value="${page.template}" scope="request"/>
<c:set var="currentPage" value="${page}" scope="request"/>
<jsp:include page="dynamic_css.jsp?prefix=.page-${status.index+1} " />
</c:forEach>