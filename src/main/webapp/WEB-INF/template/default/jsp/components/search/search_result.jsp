<%@ page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<div id="search-result">
<h2>${i18n.view['search.element-found']} '${searchResult.query}'  : ${fn:length(searchList)}</h2>
<ul>	
<c:forEach items="${searchList}" var="result">
<li class="search-block content">
<span class="title"><a href="${result.url}">${result.title}</a></span>
<span class="date">${result.dateString}</span>
<span class="description">${result.description}</span>
</li>
</c:forEach>
</ul>
</div>