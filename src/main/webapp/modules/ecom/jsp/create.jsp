<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div class="content">
<form class="standard-form" action="${info.currentURL}" method="post">
	<div>
		<input type="hidden" name="webaction" value="create" />		
		<div class="line">
			<label for="name">name</label>
			<input type="text" name="name" id="name" />
		</div>
		<div class="action">			
			<input type="submit" name="ok" value="${i18n.view["global.ok"]}" />
		</div>
	</div>	
</form>
</div>