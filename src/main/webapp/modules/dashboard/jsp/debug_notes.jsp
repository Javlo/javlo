<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div id="debug" class="content">

<table cellpadding="0" cellspacing="0" border="0" class="dyntable" id="notes">
 <thead>
     <tr>
       <th class="head0">page</th>
       <th class="head1">message</th>          
       <th class="head0">authors</th>           
     </tr>
</thead>
<colgroup>
    <col class="con0" />
    <col class="con1" />
    <col class="con0" />    
</colgroup>
<tbody> 
 <c:forEach var="note" items="${debugNotes}">
 <tr class="gradeX">
	 <td class="con0"><a href="${note.url}">${note.page.name}</a></td>
     <td class="con1">${note.message}</td>
     <td class="con0">${note.authors}</td>	 
 </tr>     
</c:forEach>
</tbody>
<tfoot>
   <tr>
       <th class="head0">page</th>
       <th class="head1">message</th>          
       <th class="head0">authors</th>           
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
		               null
		           ],
		 "oLanguage": {"sUrl": "${info.editTemplateURL}/js/plugins/i18n/datatable_${info.editLanguage}.txt"},
		 "fnInitComplete": updateLayout
	});
	
});
</script>
</div>