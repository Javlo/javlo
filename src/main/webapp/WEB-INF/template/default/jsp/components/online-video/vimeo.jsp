<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><c:if test="${!asLink}">
<div class="video-wrapper">
<iframe src="//player.vimeo.com/video/${vid}?byline=0&amp;portrait=0&amp;badge=0" width="${width}" height="${height}" frameborder="0" webkitallowfullscreen mozallowfullscreen allowfullscreen></iframe> 
<c:if test="${not empty label}"><div class="label">${label}</div></c:if>
</div>
</c:if>
<c:if test="${asLink}">
<a href="${url}"><img src="${image}" alt="${label}" /></a>
</c:if>