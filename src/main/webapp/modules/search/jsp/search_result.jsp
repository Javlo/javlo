<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:set var="selectInlineClass" value="ui-tabs-selected" />
<c:forEach var="comment" items="${commentGroup.comments}">
			<c:if test="${!comment.validReaction}">
				<c:set var="selectInlineClass" value="" />
			</c:if>
</c:forEach>
<div class="content search-result">
	<c:if test="${not empty downloadUrl}"><a class="download-link" href="${downloadUrl}">download <i class="fa fa-file-excel" aria-hidden="true"></i></a></c:if>
    <c:if test="${empty items}">${i18n.edit['search.no-result']}</c:if>
	<c:if test="${not empty items}">
		<form id="form-select-user" action="${info.currentURL}" method="post">
		<div>
			<input type="hidden" name="webaction" value="deleteUser" />
		</div>
		<table cellpadding="0" cellspacing="0" border="0" class="dyntable cell-border compact stripe" id="sitelist">
			<thead>
				<tr>
					<th class="head0"></th>
					<c:if test="${globalContext.master}"><th class="head1">site</th></c:if>
					<th class="head1">Q</th>
					<th class="head0">${i18n.edit['search.type']}</th>
					<th class="head1">${i18n.edit['search.title']}</th>
					<th class="head0">${i18n.edit['search.authors']}</th>	
					<th class="head1">${i18n.edit['search.date']}</th>									
				</tr>
			</thead>
			<colgroup>
				<col class="con0" />
				<c:if test="${globalContext.master}"><col class="con1" /></c:if>
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
					<c:if test="${globalContext.master}"><td class="head1 site">${item.context}</td></c:if>
					<td class="head1 matching">${item.matching}</td>
				    <td class="head0">${item.type}</td>
					<td class="head1"><a href="${item.url}" target="_blank">${item.title}</a></td>
					<td class="head0">${item.authors}</td>	
					<td class="head1">${item.date}</td>
				</tr>
			</c:forEach>
				
			</tbody>
			<tfoot>
				<tr>
					<th class="head0"></th>
					<c:if test="${globalContext.master}"><th class="head1">site</th></c:if>
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
												<c:if test="${!globalContext.master}">
												"aoColumns" : [   { "asSorting": [ "" ] }, null, null,	null, null, null ],
												</c:if><c:if test="${globalContext.master}">
												"aoColumns" : [   { "asSorting": [ "" ] }, null, null,	null, null, null , null ],
												</c:if>
												"oLanguage" : {
													"sUrl" : "${info.editTemplateURL}/js/plugins/i18n/datatable_${info.editLanguage}.txt"
												},
												"fnInitComplete" : updateLayout
											});

						});
	</script>
	</c:if>
</div>