<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><%
/*
 * the id is rendered below into html attributes and into an inline javascript
 * call : a taxonomy id is an identifier, so an unexpected value is dropped
 * rather than escaped twice over.
 */
String taxonomyIdParam = request.getParameter("id");
if (taxonomyIdParam == null || !taxonomyIdParam.matches("[A-Za-z0-9_.\\-]{0,64}")) {
	taxonomyIdParam = "";
}
request.setAttribute("safeTaxonomyId", taxonomyIdParam);
%><c:set var="node" value="${taxonomy.taxonomyBeanMap[safeTaxonomyId]}" />
<div id="item-wrapper-${node.id}" class="item-wrapper" draggable="true" data-id="${node.id}" data-aschild="false">
	<a id="action-list-${node.id}" href="#" class="command action-list ${fn:length(node.children)>0?'open':'close'}" title="expand"><i class="bi bi-chevron-down"></i><i class="bi bi-chevron-right"></i></a>
	<a href="#" class="command delete" title="delete" onclick="jQuery('#input-delete').val('${safeTaxonomyId}'); submitTaxonomyForm(); return false;"><i class="bi bi-trash"></i></a>
	<a href="#" class="command move" title="move"><i class="bi bi-grip-vertical"></i></a>
	<div class="item">
		<span class="name"><input type="text" name="name-${safeTaxonomyId}" class="hidden-input form-control" value="${node.name}" /></span>
		<div class="taxo-detail">
		<a href="#" class="action close ${node.labelsSize == 0?'empty':node.labelsSize<fn:length(info.contentLanguages)?'incomplete':'complete'}">
			<i class="bi bi-plus-circle"></i>
			<i class="bi bi-dash-circle"></i>
		</a>
		<ul class="translation bloc hidden">
		<li class="label id">
				<label class="lang" for="change-id-${safeTaxonomyId}">ID</label>
				<span class="text"><input type="text" name="change-id-${safeTaxonomyId}" id="change-id-${safeTaxonomyId}" class="hidden-input form-control" value="${safeTaxonomyId}" placeholder="ID" /></span>
			</li>
			<li class="label path">
				<label class="lang" for="path-${safeTaxonomyId}">Path</label>
				<span class="text"><input readonly="readonly" type="text" id="path-${safeTaxonomyId}" class="hidden-input form-control" value="<c:out value="${param.path}" />" placeholder="ID" /></span>
			</li>
		<li class="label deco">
			<label class="lang" for="change-deco-${safeTaxonomyId}"><i class="bi bi-fonts" aria-hidden="true"></i></label>
			<span class="text"><input type="text" name="change-deco-${safeTaxonomyId}" id="change-deco-${safeTaxonomyId}" class="hidden-input form-control" value="${node.decoration}" placeholder="deco" /></span>
		</li>
		<c:forEach var="lang" items="${info.contentLanguages}">
			<li class="label">
				<label class="lang" for="label-${lang}-${safeTaxonomyId}">${lang}</label>
				<span class="text"><input type="text" name="label-${lang}-${safeTaxonomyId}" id="label-${lang}-${safeTaxonomyId}" class="hidden-input form-control" value="${node.labels[lang]}" placeholder="..." /></span>
			</li>
		</c:forEach>
		</ul>
		</div>
	</div>
</div>

