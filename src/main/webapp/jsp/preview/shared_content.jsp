<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<input class="filter" type="text" placeholder="${i18n.edit['global.filter']}" onkeyup="filter(this.value, '#preview_command .shared-content .content-wrapper');" />
<div class="content shared-content">
<c:forEach var="content" items="${sharedContent}">
<div class="content-wrapper">
<div class="content" data-shared="${content.id}">
	<h4>${content.title}</h4>
	<figure><img src="${content.imageURL}" /></figure>
	<div class="description">${content.description}</div>	
</div>
</div>
</c:forEach>
</div>
