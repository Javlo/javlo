<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%><form id="delete-comments" action="${info.currentURL}" method="post">

	<%-- 	<label for="taxonomy">${i18n.edit["taxonomy"]}</label>${taxoSelect} --%>
	
	<select id="select-comps" name="components" class="form-control" placeholder="select components">
		<option class="placeholder" value="">select components</option>
		<c:forEach var="comp" items="${components}">
			<option value="${comp.key}" data-test="${searchFilterModule.componentAsMap[comp.key]}" ${not empty searchFilterModule.componentAsMap[comp.key]?'selected="selected"':''}>${comp.value}</option>
		</c:forEach>
	</select>

	<input type="hidden" name="webaction" value="search" />
	<select name="type">
		<option value="">${i18n.edit['search.type.all']}</option>
		<option value="page" ${searchFilterModule.type == 'page'?'selected="selected"':''}>${i18n.edit['search.type.page']}</option>
		<option value="file" ${searchFilterModule.type == 'file'?'selected="selected"':''}>${i18n.edit['search.type.file']}</option>
	</select>
	<input type="text" name="title" value="${searchFilterModule.title}" placeholder="${i18n.edit['search.title']}" />
	<input type="text" name="query" value="${searchFilterModule.global}" placeholder="${i18n.edit['search.global']}" />

	<button class="action-button" type="submit">${i18n.edit['search.search']}</button>
	<button class="action-button" type="submit" name="reset">${i18n.edit['search.reset']}</button>

	<!-- 			</div><div> -->
	<!-- 				<label for="select-comps">simple query (*=text)</label> -->
	<%-- 				<input name="smartquery" value="${searchFilterModule.smartquery}" /> --%>
	<!-- 			</div><div> -->
	<!-- >				<label for="select-comps">regular expression</label> -->
	<%-- 				<input name="smartqueryre" value="${searchFilterModule.smartqueryre}" /> --%>
	<!-- 			</div> -->
</form>