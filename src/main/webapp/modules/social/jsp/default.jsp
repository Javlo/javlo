<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"
%><form class="ajax" action="${info.currentURL}" method="post">
	<fieldset>
		<legend>${network.name}</legend>
		<input type="hidden" name="webaction" value="updateNetwork" />
		<input type="hidden" name="name" value="${network.name}" />
		<div class="row">
		<div class="col-xs-6">
		<div class="form-group">
			<label for="login">login</label>
			<input class="form-control" type="text" name="login" id="login" value="${network.login}" />
		</div>
		<div class="form-group">
			<label for="url">url</label>
			<input class="form-control" type="text" name="url" id="url" value="${network.URL}" />
		</div>
		</div><div class="col-xs-6">
		<div class="form-group">
			<label for="clientid">client id*</label>
			<input class="form-control" type="text" name="clientid" id="clientid" value="${network.clientId}" />
		</div>
		<div class="form-group">
			<label for="clientsecret">client secret*</label>
			<input class="form-control" type="text" name="clientsecret" id="clientsecret" value="${network.clientSecret}" />
		</div>		
		</div></div>	
		<div class="form-group">			
			<button class="btn btn-default pull-right" type="submit" name="ok">${i18n.view["global.ok"]}</button>
		</div>
		
	</fieldset>
</form>