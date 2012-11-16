<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="left">

<form id="" action="${info.currentURL}" method="post" class="standard-form">
<div>
	<input type="hidden" name="webaction" value="UpdateStaticConfig" />
</div>

<div>
	<textarea name="config_content" rows="20" cols="20" class="autogrow"><c:out value="${config_content}" escapeXml="true" /></textarea>
</div>


<div class="action">
	<input type="submit" name="back" value="${i18n.edit['global.back']}" />
	<input type="submit" name="update" value="${i18n.edit['global.update']}" />
</div>
</form>


</div>