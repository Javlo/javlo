<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div id="content" class="content ${not empty lightInterface?'light':''}">
<c:if test="${not empty event}">	
	<div class="row">
		<div class="col-sm-9">
			<h2><a href="${event.url}">${event.summary}</a> <span class="badge pull-right">${event.start} - ${event.end}</span></h2>
			<p>${event.description}</p>
		</div>
		<div class="col-sm-3">			
			<c:if test="${not empty event.imageURL}">
				<img src="${event.imageURL}" alt="${event.imageDescription}" />
			</c:if>
		</div>
	</div>
</div>
</c:if>