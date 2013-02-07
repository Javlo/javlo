<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">

<table cellpadding="0" cellspacing="0" border="0" class="dyntable" id="notes">
 <thead>
     <tr>
       <th class="head0">id</th>
       <th class="head1">title</th>          
       <th class="head0">authors</th>           
       <th class="head1">priority</th>
       <th class="head0">status</th>
       <th class="head1">date</th>
       <th class="head0">#comments</th>
       <th class="head1">context</th>
     </tr>
</thead>
<colgroup>
    <col class="con0" />
    <col class="con1" />
    <col class="con0" />
    <col class="con1" />
    <col class="con0" />
    <col class="con1" />            
    <col class="con0" />
    <col class="con1" />
</colgroup>
<tbody> 
 <c:forEach var="ticket" items="${tickets}">
 <tr class="gradeX">
	 <td class="con0"><a href="${info.currentURL}?id=${ticket.id}">${ticket.id}</a></td>
     <td class="con1">${ticket.title}</td>
     <td class="con0">${ticket.authors}</td>	 
     <td class="con1">${ticket.priority}</td>
     <td class="con0">${ticket.status}</td>
     <td class="con1">${ticket.creationDateLabel}</td>
     <td class="con0">${fn:length(ticket.comments)}</td>
     <td class="con1">${ticket.context}</td>
 </tr>     
</c:forEach>
</tbody>
<tfoot>
   <tr>
       <th class="head0">id</th>
       <th class="head1">title</th>          
       <th class="head0">authors</th>           
       <th class="head1">priority</th>
       <th class="head0">status</th>
       <th class="head1">date</th>
       <th class="head0">#comments</th>
       <th class="head1">context</th>
   </tr>
</tfoot>
</table>

<script type="text/javascript">
jQuery(document).ready(function() {
	jQuery('#notes').dataTable( {
		 "sPaginationType": "full_numbers",
		 "aaSorting": [[ 0, "desc" ]],
		 "aoColumns": [
		               null,
		               null,
		               null,
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