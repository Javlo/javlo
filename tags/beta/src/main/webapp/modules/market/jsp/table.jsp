<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<div id="gallery" class="gallery remote-resource full-height">
	<div id="gridview" class="thumbview">
		<table cellpadding="0" cellspacing="0" border="0" class="dyntable"
			id="sitelist">
			<thead>
				<tr>
					<th class="head0">${i18n.edit['market.title.name']}</th>
					<th class="head1">${i18n.edit['market.title.date']}</th>
					<th class="head0">${i18n.edit['market.title.download-url']}</th>
					<th class="head1">&nbsp;</th>
				</tr>
			</thead>
			<colgroup>
				<col class="con0" />
				<col class="con1" />
				<col class="con0" />
				<col class="con1" />				
			</colgroup>
			<tbody>
			<c:forEach var="resource" items="${resources}">
				<tr>
					<td><span>${resource.name}</span></td>
					<td><span>${resource.dateAsString}</span></td>
					<td><a href="${resource.downloadURL}">${resource.name}</a></td>
					<td class="menu"><a href="${info.currentURL}?webaction=importPage&id=${resource.id}"  class="import" title="import ${resource.name}">${i18n.edit['market.action.import']}</a></td>
				</tr>
			</c:forEach>
			</tbody>
			<tfoot>
			<tr>
				<th class="head0">${i18n.edit['market.title.name']}</th>
				<th class="head1">${i18n.edit['market.title.date']}</th>
				<th class="head0">${i18n.edit['market.title.download-url']}</th>				
				<th class="head1">&nbsp;</th>
			</tr>
			</tfoot>
		</table>
	</div>
</div>

<script type="text/javascript">
jQuery(document).ready(function() {
	jQuery('#sitelist').dataTable( {
		"sPaginationType": "full_numbers",
		 "aaSorting": [[ 1, "desc" ]],
		 "aoColumns": [
		               null,
		               null,
		               null,		               		               		               
		               { "asSorting": [ "" ] }		               
		           ],
		 "oLanguage": {"sUrl": "${info.editTemplateURL}/js/plugins/i18n/datatable_${info.editLanguage}.txt"},
		 "fnInitComplete": updateLayout
	});	
		
});
</script>

