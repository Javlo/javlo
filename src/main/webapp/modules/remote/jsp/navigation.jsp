<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<ul class="navigation">
<li ${currentRemoteRenderMode eq 'list'?'class="current"':''}><a href="${info.currentURL}?webaction=changeRenderMode&rendermode=list">list</a></li>
<li ${currentRemoteRenderMode eq 'tree'?'class="current"':''}><a href="${info.currentURL}?webaction=changeRenderMode&rendermode=tree">tree</a></li>
<li ${currentRemoteRenderMode eq 'charge'?'class="current"':''}><a href="${info.currentURL}?webaction=changeRenderMode&rendermode=charge">charge</a></li>
</ul>