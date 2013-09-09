<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<form class="standard-form ajax" action="${info.currentURL}" method="post">
	<fieldset>
		<legend>${service.name}</legend>
		<input type="hidden" name="webaction" value="updateService" />
		<input type="hidden" name="name" value="${service.name}" />
		
		<div class="line">
			<label for="name">name</label>
			<input type="text" name="login" id="name" value="${service.name}" />
		</div>
		<div class="line">
			<label for="appId">Application ID</label>
			<input type="text" name="appId" id="appId" value="${service.appId}" />
		</div>
		<div class="line">
			<label for="secretKey">Secret Key</label>
			<input type="text" name="secretKey" id="secretKey" value="${service.secretKey}" />
		</div>
		<div class="line">
			<label for="url">url</label>
			<input type="text" name="url" id="url" value="${service.URL}" />
		</div>		
		<div class="line">
			<label for="page">return page after billing.</label>
			<input type="text" name="page" id="page" value="${service.returnPage}" />
		</div>		
		<div class="action">			
			<input class="warning needconfirm" type="submit" name="delete" value="${i18n.edit['global.delete']}" />
			<input type="submit" name="ok" value="${i18n.edit['global.ok']}" />
		</div>			
	</fieldset>
</form>