<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="multimedia">
	<c:forEach var="resource" items="${resources}" varStatus="status">
	
	<c:set var="display" value="false" />
	<c:if test="${status.index >= (pagination.page-1)*pagination.pageSize || pagination.pageSize == 0}">
		<c:if test="${status.index <= (pagination.page)*pagination.pageSize || pagination.pageSize == 0}">
			<c:set var="display" value="true" />
		</c:if>
	</c:if>
	
	<c:if test="${!display}">
	<div class="item hidden">
		<a href="${resource.URL}?epbox&amp;gallery=${resource.relation}" title="${resource.title} - ${resource.location} - ${resource.shortDate}" rel="${resource.relation}" onclick="sendAction('${resource.accessURL}');" lang="en">
			download
		</a>
	</div>	
	</c:if>
	
	
	<c:if test="${display}">
	<div class="item">
			<div class="preview ${resource.cssClass}">
				<a href="${resource.URL}?epbox&amp;gallery=${resource.relation}" title="${resource.title} - ${resource.location} - ${resource.shortDate}" rel="${resource.relation}" onclick="sendAction('${resource.accessURL}');">
					<img alt="${resource.title}" src="${resource.previewURL}" />
				</a>
			</div>
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
							<c:if test="${not empty resource.location and not empty resource.shortDate}">
								-
							</c:if>
							<c:if test="${not empty resource.shortDate}">
								<span class="date">${resource.shortDate}</span>
							</c:if>
						</span>
					</c:if>
				</div>
			</c:if>
			
		</div>
		</c:if>		
	</c:forEach>
	</div>
	<jsp:include page="../../pagination.jsp"></jsp:include>
	