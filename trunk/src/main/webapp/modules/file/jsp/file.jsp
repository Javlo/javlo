<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="content nopadding">
<div id="fileManager"></div>
</div>
<script type="text/javascript">
jQuery(document).ready(function() {
	jQuery('#fileManager').elfinder({
		url : '${currentModule.path}/jsp/connector.jsp',
	})
});
</script>
