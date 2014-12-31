<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%@taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<c:if test="${fn:length(files) > 0}">
<div class="table-responsive">
<table class="table">
<thead>
	<tr>
		<th class="thumb">${i18n.view["files.header.thumb"]}</th>
		<th class="name">${i18n.view["files.header.title"]}</th>		
		<th class="type">${i18n.view["files.header.type"]}</th>
		<th class="size">${i18n.view["files.header.size"]}</th>		
	</tr>
</thead>
<c:forEach var="file" items="${files}">
<tr>
		<td class="thumb">		
			<c:if test="${!file.video}">
				<a href="${file.URL}">
				<figure class="thumbnail">
					<img src="${file.thumbURL}" alt="preview of ${file.name}" lang="en" />
					<figcaption class="caption"><p>${file.name}</p></figcaption>
				</figure>
				</a>
			</c:if>
			<c:if test="${file.video}">
					<!-- "Video For Everybody" http://camendesign.com/code/video_for_everybody -->
					<video controls="controls">
					<source src="${file.absoluteURL}" type="video/mp4" />
					<object type="application/x-shockwave-flash" data="http://flashfox.googlecode.com/svn/trunk/flashfox.swf">
						<param name="movie" value="http://flashfox.googlecode.com/svn/trunk/flashfox.swf" />
						<param name="allowFullScreen" value="true" />
						<param name="wmode" value="transparent" />
						<param name="flashVars" value="controls=true&amp;src=${file.absoluteURL}" />
						<span lang="en" title="No video playback capabilities, please download the video below">${file.name}</span>
					</object>
					</video>
			</c:if>			
		</td>
		<td class="name"><a href="${file.URL}"><h3>${file.title}</h3><p class="description">${file.description}</p></a></td>
		<td class="type"><div class="badge">${file.type}</div><div class="badge">${file.size}</div><div class="badge">${file.date}</div>
		<c:url var="shareTwitter" value="https://twitter.com/share">
			<c:param name="url" value="${file.absoluteURL}" />
			<c:param name="text" value="${file.title}" />			
		</c:url>
		<a class="btn btn-default btn-block twitter" href="${shareTwitter }" target="twitter">${i18n.view['global.shareon']} twitter</a>
		<c:url var="sharePinterest" value="https://www.pinterest.com/pin/create/button/">
			<c:param name="url" value="${file.absoluteURL}" />
			<c:param name="description" value="${file.title}" />			
		</c:url>			
		<a class="btn btn-default btn-block pinterest" href="${sharePinterest}" target="pinterest">${i18n.view['global.shareon']} pinterest</a></td>
		
</tr>
</c:forEach>
</table>
</div>
</c:if>