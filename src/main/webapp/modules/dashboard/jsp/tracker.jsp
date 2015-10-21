<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div id="content" class="content ${not empty lightInterface?'light':''}">
	<form id="roles-form" action="${info.currentURL}" method="get" class="form-inline">
		<div class="row">
		<div class="col-sm-3">
			<div class="form-group">	
				<label>date 		
				<input class="form-control" name="date" value="${date}" /></label>
			</div>
		</div><div class="col-sm-3">
			<div class="checkbox">	
				<label> 		
					<input name="excludeRessources" type="checkbox" ${param.excludeRessources == 'on'?'checked="checked"':''} /> Exclude resources
				</label>
			</div>				
		</div><div class="col-sm-6">
			<button class="btn btn-primary pull-right" type="submit">ok</button>
		</div>
		</div>
	</form>

<c:if test="${not empty tracks && fn:length(tracks)>0}">		
<table cellpadding="0" cellspacing="0" border="0" class="dyntable" id="remotelist">
 <thead>
     <tr>       
       <th class="head0">time</th>                  
       <th class="head1">ip</th>       
       <th class="head0">path</th>
       <th class="head1">action</th>       
       <th class="head0">user</th>
       <th class="head1">userAgent</th>
       <th class="head0">refered</th>
       <th class="head1">sessionId</th>
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
 <c:forEach var="track" items="${tracks}">
 <c:if test="${!(param.excludeRessources == 'on') || !track.resource}">
 <tr class="gradeX${remote.valid?' valid':' error'}">	 
     <td class="con0">${track.sortableTime}</td>     	 
 	 <td class="con1">${track.IP}</td>
 	 <td class="con0"><a href="${track.path}" title="${track.path}">${track.resource?'file':'html'}</a></td>
 	 <td class="con1">${track.action}</td>
 	 <td class="con0">${not empty track.userName?track.userName:'?'}</td>
 	 <td class="con1">${track.userAgent}</td>
 	 <td class="con0"><a href="${track.refered}" title="${track.refered}">${track.referedHost}</a></td>
 	 <td class="con1">${track.sessionId}</td>
 </tr>     
 </c:if>
</c:forEach>
</tbody>
<tfoot>
   <tr>       
       <th class="head0">time</th>                  
       <th class="head1">ip</th>       
       <th class="head0">url</th>
       <th class="head1">action</th>       
       <th class="head0">user</th>
       <th class="head1">userAgent</th>
       <th class="head0">refered</th>
       <th class="head1">sessionId</th>
     </tr>
</tfoot>
</table>
</c:if>

<script type="text/javascript">
jQuery(document).ready(function() {
	jQuery('#remotelist').dataTable( {
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
		               { "asSorting": [ "" ] }
		           ], 
		 "oLanguage": {"sUrl": "${info.editTemplateURL}/js/plugins/i18n/datatable_${info.editLanguage}.txt"},
		 "fnInitComplete": updateLayout
	});
	
});
</script>

</div>