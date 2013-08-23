<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="survey">

	<form id="survey-form">
	<fieldset>
		<legend>${ci18n['label.legend']}</legend>
		<input type="hidden" name="webaction" value="survey.submit" />
		<input type="hidden" name="compid" value="${compid}" />
		
		<c:set var="done" value="false" />
		
		<c:forEach var="question" items="${questions}" varStatus="qs">
		<div class="line">
			<div class="question">${question.question}</div>
			<c:if test="${!question.done}">
			<ul class="responses">
			<c:forEach var="response" items="${question.responses}" varStatus="rs">
				<li><input type="checkbox" name="q${qs.index}r${rs.index}" id="q${rs.index}r${rs.index}" />
				<label for="q${rs.index}r${rs.index}">${response}</label></li>
			</c:forEach>
			</ul>
			</c:if>
			<c:if test="${question.done}">
				<c:set var="done" value="true" />
				<c:if test="${not empty question.conclusion.link}">
					<a href="${question.conclusion.link}">
				</c:if>
				<p class="conclusion">${question.conclusion.text}</p>
				<c:if test="${not empty question.conclusion.link}">
					</a>
				</c:if>				
			</c:if>
		</div>
		
		</c:forEach>
		
		<c:if test="${!done}">
		<div class="line action">
			<input type="submit" value="${ci18n['label.submit']}" />
		</div>		
		</c:if>
	</fieldset>
	</form>
	
</div>
	