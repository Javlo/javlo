<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%>
<div class="special last"> <!-- components -->
<form id="delete-comments" action="${info.currentURL}" method="post">
<input type="hidden" name="webaction" value="deleteComments" />
<input type="text" name="filter" value="" placeholder="filter" />
<input type="submit" value="delete" />
</form>
<c:if test="${empty param.previewEdit}">
<a class="close" href="${info.currentURL}?webaction=displayComponentsList">x</a>
</c:if>
</div>

