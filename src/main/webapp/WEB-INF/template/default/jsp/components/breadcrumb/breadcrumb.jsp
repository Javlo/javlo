<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><div class="breadcrumb">
<c:if test="${empty pages}">
	<c:set var="pages" value="${info.parentPageList}" />
</c:if>
<c:forEach var="page" items="${pages}" varStatus="status">
<span class="item" itemscope itemtype="http://data-vocabulary.org/Breadcrumb">
  <a href="${page.url}" itemprop="url">
    <span itemprop="title">${page.info.label}</span>
  </a> ${status.index < fn:length(pages)-1?'&#x203a;':''}
</span>  
</c:forEach>
</div>