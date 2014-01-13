<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div id="content" class="content full-height ${not empty lightInterface?'light':''}">
	<table cellpadding="0" cellspacing="0" border="0" class="dyntable" id="sitelist">
 <thead>
     <tr>
         <th class="head0">${i18n.edit['persistence.title.date']}</th>
         <th class="head1">${i18n.edit['persistence.title.version']}</th>
         <th class="head0">${i18n.edit['persistence.title.type']}</th>
         <th class="head0">&nbsp;</th>
     </tr>
</thead>
<colgroup>
    <col class="con0" />
    <col class="con1" />
    <col class="con0" />
    <col class="con1" />
</colgroup>
<tbody> 
 <c:forEach var="persistence" items="${persistences}">
 <tr class="gradeX">
     <td class="con0">${persistence.date}</td>
     <td class="con1">${persistence.version}</td>
     <td class="con0">${persistence.type}</td>
     <td class="con1"><c:if test="${persistence.type eq 'preview'}">
     	<form id="restore-${persistence.version}" method="post">
     		<div class="line action">
     			<input type="hidden" name="webaction" value="restore" />
     			<input type="hidden" name="version" value="${persistence.version}" />
     			<input type="submit" class="warning needconfirm" value="${i18n.edit['persistence.title.restore']}" />
     		</div>
     	</form>
     </c:if></td>             
 </tr>
 </c:forEach>
</tbody>
<tfoot>
    <tr>
        <th class="head0">${i18n.edit['persistence.title.date']}</th>
        <th class="head1">${i18n.edit['persistence.title.version']}</th>
        <th class="head0">${i18n.edit['persistence.title.type']}</th>
        <th class="head0">&nbsp;</th>
    </tr>
</tfoot>
</table>

<script type="text/javascript">
jQuery(document).ready(function() {
	jQuery('#sitelist').dataTable( {
		"sPaginationType": "full_numbers",
		 "aaSorting": [[ 0, "desc" ]],
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
</div>