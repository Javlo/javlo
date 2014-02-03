<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="create-context">

<c:if test="${not empty messages.globalMessage && messages.globalMessage.type > 0 && !empty messages.globalMessage.message}">
<div class="message ${messages.globalMessage.type}">${ messages.globalMessage.message}</div>
</c:if>

	<form action="${info.currentURL}" method="post">
		<div class="line">
			<input type="hidden" name="webaction" value="createContext.create" />
			<input type="hidden" name="comp_id" value="${comp.id}" />
		</div>
		<div class="line">
			<label for="name">nom du site : </label>
			<input type="text" id="name" name="name" value="${param.name}" />
		</div>
		<div class="line">
			<label for="email">email de contact : </label>
			<input type="text" id="email" name="email" value="${param.email}" />
		</div>
		<div class="line">
			<label for="pwd">mot de passe : </label>
			<input type="password" id="pwd" name="pwd" value="" />
		</div>
		<div class="line">
			<label for="pwd2">confirmation mot de passe : </label>
			<input type="password" id="pwd2" name="pwd2" value="" />
		</div>
		<div class="action">
			<input type="submit" value="ok" />
		</div>
	</form>
</div>