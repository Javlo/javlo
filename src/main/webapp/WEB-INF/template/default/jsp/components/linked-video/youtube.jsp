<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><c:if test="${!asLink}">
<div class="responsive">
<iframe width="${width}" height="${height}" src="${url}" frameborder="0" allowfullscreen></iframe>
</div>
</c:if>
<c:if test="${asLink}">
<a href="${url}"><img src="${image}" alt="${title}" /></a>
</c:if>