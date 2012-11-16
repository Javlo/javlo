<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div id="search-result" class="left">
<h1>${i18n.edit['search.title']} ${param.query}</h1>
<table cellpadding="0" cellspacing="0" border="0" class="dyntable" id="sitelist">
 <thead>
     <tr>
       <th class="head0">${i18n.edit['search.title.page-relevance']}</th>
       <th class="head1">${i18n.edit['search.title.page-title']}</th>
       <th class="head0">${i18n.edit['search.title.page-url']}</th>
       <th class="head1">${i18n.edit['search.title.page-date']}</th>
       <th class="head0">${i18n.edit['search.title.page-description']}</th>       
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
 <c:forEach var="search" items="${searchList}">
 <tr class="gradeX">
     <td class="con0">${search.relevance}</td>
     <td class="con1">${search.title}</td>
     <td class="con0"><a href="${search.url}">${search.path}</a></td>
     <td class="con1">${search.dateString}</td>
     <td class="con0">${search.description}</td>                    
 </tr>
 </c:forEach>
</tbody>
<tfoot>
   <tr>
       <th class="head0">${i18n.edit['search.title.page-relevance']}</th>
       <th class="head1">${i18n.edit['search.title.page-title']}</th>
       <th class="head0">${i18n.edit['search.title.page-url']}</th>
       <th class="head1">${i18n.edit['search.title.page-date']}</th>
       <th class="head0">${i18n.edit['search.title.page-description']}</th>       
   </tr>
</tfoot>
</table>

<script type="text/javascript">
jQuery(document).ready(function() {
	jQuery('#sitelist').dataTable( {
		"sPaginationType": "full_numbers",
		 "aaSorting": [[ 0, "asc" ]],
		 "aoColumns": [
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

<div class="action">
	<form action="${info.currentURL}">
		<input type="submit" value="${i18n.edit['global.cancel']}" />
	</form>
</div>


</div>