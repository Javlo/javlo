<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="create-context">

<c:if test="${not empty messages.globalMessage && messages.globalMessage.type > 0 && !empty messages.globalMessage.message}">
<div class="message ${messages.globalMessage.type} alert alert-${messages.globalMessage.bootstrapType}">${ messages.globalMessage.message}</div>
</c:if>

	<form action="${info.currentURL}" method="post">
		<div class="line form-group">
			<input type="hidden" name="webaction" value="createContext.create" />
			<input class="form-control" type="hidden" name="comp_id" value="${comp.id}" />
		</div>
		<div class="line form-group">
			<label for="name">nom du site : </label>
			<input class="form-control" type="text" id="name" name="name" value="${param.name}" />
		</div>
		<div class="line form-group">
			<label for="url">URL : </label>
			<div class="input-group">
      			<div class="input-group-addon">http://javlo.io/</div>
      			<input type="text" class="form-control" id="url" name="url">      			
    		</div>
		</div>
		<div class="line form-group">
			<label for="email">email de contact : </label>
			<input class="form-control" type="text" id="email" name="email" value="${param.email}" />
		</div>
		<div class="line form-group">
			<label for="pwd">mot de passe : </label>
			<input class="form-control" type="password" id="pwd" name="pwd" value="" />
		</div>
		<div class="line form-group">
			<label for="pwd2">confirmation mot de passe : </label>
			<input class="form-control" type="password" id="pwd2" name="pwd2" value="" />
		</div>
		<div class="action">
			<input type="submit" value="ok" class="btn btn-default" />
		</div>
	</form>
</div>