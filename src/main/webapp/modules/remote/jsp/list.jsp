<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">

<table cellpadding="0" cellspacing="0" border="0" class="dyntable cell-border compact stripe display" id="remotelist">
 <thead>
     <tr>       
       <th class="head0">url</th>                  
       <th class="head1">priority</th>       
       <th class="head0">online ?</th>
       <th class="head1">change date</th>       
       <th class="head0">text</th>
       <th class="head1">message</th>
       <th class="head0">server info</th>
       <th class="head1">req/min</th>
       <th class="head0">online users</th>
       <th class="head1">last publish</th>
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
    <col class="con0" />
    <col class="con1" />
    <col class="con0" />
</colgroup>
<tbody> 
 <c:forEach var="remote" items="${remotes}">
 <tr class="gradeX${remote.valid?' valid':' error'}">	 
     <td class="con0"><a href="${remote.url}" target="_blank">${remote.url}</a></td>     	 
     <td class="con1">${remote.priority}</td>     
     <td class="con0 valid-cell"><span>${remote.valid}</span></td>
     <td class="con1">${remote.latestChangeDisplay}</td>
     <td class="con0">${remote.text}</td>
     <td class="con1">${remote.error}</td>   
     <td class="con0">
     	<c:if test="${remote.serverInfo.loaded}">
     		${remote.serverInfo.localName} (${remote.serverInfo.localAddr}:${remote.serverInfo.localPort})
     		<c:if test="${not empty remote.serverInfo.version}">
     			v ${remote.serverInfo.version}
     		</c:if>
     	</c:if>
     	<c:if test="${not empty remote.serverInfo.message}">
     		<c:if test="${remote.serverInfo.loaded}">
     			<br/>
     		</c:if>
     		${remote.serverInfo.message}
     	</c:if>
     </td>
     <td class="con1">
     	<c:if test="${remote.serverInfo.loaded}">
     		${remote.serverInfo.countServiceCount} (${remote.serverInfo.countServiceAverage})
     	</c:if>
     </td>
     <td class="con0">
     	<c:if test="${remote.serverInfo.connectedUsers != null}">
     		<c:set var="userList"><c:forEach var="u" varStatus="status" items="${remote.serverInfo.connectedUsers}">${u}${not status.last ? ', ' : ''}</c:forEach></c:set>
     		<span title="Users: ${empty userList ? '-' : userList}">${fn:length(remote.serverInfo.connectedUsers)}</span>
     	</c:if>
     </td>   
     <td class="con1">
     	<c:if test="${remote.serverInfo.loaded}">
     		${remote.serverInfo.lastPublishDate} (${remote.serverInfo.lastPublisher})
     	</c:if>
     </td>
     <td class="con0">
     	<a class="action-button" href="${info.currentURL}?webaction=check&id=${remote.id}">check</a>
     	<a class="action-button" href="${info.currentURL}?webaction=delete&id=${remote.id}">delete</a>
     </td>
 </tr>     
</c:forEach>
</tbody>
<tfoot>
   <tr>       
       <th class="head0">url</th>                  
       <th class="head1">priority</th>       
       <th class="head0">online ?</th>
       <th class="head1">change date</th>       
       <th class="head0">text</th>
       <th class="head1">message</th>
       <th class="head0">server info</th>
       <th class="head1">req/min</th>
       <th class="head0">online users</th>
       <th class="head1">last publish</th>
       <th class="head0">&nbsp;</th>
     </tr>
</tfoot>
</table>

<script type="text/javascript">
jQuery(document).ready(function() {
	jQuery('#remotelist').dataTable( {
		 "aaSorting": [[ 2, "desc" ]],
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
		               null,
		               { "asSorting": [ "" ] }
		           ], 
		 "oLanguage": {"sUrl": "${info.editTemplateURL}/js/plugins/i18n/datatable_${info.editLanguage}.txt"},
		 "fnInitComplete": updateLayout
	});
	
});

</script>
</div>