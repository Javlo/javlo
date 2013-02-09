<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="special">
<form id="select-status js-submit" method="post" action="${info.currentURL}">
    <div class="line">
    <label for="filter_status">status</label>
	<select name="filter_status" id="filter_status">
		<option></option>
		<option ${param.filter_status == 'new'?'selected="selected"':''}>new</option>
		<option ${param.filter_status == 'working'?'selected="selected"':''}>working</option>
		<option ${param.filter_status == 'refuse'?'selected="selected"':''}>refuse</option>
		<option ${param.filter_status == 'onold'?'selected="selected"':''}>onold</option>
		<option ${param.filter_status == 'archived'?'selected="selected"':''}>archived</option>
	</select>
	<input type="submit" value="${i18n.edit['global.ok']}" />
	</div>
</form>
</div>