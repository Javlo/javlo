<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><div class="quiz"><c:if test="${empty quiz}">

<jsp:include page="default.jsp"></jsp:include>
<h3>${comp.resultTitle}</h3>
<ul class="result">
<c:forEach var="response" items="${status.responses}">
	<c:set var="cssClass" value="free" />
	<c:if test="${not empty response.question.response}">
		<c:set var="cssClass" value="wrong" />
		<c:if test="${response.response eq response.question.response}">
			<c:set var="cssClass" value="right" />
		</c:if>		
	</c:if>
	<li class="${cssClass}"><span class="question">${response.question.label}</span> : <span class="response">${response.response}</span> </li>
</c:forEach>
</ul>

</c:if><c:if test="${not empty quiz}">

<h3>${comp.quizTitle}</h3>
<form method="post" action="${info.currentURL}" ${comp.file?' enctype="multipart/form-data"':''}>
<input type="hidden" name="webaction" value="quiz.response" />
<input type="hidden" name="comp-id" value="${comp.id}" />
<c:set var="field" value="${status.response.question}" scope="request" />
<span class="status">${status.question}/${fn:length(comp.questions)}</span>
<jsp:include page="field.jsp" />
<input type="submit" class="btn btn-default" value="${ci18n['next']}" />
</form>
</c:if></div>