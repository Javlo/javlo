<%@page import="org.javlo.servlet.IVersion"%><%@ taglib
	uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib
	prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
	
	
<div class="form-group">
	<input type="text" name="title-${compid}" class="form-control" value="${fields.title}" placeholder="title"/>
</div>

<fieldset>
	<legend>question</legend>
	<textarea class="form-control" name="questions-${compid}" rows="14">${fields.questions}</textarea>
</fieldset>

<c:if test="${empty noresponse}">
<fieldset>
	<legend>response</legend>
	<textarea class="form-control" name="responses-${compid}" rows="3">${fields.responses}</textarea>
</fieldset>
</c:if>

<div class="form-group">
	<input type="text" name="sendlabel-${compid}" class="form-control" value="${fields.sendlabel}" placeholder="send label"/>
</div>
	