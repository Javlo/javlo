<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<ul id="search-result">	
<c:forEach items="${searchList}" var="result">
<c:if test="${not empty result.components}">
<c:forEach items="${result.components}" var="comp">
<li class="search-block ${comp.type}">
${comp.xhtml}
</li>
</c:forEach>
</c:if>
<c:if test="${empty result.components}">
<li class="search-block content">
<span class="title"><a href="${result.url}">${result.title}</a></span>
<span class="date">${result.dateString}</span>
<span class="description">${result.description}</span>
</li>
</c:if>
</c:forEach>
</ul>