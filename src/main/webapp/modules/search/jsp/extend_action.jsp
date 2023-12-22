<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"%><form id="delete-comments" action="${info.currentURL}" method="post">

	<%-- 	<label for="taxonomy">${i18n.edit["taxonomy"]}</label>${taxoSelect} --%>

	<div class="content extend-search">

		<fieldset class="_jv_collapsable${searchFilterModule.extendSearch?' _jv_collapsable_default_open':''}">

			<legend>Extend search</legend>

			<c:if test="${not empty taxoSelect}">
			<label for="taxonomy">${i18n.edit["taxonomy"]}</label>${taxoSelect}
			</c:if>

			<div class="_jv_flex-line">

				<select id="select-comps" name="components" placeholder="select components">
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
				<input type="text" name="query" value="${searchFilterModule.global}" placeholder="${i18n.edit['global.search']}" />

				<button class="action-button" type="submit">${i18n.edit['search.search']}</button>
				<button class="action-button" type="submit" name="reset" value="true">${i18n.edit['search.reset']}</button>

				<!-- 			</div><div> -->
				<!-- 				<label for="select-comps">simple query (*=text)</label> -->
				<%-- 				<input name="smartquery" value="${searchFilterModule.smartquery}" /> --%>
				<!-- 			</div><div> -->
				<!-- >				<label for="select-comps">regular expression</label> -->
				<%-- 				<input name="smartqueryre" value="${searchFilterModule.smartqueryre}" /> --%>
				<!-- 			</div> -->

			</div>

		</fieldset>
</form>