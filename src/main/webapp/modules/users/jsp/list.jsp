<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<div class="left">

	<form id="form-select-user" action="${info.currentURL}" method="post">
		<div>
			<input type="hidden" name="webaction" value="deleteUser" />
		</div>
		<table cellpadding="0" cellspacing="0" border="0" class="dyntable"
			id="sitelist">
			<thead>
				<tr>
					<th class="head0">&nbsp;</th>
					<th class="head1">${i18n.edit['user.login']}</th>
					<th class="head0">${i18n.edit['user.firstname']}</th>
					<th class="head1">${i18n.edit['user.lastname']}</th>
					<th class="head0">${i18n.edit['user.email']}</th>					
				</tr>
			</thead>
			<colgroup>
				<col class="con0" />
				<col class="con1" />
				<col class="con0" />
				<col class="con1" />
				<col class="con0" />
			</colgroup>
			<tbody>
				<c:forEach var="user" items="${users}">
					<tr class="gradeX">
						<td class="con0"><input type="checkbox" name="${user.login}" /> </td>
						<td class="con1"><a href="${info.currentURL}?webaction=edit&cuser=${user.encryptLogin}">${user.login}</a></td>
						<td class="con0"><c:out value="${user.firstName}" default="" /></td>
						<td class="con1">${user.lastName}</td>
						<td class="con0"><a href="mailto:${user.email}">${user.email}</a></td>
						
					</tr>
				</c:forEach>
			</tbody>
			<tfoot>
				<tr>
					<th class="head0">&nbsp;</th>
					<th class="head1">${i18n.edit['user.login']}</th>
					<th class="head0">${i18n.edit['user.firstname']}</th>
					<th class="head1">${i18n.edit['user.lastname']}</th>
					<th class="head0">${i18n.edit['user.email']}</th>	
				</tr>
			</tfoot>
		</table>
		<div class="action">
			<input type="submit" name="delete" value="delete" class="js-hidden" />
		</div>
	</form>

	<script type="text/javascript">
		jQuery(document)
				.ready(
						function() {
							jQuery('#sitelist')
									.dataTable(
											{
												"sPaginationType" : "full_numbers",
												"aaSorting" : [ [ 1, "asc" ] ],
												"aoColumns" : [ null, null,	null, null, null ],
												"oLanguage" : {
													"sUrl" : "${info.editTemplateURL}/js/plugins/i18n/datatable_${info.editLanguage}.txt"
												},
												"fnInitComplete" : updateLayout
											});

						});
	</script>

</div>