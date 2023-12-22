<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><div id="template-properties" class="content">
	<div class="tabs">
	<c:if test="${not empty templateEditorContext.area}">	
		<c:url var="taburl" value="${info.currentURL}" context="/">
			<c:param name="webaction" value="data.tab"></c:param>
		</c:url>	
		<ul>
			<li${tab == 'template'?' class="ui-tabs-selected"':''}><a href="#template" onclick="ajaxRequest('${taburl}&tab=template',null,null);">template</a></li>						
			<li${tab == 'row'?' class="ui-tabs-selected"':''}><a href="#row" onclick="ajaxRequest('${taburl}&tab=row',null,null);">row</a></li>
			<li${tab == 'area'?' class="ui-tabs-selected"':''}><a href="#area" onclick="ajaxRequest('${taburl}&tab=area',null,null);">area</a></li>
		</ul>	
		<c:set var="delete" value="" />
		<c:if test="${templateEditorContext.currentTemplate.parent != 'default'}">
			<c:set var="delete" value="&delete=true" />
		</c:if>
		<c:set var="part" value="${templateEditorContext.currentTemplate.style}" scope="request" />
		<c:set var="exclude" value="${templateEditorContext.currentTemplate.templateExcludeProperties}" scope="request" />
		<div id="template"><jsp:include page="part.jsp?title=template&webaction=template-editor.updateStyle${delete}&upload=true&parent=true" /></div>			
		<c:set var="delete" value="" />
		<c:if test="${templateEditorContext.area.name != 'content'}">
			<c:set var="delete" value="&delete=true" />
		</c:if>	
		<c:set var="part" value="${templateEditorContext.area}" scope="request" />
		<c:set var="exclude" value="${templateEditorContext.currentTemplate.areaExcludeProperties}" scope="request" />
		<div id="area"><jsp:include page="part.jsp?title=area&webaction=template-editor.updateArea${delete}&editName=true&priority=true" /></div>
		
		<c:set var="delete" value="" />
		<c:if test="${templateEditorContext.area.row.name != 'row-1'}">
			<c:set var="delete" value="&delete=true" />
		</c:if>
		<c:set var="part" value="${templateEditorContext.area.row}" scope="request" />
		<c:set var="exclude" value="${templateEditorContext.currentTemplate.rowExcludeProperties}" scope="request" />
		<div id="row"><jsp:include page="part.jsp?title=row&webaction=template-editor.updateRow${delete}&editName=true" /></div>
	</c:if>
	</div>

	<c:url var="url" value="${info.currentURL}" context="/">
		<c:param name="webaction" value="template-editor.selectArea"></c:param>
	</c:url>
	
	<script type="text/javascript">
	
		function updateProperties(area) {
			var url = "${url}&area="+area;
			ajaxRequest(url,null,null);
		}
		
		updateColorInput();
	
	</script>
	
</div>


