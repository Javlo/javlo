<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><div class="item-wrapper" id="newname-${param.id}" data-id="${param.id}" data-aschild="true">
<div class="item">
<c:set var="focusKey" value="focus${param.id}" />
<span class="new-name"><input type="text" name="newname-${param.id}" class="hidden-input form-control${not empty param[focusKey]?' needfocus':' nofocus'}" placeholder="create new node" /></span>
</div>
</div>