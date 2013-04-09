<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:if test="${empty quiz}">
<jsp:include page="default.jsp"></jsp:include>
</c:if><c:if test="${not empty quiz}">
<c:set var="field" value="${response.question}" scope="request" />
<form method="post" action="${info.currentURL}" ${comp.file?' enctype="multipart/form-data"':''}>
<input type="hidden" name="webaction" value="quiz.response" />
<jsp:include page="field.jsp" />
<span class="status">${status.question}/${fn:length(comp.questions)}</span>
<input type="submit" value="next" />
</form>
</c:if>