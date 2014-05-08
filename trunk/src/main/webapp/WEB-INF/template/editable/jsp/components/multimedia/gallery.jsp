<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="multimedia">
	<table width="100%"><tr>
	<c:forEach var="resource" items="${resources}" varStatus="status">
	<c:if test="${status.index % 4 == 0 && status.index > 0}">
		</tr><tr>
	</c:if>
			<td width="25%" align="center">
				<a href="${resource.URL}" title="${resource.title} - ${resource.location} - ${resource.shortDate}" rel="${resource.relation}" onclick="sendAction('${resource.accessURL}');">
					<img alt="${resource.title}" src="${resource.previewURL}" />
				</a>			
				<c:if test="${not empty resource.title or not empty resource.location or not empty resource.shortDate}">
					<div class="info">
						<c:if test="${not empty resource.title}">
							<span class="ep_title">${resource.title}</span>
						</c:if>
						<c:if test="${not empty resource.location or not empty resource.shortDate}">
							<span class="subtitle">
								<c:if test="${not empty resource.location}">
									<span class="location">${resource.location}</span>
								</c:if>							
							</span>
						</c:if>
					</div>
				</c:if>
			</td>		
	</c:forEach>
	</tr></table>
	</div>	
	