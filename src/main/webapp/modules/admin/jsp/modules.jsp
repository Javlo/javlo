<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="left">

<form id="form-modules" action="${info.currentURL}" method="post" class="standard-form">
<div>
<input type="hidden" name="webaction" value="modulesSelect" />
<input type="hidden" name="context" value="${context}" />
</div>

<table cellpadding="0" cellspacing="0" border="0" class="dyntable" id="sitelist">
 <thead>
     <tr>       
       <th class="head0">${i18n.edit['global.name']}</th>
       <th class="head1">${i18n.edit['admin.module.version']}</th>
       <th class="head0">${i18n.edit['global.title']}</th>
       <th class="head1">${i18n.edit['global.description']}</th>       
       <th class="head0">${i18n.edit['global.select']}</th>
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
 <c:forEach var="module" items="${allModules}">
 <tr class="gradeX">
     <td class="con0">${module.name}</td>
     <td class="con1">${module.version}</td>
     <td class="con0">${module.title}</td>
     <td class="con1">${module.description}</td>
     <td class="con0">
     <c:set var="contains" value="false" />
		<c:forEach var="cmod" items="${currentModules}">		
  	 	<c:if test="${module.name eq cmod}">
    		<c:set var="contains" value="true" />
  		</c:if>
	</c:forEach>
	<div style="display: none;">${contains}</div><input type="checkbox"${contains?' checked="checked"':''} name="${module.name}" /> 
     </td>
 </tr>     
 </c:forEach>
</tbody>
<tfoot>
   <tr>       
       <th class="head0">${i18n.edit['global.name']}</th>
       <th class="head1">${i18n.edit['admin.module.version']}</th>
       <th class="head0">${i18n.edit['global.title']}</th>
       <th class="head1">${i18n.edit['global.description']}</th>       
       <th class="head0">${i18n.edit['global.select']}</th>
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
<input type="submit" name="back" value="${i18n.edit['global.back']}" />
<input type="submit" name="select" value="${i18n.edit['global.select']}" />
</div>
</form>


</div>