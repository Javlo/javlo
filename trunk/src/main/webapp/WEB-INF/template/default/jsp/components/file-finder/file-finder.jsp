<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%@taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<c:if test="${fn:length(files) > 0}">
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
			<a href="${file.URL}">
				<figure class="thumbnail">
					<img src="${file.thumbURL}" alt="preview of ${file.name}" lang="en" />
					<figcaption class="caption"><p>${file.name}</p></figcaption>
				</figure>
			</a>
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
</c:if>