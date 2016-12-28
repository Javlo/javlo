<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><c:if test="${!asLink}">
<div class="video-wrapper">
<iframe width="${width}" height="${height}" src="${url}" frameborder="0" allowfullscreen></iframe>
<c:if test="${not empty label}"><div class="label">${label}</div></c:if>
</div>
</c:if>
<c:if test="${asLink}">
<a href="${url}"><img src="${image}" alt="${label}" /></a>
</c:if>