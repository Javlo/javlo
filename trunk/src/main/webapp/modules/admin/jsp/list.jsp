<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="left">

<table cellpadding="0" cellspacing="0" border="0" class="dyntable" id="sitelist">
 <thead>
     <tr>
         <th class="head0">${i18n.edit['admin.title.context-key']}</th>
         <th class="head1">${i18n.edit['admin.title.admin']}</th>
         <th class="head0">${i18n.edit['admin.title.alias']}</th>
         <th class="head1">${i18n.edit['admin.title.creation']}</th>
         <th class="head0">${i18n.edit['admin.title.latest-login']}</th>
         <th class="head1">${i18n.edit['admin.title.count-user']}</th>
         <th class="head0">${i18n.edit['admin.title.loaded']}</th>         
         <th class="head0">${i18n.edit['admin.title.view']}</th>
         <th class="head1">${i18n.edit['admin.title.edit']}</th>         
         <th class="head0">&nbsp;</th>
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
 <c:forEach var="context" items="${contextList}">
 <tr class="gradeX">
     <td class="con0">${context.key}</td>
     <td class="con1">${context.administrator}</td>
     <td class="con0">${context.aliasOf}</td>
     <td class="con1">${context.creationDate}</td>
     <td class="con0">${context.latestLoginDate}</td>
     <td class="con1">${context.countUser}</td>
     <td class="con0">
     	<c:if test="${context.view eq 'true'}">
     	<a href="${info.currentURL}?webaction=releaseContent&view=true&context=${context.key}"><span class="icone_true">true</span></a>
     	</c:if>
     	<c:if test="${context.view eq 'false'}">
     	<span class="icone_false">false</span>
     	</c:if>
     	/
     	<c:if test="${context.edit eq 'true'}">
     	<a href="${info.currentURL}?webaction=releaseContent&context=${context.key}"><span class="icone_true">true</span></a>
     	</c:if>
     	<c:if test="${context.edit eq 'false'}">
     	<span class="icone_false">false</span>
     	</c:if>
     </td>
     <td class="con1"><span class="icone_${context.visibility}">${context.visibility}</span></td>
     <td class="con0"><span class="icone_${context.editability}">${context.editability}</span></td>
     <td class="con1">
     	<form id="form-change-site" action="${info.currentURL}" method="post" class="array-form">
     		<div>
     			<input type="hidden" name="webaction" value="changesite" />
     			<input type="hidden" name="context" value="${context.key}" />
     		</div>
     		<div class="line action">
     			<input type="submit" name="change" value="${i18n.edit['global.change']}" />
     			<input type="submit" name="components" value="${i18n.edit['command.admin.components']}" />     			
     		</div>
     	</form>
     </td>          
 </tr>
 </c:forEach>
</tbody>
<tfoot>
   <tr>
     <th class="head0">${i18n.edit['admin.title.context-key']}</th>
     <th class="head1">${i18n.edit['admin.title.context-key']}</th>
     <th class="head0">${i18n.edit['admin.title.alias']}</th>
     <th class="head1">${i18n.edit['admin.title.alias']}</th>
     <th class="head0">${i18n.edit['admin.title.latest-login']}</th>
     <th class="head1">${i18n.edit['admin.title.count-user']}</th>
	 <th class="head0">${i18n.edit['admin.title.loaded']}</th>         
     <th class="head0">${i18n.edit['admin.title.view']}</th>
     <th class="head1">${i18n.edit['admin.title.edit']}</th>
     <th class="head0">&nbsp;</th>
   </tr>
</tfoot>
</table>

<script type="text/javascript">
jQuery(document).ready(function() {
	jQuery('#sitelist').dataTable( {
		"sPaginationType": "full_numbers",
		 "aaSorting": [[ 4, "desc" ]],
		 "aoColumns": [
		               null,
		               null,
		               null,
		               null,
		               null,
		               null,
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