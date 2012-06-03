<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<c:forEach items="${pages}" var="page" varStatus="status">
	<div class="page-link index-${status.index}" lang="${page.language}">
	<c:if test="${not empty page.imageURL}">
		<a href="${page.viewImageURL}" rel="shadowbox" title="${page.imageDescription}"><img src="${page.imageURL}" alt="${page.imageDescription}" /></a>
	</c:if>	
	<h4><a href="${page.url}">${page.title}</a><c:forEach items="${page.tags}" var="tag"><span class="bullet ${tag}"></span></c:forEach></h4>
	<c:if test="${not empty page.location}">
		<span class="place">${page.location} -</span>
	</c:if>	
	<span class="date">${page.date}</span>
	<p>${page.description}</p>
	<a class="read-more" href="${page.url}" title="${page.attTitle}">${i18n.view["global.read-more"]}</a>
	<c:if test="${not empty page.links}">
			<div class="content-languages">
				<ul>
					<c:forEach items="${page.links}" var="link">
					<li><a href="${link.url}">${link.title}</a></li>
					</c:forEach>
				</ul>
			</div>		
	</c:if>
	<div class="content_clear"><span>&nbsp;</span></div>
	</div> 
</c:forEach>