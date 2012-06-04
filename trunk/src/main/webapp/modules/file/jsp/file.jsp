<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${not info.editLanguage eq 'en'}"><script type="text/javascript" src="${currentModule.path}/js/i18n/elfinder.${info.editLanguage}.js"></script></c:if>
<div class="content nopadding">
<div id="fileManager" class="elfinder"></div>
</div>
<script type="text/javascript">
jQuery(document).ready(function() {
	jQuery('#fileManager').elfinder({
		url : '${currentModule.path}/jsp/connector.jsp',
	}).elfinder('instance');
});
</script>
