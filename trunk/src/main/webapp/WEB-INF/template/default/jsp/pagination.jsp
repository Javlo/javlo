<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:if test="${pagination.pageSize > 0}"> 
<div class="pagination">
	<c:if test="${pagination.page > 1 && pagination.maxPage > 1}">
		<span class="previous"><a href="${info.currentURL}?webaction=view.pagination&page=${pagination.page-1}&key=${pagination.key}">&lt;&lt;</a></span>
	</c:if>
	
	<c:if test="${not empty param['short']}">
		<c:if test="${pagination.maxPage > 1}">
			<span class="info">${pagination.page}/${pagination.maxPage}</span>
		</c:if>
	</c:if>
	<c:if test="${empty param['short']}">
		<c:set var="from" value="${pagination.page-3}" />
		<c:set var="to" value="${pagination.page+3}" />	
		<c:if test="${from > 3}">
			<a href="${info.currentURL}?webaction=view.pagination&page=1&key=${pagination.key}">1</a>
			<a href="${info.currentURL}?webaction=view.pagination&page=2&key=${pagination.key}">2</a>
			<fmt:formatNumber var="softPage" value="${from/2}" maxFractionDigits="0" />
			<a href="${info.currentURL}?webaction=view.pagination&page=${softPage}&key=${pagination.key}">...</a>
		</c:if>
		<c:if test="${from <= 3}">
			<c:set var="from" value="1" />
		</c:if>
		<c:set var="listTo" value="${to}" />	
		<c:if test="${listTo >= pagination.maxPage}">
			<c:set var="listTo" value="${pagination.maxPage}" />
		</c:if>
		<c:set var="selected" value=" class=${info.template.selectedClass}" />
		<c:forEach var="i" begin="${from}" end="${listTo}" step="1" varStatus ="status">
			<a href="${info.currentURL}?webaction=view.pagination&page=${i}&key=${pagination.key}"${pagination.page == i?selected:''}>${i}</a>
		</c:forEach>
		<c:if test="${to <= pagination.maxPage-3}">
			<fmt:formatNumber var="softPage" value="${to+((pagination.maxPage-to)/2)}" maxFractionDigits="0" />
			<a href="${info.currentURL}?webaction=view.pagination&page=${softPage}&key=${pagination.key}">...</a>
			<a href="${info.currentURL}?webaction=view.pagination&page=${pagination.maxPage-1}&key=${pagination.key}">${pagination.maxPage-1}</a>
			<a href="${info.currentURL}?webaction=view.pagination&page=${pagination.maxPage}&key=${pagination.key}">${pagination.maxPage}</a>
		</c:if>
	</c:if>
	
	<c:if test="${pagination.page < pagination.maxPage && pagination.maxPage > 1}">
		<span class="next"><a href="${info.currentURL}?webaction=view.pagination&page=${pagination.page+1}&key=${pagination.key}">&gt;&gt;</a></span>
	</c:if>
</div>
</c:if>