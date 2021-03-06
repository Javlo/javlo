<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
 %><c:set var="titleCount" value="0" />
<c:if test="${not empty clipboard.copied || not empty editInfo.copiedPage}">
<div id="_ep_clipboard" class="clipboard">
	<c:url var="url" value="${info.currentURL}" context="/">
		<c:param name="webaction" value="edit.clearClipboard" />
	</c:url>
	<h2><span class="glyphicon glyphicon glyphicon-duplicate" aria-hidden="true"></span>${i18n.edit['global.clipboard']}<a href="${url}" class="ajax close">X</a></h2>
	<div class="body component-list">
		<c:if test="${not empty clipboard.copied}">
		<div class="component" data-type="clipboard" data-deletable="true">
			<div class="wrapper-in">
				<div class="figure"><i class="fa fa-clipboard" aria-hidden="true"></i></div>				
				<span>${clipboard.label}</span>
				<div class="category">(${i18n.edit['global.clipboard']})</div>
			</div>
		</div>
		</c:if><c:if test="${not empty editInfo.copiedPage}">
		<div class="component page" data-type="clipboard-page" data-deletable="true">
			<div class="wrapper-in" title="${editInfo.copiedPage}">
				<div class="figure"><i class="fa fa-clone" aria-hidden="true"></i></div>				
				<span>${editInfo.copiedPage}</span>
				<div class="category">(page)</div>
			</div>
		</div>
		</c:if>
	</div>
</div>
</c:if>
<c:if test="${globalContext.componentsFiltered}">
<script type="text/javascript">
function displayComplexity(l) {
	pjq('.components-group .btn').removeClass('active');
	pjq('.components-group .btn-'+l).addClass('active');
	pjq('#preview_command .component-list').removeClass("display-1");
	pjq('#preview_command .component-list').removeClass("display-2");
	pjq('#preview_command .component-list').removeClass("display-3");
	pjq('#preview_command .component-list').removeClass("display-b");
	pjq('#preview_command .component-list').addClass("display-"+l);
	document.cookie="_component_tab="+l+";path=/";
}
document.addEventListener("DOMContentLoaded", function(event) { 
	var componentTab = editPreview.readCookie("_component_tab");
	if (componentTab != null) {
		displayComplexity(componentTab);
	}
});
</script>
</c:if>
<c:if test="${info.admin}"><button onclick="editPreview.openModal('Components', '${info.currentEditURL}?module=admin&context=${info.contextKey}&webaction=admin.previewEditComponent&previewEdit=true'); return false;" class="btn btn-default btn-xs pull-right"><span class="glyphicon glyphicon-plus-sign" aria-hidden="true"></span></button></c:if>
 <h2><span class="glyphicon glyphicon-briefcase" aria-hidden="true"></span>Content</h2>
<c:if test="${globalContext.componentsFiltered}">
<div class="btn-group btn-group-justified components-group" role="group">
  <div class="btn-group" role="group"> 	
  <button type="button" class="btn btn-default btn-xs active btn-1" role="group" onclick="displayComplexity(1);">${i18n.edit['preview.component-group.basic']}</button>
  </div><div class="btn-group btn-group-sm" role="group">
  <button type="button" class="btn btn-default btn-xs btn-2" role="group" onclick="displayComplexity(2);">${i18n.edit['preview.component-group.extended']}</button>
  </div><div class="btn-group btn-group-sm" role="group">
  <button type="button" class="btn btn-default btn-xs btn-b" role="group" onclick="displayComplexity('b');">${i18n.edit['preview.component-group.business']}</button>
  </div><c:if test="${info.admin}"><div class="btn-group" role="group">
  <button type="button" class="btn btn-default btn-xs btn-3" role="group" onclick="displayComplexity(3);">${i18n.edit['preview.component-group.admin']}</button>
  </div></c:if>
</div>

<div class="filter-wrapper">
<input id="filter-components" type="text" class="form-control filter" placeholder="filter..." onkeyup="filter(this.value);"/>
<button type="button" class="reset-filter"><i class="fa fa-times-circle" aria-hidden="true" onclick="document.getElementById('filter-components').value = ''; filter('');"></i></button>
</div>

<script>
	function filter(text) {
		if (text === null || text.length == 0) {
			document.querySelectorAll('#preview_command .component-list .component').forEach(item => {
				item.classList.remove('hidden');
			});
			return;
		}
		text = text.toLowerCase();
		document.querySelectorAll('#preview_command .component-list .component').forEach(item => {
			if (item.querySelectorAll('.text')[0].innerHTML.toLowerCase().indexOf(text) < 0) {
				item.classList.add('hidden');
			} else {
				item.classList.remove('hidden');
			}	
		});
	}
</script>

</c:if>
<div class="component-list height-to-bottom ${globalContext.componentsFiltered?'display-1':''}">
<c:set var="cat" value="" />
<c:forEach var="comp" items="${components}">
<c:if test="${comp.metaTitle}">
<c:set var="cat" value="${i18n.edit[comp.value]}" />
</c:if><c:if test="${!comp.metaTitle}"
><c:set var="toolTipKey" value="content.${comp.type}.description" /><c:set var="toolTip" value="data-toggle=\"tooltip\" data-placement=\"right\" title=\"${i18n.edit[toolTipKey]}\"" />
<div ${i18n.edit[toolTipKey] != toolTipKey?toolTip:''} class="component${comp.selected?' selected':''} component-${comp.type} complexity-${comp.complexityLevel} business-${comp.dynamicComponent}" data-type="${comp.type}" title="${i18n.edit[cat]} : ${comp.label}">
<div class="wrapper-in">
<div class="figure"><i class="fa fa-${comp.fontAwesome}" aria-hidden="true"></i></div>
<div class="text">
<span>${comp.label}</span>
<div class="category">(${i18n.edit[cat]})</div>
</div>
</div>
</div>
</c:if></c:forEach>
<div style="clear: both;"><span></span></div>
</div>
