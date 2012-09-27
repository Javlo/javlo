<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<a class="ep_media" href="${url}?epbox&amp;gallery=video" title="${label}" onclick="sendAction('${accessURL}');">
	<img src="${fn:replace(image,'/video/','/video250/')}" alt="${label}" title="${i18n.view['epbox.watch-video']}" />
	<span class="ep_endbox layer" title="${i18n.view['epbox.watch-video']}">&nbsp;</span>		  
</a>

	