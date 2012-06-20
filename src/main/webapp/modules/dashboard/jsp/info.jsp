<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="content">
<ul>
	<li><span class="label">ContextKey</span>${globalContext.contextKey}</li>	
	<li><span class="label">Folder</span>${globalContext.folder}</li>
	<li><span class="label">User Factory</span>${globalContext.userFactoryClassName}</li>
	<li><span class="label">Admin User Factory</span>${globalContext.adminUserFactoryClassName}</li>
	<li><span class="label">Size</span>${globalContext.accountSizeLabel}</li>
	<li><span class="label">Admin</span><a href="mailto:${globalContext.administratorEmail}">${globalContext.administratorEmail}</a></li>
</ul>
</div>