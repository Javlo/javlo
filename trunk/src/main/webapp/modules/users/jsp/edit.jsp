<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="widgetbox edit-user">
<h3><span>${i18n.edit['edit.title']}</span></h3>
<div class="content">

<form id="form-edit-user" class="standard-form" action="${info.currentURL}" method="post">

<div>
	<input type="hidden" name="webaction" value="update" />
	<input type="hidden" name="user" value="${user.name}" />
</div>

<c:forEach var="key" items="${userInfoKeys}">
	<div class="line">
		<label for="${key}">${key}</label>
		<input type="text" id="${key}" name="${key}" value="${userInfoMap[key]}" /> 
	</div>
</c:forEach>

<div class="action">
	<input type="submit" name="back" value="${i18n.edit['global.back']}" />
	<input type="submit" name="ok" value="${i18n.edit['global.ok']}" />
</div>

</form>


</div>
</div>

