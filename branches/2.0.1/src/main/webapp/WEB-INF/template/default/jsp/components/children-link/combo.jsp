<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="combo">
<form method="post" action="${currentPageUrl}">
	<div class="ep_label"><div><label for="boxfilter1">${title}</label></div></div>
		<div class="ep_data">			
			<select name="forward-path">
				<c:forEach var="child" items="${children}" varStatus="status">
					<option value="${child.url}" ${child.selected?'selected="selected"':''}>${child.fullLabel}</option>
				</c:forEach>
			</select>							
	</div>
	<input type="submit" name="ok" value="${i18n.view['global.ok']}" />	
</form>
</div>