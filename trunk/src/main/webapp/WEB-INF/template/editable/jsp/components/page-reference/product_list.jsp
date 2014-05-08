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
			<td style="width: 15px;">&nbsp;</td>			
			<td valign="top" style="margin: 5px; border: 1px #C7C7C7 solid; padding: 5px;">
				<table width="100%" class="product">
					<tr><td width="100" style="width: 100px; padding: 0 10px 0 0;">
					<c:if test="${empty image}">&nbsp;</c:if>
					<c:if test="${not empty image}">
						<figure>
							<a title="${page.title}" href="${page.url}"> <img
								src="${image.url}" class="frame" alt="${image.description}" />
							</a>
						</figure>
					</c:if></td>
					<td valign="top">
						<div class="info">
						<c:if test="${globalContext.collaborativeMode && not empty page.creator}">
							<div class="authors">${page.creator}</div>
						</c:if>								
						<c:if test="${page.contentDate}"><span class="date">${page.date}</span></c:if> <a title="${page.title}"
							href="${page.url}">${page.title}</a>
						</div>
						<c:if test="${not empty page.description}">
							<div class="description">${page.description}</div>
						</c:if>
					</td></tr>
				</table>
			</td>
			<td style="width: 15px;">&nbsp;</td>
			</tr>
			<tr><td colspan="3" style="height: 15px;">&nbsp;</td></tr>
		</c:forEach>
	</table>
</c:if>