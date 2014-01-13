<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<form class="standard-form ajax" action="${info.currentURL}" method="post">
	<fieldset>
		<legend>${network.name}</legend>
		<input type="hidden" name="webaction" value="updateNetwork" />
		<input type="hidden" name="name" value="${network.name}" />
		<div class="line">
			<label for="login">login</label>
			<input type="text" name="login" id="login" value="${network.login}" />
		</div>
		<div class="line">
			<label for="token">token</label>
			<input type="text" name="token" id="token" value="${network.token}" />
		</div>
		<div class="line">
			<label for="url">url</label>
			<input type="text" name="url" id="url" value="${network.URL}" />
		</div>	
		<div class="action">			
			<input type="submit" name="ok" value="${i18n.view["global.ok"]}" />
		</div>			
	</fieldset>
</form>