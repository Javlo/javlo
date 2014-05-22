<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="left">

<form id="form-components" action="${info.currentURL}" method="post" class="standard-form">
<div>
<input type="hidden" name="webaction" value="componentsSelect" />
<input type="hidden" name="context" value="${context}" />
</div>

<table cellpadding="0" cellspacing="0" border="0" class="dyntable" id="sitelist">
 <thead>
     <tr>
       <th class="head0">type</th>
       <th class="head1">${i18n.edit['global.name']}</th>
       <th class="head0">${i18n.edit['admin.title.color']}</th>
       <th class="head1">${i18n.edit['content.title.cacheable']}</th>
       <th class="head0">${i18n.edit['admin.title.complexity']}</th>
       <th class="head1">class</th>
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
 <c:forEach var="comp" items="${components}">
 <tr class="gradeX">
     <td class="con0">${comp.type}</td>
     <c:set var="i18nKey" value="content.${comp.type}"></c:set>
     <td class="con1">${i18n.edit[i18nKey]}</td>
     <td class="con0" style="background-color: #${comp.hexColor}">${comp.hexColor}</td>
     <td class="con1" align="center"><c:if test="${comp.cacheable}"><span class="icone_true">${comp.cacheable}</span></c:if><c:if test="${not comp.cacheable}">&nbsp;</c:if></td>
     <td class="con0 complexity" align="center"><span class="complexity-${comp.complexityLevel}">${comp.complexityLevel}</span></td>
     <td class="con1">${comp.component.className}</td>
     <td class="con0">
     <c:set var="contains" value="false" />
		<c:forEach var="item" items="${currentComponents}">		
  	 	<c:if test="${comp.component.className eq item}">
    		<c:set var="contains" value="true" />
  		</c:if>
	</c:forEach>
	<div style="display: none;">${contains}</div><input type="checkbox"${contains?' checked="checked"':''} name="${comp.component.className}" /> 
     </td>
 </tr>     
 </c:forEach>
</tbody>
<tfoot>
   <tr>
     <th class="head0">type</th>
     <th class="head1">${i18n.edit['global.name']}</th>
     <th class="head0">${i18n.edit['admin.title.color']}</th>
     <th class="head1">${i18n.edit['content.title.listable']}</th>
     <th class="head0">${i18n.edit['admin.title.complexity']}</th>
     <th class="head1">class</th>
     <th class="head0">${i18n.edit['global.select']}</th>
   </tr>
</tfoot>
</table>

<script type="text/javascript">
jQuery(document).ready(function() {
	jQuery('#sitelist').shadowInputs().dataTable( {
		 "sPaginationType": "full_numbers",
		 "aaSorting": [[ 2, "desc" ]],
		 "aoColumns": [
		               { "asSorting": [ "" ] },
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
<div class="action">
<input type="submit" name="back" value="${i18n.edit['global.back']}" />
<input type="submit" name="select" value="${i18n.edit['global.select']}" />
</div>
</form>


</div>