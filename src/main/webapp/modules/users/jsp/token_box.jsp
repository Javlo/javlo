<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%-- token operations of a user (static include in a user edition form) --%>
<fieldset class="user-tokens">
<legend>${i18n.edit['user.token']}</legend>
<div class="row">
	<div class="col-xs-8 col-sm-8">
		<div class="form-group access-token">
			<label>${i18n.edit['user.access-token']}</label>
			<c:if test="${not empty newAccessToken}">
				<div class="alert alert-warning">
					<p><c:out value="${i18n.edit['user.access-token.copy-warning']}" /></p>
					<div class="input-group new-access-token">
						<input class="form-control" type="text" id="newAccessToken" readonly="readonly" value="<c:out value="${newAccessToken}" />" onclick="this.select();" style="color:#222;background-color:#fff;opacity:1;font-family:monospace;" />
						<span class="input-group-btn">
							<button class="btn btn-default" type="button" title="copy" aria-label="copy"
								onclick="var f=document.getElementById('newAccessToken'),i=document.getElementById('newAccessTokenCopyIcon');var done=function(){i.className='bi bi-check-lg';setTimeout(function(){i.className='bi bi-clipboard';},2000);};f.select();if(navigator.clipboard&amp;&amp;window.isSecureContext){navigator.clipboard.writeText(f.value).then(done);}else if(document.execCommand('copy')){done();}return false;">
								<i id="newAccessTokenCopyIcon" class="bi bi-clipboard"></i>
							</button>
						</span>
					</div>
				</div>
			</c:if>
			<c:choose>
				<c:when test="${accessTokenActive}">
					<p class="access-token-info">
						${i18n.edit['user.access-token.created']} <c:out value="${accessTokenCreation}" />
						<c:if test="${not empty accessTokenExpiration}"> - ${accessTokenExpired ? i18n.edit['user.access-token.expired'] : i18n.edit['user.access-token.expire']} <c:out value="${accessTokenExpiration}" /></c:if>
					</p>
				</c:when>
				<c:otherwise><p class="access-token-info">${i18n.edit['user.access-token.none']}</p></c:otherwise>
			</c:choose>
			<input class="btn btn-default" type="submit" name="generateAccessToken" value="${i18n.edit['user.access-token.generate']}" />
			<c:if test="${accessTokenActive}">
				<input class="btn btn-default" type="submit" name="revokeAccessToken" value="${i18n.edit['user.access-token.revoke']}" />
			</c:if>
		</div>
	</div>
	<div class="col-xs-4 col-sm-4">
		<div class="form-group internal-token">
			<label>${i18n.edit['user.internal-token']}</label>
			<p class="internal-token-info">${i18n.edit['user.internal-token.info']}</p>
			<c:if test="${internalTokenReset}"><p class="alert alert-info">${i18n.edit['user.internal-token.done']}</p></c:if>
			<input class="btn btn-default" type="submit" name="resetInternalToken" value="${i18n.edit['user.internal-token.reset']}" />
		</div>
	</div>
</div>
</fieldset>
