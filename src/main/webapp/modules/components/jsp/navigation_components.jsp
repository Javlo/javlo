<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
<div class="select-group">

	<div class="comp-list">
		<c:forEach var="link" items="${globalContext.externComponents}" varStatus="status">
			<c:url var="navURL" value="${info.currentURL}" context="/">
				<c:param name="component" value="${link.name}" />
			</c:url>
			<a class="select-comp ${param.component eq link.name?'active':''}" " href="${navURL}">${link.name}</a>
		</c:forEach>
	</div>


	<form id="form-add-component" action="${info.currentURL}" method="post" class="_jv_flex-line">
		<div class="input">
			<input name="webaction" value="components.addcomponent" type="hidden">
			<input class="form-control" name="component" placeholder="add component..." type="text">
		</div>
		<div class="button">
			<input class="btn btn-default btn-xs" value="ok" type="submit">
		</div>
	</form>

</div>
