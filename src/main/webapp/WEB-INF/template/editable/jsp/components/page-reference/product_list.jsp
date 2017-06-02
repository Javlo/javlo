<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@ taglib
	prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%><c:if
	test="${fn:length(pages)>0}">
	
	<c:if test="${not empty firstPage}">
		<div class="first-page-complete">${firstPage}</div>
	</c:if>
	
	<c:if test="${not empty title}">
		<h2>${title}</h2>
	</c:if>

	<table width="100%" class="products">
		<c:forEach items="${pages}" var="page" varStatus="status">
			<c:set var="image" value="${null}" />
			<c:if test="${fn:length(page.images)>0}">
				<c:set var="image" value="${page.images[0]}" />
			</c:if>			
			<tr>						
			<td valign="top" style="border: 1px #C7C7C7 solid; padding: 0">
				<table width="100%" class="product">
					<tr><td width="160" style="width: 160px;">
					<c:if test="${empty image}">&nbsp;</c:if>
					<c:if test="${not empty image}">
						<figure style="width: 100%; margin: 0; padding: 0;">
							<a title="${page.title}" href="${page.url}"> <img style="width: 100%;" src="${image.url}" class="frame" alt="${image.description}" /></a>
						</figure>
					</c:if></td>
					<td width="10" style="width: 10px">&nbsp;</td>
					<td valign="top">
						<div class="info">
						<c:if test="${globalContext.collaborativeMode && not empty page.creator}">
							<div class="authors">${page.creator}</div>
						</c:if>
						<div style="margin: 0 0 5px 0; padding: 0;">								
						<h2 style="margin: 0; padding: 0;"><a title="${page.title}"	href="${page.url}">${page.title}</a></h2>
						<c:if test="${page.contentDate}"><div class="date" style="font-size: 10px; text-align: left;'">${page.date}</div></c:if>
						</div></div>
						<c:if test="${not empty page.description}">
							<div class="description">${page.description}</div>
						</c:if>
					</td>
					<td width="10" style="width: 10px">&nbsp;</td>
					</tr>
				</table>
			</td>			
			</tr>
			<tr><td colspan="3" style="height: 15px;">&nbsp;</td></tr>
		</c:forEach>
	</table>
</c:if>