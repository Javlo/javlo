<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="selectInlineClass" value="ui-tabs-selected" />
<c:forEach var="comment" items="${commentGroup.comments}">
			<c:if test="${!comment.validReaction}">
				<c:set var="selectInlineClass" value="" />
			</c:if>
</c:forEach>
<div class="content search-result">
    <c:if test="${empty items}">${i18n.edit['search.no-result']}</c:if>
	<c:if test="${not empty items}">
		<form id="form-select-user" action="${info.currentURL}" method="post">
		<div>
			<input type="hidden" name="webaction" value="deleteUser" />
		</div>
		<table cellpadding="0" cellspacing="0" border="0" class="dyntable" id="sitelist">
			<thead>
				<tr>
					<th class="head0"></th>
					<th class="head1">Q</th>
					<th class="head0">${i18n.edit['search.type']}</th>
					<th class="head1">${i18n.edit['search.title']}</th>
					<th class="head0">${i18n.edit['search.authors']}</th>	
					<th class="head1">${i18n.edit['search.date']}</th>									
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
			
			<c:forEach var="item" items="${items}">
				<tr>
					<td class="head0 image"><c:if test="${not empty item.previewURL}"><img class="responsive-img" src="${item.previewURL}"/></c:if></td>
					<td class="head1 matching">${item.matching}</td>
				    <td class="head0">${item.type}</td>
					<td class="head1"><a href="${item.url}">${item.title}</a></td>
					<td class="head0">${item.authors}</td>	
					<td class="head1">${item.date}</td>
				</tr>
			</c:forEach>
				
			</tbody>
			<tfoot>
				<tr>
					<th class="head0"></th>
					<th class="head1">Q</th>
					<th class="head0">${i18n.edit['search.type']}</th>
					<th class="head1">${i18n.edit['search.title']}</th>
					<th class="head0">${i18n.edit['search.authors']}</th>	
					<th class="head1">${i18n.edit['search.date']}</th>									
				</tr>
			</tfoot>
		</table>
		<div class="action">
			<input type="submit" name="delete" value="delete" class="js-hidden" />
		</div>
	</form>

	<script type="text/javascript">
		jQuery(document)
				.ready(
						function() {
							jQuery('#sitelist')
									.dataTable(
											{
												"sPaginationType" : "full_numbers",
												"aaSorting" : [ [ 1, "desc" ] ],
												"aoColumns" : [   { "asSorting": [ "" ] }, null, null,	null, null, null ],
												"oLanguage" : {
													"sUrl" : "${info.editTemplateURL}/js/plugins/i18n/datatable_${info.editLanguage}.txt"
												},
												"fnInitComplete" : updateLayout
											});

						});
	</script>
	</c:if>
</div>