<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%><form id="delete-comments" action="${info.currentURL}" method="post">

	<%-- 	<label for="taxonomy">${i18n.edit["taxonomy"]}</label>${taxoSelect} --%>
	<input type="hidden" name="webaction" value="search" />
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
</form>