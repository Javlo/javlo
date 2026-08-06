<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%>
<div class="box preview">
<h3><span>${i18n.edit['mailing.title.unsubscribe']}</span></h3>
	<div class="content">
		<table cellpadding="0" cellspacing="0" border="0" class="dyntable cell-border compact stripe display" id="unsubscribe-table">
			<thead>
				<tr>
					<th class="head1">${i18n.edit['mailing.title.unsubscribe.email']}</th>
					<th class="head0">${i18n.edit['mailing.title.unsubscribe.roles']}</th>
					<th class="head1">${i18n.edit['mailing.title.unsubscribe.date']}</th>
					<th width="120" class="head0">&nbsp;</th>
				</tr>
			</thead>
			<colgroup>
				<col class="con1" />
				<col class="con0" />
				<col class="con1" />
				<col class="con0" />
			</colgroup>
			<tbody>
				<c:forEach var="entry" items="${unsubscribeList}">
				<c:url var="resubscribeURL" value="${info.currentURL}" context="/">
					<c:param name="webaction" value="mailing.resubscribe" />
					<c:param name="email" value="${entry.email}" />
				</c:url>
				<tr>
					<td class="con1"><c:out value="${entry.email}" escapeXml="true" /></td>
					<td class="con0"><c:forEach var="role" items="${entry.roles}" varStatus="s"><c:out value="${role}" escapeXml="true" /><c:if test="${!s.last}">, </c:if></c:forEach></td>
					<td class="con1">${entry.date}</td>
					<td class="con0"><a href="${resubscribeURL}">${i18n.edit['mailing.action.resubscribe']}</a></td>
				</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</div>
