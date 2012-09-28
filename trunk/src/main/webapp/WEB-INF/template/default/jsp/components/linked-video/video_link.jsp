<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="video-link">
<a class="media shadowbox" rel="shadowbox" href="${url}" title="${label}" >
	<img src="${image}" alt="${label}" />	
	<span class="layer">&nbsp;</span>		  
</a>
<div class="info">
	<span class="label">${label}</span>
	<span class="description">${description}</span>
</div>
</div>
	