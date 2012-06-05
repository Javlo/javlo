<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${not empty param.path}">
	<c:set var="ELPath" value="${param.path}" scope="session" />
</c:if>
<c:if test="${not info.editLanguage eq 'en'}"><script type="text/javascript" src="${currentModule.path}/js/i18n/elfinder.${info.editLanguage}.js"></script></c:if>
<div class="content nopadding">
<div id="fileManager" class="elfinder"></div>
</div>
<script type="text/javascript">
jQuery(document).ready(function() {
	jQuery('#fileManager').elfinder({
		/*<c:set var="pathParam" value="?path=${param.path}" />*/
		/*url : '${info.staticRootURL eq "/"?"":info.staticRootURL}${currentModule.path}/jsp/connector.jsp${not empty param.path?pathParam:""}',*/
		url : '${info.staticRootURL eq "/"?"":info.staticRootURL}${currentModule.path}/jsp/connector.jsp',
		lang : '${info.editLanguage}',
		height: jQuery("#footer").offset().top - jQuery("#fileManager").offset().top - jQuery(".mainBoxe .widgetbox h3").height(),
		quicklook : {
			autoplay : true,
			jplayer  : 'extensions/jplayer'
		},
		uiOptions : {
		toolbar : [
		   		['back', 'forward'],
 		   		['mkdir', 'mkfile', 'upload'],
		   		['open', 'download', 'getfile'],
		   		['info'],
		   		/*['quicklook'],*/
		   		['copy', 'cut', 'paste'],
		   		['rm'],
		   		['duplicate', 'rename', 'edit', 'resize'],		   		
		   		['search'],
		   		['view'],
		   		['help']
		   	] }
	}).elfinder('instance');
	changeFooter();
});
</script>
