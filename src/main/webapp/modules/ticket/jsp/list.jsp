<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">

<table cellpadding="0" cellspacing="0" border="0" class="dyntable" id="notes">
 <thead>
     <tr>       
       <th class="head0">title</th>                  
       <th class="head1">priority</th>       
       <th class="head0">create</th>
       <th class="head1">update</th>       
       <th class="head0">#comments</th>
       <th class="head1">status</th>
       <c:if test="${globalContext.master}">
       <th class="head0">authors</th>
       <th class="head1">context</th>
       </c:if>       
     </tr>     
</thead>
<colgroup>   
    
    <col class="con0" />
    <col class="con1" />
    <col class="con0" />
    <col class="con1" />            
    <col class="con0" />
    <col class="con1" />
    <c:if test="${globalContext.master}">
    <col class="con0" />
    <col class="con1" />
    </c:if>
</colgroup>
<tbody> 
 <c:forEach var="ticket" items="${tickets}">
 <tr class="gradeX">	 
     <td class="con0"><a class="${ticket.read?'read':'unread'}" href="${info.currentURL}?id=${ticket.id}">${ticket.title}</a><span class="hidden">${ticket.message}</span></td>     	 
     <td class="con1">${ticket.priority}</td>     
     <td class="con0">${ticket.creationDateLabel}</td>
     <td class="con1">${ticket.lastUpdateDateLabel}</td>
     <td class="con0">${fn:length(ticket.comments)}</td>     
     <td class="con1"><a class="status ${ticket.status}" href="${info.currentURL}?id=${ticket.id}">${ticket.status}</a></td>
     <c:if test="${globalContext.master}">
     <td class="con0">${ticket.authors}</td>
     <td class="con1">${ticket.context}</td>
     </c:if>
 </tr>     
</c:forEach>
</tbody>
<tfoot>
   <tr>       
        <tr>       
        <th class="head0">title</th>                  
       <th class="head1">priority</th>       
       <th class="head0">create</th>
       <th class="head1">update</th>       
       <th class="head0">#comments</th>
       <th class="head1">status</th>
       <c:if test="${globalContext.master}">
       <th class="head0">authors</th>
       <th class="head1">context</th>
       </c:if>        
     </tr> 
   </tr>
</tfoot>
</table>

<script type="text/javascript">
jQuery(document).ready(function() {
	jQuery('#notes').dataTable( {
		 "sPaginationType": "full_numbers",
		 "aaSorting": [[ 3, "desc" ]],
		 "aoColumns": [
		               null,
		               null,
		               null,
		               null,
		               null,		               
		               <c:if test="${globalContext.master}">
		               null,
		               null,
		               </c:if>
		               null
		           ],
		 "oLanguage": {"sUrl": "${info.editTemplateURL}/js/plugins/i18n/datatable_${info.editLanguage}.txt"},
		 "fnInitComplete": updateLayout
	});
	
});
</script>
</div>