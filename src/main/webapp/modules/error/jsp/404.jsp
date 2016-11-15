<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<div class="content">
	<table cellpadding="0" cellspacing="0" border="0" class="dyntable" id="notes">
		<thead>
			<tr>
				<th class="head0" style="width: 30px">del</th>
				<th class="head1 sorting_desc" rowspan="1" colspan="1">url</th>
				<th class="head0 sorting" rowspan="1" colspan="1" style="width: 80px;">Referer</th>
				<th class="head1 sorting" rowspan="1" colspan="1" style="width: 180px;">forward</th>
			</tr>
		</thead>
		<colgroup>
			<col class="con0">
			<col class="con1">
			<col class="con0">
			<col class="con1">
		</colgroup>
		<tbody>
			<c:forEach var="item" items="${list404}">
				<tr>
					<td><a class="btn btn-default btn-sm" href="${info.currentURL}?webaction=error.delete&url=${item.key}">X</a></td>
					<td>${item.key}</td>
					<td class="referer"><a href="${item.value}" target="_blanck">open<a></td>
					<td class="action">
						<form action="${info.currentURL}" method="post">
							<input type="text" class="form-input" name="page" placeholder="forward to page." />
							<input type="hidden" name="url" value="${item.key}" />
							<input type="hidden" name="webaction" value="forward" />
							<input type="submit" class="btn btn-default btn-sm" name="forward" value="ok" />
						</form>
					</td>
				</tr>
			</c:forEach>
		</tbody>
		<tfoot>
			<tr>
				<th class="head0" style="width: 30px">del</th>
				<th class="head1 sorting_desc" rowspan="1" colspan="1">url</th>
				<th class="head0 sorting" rowspan="1" colspan="1">Referer</th>
				<th class="head1 sorting" rowspan="1" colspan="1"">forward</th>
			</tr>
		</tfoot>
	</table>
</div>
<script type="text/javascript">
jQuery(document).ready(function() {
	jQuery('#notes').dataTable( {
		 "sPaginationType": "full_numbers",
		 "aaSorting": [[ 1, "desc" ]],
		 "aoColumns": [
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