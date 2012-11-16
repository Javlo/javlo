<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="content">
<ul>
	<li><span class="label">ContextKey</span>${globalContext.contextKey}</li>	
	<c:if test="${empty lightInterface}"><li><span class="label">Folder</span>${globalContext.folder}</li></c:if>
	<c:if test="${empty lightInterface}"><li><span class="label">User Factory</span>${globalContext.userFactoryClassName}</li></c:if>
	<c:if test="${empty lightInterface}"><li><span class="label">Admin User Factory</span>${globalContext.adminUserFactoryClassName}</li></c:if>
	<li><span class="label">Size</span>${globalContext.accountSizeLabel}</li>
	<li><span class="label">Admin</span><a href="mailto:${globalContext.administratorEmail}">${globalContext.administratorEmail}</a></li>
	<li><span class="label">Publish date : </span>${globalContext.publishDateLabel}</li>
	<li><span class="label">Latest publisher : </span>${globalContext.latestPublisher}</li>	
</ul>
</div>