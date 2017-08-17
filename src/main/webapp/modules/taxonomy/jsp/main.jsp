<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">
<form id="taxonomy-form" action="${info.currentURL}" method="post" class="ajax">
	<button type="submit" class="btn btn-primary pull-right">update</button>
	<div class="taxonomy-list">
	<input type="hidden" name="webaction" value="taxonomy.update" />
	<input type="hidden" name="render-mode" value="1" />
	<input type="hidden" name="delete" value="" id="input-delete" />		
	<input type="hidden" name="moveto" value="" id="moveto" />
	<input type="hidden" name="moved" value="" id="moved" />
	<input type="hidden" name="aschild" value="false" id="aschild" />
	<jsp:include page="list.jsp?id=${taxonomy.root.id}" />		
	</div>
</form>
</div>