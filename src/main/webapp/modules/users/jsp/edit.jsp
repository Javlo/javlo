<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="widgetbox edit-user">
<h3><span>${i18n.edit['user.title.edit']} : ${user.name}</span></h3>
<div class="content">

<form id="form-edit-user" class="standard-form" action="${info.currentURL}" method="post">

<div>
	<input type="hidden" name="webaction" value="update" />
	<input type="hidden" name="user" value="${user.name}" />
</div>

<fieldset>
<legend>${i18n.edit['user.info']}</legend>
<c:forEach var="key" items="${userInfoKeys}">
	<div class="line">
		<label for="${key}">${key}</label>
		<input type="text" id="${key}" name="${key}" value="${userInfoMap[key]}" /> 
	</div>
</c:forEach>
</fieldset>

<fieldset>
<legend>${i18n.edit['user.roles']}</legend>
<c:forEach var="role" items="${roles}">

<div class="inline">
	
	<c:set var="contains" value="false" />
	<c:forEach var="userRole" items="${user.roles}">
 		 <c:if test="${userRole eq role}">
   		 <c:set var="contains" value="true" />
 		 </c:if>
	</c:forEach>
	<input type="checkbox" id="${role}" name="${role}" <c:if test="${contains}">checked="checked"</c:if> />
	<label for="${role}">${role}</label>
</div>

</c:forEach>
</fieldset>

<div class="action">
	<input type="submit" name="back" value="${i18n.edit['global.back']}" />
	<input type="submit" name="ok" value="${i18n.edit['global.ok']}" />
</div>

</form>

</div>
</div>

