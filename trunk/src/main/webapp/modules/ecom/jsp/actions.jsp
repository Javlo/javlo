<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div class="content">
<form id="actions" method="post" action="${info.currentURL}">
<div>
	<input type="hidden" name="webaction" value="ecom.storeBasket" />
	<input type="submit" value="export baskets" />
</div>
</form>
</div>