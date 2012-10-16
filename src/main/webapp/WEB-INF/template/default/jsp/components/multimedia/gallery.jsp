<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="multimedia">
	<c:forEach var="resource" items="${resources}" varStatus="status">
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
			
			<ul>
				<li class="languages">
					<div>
						<a href="${resource.URL}?epbox&amp;gallery=tr-${resource.relation}" lang="${resource.language}" hreflang="${resource.language}" xml:lang="${resource.language}">
						<span class="lang">${resource.language}</span>
						</a>
						<c:forEach var="trResource" items="${resource.translation}" varStatus="status">
							<a href="${trResource.URL}?epbox&amp;gallery=tr-${trResource.relation}" lang="${trResource.language}" hreflang="${trResource.language}" xml:lang="${trResource.language}">
							<span class="lang">${trResource.language}</span>
							</a>
						</c:forEach>
					</div>
				</li>
			</ul>
		</div>		
	</c:forEach>
	</div>