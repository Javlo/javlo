<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div id="notification" class="content">

<table cellpadding="0" cellspacing="0" border="0" class="dyntable" id="notifications">
 <thead>
     <tr>
       <th class="head1">time</th>
       <th class="head0">user</th>
       <th class="head1">type</th>
       <th class="head0">message</th>       
     </tr>
</thead>
<colgroup>
    <col class="con0" />
    <col class="con1" />
    <col class="con0" />    
</colgroup>
<tbody> 
 <c:forEach var="notif" items="${notification}">
 <tr class="gradeX ${notif.notification.typeLabel}${!notif.read?' current':''}">
	 <td class="con1">${notif.notification.sortableTimeLabel}</td>
     <td class="con0">${notif.userId}</td>
     <td class="con1">${notif.notification.typeLabel}</td>
	 <td class="con0">${notif.notification.message}</td>
 </tr>     
</c:forEach>
</tbody>
<tfoot>
   <tr>
   	 <th class="head1">time</th>
     <th class="head0">user</th>
     <th class="head1">type</th>
     <th class="head0">message</th>
   </tr>
</tfoot>
</table>

<script type="text/javascript">
jQuery(document).ready(function() {
	jQuery('#notifications').dataTable( {
		 "sPaginationType": "full_numbers",
		 "aaSorting": [[ 0, "desc" ]],
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
</div>