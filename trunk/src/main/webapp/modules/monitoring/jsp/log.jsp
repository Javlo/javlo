<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">
	<div class="log-pause">
		<label><input type="checkbox" />Pause</label>
	</div>
	<div class="log-content">
		<jsp:include page="log_content.jsp" />
	</div>
</div>
