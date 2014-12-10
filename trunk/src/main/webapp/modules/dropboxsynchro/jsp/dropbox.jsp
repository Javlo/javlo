<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="content">
	<c:if test="${not empty dropboxUrl}">
		<a class="dropboxUrl" href="${dropboxUrl}"><span>init drop box acccess.</span></a>	
	</c:if>
	<c:if test="${not empty config}">
	
		<c:if test="${not empty dropboxThread}">
			<p class="running"><span>running... (${dropboxThread.humanDownloadSize})</span></p>
		</c:if>
		<c:if test="${empty dropboxThread}">	
		<form id="config-form" role="form" action="${info.currentURL}" method="post">
			<div>
				<input type="hidden" name="webaction" value="dropbox.config" />
			</div>
			<div class="form-group">
				<label>
				Account :
				<input class="form-control" type="text" readonly="readonly" value="${linkedAccount}" />
				</label>
			</div>
			<div class="form-group">
				<label>
				local folder :
				<input class="form-control" type="text" name="localFolder" value="${config.localFolder}" />
				</label>
			</div>
			<div class="form-group">
				<label>
				dropbox folder :
				<input class="form-control" type="text" name="dropboxFolder" value="${config.dropboxFolder}" />
				</label>
			</div>
			<div class="form-group">
				<input type="submit" class="btn btn-default action-button" value="${i18n.edit['global.ok']}" />
				<input type="submit" class="btn btn-default action-button" name="reset" value="${i18n.edit['global.reset']}" />
			</div>
		</form>
		</c:if>
	</c:if>
</div>
