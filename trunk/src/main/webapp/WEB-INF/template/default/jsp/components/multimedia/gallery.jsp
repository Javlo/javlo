<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="multimedia ep_noborderbox ep_autopaginatebox">
<div class="ep_boxbody">
<div class="ep_block">		
	<c:forEach var="resource" items="${resources}" varStatus="status">
		<c:if test="${(status.index mod 3 eq 0) and (not (status.index eq 0))}">
				<span class="ep_endbox">&nbsp;</span>
			</div>
			<div class="ep_block">
		</c:if>
		<div class="ep_element3col">
			<div class="ep_elementtext">
				<a href="${resource.URL}?epbox&amp;gallery=${resource.relation}" title="${resource.title} - ${resource.location} - ${resource.shortDate}" rel="${resource.relation}" onclick="sendAction('${resource.accessURL}');">
					<img alt="${resource.title}" src="${resource.previewURL}" />
				</a>
			</div>
			<c:if test="${not empty resource.title or not empty resource.location or not empty resource.shortDate}">
				<div class="ep_elementsubheading">
					<c:if test="${not empty resource.title}">
						<span class="ep_title">${resource.title}</span>
					</c:if>
					<c:if test="${not empty resource.location or not empty resource.shortDate}">
						<span class="ep_subtitle">
							<c:if test="${not empty resource.location}">
								<span class="ep_theme">${resource.location}</span>
							</c:if>
							<c:if test="${not empty resource.location and not empty resource.shortDate}">
								-
							</c:if>
							<c:if test="${not empty resource.shortDate}">
								<span class="ep_date">${resource.shortDate}</span>
							</c:if>
						</span>
					</c:if>
				</div>
			</c:if>
			
			<div class="ep_elementlinks">
				<ul>
					<li class="ep_multi">
						<div>
							<a href="${resource.URL}?epbox&amp;gallery=tr-${resource.relation}" lang="${resource.language}" hreflang="${resource.language}" xml:lang="${resource.language}">
							<span class="ep_lang">${resource.language}</span></a>
							<c:forEach var="trResource" items="${resource.translation}" varStatus="status">
								<a href="${trResource.URL}?epbox&amp;gallery=tr-${trResource.relation}" lang="${trResource.language}" hreflang="${trResource.language}" xml:lang="${trResource.language}">
								<span class="ep_lang">${trResource.language}</span></a>
							</c:forEach>
						</div>
					</li>
				</ul>
			</div>
						
			<span class="ep_endbox">&nbsp;</span>
		</div>
	</c:forEach>	
	</div>
	<span class="ep_endbox">&nbsp;</span>
</div>
</div>


