<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div class="content">
<table cellpadding="0" cellspacing="0" border="0" class="dyntable" id="baskets">
 <thead>
     <tr>       
       <th class="head1">id</th>
       <th class="head0">total (VAT excluded)</th>                  
       <th class="head1">total (VAT include)</th>       
       <th class="head0">date</th>
       <th class="head1">status</th>    
       <th class="head0">valid info</th>          
       <th class="head1"># products</th>
       <th class="head0">actions</th>
     </tr>     
</thead>
<colgroup>
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
	<c:forEach var="basket" items="${baskets}" varStatus="status">
	<c:if test="${not basket.deleted}">
		<tr>
			<td class="id">${basket.id}</td>
			<td class="total">${basket.totalExcludingVATString}</td>
			<td class="total">${basket.totalIncludingVATString}</td>
			<td class="date">${basket.dateString}</td>
			<td class="status">${basket.status}</td>
			<td class="valid">${fn:length(basket.validationInfo)}</td>
			<td class="count_product">${fn:length(basket.productsBean)}</td>
			<td class="actions">
				<form action="${info.currentURL}" method="post">
					<div>
						<input type="hidden" name="webaction" value="ecom.manualPay" />
						<input type="hidden" name="basket" value="${basket.id}" />
						<input type="submit" value="manual pay" />
					</div>
				</form>				
				<form action="${info.currentURL}" method="post">
					<div>
						<input type="hidden" name="webaction" value="ecom.sended" />
						<input type="hidden" name="basket" value="${basket.id}" />
						<input type="submit" value="sended" />
					</div>
				</form>
				<form action="${info.currentURL}" method="post">
					<div>
						<input type="hidden" name="webaction" value="ecom.delete" />
						<input type="hidden" name="basket" value="${basket.id}" />
						<input type="submit" value="delete" />
					</div>
				</form>
			</td>
		</tr>
	</c:if>
	</c:forEach>
</tbody>
<tfoot>
   <tr>       
    <th class="head1">id</th>
    <th class="head0">total (VAT excluded)</th>                  
    <th class="head1">total (VAT include)</th>       
    <th class="head0">date</th>
   	<th class="head1">status</th>
 	<th class="head0">valid info</th>          
    <th class="head1"># products</th>
    <th class="head0">actions</th>
   </tr>
</tfoot>
</table>
</div>

<script type="text/javascript">
jQuery(document).ready(function() {
	jQuery('#baskets').dataTable( {
		 "sPaginationType": "full_numbers",
		 "aaSorting": [[ 3, "desc" ]],
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