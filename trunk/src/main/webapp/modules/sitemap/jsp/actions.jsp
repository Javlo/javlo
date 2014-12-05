<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="special">
<form class="form_default" method="post" action="${info.currentURL}">
<label>${i18n.edit['sitemap.lable.depth']}
<select class="with-title" name="max-depth" onchange="filterDepth(jQuery(this).val());" >
	<option value="0">${i18n.edit["global.all"]}</option>
	<c:forEach var="depth" begin="1" end="6"><option>${depth}</option></c:forEach>
</select>
</label>
</div>
<script type="text/javascript">
function filterDepth(depth) {	
	jQuery("#sitemap tr").show();
	if (depth>0) {
		for (var i=depth; i<999; i++) {
			jQuery("#sitemap .depth-"+(i+1)).hide();
		}
	}
}
</script>
