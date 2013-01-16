<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<div id="gallery" class="gallery remote-resource full-height">
	<div id="gridview" class="thumbview">
		<table>
			<c:forEach var="resource" items="${resources}" begin="1" end="${max}">
				<tr>
					<td><label>${i18n.edit['global.name']}:</label> <span>${resource.name}</span>
					</td>
					<td><label>${i18n.edit['global.date']}:</label> <span>${resource.dateAsString}</span>
					</td>
					<td><a href="${resource.downloadURL}">${i18n.edit['resource.download']}</a>
					</td>
					<td class="menu"><a href="${remote.URL}" class="preview"
						title="${resource.name}" target="_blank"></a> <a
						href="${info.currentURL}?webaction=importPage&id=${resource.id}"
						class="import" title="import ${resource.name}"></a></td>

				</tr>
			</c:forEach>
		</table>
	</div>
</div>

