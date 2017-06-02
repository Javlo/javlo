<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@ taglib
	prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%><c:if
	test="${fn:length(pages)>0}">
	
	<c:if test="${not empty firstPage}">
		<div class="first-page-complete">${firstPage}</div>
	</c:if>
	
	<c:if test="${not empty title}">
		<h2>${title}</h2>
	</c:if>

	<table width="100%" class="products" style="border-collapse: collapse;">
		<tr>
		<c:forEach items="${pages}" var="page" varStatus="status">
			<c:set var="image" value="${null}" />
			<c:if test="${fn:length(page.images)>0}">
				<c:set var="image" value="${page.images[0]}" />
			</c:if>									
			<td width="50%" valign="top" style="border: 1px #C7C7C7 solid;">			
				<table width="100%" class="product" style="border-collapse: collapse;">					
					<tr><td>					
					<c:if test="${empty image}">&nbsp;</c:if>
					<c:if test="${not empty image}">
						<figure style="width: 100%; margin: 0; padding: 0;">
							<a title="${page.title}" href="${page.url}"> <img style="width: 100%;" src="${image.url}" /></a>
						</figure>
					</c:if></td></tr>
					<tr><td height="10" style="height: 10px; font-size: 0;">&nbsp;</td></tr>
					<tr><td valign="top">
						<div class="info">						
						<div style="margin: 0; padding: 0;">								
						<h2 style="margin: 0; padding: 0; text-align: center;"><a title="${page.title}"	href="${page.url}">${page.title}</a></h2>						
						</div></div>						
					</td></tr>
					<tr><td height="10" style="height: 10px; font-size: 0;">&nbsp;</td></tr>										
				</table>
			</td>			
			<c:if test="${status.index%2!=0}"></tr><tr></c:if>
		</c:forEach>
		</tr>
	</table>
</c:if>