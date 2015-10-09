<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:url var="createURL" value="${info.currentURL}">
	<c:param name="id" value="new" />
</c:url>
<a class="action-button more page" href="${createURL}"><span>${i18n.edit['global.create']}</span></a>

<c:if test="${not noFilter}">
<div class="special">
<form id="select-status js-submit" method="post" action="${info.currentURL}">
    <div class="line">
    <label for="filter_status">status</label>
	<select name="filter_status" id="filter_status">
		<option></option>
		<option ${param.filter_status == 'new'?'selected="selected"':''}>new</option>
		<option ${param.filter_status == 'working'?'selected="selected"':''}>working</option>
		<option ${param.filter_status == 'rejected'?'selected="selected"':''}>rejected</option>
		<option ${param.filter_status == 'onhold'?'selected="selected"':''} value="onhold">on hold</option>
		<option ${param.filter_status == 'done'?'selected="selected"':''}>done</option>
		<option ${param.filter_status == 'archived'?'selected="selected"':''}>archived</option>
	</select>
	<input type="submit" value="${i18n.edit['global.ok']}" />
	</div>
</form>
</div>
</c:if>
