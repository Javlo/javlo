<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div class="widgetbox edit-user">
<h3><span>${i18n.edit['user.title.edit']} : ${user.name}</span></h3>
<div class="content">

<form id="form-edit-user"  action="${info.currentURL}" method="post" enctype="multipart/form-data">

<div>
	<input type="hidden" name="webaction" value="update" />
	<input type="hidden" name="user" value="${user.name}" />
</div>



<fieldset>
<legend>${i18n.edit['user.main-info']}</legend>
<div class="row">
	<div class="col-xs-4">
		<div class="form-group">
			<label>login <input class="form-control" name="login" value="${userInfoMap['login']}" /></label>
		</div>
		<div class="form-group">
			<label>password <input class="form-control" name="password" value="${userInfoMap['password']}" /></label>
		</div>
	</div>	
	<div class="col-xs-4">
		<div class="form-group">
			<label>firstName <input class="form-control" name="firstName" value="${userInfoMap['firstName']}" /></label>
		</div>
		<div class="form-group">
			<label>lastName <input class="form-control" name="lastName" value="${userInfoMap['lastName']}" /></label>
		</div>
	</div>
	<div class="col-xs-4">
		<div class="form-group">
			<label>title <input class="form-control" name="title" value="${userInfoMap['title']}" /></label>
		</div>	
		<div class="form-group"> 
			<label>email <input class="form-control" name="email" value="${userInfoMap['email']}" /></label>
		</div>			
	</div>
</div>
</fieldset>

<fieldset>
<legend>${i18n.edit['user.info']}</legend>
<div class="row">
<div class="col-xs-4">
<c:set var="fieldIndex" value="0" />
<c:forEach var="key" items="${userInfoKeys}" varStatus="status">
<c:if test="${key != 'login' && key != 'password' && key != 'email' && key != 'firstName' && key != 'lastName' && key != 'title'}">
	<div class="form-group">		 
		<label>${key}	
		<input class="form-control" type="text" name="${key}" value="${userInfoMap[key]}" /></label>		 
	</div>
	<c:set var="fieldIndex" value="${fieldIndex+1}" />
	<c:if test="${fieldIndex>(fn:length(userInfoKeys)-6)/3}"><c:set var="fieldIndex" value="0" />
		</div><div class="col-xs-4">
	</c:if>
	</c:if>	
</c:forEach>
</div>
</div>
</fieldset>

<c:if test="${fn:length(roles) > 0}">
<fieldset>
<legend>${i18n.edit['user.roles']}</legend>
<c:forEach var="role" items="${roles}">
	<c:set var="contains" value="false" />
	<c:forEach var="userRole" items="${user.roles}">
 		 <c:if test="${userRole eq role}">
   		 <c:set var="contains" value="true" />
 		 </c:if>
	</c:forEach>
	<label class="checkbox-inline"><input type="checkbox" id="role-${role}" name="role-${role}" <c:if test="${contains}">checked="checked"</c:if> />${role}</label>
</c:forEach>
</fieldset>
</c:if>

<c:if test="${not empty contextRoles}">
<fieldset>
<legend>${i18n.edit['user.context-roles']}</legend>
<c:forEach var="role" items="${contextRoles}">
	<c:set var="contains" value="false" />
	<c:forEach var="userRole" items="${user.roles}">
 		 <c:if test="${userRole eq role}">
   		 <c:set var="contains" value="true" />
 		 </c:if>
	</c:forEach>
	<label class="checkbox-inline"><input type="checkbox" id="role-${role}" name="role-${role}" <c:if test="${contains}">checked="checked"</c:if> />${role}</label>
</c:forEach>
</fieldset>
</c:if>

<div class="btn-group pull-right">
	<button type="submit" name="back" class="btn btn-default">${i18n.edit['global.back']}</button>
	<input type="submit" name="ok" class="btn btn-primary" value="${i18n.edit['global.ok']}" />
</div>

</form>

</div>
</div>

