<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%>
<div class="box preview">
<h3><span>${i18n.edit['mailing.title.history']}</span></h3>
	<div class="content">
	<form id="form-mailing-history" action="${info.currentURL}" method="post">
		<div>
			<input type="hidden" name="webaction" value="selectHistory" />
		</div>
		<table cellpadding="0" cellspacing="0" border="0" class="dyntable" id="history-table">
			<thead>
				<tr>
					<th width="13" class="head0">&nbsp;</th>
					<th class="head1">${i18n.edit['mailing.form.sender']}</th>
					<th class="head0">${i18n.edit['mailing.form.subject']}</th>
					<th class="head1">${i18n.edit['mailing.title.countReceivers']}</th>
					<th width="120" class="head0">${i18n.edit['mailing.title.sendDate']}</th>
					<th class="head1">${i18n.edit['global.message']}</th>				
				</tr>
			</thead>
			<colgroup>
				<col class="con0" />
				<col class="con1" />
				<col class="con0" />
				<col class="con1" />				
				<col class="con0" />
				<col class="con1" />
			</colgroup>
			<tbody>
				
				<c:forEach var="mailing" items="${allMailing}">
				<c:if test="${!mailing.test}">
				<tr>
					<c:url var="delURL" value="${info.currentURL}" context="/">
						<c:param name="webaction" value="mailing.deletemailing" />
						<c:param name="id" value="${mailing.id}" />
					</c:url>
					<td class="con0"><a href="${delURL}" title="delete mailing" class="needconfirm"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span></a></td>
					<td class="con1">${mailing.from}</td>
					<td class="con0">${mailing.subject}</td>
					<td class="con1">${mailing.receiversSize}</td>
					<td class="con0">${mailing.dateString}</td>
					<td class="con1">${mailing.message}</td>
				</tr>			
				</c:if>		
				</c:forEach>
				
			</tbody>
			<tfoot>
				<tr>
					<th class="head0">&nbsp;</th>
					<th class="head1">${i18n.edit['mailing.form.sender']}</th>
					<th class="head0">${i18n.edit['mailing.form.subject']}</th>
					<th class="head1">${i18n.edit['mailing.title.countReceivers']}</th>
					<th width="120" class="head0">${i18n.edit['mailing.title.sendDate']}</th>	
					<th class="head1">${i18n.edit['global.message']}</th>			
				</tr>
			</tfoot>
		</table>
	</form>
	</div>
	<script type="text/javascript">
	jQuery(document).ready(function() {
		jQuery('#history-table').dataTable( {
			"sPaginationType": "full_numbers",
			 "aaSorting": [[ 4, "desc" ]],
			 "aoColumns": [
						  { "asSorting": [ "" ] },
			               null,
			               null,
			               null,
			               null,
			               null			               		               
			           ],
			 "oLanguage": {"sUrl": "${info.editTemplateURL}/js/plugins/i18n/datatable_${info.editLanguage}.txt"},
			 "fnInitComplete": updateLayout
		});	
			
	});
	</script>

</div>