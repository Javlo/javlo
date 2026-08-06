<%@ taglib uri="jakarta.tags.core" prefix="c"
%><div class="popup">
<form action="<c:out value="${param.currentURL}" />" method="post" enctype="multipart/form-data">
	<input type="hidden" name="webaction" value="upload" />
	<input type="hidden" name="admin" value="<c:out value="${param.admin}" />" />
	<c:if test="${not empty param.role}">
	<input type="hidden" name="role" value="<c:out value="${param.role}" />" />
	</c:if>
	
	<label for="upload_vrac">${i18n.edit['user.upload.vrac']}${not empty param.role?" - ":""}${not empty param.role?param.role:""}</label>
	<textarea id="upload_vrac" name="vrac" rows="4" cols="44"></textarea>
	
	<input type="file" name="file" />
	<input type="submit" />
	
	<div class="line">
		<br />
		<input type="checkbox" id="merge" name="merge" />
		<label for="merge">merge</label>		
	</div>
	
</form>
</div>
