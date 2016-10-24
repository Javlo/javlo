<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib
	prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%><div
	class="content jeditor">
	<div class="image-wrapper">
		<img src="${imageURL}" class="img-responsive" />
		<div class="corner-wrapper">
			<div class="corner topleft"></div>
			<div class="corner topright"></div>
			<div class="corner bottomleft"></div>
			<div class="corner bottomright"></div>
		</div>
		<div class="crop-zone"></div>
		<div class="crop-shadow crop-shadow-1"></div>
		<div class="crop-shadow crop-shadow-2"></div>
		<div class="crop-shadow crop-shadow-3"></div>
		<div class="crop-shadow crop-shadow-4"></div>
	</div>
	<div class="command">
		<c:url var="backURL" value="${info.currentURL}" context="/">
			<c:if test="${not empty param[BACK_PARAM_NAME]}">			
				<c:param name="${BACK_PARAM_NAME}" value="${param[BACK_PARAM_NAME]}" />				
			</c:if>				
		</c:url>		
		<c:if test="${param.backDirect && not empty param[BACK_PARAM_NAME]}">
			<c:url var="backURL" value="${param[BACK_PARAM_NAME]}" context="/" />
		</c:if>
		<form action="${backURL}" method="post">
				<input type="hidden" name="webaction" value="file.editimage" />
				<input type="hidden" name="webaction" value="file.changeRenderer" />
				<input type="hidden" name="page" value="meta" />
				<input type="hidden" name="file" value="${editFile}" />
				<input type="hidden" name="crop-left" value="" /> <input				
				type="hidden" name="crop-top" value="" /> <input type="hidden"
				name="crop-width" value="" /> <input type="hidden"
				name="crop-height" value="" /> <input type="hidden" id="flip"
				name="flip" value="false" />
			<div class="form-group">
				<button class="btn btn-default btn-flip">
					<span class="glyphicon glyphicon-resize-horizontal"
						aria-hidden="true"></span><span class="hidden" lang="en">flip</span>
				</button>
				<input type="submit" name="cancel" class="btn btn-default" value="${i18n.edit['global.cancel']}" /> <input
					type="submit" class="btn btn-primary" value="${i18n.edit['global.save']}" />
			</div>
		</form>
	</div>
</div>
