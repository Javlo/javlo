<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">

<table cellpadding="0" cellspacing="0" border="0" class="dyntable" id="notes">
 <thead>
     <tr>       
       <th class="head1">Authors</th>
       <th class="head0">title</th>                  
       <c:if test="${not info.editContext.lightInterface}">
       <th class="head1">priority</th>       
       <th class="head0">create</th>
       </c:if>
       <th class="head1">update</th>       
       <th class="head0">#comments</th>
       <th class="head1">status</th>
       <c:if test="${globalContext.master}">       
       <th class="head0">authors</th>
       <th class="head1">context</th>
       <th class="head0">share</th>
       </c:if>       
     </tr>     
</thead>
<colgroup>   
    <col class="con1" />
    <col class="con0" />
    <c:if test="${not info.editContext.lightInterface}">
    <col class="con1" />
    <col class="con0" />
    </c:if>
    <col class="con1" />            
    <col class="con0" />
    <col class="con1" />
    <c:if test="${globalContext.master}">
    <col class="con0" />
    <col class="con1" />
    <col class="con0" />
    </c:if>
</colgroup>
<tbody> 
 <c:forEach var="ticket" items="${tickets}">
 <c:if test="${not empty ticket.title}">
 <tr class="gradeX${ticket.deleted?' deleted':''}">
 	 <td class="con1">${ticket.authors}</td>
 	 <c:url var="ticketURL" value="${info.currentURL}" context="/">
 	 	<c:param name="id" value="${ticket.id}" />
 	 </c:url>	 
     <td class="con0"><a class="${ticket.read?'read':'unread'}" href="${ticketURL}">${empty ticket.title ? '?' : ticket.title}</a><span class="hidden">${ticket.message}</span></td>
     <c:if test="${not info.editContext.lightInterface}">
     <td class="con1">${ticket.priority}</td>     
     <td class="con0">${ticket.creationDateLabel}</td>
     </c:if>
     <td class="con1">${ticket.lastUpdateDateLabel}</td>
     <td class="con0">${fn:length(ticket.comments)}</td>     
     <td class="con1"><a class="status ${ticket.status}" href="${ticketURL}">${ticket.status}</a></td>
     <c:if test="${globalContext.master}">
     <td class="con0">${ticket.authors}</td>
     <td class="con1">${ticket.context}</td>
     <td class="con0"><a class="share ${ticket.share}" href="${ticketURL}"><c:if test="${empty ticket.share}">none</c:if>${ticket.share}</a></td>
     </c:if>
 </tr>  
 </c:if>   
</c:forEach>
</tbody>
<tfoot>
   <tr>       
        <tr> 
        <th class="head1">Authors</th>      
        <th class="head0">title</th>                  
        <c:if test="${not info.editContext.lightInterface}">
        <th class="head1">priority</th>       
        <th class="head0">create</th>
        </c:if>
        <th class="head1">update</th>       
        <th class="head0">#comments</th>
        <th class="head1 filter">status</th>
        <c:if test="${globalContext.master}">
        <th class="head0 filter">authors</th>
        <th class="head1 filter">context</th>
        <th class="head0">share</th>
       </c:if>        
     </tr> 
   </tr>
</tfoot>
</table>

<script type="text/javascript">
jQuery(document).ready(function() {
	jQuery('#notes').dataTable( {
		 "sPaginationType": "full_numbers",
		 "aaSorting": [[ ${not info.editContext.lightInterface ? 3 : 1}, "desc" ]],
		 "aoColumns": [
		               null,
		               null,
		               <c:if test="${not info.editContext.lightInterface}">
		               null,
		               null,
		               </c:if>
		               null,
		               null,		               
		               <c:if test="${globalContext.master}">
		               null,
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