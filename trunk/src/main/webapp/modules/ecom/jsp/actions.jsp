<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div class="content">
<form id="actions" method="post" action="${info.currentURL}">
<div>
	<input type="hidden" name="webaction" value="ecom.storeBasket" />
	<input type="submit" value="export baskets" />
</div>
</form>
<form class="standard-form" id="actions" method="post" action="${info.currentURL}" enctype="multipart/form-data">
<fieldset>
<legend>Import payement.</legend>
	<input type="hidden" name="webaction" value="ecom.importPayement" />
	<div class="line">
	<input type="file" name="file" />
	</div>
	<div class="action">
	<input type="submit" value="${i18n.edit['global.ok']}" />
	</div>
</fielset>
</form>
</div>