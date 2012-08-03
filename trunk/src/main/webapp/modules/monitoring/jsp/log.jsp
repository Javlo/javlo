<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">	
	<div class="log-pause line">
		<input id="pause-log" type="checkbox" />
		<label for="pause-log">Pause</label>		
	</div>
	<div class="log-content">
		<jsp:include page="log_content.jsp" />
	</div>
	<div id="wait"><span id="ajax-loader">&nbsp;</span></div>	
</div>
