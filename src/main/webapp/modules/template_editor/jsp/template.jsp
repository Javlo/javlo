<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<div id="template" class="content">
	<c:if test="${not empty templateURL}">
		<iframe id="template-iframe" style="width: 100%; height: 800px;" src="${templateURL}"></iframe>
	</c:if>
	<c:if test="${empty templateURL}">
		<br />
		<div id="message-container">
			<div class="notification msgalert">				
				no editable template found, create one.
			</div>
		</div>
	</c:if>
</div>