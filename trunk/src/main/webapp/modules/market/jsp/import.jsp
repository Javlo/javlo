<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="one_half card">
<fieldset>
<legend>${remoteResource.name}</legend>

<div class="image"><img src="${remoteResource.imageURL}" alt="${remoteResource.name}" /></div>

<div class="line type">
<span class="label">${i18n.edit['market.field.type']}</span>
<span class="value">${remoteResource.type}</span>
</div>

<div class="line category">
<span class="label">${i18n.edit['market.field.category']}</span>
<span class="value">${remoteResource.category}</span>
</div>

<div class="line description">
<span class="label">${i18n.edit['global.description']}</span>
<span class="value">${remoteResource.description}</span>
</div>

<div class="line version">
<span class="label">${i18n.edit['global.version']}</span>
<span class="value">${remoteResource.version}</span>
</div>

<div class="clear">&nbsp;</div>

<c:if test="${empty localResource}">
<div class="actions">
	<form action="${info.currentURL}" method="post">
		<input type="hidden" name="webaction-1" value="import" />
		<input type="hidden" name="webaction-2" value="importPage" />		
		<input type="hidden" name="id" value="${remoteResource.id}" />
		<input class="action-button" type="submit" value="${i18n.edit['market.action.install']}" />
	</form>
</div>
</c:if>

</fieldset>
</div>
<c:if test="${not empty localResource}">
<div class="one_half last card">
<fieldset>	
<legend>${localResource.name}</legend>

<div class="image"><img src="${localResource.imageURL}" alt="${localResource.name}" /></div>

<div class="line type">
<span class="label">${i18n.edit['market.field.type']}</span>
<span class="value">${localResource.type}</span>
</div>

<div class="line category">
<span class="label">${i18n.edit['market.field.category']}</span>
<span class="value">${localResource.category}</span>
</div>

<div class="line description">
<span class="label">${i18n.edit['global.description']}</span>
<span class="value">${localResource.description}</span>
</div>

<div class="line version">
<span class="label">${i18n.edit['global.version']}</span>
<span class="value">${localResource.version}</span>
</div>

<div class="clear">&nbsp;</div>

<div class="actions">
	<form action="${info.currentURL}" method="post">
		<input type="hidden" name="webaction-1" value="delete" />
		<input type="hidden" name="webaction-2" value="importPage" />
		<input type="hidden" name="id" value="${remoteResource.id}" />
		<input type="hidden" name="lid" value="${localResource.id}" />
		<input class="action-button warning needconfirm" type="submit" value="${i18n.edit['market.action.delete']}" />
	</form>
</div>
</fieldset>
</div>
</c:if>