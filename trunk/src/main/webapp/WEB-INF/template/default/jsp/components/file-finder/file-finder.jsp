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
		<td class="name"><a href="${file.URL}"><h3>${file.title}</h3><div class="description">${file.description}</div></a></td>
		<td class="type"><div class="badge">${file.type}</div><div class="badge">${file.size}</div><div class="badge">${file.date}</div></td>			
</tr>
</c:forEach>
</table>
</c:if>