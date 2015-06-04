<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><div class="event-registration panel panel-${(!confirmed && !canceled)?'default':confirmed?'success':'danger'}">
  <c:if test="${!confirmed && !canceled}"><div class="panel-heading">${fields.question}</div></c:if>
  <c:if test="${confirmed}"><div class="panel-heading">${fields.confirmed}</div></c:if>
  <c:if test="${canceled}"><div class="panel-heading">${fields.canceled}</div></c:if>
  <c:if test="${!info.pageMode}">
  <div class="panel-body">
  	<c:if test="${not empty user}"><c:if test="${not empty event}"><c:if test="${empty closeEvent}">
  	<form action="${info.currentURL}" method="post">
  			<input type="hidden" name="webaction" value="event-registration.confirm" />
			<input type="hidden" name="comp-id" value="${compid}" />		
			<div class="btn-group pull-right" role="group">				
				<c:if test="${!confirmed && !canceled}">
				<button class="btn btn-primary" type="submit" name="confirm">${fields.confirm}</button>
				<button class="btn btn-default" type="submit" name="cancel">${fields.cancel}</button>
				</c:if><c:if test="${confirmed || canceled}">
				<button class="btn btn-default" type="submit" name="reset">${fields.reset}</button></c:if>
			</div>
	</form></c:if></c:if>
	<c:if test="${not empty closeEvent}"><div class="alert alert-warning" role="alert">${fields.tooLate}</div></c:if></c:if><c:if test="${empty user}"><p class="alert alert-warning" role="alert">${fields.notlogged}</p></c:if>
	<c:if test="${empty event}"><div class="alert alert-warning" role="alert">Error: this page is not a event.</div></c:if>
  </div>
  </c:if><c:if test="${info.pageMode}">
  <c:if test="${globalContext.collaborativeMode}">
  	<a href="${info.currentURL}">${fields.mailingButton}</a>
  </c:if><c:if test="${!globalContext.collaborativeMode}">
  	<a href="${info.absolutePreviewURL}">${fields.mailingButton}</a>
  </c:if></c:if>
</div>
<h3>${fields.participants}</h3>
<div class="participants">
	<c:if test="${fn:length(participants)==0}">${fields.noParticipant}</c:if>
	<c:if test="${fn:length(participants)>0}">
	<ul>
	<c:forEach var="user" items="${participants}">
	<li><a href="mailto:${user.userInfo.email}">${user.label}</a></li>
	</c:forEach>
	</ul>
	</c:if>  	
</div>
