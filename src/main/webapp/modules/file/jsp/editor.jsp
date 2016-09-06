<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><div class="content editor">

<form action="${info.currentURL}" method="post">

	<textarea rows="20" cols="100" class="form-control">${content}</textarea>

</form>

</div>

