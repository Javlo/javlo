<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<ul class="navigation">
<li ${currentRemoteRenderMode eq 'list'?'class="current"':''}><a href="${info.currentURL}?webaction=changeRenderMode&rendermode=list">list</a></li>
<li ${currentRemoteRenderMode eq 'tree'?'class="current"':''}><a href="${info.currentURL}?webaction=changeRenderMode&rendermode=tree">tree</a></li>
<li ${currentRemoteRenderMode eq 'charge'?'class="current"':''}><a href="${info.currentURL}?webaction=changeRenderMode&rendermode=charge">charge</a></li>
<li ${currentRemoteRenderMode eq 'status'?'class="current"':''}><a href="${info.currentURL}?webaction=changeRenderMode&rendermode=status">status</a></li>
<li ${currentRemoteRenderMode eq 'sitemap'?'class="current"':''}><a href="${info.currentURL}?webaction=changeRenderMode&rendermode=sitemap">sitemap</a></li>
</ul>