<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"
%><%@ taglib uri="/WEB-INF/javlo.tld" prefix="jv"%>
<h2>use</h2>
<div class="select-year">
<c:set var="currentYear" value="${not empty param.y?param.y:info.currentYear}" />
<c:forEach var="y" begin="2014" end="${info.currentYear}">		
	<c:url var="url" value="${info.currentURL}" context="/">
		<c:param name="y" value="${y}" />
		<c:param name="expand" value="${param.expand}" />
	</c:url>
	<a class="btn btn-default btn-sm ${currentYear==y?'active':''}" href="${url}">${y}</a>
</c:forEach>
<c:url var="url" value="${info.currentURL}" context="/">
		<c:param name="y" value="${currentYear}" />
		<c:param name="expand" value="${!param.expand}" />
	</c:url>
<a class="btn-expand" href="${url}">${param.expand?'<i class="fa fa-minus-square" aria-hidden="true"></i>':'<i class="fa fa-plus-square" aria-hidden="true"></i>'}</a>
</div>
<c:set var="emptyList" value="${fn:length(dayInfos)==0}" />
<c:if test="${!emptyList}">
<table class="table">
<tr>
	<th>date</th>
	<th>#publish</th>
	<th>#save</th>
	<th>page most saved</th>
</tr>

<c:set var="m" value="-1" />
<c:set var="pt" value="0" />
<c:set var="sp" value="0" />
<c:set var="startDate" value="" />
<c:set var="endDate" value="" />
<c:forEach var="day" items="${dayInfos}" varStatus="status">
	<c:set var="emptyList" value="false" />
	<c:if test="${m == day.month}">
		<c:set var="pt" value="${pt+day.publishCount}" />
		<c:set var="st" value="${st+day.saveCount}" />
	</c:if>
	<c:if test="${m != day.month}">
	<c:if test="${m != -1}">
	<tr>
		<th>${startDate} - ${endDate}</th>
		<th>${pt}</th>
		<th>${st}</th>
		<th></th>
	</tr>
	</c:if>
	<c:set var="pt" value="${day.publishCount}" />
	<c:set var="st" value="${day.saveCount}" />
	<c:set var="m" value="${day.month}" />
	<c:set var="startDate" value="" />
	</c:if>
	<c:if test="${param.expand}">
	<tr>
		<td>${day.date}</td>
		<td>${day.publishCount}</td>
		<td>${day.saveCount}</td>
		<jv:pageurl var="pageURL" name="${day.mostSavePage}" view="true" />
		<td><a href="${pageURL}" target="_blank">${day.mostSavePage}</a></td>
	</tr>
	</c:if>
	<c:if test="${empty startDate}">
		<c:set var="startDate" value="${day.date}" />
	</c:if>
	<c:set var="endDate" value="${day.date}" />
</c:forEach>
<tr>
	<th>${startDate} - ${endDate}</th>
	<th>${pt}</th>
	<th>${st}</th>
	<th></th>
</tr>
</table>
</c:if><c:if test="${emptyList}">
<div class="alert alert-warning">no data</div>
</c:if>