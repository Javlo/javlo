<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div id="search-result">
<h2>${i18n.view['search.element-found']} '${searchResult.query}'  : ${fn:length(searchList)}</h2>
<ul>	
<c:forEach items="${searchList}" var="result">
<c:if test="${empty result.components}">
<li class="search-block content">
<span class="title"><a href="${result.url}">${result.title}</a></span>
<span class="date">${result.dateString}</span>
<span class="description">${result.description}</span>
</li>
</c:if>
</c:forEach>
</ul>
</div>